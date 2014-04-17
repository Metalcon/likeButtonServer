package de.metalcon.like.requests;

import junit.framework.Assert;
import net.hh.request_dispatcher.Callback;
import net.hh.request_dispatcher.Dispatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zeromq.ZMQ;

import de.metalcon.api.responses.Response;
import de.metalcon.domain.Muid;
import de.metalcon.domain.UidType;
import de.metalcon.exceptions.MetalconException;
import de.metalcon.exceptions.ServiceOverloadedException;
import de.metalcon.like.api.Vote;
import de.metalcon.like.api.requests.LikeServerAddRelationRequest;
import de.metalcon.like.api.requests.LikeServerFollowsRequest;
import de.metalcon.like.api.requests.LikeServerGetCommonsRequest;
import de.metalcon.like.api.requests.LikeServerGetLikedLikesRequest;
import de.metalcon.like.api.requests.LikeServerGetLikesRequest;
import de.metalcon.like.api.requests.LikeServerRemoveRelationRequest;
import de.metalcon.like.api.responses.RequestQueuedResponse;
import de.metalcon.like.server.LikeButtonServer;
import de.metalcon.like.server.core.Configs;

public class ZmqTest {

    private LikeButtonServer server;

    private Dispatcher dispatcher;

    private ZMQ.Context ctx;

    @Before
    public void setUpBeforeClass() throws Exception {
        Configs.initialize(LikeButtonServer.DEFAULT_CONFIG_PATH);

        try {
            server = new LikeButtonServer();
            server.start();
        } catch (MetalconException e) {
            e.printStackTrace();
        }

        ctx = ZMQ.context(1);

        dispatcher = new Dispatcher();

        String endpoint = Configs.FRONTEND_LISTEN_URI;
        dispatcher.registerService(LikeServerGetCommonsRequest.class, endpoint);
        dispatcher.registerService(LikeServerFollowsRequest.class, endpoint);
        dispatcher.registerService(LikeServerGetLikedLikesRequest.class,
                endpoint);
        dispatcher.registerService(LikeServerGetLikesRequest.class, endpoint);

        dispatcher.registerService(LikeServerRemoveRelationRequest.class,
                endpoint);

        dispatcher
                .registerService(LikeServerAddRelationRequest.class, endpoint);
    }

    @After
    public void tearDown() throws Exception {
        server.interrupt();
        dispatcher.shutdown();
        ctx.term();
    }

    @Test
    public void testLikeServer() throws ServiceOverloadedException {
        final Response[] responses = new Response[2];

        //        dispatcher.execute(new LikeServerGetCommonsRequest(Uid.createFromID(0),
        //                Uid.createFromID(1), Vote.UP), new Callback<Response>() {
        //
        //            @Override
        //            public void onSuccess(final Response response) {
        //                responses[0] = response;
        //            }
        //        });
        //        dispatcher.gatherResults();
        //
        //        Assert.assertEquals(responses[0].getClass(),
        //                LikeServerMuidListResponse.class);

        dispatcher.execute(
                new LikeServerAddRelationRequest(Muid.create(UidType.BAND),
                        Muid.create(UidType.BAND), Vote.UP),
                new Callback<Response>() {

                    @Override
                    public void onSuccess(final Response response) {
                        responses[0] = response;
                    }
                });
        dispatcher.gatherResults();

        Assert.assertEquals(responses[0].getClass(),
                RequestQueuedResponse.class);

        dispatcher.execute(
                new LikeServerRemoveRelationRequest(Muid.create(UidType.BAND),
                        Muid.create(UidType.BAND)), new Callback<Response>() {

                    @Override
                    public void onSuccess(final Response response) {
                        responses[0] = response;
                    }
                });
        dispatcher.gatherResults();
        Assert.assertEquals(responses[0].getClass(),
                RequestQueuedResponse.class);
    }

}
