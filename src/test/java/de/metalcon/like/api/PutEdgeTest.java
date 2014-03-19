package de.metalcon.like.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PutEdgeTest extends AbstractLikeServiceTest {

    @Test
    public void testCreateNodeWhileCreatingEdges() {
        assertNull(likeService.getLikedInNodes(1));
        assertNull(likeService.getLikedInNodes(2));
        assertNull(likeService.getLikedOutNodes(1));
        assertNull(likeService.getLikedOutNodes(2));

        likeService.putEdge(1, 2, Vote.UP);
        assertNotNull(likeService.getLikedOutNodes(1));
        assertNotNull(likeService.getLikedInNodes(2));

        assertNull(likeService.getLikedOutNodes(2));
        assertNull(likeService.getLikedInNodes(1));
    }

    @Test
    public void testNotExistentEdges() {
        assertEquals(likeService.follows(1, 2), Vote.NEUTRAL);

        likeService.putEdge(1, 2, Vote.DOWN);
        assertEquals(likeService.follows(1, 2), equals(Vote.DOWN));

        likeService.putEdge(1, 2, Vote.UP);
        assertEquals(likeService.follows(1, 2), equals(Vote.UP));

        likeService.putEdge(1, 2, Vote.NEUTRAL);
        assertEquals(likeService.follows(1, 2), equals(Vote.NEUTRAL));

    }
}
