package de.metalcon.like.api;

import org.junit.Test;

public class PutEdgeTest extends AbstractLikeServiceTest {

    @Test
    public void testPutEdge() {
        assert (likeService.follows(1, 2) == Vote.NEUTRAL);

        assert (likeService.getLikedInNodes(1) == null);
        assert (likeService.getLikedInNodes(2) == null);
        likeService.putEdge(1, 2, Vote.UP);
        assert (likeService.getLikedInNodes(1) != null);
        assert (likeService.getLikedInNodes(2) != null);

        assert (likeService.follows(1, 2) == Vote.UP);

        likeService.putEdge(1, 2, Vote.DOWN);
        assert (likeService.follows(1, 2) == Vote.DOWN);

        likeService.putEdge(1, 2, Vote.NEUTRAL);
        assert (likeService.follows(1, 2) == Vote.NEUTRAL);

    }

}
