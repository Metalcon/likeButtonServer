package de.metalcon.like.server.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import de.metalcon.like.api.Direction;

public class DeleteEdgeTest extends AbstractLikeServiceTest {

	/**
	 * Checks if the removed edge is not in the in and out links any more
	 * 
	 * @throws IOException
	 * 
	 */
	@Test
	public void testStateafterRemove() throws IOException {
		likeService.putEdge(1, 2, Vote.UP);
		likeService.putEdge(2, 3, Vote.UP);

		likeService.putEdge(1, 4, Vote.UP);
		likeService.putEdge(4, 3, Vote.UP);

		likeService.updateAllNodes();

		long[] commons = likeService.getCommonNodes(1, 3);
		HashSet<Long> set = convertArrayToHashSet(commons);

		assertTrue(set.contains(2L));
		assertTrue(set.contains(4L));

		long[] likes = likeService.getLikes(1, Direction.BOTH, Vote.UP);
		System.out.println(Arrays.toString(likes));

		likeService.deleteEdge(1, 2);

		likes = likeService.getLikes(1, Direction.BOTH, Vote.UP);
		System.out.println(Arrays.toString(likes));

		set = convertArrayToHashSet(likeService.getCommonNodes(1, 3));
		System.out.println(set.toString());
		assertFalse(set.contains(2L));
		assertTrue(set.contains(4L));

	}

}