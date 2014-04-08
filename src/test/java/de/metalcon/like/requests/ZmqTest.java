package de.metalcon.like.requests;

import net.hh.request_dispatcher.Callback;
import net.hh.request_dispatcher.Dispatcher;
import net.hh.request_dispatcher.service_adapter.ZmqAdapter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.zeromq.ZMQ;

import de.metalcon.api.responses.Response;
import de.metalcon.api.responses.SuccessResponse;
import de.metalcon.domain.Uid;
import de.metalcon.like.api.Vote;
import de.metalcon.like.api.requests.LikeServerFollowsRequest;
import de.metalcon.like.api.requests.LikeServerGetCommonsRequest;
import de.metalcon.like.api.requests.LikeServerGetLikedLikesRequest;
import de.metalcon.like.api.requests.LikeServerGetLikesRequest;
import de.metalcon.like.api.requests.LikeServerRemoveRelationRequest;
import de.metalcon.like.api.requests.LikeServerRequest;
import de.metalcon.like.server.LikeButtonServer;

public class ZmqTest {

    static LikeButtonServer server;

    static Dispatcher dispatcher;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        //        LikeButtonServer server;
        //        try {
        //            server = new LikeButtonServer();
        //            server.start();
        //        } catch (MetalconException e) {
        //            e.printStackTrace();
        //        }
        //
        //        Thread.sleep(100);
        dispatcher = new Dispatcher();

        ZMQ.Context ctx = ZMQ.context(1);

        String serviceID = "serviceID";
        dispatcher.registerServiceAdapter(serviceID,
                new ZmqAdapter<LikeServerRequest, SuccessResponse>(ctx,
                        LikeButtonServer.WRITE_WORKER_LISTEN_URI));
        dispatcher.setDefaultService(LikeServerGetCommonsRequest.class,
                serviceID);
        dispatcher.setDefaultService(LikeServerFollowsRequest.class, serviceID);
        dispatcher.setDefaultService(LikeServerGetLikedLikesRequest.class,
                serviceID);
        dispatcher
                .setDefaultService(LikeServerGetLikesRequest.class, serviceID);
        dispatcher.setDefaultService(LikeServerRemoveRelationRequest.class,
                serviceID);

    }

    @Test
    public void testLikeServer() {
        dispatcher.execute(new LikeServerGetCommonsRequest(Uid.createFromID(0),
                Uid.createFromID(1), Vote.UP), new Callback<Response>() {

            @Override
            public void onSuccess(final Response response) {
                System.out.println(response.getClass().getName() + "!!!!!!");

            }
        });

        dispatcher.gatherResults();
    }

}
