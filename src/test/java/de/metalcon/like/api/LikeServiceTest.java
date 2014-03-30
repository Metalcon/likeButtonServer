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
}
