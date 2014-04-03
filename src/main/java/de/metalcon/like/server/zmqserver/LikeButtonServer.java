package de.metalcon.like.server.zmqserver;

import net.hh.request_dispatcher.server.ZmqWorker;
import de.metalcon.like.api.LikeServerRequest;

public class LikeButtonServer {

    // TODO: change templates to request
    public static void main(String[] args) {
        ZmqWorker<LikeServerRequest, LikeServerRequest> worker =
                new ZmqWorker<LikeServerRequest, LikeServerRequest>("tcp://",
                        new LikeServerRequestHandler());
    }
}
