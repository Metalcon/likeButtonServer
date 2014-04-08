/**
 * 
 */
package de.metalcon.like.server.api.backend;

import java.io.IOException;

import de.metalcon.like.api.Direction;
import de.metalcon.like.api.Vote;

/**
 * @author Rene Pickhardt
 * 
 */
public interface LikeGraphApi {

	/**
	 * Retrieves the list of common neighbors (X) such that
	 * 
	 * (from) -[:(dis)like]-> (X) -[:(dis)like]-> (to)
	 * 
	 * @param from
	 *            muid of the (from) node (see top)
	 * @param to
	 *            muid of the (to) node (see top)
	 * @param v
	 *            defines the relation: Vote.up or Vote.down between (from), (X)
	 *            and (to)
	 * @return (X), the muids that (from) has in common with (to)
	 */
	public long[] getCommonNodes(long from, long to, Vote v) throws IOException;

	/**
	 * Returns a list of Muids of nodes (dis)liked by nodes (dis)liked by the
	 * node associated with the given nodeMUID
	 * 
	 * @param nodeMUID
	 *            The requested node
	 * @param vote
	 *            The relation type (Vote.UP or Vote.DOWN)
	 * @return The list of nodes liked by any node liked by the node with the
	 *         given MUID
	 */
	public long[] getLikedLikes(final long nodeMUID, final Vote vote)
			throws IOException;

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
