/**
 * 
 */
package de.metalcon.like.server.api;

import java.io.IOException;

import de.metalcon.like.api.Direction;

/**
 * @author Rene Pickhardt
 * 
 */
public interface LikeGraphApi {

	/**
	 * Retrieves the list of common neighbors of 2 nodes in the graph with uuid1
	 * and uuid2
	 * 
	 * @param uuid1
	 * @param uuid2
	 * @return
	 */
	public long[] getCommonNodes(long uuid1, long uuid2) throws IOException;

	/**
	 * Returns a list of MUIDs of nodes liked by nodes liked by the node
	 * associated with the given nodeMUID
	 * 
	 * @param nodeMUID
	 *            The requested node
	 * @return The list of nodes liked by any node liked by the node with the
	 *         given MUID
	 */
	public long[] getLikedLikes(final long nodeMUID) throws IOException;

	/**
	 * puts a new Edge to the data store.
	 * 
	 * @param from
	 * @param to
	 */
	public void putEdge(long from, long to, final Vote vote) throws IOException;

	/**
	 * deletes an Edge from the graph
	 * 
	 * @param from
	 * @param to
	 * @return true if the edge was in the graph
	 */
	public void deleteEdge(long from, long to) throws IOException;

	/**
	 * Returns a list of MUIDs of nodes (dis)liked by the node with the MUID
	 * 'nodeMUID' if directionOut is true (or null if the requested node does
	 * not exist). If directionOut is set to false all nodes disliking the given
	 * node are returned
	 * 
	 * @param nodeMUID
	 *            The requested node
	 * @return The list of nodes (dis)liking the node or being (dis)liked by the
	 *         node
	 */
	public long[] getLikes(final long nodeMUID, final Direction direction,
			final Vote vote) throws IOException;

}
