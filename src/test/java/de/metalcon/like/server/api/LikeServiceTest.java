package de.metalcon.like.server.api;

import static org.junit.Assert.fail;

import org.junit.Test;

import de.metalcon.exceptions.MetalconException;
import de.metalcon.like.server.api.backend.LikeService;

public class LikeServiceTest extends AbstractLikeServiceTest {

	@Test
	public void testLikeService() throws MetalconException {
		likeService.clearDataBase("Yes I am");
		try {
			likeService = new LikeService("/dev/shm");
		} catch (Exception e) {
			fail(e.getMessage());
		}

		try {
			likeService = new LikeService(TEST_FOLDER);
			fail("LikeService() could be initialized twice");
		} catch (Exception e) {
		}

		try {
			likeService.clearDataBase("Yes I am");
			likeService = new LikeService(TEST_FOLDER + (Math.random() * 10000));
		} catch (Exception e) {
			e.printStackTrace();
			fail("LikeService() canot create directory " + e.getMessage());
		}

		// TODO: fitting this to the desired API. class LikeService() has to be
		// singleton and it should be possible to chose any directory

	}
}
