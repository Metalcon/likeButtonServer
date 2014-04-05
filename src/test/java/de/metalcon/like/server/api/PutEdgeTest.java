package de.metalcon.like.server.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import de.metalcon.like.api.Direction;
import de.metalcon.like.api.Vote;

public class PutEdgeTest extends AbstractLikeServiceTest {

	@Test
	public void testCreateNodeWhileCreatingEdges() {
		assertNull(likeService.getLikes(1, Direction.BOTH, Vote.UP));
		assertNull(likeService.getLikes(1, Direction.BOTH, Vote.UP));

		likeService.putEdge(1, 2, Vote.UP);

		Assert.assertEquals(1,
				likeService.getLikes(1, Direction.OUTGOING, Vote.UP).length);
		Assert.assertEquals(1,
				likeService.getLikes(2, Direction.INCOMING, Vote.UP).length);

		/*
		 * we've only created (1) -[:UP]-> (2) so (2) should not have any
		 * outNode and (1) not any InNode
		 */
		assertNull(likeService.getLikes(2, Direction.OUTGOING, Vote.UP));
		assertNull(likeService.getLikes(1, Direction.INCOMING, Vote.UP));

		/*
		 * Check Direction.BOTH
		 */
		Assert.assertEquals(1,
				likeService.getLikes(1, Direction.BOTH, Vote.UP).length);
		Assert.assertEquals(1,
				likeService.getLikes(2, Direction.BOTH, Vote.UP).length);

		likeService.putEdge(1, 3, Vote.UP);
		likeService.putEdge(2, 1, Vote.UP);
		likeService.putEdge(2, 3, Vote.UP);
		/*
		 * Now 1 and 2 should have 1 incoming and 2 outgoing likes.
		 * Directoin.both should only return 2 as 1->2 and 2->1 are creating
		 * duplicate IDs
		 */

		Assert.assertEquals(2,
				likeService.getLikes(1, Direction.BOTH, Vote.UP).length);
		Assert.assertEquals(2,
				likeService.getLikes(2, Direction.BOTH, Vote.UP).length);
		Assert.assertEquals(2,
				likeService.getLikes(3, Direction.BOTH, Vote.UP).length);

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
		assertNull(likeService.getLikes(1, Direction.INCOMING, Vote.UP));
		assertNull(likeService.getLikes(2, Direction.INCOMING, Vote.UP));
		assertNull(likeService.getLikes(1, Direction.OUTGOING, Vote.UP));
		assertNull(likeService.getLikes(2, Direction.OUTGOING, Vote.UP));

		likeService.putEdge(1, 2, Vote.UP);

		assertNotNull(likeService.getLikes(1, Direction.OUTGOING, Vote.UP));
		assertNotNull(likeService.getLikes(2, Direction.INCOMING, Vote.UP));
		assertNull(likeService.getLikes(1, Direction.OUTGOING, Vote.DOWN));
		assertNull(likeService.getLikes(2, Direction.OUTGOING, Vote.DOWN));

		assertNull(likeService.getLikes(2, Direction.OUTGOING, Vote.UP));
		assertNull(likeService.getLikes(1, Direction.INCOMING, Vote.UP));

		likeService.putEdge(1, 2, Vote.DOWN);

		assertNull(likeService.getLikes(1, Direction.OUTGOING, Vote.UP));
		assertNull(likeService.getLikes(2, Direction.INCOMING, Vote.UP));
		assertNotNull(likeService.getLikes(1, Direction.OUTGOING, Vote.DOWN));
		assertNotNull(likeService.getLikes(2, Direction.INCOMING, Vote.DOWN));

		assertNull(likeService.getLikes(2, Direction.OUTGOING, Vote.UP));
		assertNull(likeService.getLikes(1, Direction.INCOMING, Vote.UP));

		likeService.putEdge(1, 2, Vote.NEUTRAL);
		assertNull(likeService.getLikes(1, Direction.INCOMING, Vote.UP));
		assertNull(likeService.getLikes(2, Direction.INCOMING, Vote.UP));
		assertNull(likeService.getLikes(1, Direction.OUTGOING, Vote.UP));
		assertNull(likeService.getLikes(2, Direction.OUTGOING, Vote.UP));
	}

	@Test
	public void testPutMultiedge() {
		assertNull(likeService.getLikes(1, Direction.INCOMING, Vote.UP));
		assertNull(likeService.getLikes(2, Direction.INCOMING, Vote.UP));
		assertNull(likeService.getLikes(3, Direction.INCOMING, Vote.UP));
		assertNull(likeService.getLikes(4, Direction.INCOMING, Vote.UP));
		assertNull(likeService.getLikes(1, Direction.OUTGOING, Vote.UP));
		assertNull(likeService.getLikes(2, Direction.OUTGOING, Vote.UP));
		assertNull(likeService.getLikes(3, Direction.OUTGOING, Vote.UP));
		assertNull(likeService.getLikes(4, Direction.OUTGOING, Vote.UP));

		likeService.putEdge(1, 2, Vote.UP);
		likeService.putEdge(1, 3, Vote.UP);
		likeService.putEdge(2, 3, Vote.UP);

		// TODO: length is expected to be 2 in this case. no zeros should be
		// atached to the array
		System.out
				.println(likeService.getLikes(3, Direction.INCOMING, Vote.UP).length);
		for (long l : likeService.getLikes(3, Direction.INCOMING, Vote.UP)) {
			System.out.println(l);
		}

		assertEquals(
				likeService.getLikes(3, Direction.INCOMING, Vote.UP).length, 2);
		assertEquals(
				likeService.getLikes(2, Direction.OUTGOING, Vote.UP).length, 1);

		likeService.putEdge(1, 3, Vote.DOWN);
		assertEquals(
				likeService.getLikes(3, Direction.INCOMING, Vote.UP).length, 1);
		assertEquals(
				likeService.getLikes(2, Direction.OUTGOING, Vote.UP).length, 1);

		assertEquals(
				likeService.getLikes(3, Direction.INCOMING, Vote.DOWN).length,
				1);
		assertEquals(
				likeService.getLikes(1, Direction.OUTGOING, Vote.DOWN).length,
				1);

		System.out
				.println(likeService.getLikes(3, Direction.INCOMING, Vote.UP).length);
		for (long l : likeService.getLikes(3, Direction.INCOMING, Vote.UP)) {
			System.out.println(l);
		}

		System.out.println(likeService.getLikes(3, Direction.INCOMING,
				Vote.DOWN).length);
		for (long l : likeService.getLikes(3, Direction.INCOMING, Vote.DOWN)) {
			System.out.println(l);
		}

		assertEquals(likeService.getLikes(3, Direction.INCOMING, Vote.DOWN)[0],
				1);
		assertEquals(likeService.getLikes(1, Direction.OUTGOING, Vote.DOWN)[0],
				3);

	}
}
