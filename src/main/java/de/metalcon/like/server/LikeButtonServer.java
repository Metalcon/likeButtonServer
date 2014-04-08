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
public class LikeButtonServer {

    public final static String FRONTEND_LISTEN_URI = "tcp://*:1003";

    public final static String WRITE_WORKER_LISTEN_URI = "inproc://like";

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

    public LikeButtonServer() throws MetalconException {
        service = new LikeService(STORAGE_DIR);

        ZMQ.Context ctx = ZMQ.context(1);

        frontendWorker =
                new ZmqWorker<LikeServerRequest, Response>(ctx,
                        FRONTEND_LISTEN_URI, new LikeServerRequestHandler(ctx,
                                service));

        writeWorker =
                new ZmqWorker<LikeServerRequest, Response>(ctx,
                        WRITE_WORKER_LISTEN_URI,
                        new LikeServerWriteRequestHandler(service));
    }

    public void run() {
        frontendWorker.start(); // starts thread
        writeWorker.run(); // blocks this thread
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
