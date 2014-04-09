package de.metalcon.like.server.api.frontend;

import java.io.IOException;

import net.hh.request_dispatcher.server.RequestHandler;
import de.metalcon.api.responses.Response;
import de.metalcon.exceptions.MetalconRuntimeException;
import de.metalcon.like.api.requests.LikeServerAddRelationRequest;
import de.metalcon.like.api.requests.LikeServerRemoveRelationRequest;
import de.metalcon.like.api.requests.LikeServerRequest;
import de.metalcon.like.server.api.backend.LikeService;

public class LikeServerWriteRequestHandler implements
        RequestHandler<LikeServerRequest, Response> {

    private final LikeService service;

    private static final long serialVersionUID = -4926311493102744734L;

    public LikeServerWriteRequestHandler(
            final LikeService service) {
        this.service = service;
    }

    @Override
    public Response handleRequest(final LikeServerRequest request) {
        /*
         * add relation
         */
        if (request instanceof LikeServerAddRelationRequest) {
            final LikeServerAddRelationRequest r =
                    (LikeServerAddRelationRequest) request;
            service.putEdge(r.getFrom(), r.getTo(), r.getVote());
            return null;
        }

        /*
         * remove relation
         */
        if (request instanceof LikeServerRemoveRelationRequest) {
            final LikeServerRemoveRelationRequest r =
                    (LikeServerRemoveRelationRequest) request;
            try {
                service.deleteEdge(r.getFrom(), r.getTo());
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                throw new MetalconRuntimeException(
                        e.getMessage()
                                + "\n"
                                + "Check if the like service DB directories are writable");
            }
        }
        return null;
    }
}
