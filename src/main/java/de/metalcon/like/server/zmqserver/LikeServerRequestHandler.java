package de.metalcon.like.server.zmqserver;

import java.io.IOException;

import net.hh.request_dispatcher.server.RequestHandler;
import de.metalcon.api.responses.Response;
import de.metalcon.api.responses.SuccessResponse;
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
import de.metalcon.like.server.api.LikeService;

public class LikeServerRequestHandler implements
		RequestHandler<LikeServerRequest, Response> {

	private final LikeService service;

	private static final long serialVersionUID = -4926311493102744734L;

	public LikeServerRequestHandler(final LikeService service) {
		this.service = service;
	}

	@Override
	public Response handleRequest(LikeServerRequest request) {

		/*
		 * get common nodes
		 */
		if (request instanceof LikeServerGetCommonsRequest) {
			final LikeServerGetCommonsRequest r = (LikeServerGetCommonsRequest) request;
			long[] commons = service.getCommonNodes(r.getFrom(), r.getTo(),
					r.getVote());
			return new LikeServerMuidListResponse(commons);
		}

		/*
		 * add relation
		 */
		if (request instanceof LikeServerAddRelationRequest) {
			final LikeServerAddRelationRequest r = (LikeServerAddRelationRequest) request;
			service.putEdge(r.getFrom(), r.getTo(), r.getVote());
			return new SuccessResponse();
		}

		/*
		 * follows
		 */
		if (request instanceof LikeServerFollowsRequest) {
			final LikeServerFollowsRequest r = (LikeServerFollowsRequest) request;
			Vote v = service.follows(r.getFrom(), r.getTo());
			return new LikeServerVoteResponse(v);
		}

		/*
		 * get likes
		 */
		if (request instanceof LikeServerGetLikesRequest) {
			final LikeServerGetLikesRequest r = (LikeServerGetLikesRequest) request;
			if (r.getVote() == Vote.NEUTRAL) {
				return new InternalServerErrorResponse(
						LikeServerGetLikesRequest.class.getName()
								+ " received with Vote.NEUTRAL",
						"You may only send requests with Vote.UP or Vote.DOWN");
			}

			long[] likedNodes = service.getLikes(r.getNode(), r.getDirection(),
					r.getVote());
			return new LikeServerMuidListResponse(likedNodes);
		}

		/*
		 * get liked likes
		 */
		if (request instanceof LikeServerGetLikedLikesRequest) {
			final LikeServerGetLikedLikesRequest r = (LikeServerGetLikedLikesRequest) request;
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
		 * remove relation
		 */
		if (request instanceof LikeServerRemoveRelationRequest) {
			final LikeServerRemoveRelationRequest r = (LikeServerRemoveRelationRequest) request;
			try {
				service.deleteEdge(r.getFrom(), r.getTo());
				return new SuccessResponse();
			} catch (IOException e) {
				e.printStackTrace();
				return new InternalServerErrorResponse(e.getMessage(),
						"Check if the like service DB directories are writable");
			}
		}
		return null;
	}
}
