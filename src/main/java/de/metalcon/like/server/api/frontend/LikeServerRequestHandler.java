package de.metalcon.like.server.api.frontend;

import net.hh.request_dispatcher.Dispatcher;
import net.hh.request_dispatcher.server.RequestHandler;
import net.hh.request_dispatcher.service_adapter.ZmqAdapter;

import org.zeromq.ZMQ;

import de.metalcon.api.responses.Response;
import de.metalcon.api.responses.errors.InternalServerErrorResponse;
import de.metalcon.like.api.Vote;
import de.metalcon.like.api.requests.LikeServerAddRelationRequest;
import de.metalcon.like.api.requests.LikeServerFollowsRequest;
import de.metalcon.like.api.requests.LikeServerGetCommonsRequest;
import de.metalcon.like.api.requests.LikeServerGetLikedLikesRequest;
import de.metalcon.like.api.requests.LikeServerGetLikesRequest;
import de.metalcon.like.api.requests.LikeServerRemoveRelationRequest;
import de.metalcon.like.api.requests.LikeServerRequest;
import de.metalcon.like.api.responses.LikeServerMuidListResponse;
import de.metalcon.like.api.responses.LikeServerVoteResponse;
import de.metalcon.like.api.responses.RequestQueuedResponse;
import de.metalcon.like.server.LikeButtonServer;
import de.metalcon.like.server.api.backend.LikeService;

public class LikeServerRequestHandler implements
        RequestHandler<LikeServerRequest, Response>, AutoCloseable {

    private static final long serialVersionUID = 5330271229952005271L;

    private final LikeService service;

    private final Dispatcher writeWorkDispatcher = new Dispatcher();

    public LikeServerRequestHandler(
            final ZMQ.Context ctx,
            final LikeService service) {
        this.service = service;

        final String writeWorkerID = "WriteWorker";

        writeWorkDispatcher.registerServiceAdapter(writeWorkerID,
                new ZmqAdapter(ctx, LikeButtonServer.WRITE_WORKER_LISTEN_URI));

        writeWorkDispatcher.setDefaultService(
                LikeServerAddRelationRequest.class, writeWorkerID);
        writeWorkDispatcher.setDefaultService(
                LikeServerRemoveRelationRequest.class, writeWorkerID);
    }

    @Override
    public Response handleRequest(final LikeServerRequest request) {
        /*
         * get common nodes
         */
        if (request instanceof LikeServerGetCommonsRequest) {
            final LikeServerGetCommonsRequest r =
                    (LikeServerGetCommonsRequest) request;
            long[] commons =
                    service.getCommonNodes(r.getFrom(), r.getTo(), r.getVote());
            return new LikeServerMuidListResponse(commons);
        }

        /*
         * follows
         */
        if (request instanceof LikeServerFollowsRequest) {
            final LikeServerFollowsRequest r =
                    (LikeServerFollowsRequest) request;
            Vote v = service.follows(r.getFrom(), r.getTo());
            return new LikeServerVoteResponse(v);
        }

        /*
         * get likes
         */
        if (request instanceof LikeServerGetLikesRequest) {
            final LikeServerGetLikesRequest r =
                    (LikeServerGetLikesRequest) request;
            if (r.getVote() == Vote.NEUTRAL) {
                return new InternalServerErrorResponse(
                        LikeServerGetLikesRequest.class.getName()
                                + " received with Vote.NEUTRAL",
                        "You may only send requests with Vote.UP or Vote.DOWN");
            }

            long[] likedNodes =
                    service.getLikes(r.getNode(), r.getDirection(), r.getVote());
            return new LikeServerMuidListResponse(likedNodes);
        }

        /*
         * get liked likes
         */
        if (request instanceof LikeServerGetLikedLikesRequest) {
            final LikeServerGetLikedLikesRequest r =
                    (LikeServerGetLikedLikesRequest) request;
            if (r.getVote() == Vote.NEUTRAL) {
                return new InternalServerErrorResponse(
                        LikeServerGetLikedLikesRequest.class.getName()
                                + " received with Vote.NEUTRAL",
                        "You may only send requests with Vote.UP or Vote.DOWN");
            }
            long[] likedNodes = service.getLikedLikes(r.getNode(), r.getVote());
            return new LikeServerMuidListResponse(likedNodes);
        }

        /*
         * All other request should be write requests -> forward them to the
         * write worker asynchronously
         */
        writeWorkDispatcher.execute(request, /* ignore the response */null);

        return new RequestQueuedResponse();
    }

    @Override
    public void close() {

    }
}
