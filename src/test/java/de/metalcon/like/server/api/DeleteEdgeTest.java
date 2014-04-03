package de.metalcon.like.server.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashSet;

import org.junit.Test;

import de.metalcon.like.server.api.Vote;

public class DeleteEdgeTest extends AbstractLikeServiceTest {

	/**
	 * Remove a non existing edge should return an error
	 */
	@Test
	public void testDeleteExistingEdge() {
		try {
			likeService.deleteEdge(1, 2);
			fail("should return an error or at least a message telling that this is not possible");
		} catch (Exception e) {
			// TODO: define a kind of exception. (we could also have a return
			// value for deleteEdge)
		}
	}

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

		HashSet<Long> set = convertArrayToHashSet(likeService.getCommonNodes(3,
				1));
		assertTrue(set.contains(2));
		assertTrue(set.contains(4));

		likeService.deleteEdge(1, 2);
		set = convertArrayToHashSet(likeService.getCommonNodes(1, 3));
		assertFalse(set.contains(2));
		assertTrue(set.contains(4));

	}

}
