package de.metalcon.like.server;

import net.hh.request_dispatcher.server.ZmqWorker;

import org.zeromq.ZMQ;

import de.metalcon.api.responses.Response;
import de.metalcon.exceptions.MetalconException;
import de.metalcon.like.api.requests.LikeServerRequest;
import de.metalcon.like.server.api.backend.LikeService;
import de.metalcon.like.server.api.frontend.LikeServerRequestHandler;
import de.metalcon.like.server.api.frontend.LikeServerWriteRequestHandler;

/**
 * The server is running several threads on the front end. It is designed to
 * respond to any kind of read requests asap while write requests are queued by
 * a zmq connection to a backend thread.
 * 
 * @author Jonas Kunze (kunze.jonas@gmail.com)
 * 
 */
public class LikeButtonServer extends Thread {

    public final static String FRONTEND_LISTEN_URI = "tcp://*:1234";

    public final static String WRITE_WORKER_LISTEN_URI = "ipc://like";

    private final static String STORAGE_DIR = "/dev/shm/likeDB";

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
        service = new LikeService(STORAGE_DIR);

        final ZMQ.Context ctx = ZMQ.context(1);

        likeRequestHandler = new LikeServerRequestHandler(ctx, service);

        frontendWorker =
                new ZmqWorker<LikeServerRequest, Response>(ctx,
                        FRONTEND_LISTEN_URI, likeRequestHandler);

        writeWorker =
                new ZmqWorker<LikeServerRequest, Response>(ctx,
                        WRITE_WORKER_LISTEN_URI,
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
            long ns = service.updateAllNodes();
            if (ns < 1E9) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(final String[] args) {
        LikeButtonServer server;
        try {
            server = new LikeButtonServer();
            server.run();
        } catch (MetalconException e) {
            e.printStackTrace();
        }
    }
}
