package de.metalcon.like.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class PutEdgeTest extends AbstractLikeServiceTest {

	@Test
	public void testCreateNodeWhileCreatingEdges() {
		assertNull(likeService.getLikes(1, false, Vote.UP));
		assertNull(likeService.getLikes(2, false, Vote.UP));
		assertNull(likeService.getLikes(1, true, Vote.UP));
		assertNull(likeService.getLikes(2, true, Vote.UP));

		likeService.putEdge(1, 2, Vote.UP);

		assertNotNull(likeService.getLikes(1, true, Vote.UP));
		assertNotNull(likeService.getLikes(2, false, Vote.UP));

		/*
		 * we've only created (1) -[:UP]-> (2) so (2) should not have any
		 * outNode and (1) not any InNode
		 */
		assertNull(likeService.getLikes(2, true, Vote.UP));
		assertNull(likeService.getLikes(1, false, Vote.UP));
	}

	@Test
	public void testNodeCreation() {
		Assert.assertFalse(likeService.nodeExists(1));
		likeService.putEdge(1, 2, Vote.UP);
		Assert.assertTrue(likeService.nodeExists(1));
	}

	@Test
	public void testNotExistentEdges() throws IOException {
		/*
		 * Deletion means setting like to Vote.NEUTRAL
		 */
		assertNull(likeService.follows(1, 2));

		likeService.putEdge(1, 2, Vote.UP);
		assertEquals(likeService.follows(1, 2), Vote.UP);

		likeService.putEdge(1, 2, Vote.DOWN);
		assertEquals(likeService.follows(1, 2), Vote.DOWN);

		likeService.deleteEdge(1, 2);
		assertNull(likeService.follows(1, 2));

	}

	@Test
	public void testChangeVote() {
		assertNull(likeService.getLikes(1, false, Vote.UP));
		assertNull(likeService.getLikes(2, false, Vote.UP));
		assertNull(likeService.getLikes(1, true, Vote.UP));
		assertNull(likeService.getLikes(2, true, Vote.UP));

		likeService.putEdge(1, 2, Vote.UP);
		assertNotNull(likeService.getLikes(1, true, Vote.UP));
		assertNotNull(likeService.getLikes(2, false, Vote.UP));
		assertNull(likeService.getLikes(1, true, Vote.DOWN));
		assertNull(likeService.getLikes(2, true, Vote.DOWN));

		assertNull(likeService.getLikes(2, true, Vote.UP));
		assertNull(likeService.getLikes(1, false, Vote.UP));
		likeService.putEdge(1, 2, Vote.DOWN);

		// Return null instead of long[]{0,0,0,0}
		assertNull(likeService.getLikes(1, true, Vote.UP));
		assertNull(likeService.getLikes(2, false, Vote.UP));
		assertNotNull(likeService.getLikes(1, true, Vote.DOWN));
		assertNotNull(likeService.getLikes(2, true, Vote.DOWN));

		assertNull(likeService.getLikes(2, true, Vote.UP));
		assertNull(likeService.getLikes(1, false, Vote.UP));

		// even if it is used with delete edge the array now consits of several
		// elements with id 0
		likeService.putEdge(1, 2, Vote.NEUTRAL);
		assertNull(likeService.getLikes(1, false, Vote.UP));
		assertNull(likeService.getLikes(2, false, Vote.UP));
		assertNull(likeService.getLikes(1, true, Vote.UP));
		assertNull(likeService.getLikes(2, true, Vote.UP));
	}

	@Test
	public void testPutMultiedge() {
		assertNull(likeService.getLikes(1, false, Vote.UP));
		assertNull(likeService.getLikes(2, false, Vote.UP));
		assertNull(likeService.getLikes(3, false, Vote.UP));
		assertNull(likeService.getLikes(4, false, Vote.UP));
		assertNull(likeService.getLikes(1, true, Vote.UP));
		assertNull(likeService.getLikes(2, true, Vote.UP));
		assertNull(likeService.getLikes(3, true, Vote.UP));
		assertNull(likeService.getLikes(4, true, Vote.UP));

		likeService.putEdge(1, 2, Vote.UP);
		likeService.putEdge(1, 3, Vote.UP);
		likeService.putEdge(2, 3, Vote.UP);

		// TODO: length is expected to be 2 in this case. no zeros should be
		// atached to the array
		System.out.println(likeService.getLikes(3, false, Vote.UP).length);
		for (long l : likeService.getLikes(3, false, Vote.UP)) {
			System.out.println(l);
		}

		assertEquals(likeService.getLikes(3, false, Vote.UP).length, 2);
		assertEquals(likeService.getLikes(2, true, Vote.UP).length, 2);

		likeService.putEdge(1, 3, Vote.DOWN);
		assertEquals(likeService.getLikes(3, false, Vote.UP).length, 1);
		assertEquals(likeService.getLikes(2, true, Vote.UP).length, 1);

		assertEquals(likeService.getLikes(3, false, Vote.DOWN).length, 1);
		assertEquals(likeService.getLikes(1, true, Vote.DOWN).length, 1);

		System.out.println(likeService.getLikes(3, false, Vote.UP).length);
		for (long l : likeService.getLikes(3, false, Vote.UP)) {
			System.out.println(l);
		}

		System.out.println(likeService.getLikes(3, false, Vote.DOWN).length);
		for (long l : likeService.getLikes(3, false, Vote.DOWN)) {
			System.out.println(l);
		}

		assertEquals(likeService.getLikes(3, false, Vote.DOWN)[0], 1);
		assertEquals(likeService.getLikes(1, false, Vote.DOWN)[0], 3);

	}
}
