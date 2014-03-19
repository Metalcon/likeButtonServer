package de.metalcon.like.api;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;

import org.junit.Test;

public class LikeServiceTest extends AbstractLikeServiceTest {

    @Test
    public void testLikeService() {
        try {
            likeService = new LikeService("/dev/shm");
        } catch (Exception e) {
            fail("LikeService() canot create directory" + e.getMessage());
        }

        try {
            likeService = new LikeService(TEST_FOLDER);
        } catch (Exception e) {

        }
        try {
            likeService =
                    new LikeService(TEST_FOLDER + (Math.random() * 10000));
        } catch (Exception e) {
            assert (e.getClass().equals(FileNotFoundException.class));
        }

        //TODO: fitting this to the desired API. class LikeService() has to be singleton and it should be possible to chose any directory 

    }

    @Test
    public void testGetCommonNodes() {
        fail("Not yet implemented");
    }

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

    @Test
    public void testDeleteEdge() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetLikedInNodes() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetLikedOutNodes() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetDislikedInNodes() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetDislikedOutNodes() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetLikedLikes() {
        fail("Not yet implemented");
    }

}
