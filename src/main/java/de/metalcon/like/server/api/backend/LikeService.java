package de.metalcon.like.server.api.backend;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.metalcon.dbhelper.LevelDbHandler;
import de.metalcon.exceptions.MetalconException;
import de.metalcon.exceptions.MetalconRuntimeException;
import de.metalcon.like.api.Direction;
import de.metalcon.like.api.Vote;
import de.metalcon.like.server.core.Like;
import de.metalcon.like.server.core.Node;
import de.metalcon.like.server.core.NodeFactory;
import de.metalcon.like.server.core.PersistentLikeHistory;
import de.metalcon.like.server.core.PersistentUidSet;

/**
 * TODO: implement Vote follows(long from, long to). This method should be O(1)
 * put this to the interface so probably the likeserver needs to store another
 * hashmap
 * 
 * @author Jonas Kunze, rpickhardt
 */
public class LikeService implements LikeGraphApi {

    public LikeService(
            final String storageDir) throws MetalconException {
        File f = new File(storageDir);

        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new MetalconException("Unable to create directory "
                        + storageDir);
            }
        }

        LevelDbHandler.initialize(storageDir + "/levelDB");
        PersistentLikeHistory.initialize(storageDir + "/likesDB");
        PersistentUidSet.initialize();
        NodeFactory.initialize(storageDir);
    }

    @Override
    public long[] getCommonNodes(final long from, final long to, final Vote v) {
        Node f = NodeFactory.getNode(from);
        if (f == null) {
            throw new MetalconRuntimeException(
                    "Requested getCommonNodes with an unknown from ID");
            // return null;
        }
        return f.getCommonNodes(to, v);
    }

    @Override
    public void putEdge(final long from, final long to, final Vote vote) {
        try {
            Node f = NodeFactory.getNode(from);
            if (f == null) {
                f = NodeFactory.createNewNode(from);
            }

            f.addLike(new Like(to, (int) (System.currentTimeMillis() / 1000),
                    vote));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Delete the friendship between from and to. If from, to or the edge did
     * not exist, nothing will happen
     * 
     * @throws IOException
     */
    @Override
    public void deleteEdge(final long from, final long to) throws IOException {
        final Node f = NodeFactory.getNode(from);
        if (f == null) {
            return;
        }

        Node t = NodeFactory.getNode(to);
        if (t == null) {
            return;
        }

        f.removeFriendship(t);
    }

    /**
     * Returns a list of MUIDs of nodes liking the node with the MUID 'nodeMUID'
     * or null if the requested node does not exist
     * 
     * @param nodeMUID
     *            The requested node
     * @return The list of nodes liking the node with the given MUID
     */
    @Override
    public long[] getLikes(
            final long nodeMUID,
            final Direction direction,
            final Vote vote) {
        final Node n = NodeFactory.getNode(nodeMUID);
        if (n == null) {
            return null;
        }

        long[] result;
        if (direction == Direction.INCOMING) {
            result = n.getLikesIn(vote).toArray();
        } else if (direction == Direction.OUTGOING) {
            result = n.getLikesOut(vote).toArray();
        } else {
            long[] incoming = n.getLikesIn(vote).toArray();
            long[] outgoing = n.getLikesOut(vote).toArray();

            if (incoming == null) {
                if (outgoing == null) {
                    return null;
                } else {
                    return outgoing;
                }
            }
            if (outgoing == null) {
                return incoming;
            }
            /*
             * Merge both lists
             */
            if (incoming.length + outgoing.length != 0) {
                /*
                 * Remove duplicate elements
                 */
                Set<Long> set = new HashSet<Long>();
                for (long l : incoming) {
                    set.add(l);
                }
                for (long l : outgoing) {
                    set.add(l);
                }

                result = new long[set.size()];
                int pos = 0;
                for (long l : set) {
                    result[pos++] = l;
                }
            } else {
                return null;
            }
        }
        if (result == null || result.length == 0) {
            return null;
        }
        return result;
    }

    /**
     * Returns a list of MUIDs of nodes liked by nodes liked by the node
     * associated with the given nodeMUID
     * 
     * @param nodeMUID
     *            The requested node
     * @return The list of nodes liked by any node liked by the node with the
     *         given MUID
     */
    @Override
    public long[] getLikedLikes(final long nodeMUID, final Vote vote) {
        final Node n = NodeFactory.getNode(nodeMUID);
        if (n == null) {
            return null;
        }

        HashSet<Long> likedLikedNodes = new HashSet<Long>();

        /*
         * Iterate through all nodes liked by n
         */
        for (long likedMUID : n.getLikesOut(vote)) {
            if (likedMUID == 0) {
                break;
            }

            /*
             * Iterate through all nodes liked by likedNode and add those liked
             * nodes to the set
             */
            final Node likedNode = NodeFactory.getNode(likedMUID);
            for (long likedlikedMUID : likedNode.getLikesOut(vote)) {
                if (likedlikedMUID == 0) {
                    break;
                }
                likedLikedNodes.add(likedlikedMUID);
            }
        }
        long[] result = new long[likedLikedNodes.size()];
        int i = 0;
        for (long l : likedLikedNodes) {
            result[i++] = l;
        }
        return result;
    }

    /**
     * This will delete the whole Database
     * 
     * @param areYouSure
     *            Must be "Yes I am"
     * @throws IOException
     */
    public void clearDataBase(final String areYouSure) throws MetalconException {
        if (areYouSure.equals("Yes I am")) {
            try {
                LevelDbHandler.clearDataBase(areYouSure);
                PersistentLikeHistory.clearDataBase(areYouSure);
                NodeFactory.clearDataBase(areYouSure);
            } catch (IOException e) {
                throw new MetalconException("Unable to Clear LevelDB");
            }
        }
    }

    /**
     * 
     * @param from
     *            the muid of the node following to
     * @param to
     *            the muid of the node being followed by from
     * @return true if from follows to
     */
    public Vote follows(final long from, final long to) {
        final Node n = NodeFactory.getNode(from);
        if (n == null) {
            return null;
        }
        for (long l : n.getLikesOut(Vote.UP)) {
            if (l == to) {
                return Vote.UP;
            }
        }

        for (long l : n.getLikesOut(Vote.DOWN)) {
            if (l == to) {
                return Vote.DOWN;
            }
        }
        return null;
    }

    /**
     * 
     * @param muid
     *            the muid of the searched node
     * @return <true> if the given muid is stored in the database. This is only
     *         the case if at least on edge exists going from or to this node
     */
    public boolean nodeExists(final long muid) {
        return NodeFactory.nodeExists(muid);
    }

    /**
     * @return all Muids of nodes that have ever stored any like
     */
    public long[] getAllNownNodes() {
        return NodeFactory.getAllNodeMuids();
    }

    /**
     * Reads the persistent like histories and adds new likes into the commons
     * list of each node
     * 
     * @return The number of nanoseconds spend within this method
     */
    public long updateAllLargeNodes() {
        long start = System.nanoTime();
        long[] allNodes = NodeFactory.getAllNodeMuids();
        if (allNodes == null) {
            return System.nanoTime() - start;
        }

        for (long uuid : allNodes) {
            Node n = NodeFactory.getNode(uuid);
            n.updateLargeNodeCommons();
        }
        return System.nanoTime() - start;
    }
}
