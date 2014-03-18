/**
 * 
 */
package de.metalcon.like.api;


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
    public long[] getCommonNodes(long uuid1, long uuid2);

    /**
     * Returns a list of MUIDs of nodes liked by nodes liked by the node
     * associated with the given nodeMUID
     * 
     * @param nodeMUID
     *            The requested node
     * @return The list of nodes liked by any node liked by the node with the
     *         given MUID
     */
    public long[] getLikedLikes(final long nodeMUID);

    /**
     * puts a new Edge to the data store.
     * 
     * @param from
     * @param to
     */
    public void putEdge(long from, long to, final Vote vote);

    /**
     * deletes an Edge from the graph
     * 
     * @param from
     * @param to
     * @return true if the edge was in the graph
     */
    public void deleteEdge(long from, long to);
    
    /**
     * Returns a list of MUIDs of nodes liking the node with the MUID 'nodeMUID'
     * or null if the requested node does not exist
     * 
     * @param nodeMUID
     *            The requested node
     * @return The list of nodes liking the node with the given MUID
     */
    public long[] getLikedInNodes(final long nodeMUID);

    /**
     * Returns a list of MUIDs of nodes liked by the node with the MUID
     * 'nodeMUID' or null if the requested node does not exist
     * 
     * @param nodeMUID
     *            The requested node
     * @return The list of nodes liked by the node with the given MUID
     */
    public long[] getLikedOutNodes(final long nodeMUID);

    /**
     * Returns a list of MUIDs of nodes disliking the node with the MUID
     * 'nodeMUID' or null if the requested node does not exist
     * 
     * @param nodeMUID
     *            The requested node
     * @return The list of nodes disliking the node with the given MUID
     */
    public long[] getDislikedInNodes(final long nodeMUID);

    /**
     * Returns a list of MUIDs of nodes disliked by the node with the MUID
     * 'nodeMUID' or null if the requested node does not exist
     * 
     * @param nodeMUID
     *            The requested node
     * @return The list of nodes liked by the node with the given MUID
     */
    public long[] getDislikedOutNodes(final long nodeMUID);

    
}
