package de.metalcon.like.server;

import net.hh.request_dispatcher.ZmqWorker;

import org.zeromq.ZMQ;

import de.metalcon.api.responses.Response;
import de.metalcon.exceptions.MetalconException;
import de.metalcon.like.api.requests.LikeServerRequest;
import de.metalcon.like.server.api.backend.LikeService;
import de.metalcon.like.server.api.frontend.LikeServerRequestHandler;
import de.metalcon.like.server.api.frontend.LikeServerWriteRequestHandler;
import de.metalcon.like.server.core.Configs;

/**
 * The server is running several threads on the front end. It is designed to
 * respond to any kind of read requests asap while write requests are queued by
 * a zmq connection to a backend thread.
 * 
 * @author Jonas Kunze (kunze.jonas@gmail.com)
 * 
 */
public class LikeButtonServer extends Thread {

    /**
     * default value for configuration file path
     */
    protected static final String DEFAULT_CONFIG_PATH =
            "/usr/share/metalcon/like/main.cfg";

    private final LikeService service;

    /*
     * ZMQ fronted worker receiving all kind of requests
     */
    private final ZmqWorker<LikeServerRequest, Response> frontendWorker;

    /*
     * ZMQ backend worker receiving all write requests from the frontend request
     * handler
     */
    private final ZmqWorker<LikeServerRequest, Response> writeWorker;

    private final LikeServerRequestHandler likeRequestHandler;

    public LikeButtonServer() throws MetalconException {
        service = new LikeService(Configs.STORAGE_DIR);

        final ZMQ.Context ctx = ZMQ.context(1);

        likeRequestHandler = new LikeServerRequestHandler(ctx, service);

        frontendWorker =
                new ZmqWorker<LikeServerRequest, Response>(ctx,
                        Configs.FRONTEND_LISTEN_URI, likeRequestHandler);

        writeWorker =
                new ZmqWorker<LikeServerRequest, Response>(ctx,
                        Configs.WRITE_WORKER_LISTEN_URI,
                        new LikeServerWriteRequestHandler(service));

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                likeRequestHandler.close();

                frontendWorker.close();
                writeWorker.close();
                ctx.term();
                try {
                    frontendWorker.join();
                    writeWorker.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void run() {
        frontendWorker.start();
        writeWorker.start();

        while (true) {
            long ns = service.updateAllLargeNodes();
            if (ns < 1E9) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    /**
     * Starts the server with the zmq worker and update thread
     */
    public static void main(final String[] args) {

        String configPath;
        if (args.length > 0) {
            configPath = args[0];
        } else {
            configPath = DEFAULT_CONFIG_PATH;
            System.out
                    .println("[INFO] using default configuration file path \""
                            + DEFAULT_CONFIG_PATH + "\"");
        }
        Configs.initialize(configPath);

        LikeButtonServer server;
        try {
            server = new LikeButtonServer();
            server.run();
        } catch (MetalconException e) {
            e.printStackTrace();
        }
    }
}
