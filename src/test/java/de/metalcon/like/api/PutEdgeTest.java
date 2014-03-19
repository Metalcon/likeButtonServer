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

    @Test
    public void testChangeVote() {
        assertNull(likeService.getLikedInNodes(1));
        assertNull(likeService.getLikedInNodes(2));
        assertNull(likeService.getLikedOutNodes(1));
        assertNull(likeService.getLikedOutNodes(2));

        likeService.putEdge(1, 2, Vote.UP);
        assertNotNull(likeService.getLikedOutNodes(1));
        assertNotNull(likeService.getLikedInNodes(2));
        assertNull(likeService.getDislikedOutNodes(1));
        assertNull(likeService.getDislikedInNodes(2));

        assertNull(likeService.getLikedOutNodes(2));
        assertNull(likeService.getLikedInNodes(1));
        likeService.putEdge(1, 2, Vote.DOWN);
        assertNull(likeService.getLikedOutNodes(1));
        assertNull(likeService.getLikedInNodes(2));
        assertNotNull(likeService.getDislikedOutNodes(1));
        assertNotNull(likeService.getDislikedInNodes(2));

        assertNull(likeService.getLikedOutNodes(2));
        assertNull(likeService.getLikedInNodes(1));

        // even if it is used with delete edge the array now consits of several elements with id 0
        likeService.putEdge(1, 2, Vote.NEUTRAL);
        assertNull(likeService.getLikedInNodes(1));
        assertNull(likeService.getLikedInNodes(2));
        assertNull(likeService.getLikedOutNodes(1));
        assertNull(likeService.getLikedOutNodes(2));
    }

    @Test
    public void testPutMultiedge() {
        assertNull(likeService.getLikedInNodes(1));
        assertNull(likeService.getLikedInNodes(2));
        assertNull(likeService.getLikedInNodes(3));
        assertNull(likeService.getLikedInNodes(4));
        assertNull(likeService.getLikedOutNodes(1));
        assertNull(likeService.getLikedOutNodes(2));
        assertNull(likeService.getLikedOutNodes(3));
        assertNull(likeService.getLikedOutNodes(4));

        likeService.putEdge(1, 2, Vote.UP);
        likeService.putEdge(1, 3, Vote.UP);
        likeService.putEdge(2, 3, Vote.UP);

        //TODO: length is expected to be 2 in this case. no zeros should be atached to the array
        System.out.println(likeService.getLikedInNodes(3).length);
        for (long l : likeService.getLikedInNodes(3)) {
            System.out.println(l);
        }

        assertEquals(likeService.getLikedInNodes(3).length, 2);
        assertEquals(likeService.getLikedOutNodes(2).length, 2);

        likeService.putEdge(1, 3, Vote.DOWN);
        assertEquals(likeService.getLikedInNodes(3).length, 1);
        assertEquals(likeService.getLikedOutNodes(2).length, 1);

        assertEquals(likeService.getDislikedInNodes(3).length, 1);
        assertEquals(likeService.getDislikedOutNodes(1).length, 1);

        System.out.println(likeService.getLikedInNodes(3).length);
        for (long l : likeService.getLikedInNodes(3)) {
            System.out.println(l);
        }

        System.out.println(likeService.getDislikedInNodes(3).length);
        for (long l : likeService.getDislikedInNodes(3)) {
            System.out.println(l);
        }

        assertEquals(likeService.getDislikedInNodes(3)[0], 1);
        assertEquals(likeService.getDislikedInNodes(1)[0], 3);

    }
}
