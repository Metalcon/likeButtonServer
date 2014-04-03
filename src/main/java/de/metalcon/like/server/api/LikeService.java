package de.metalcon.like.server.api;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.metalcon.exceptions.MetalconException;
import de.metalcon.like.api.Direction;
import de.metalcon.like.server.core.LevelDBHandler;
import de.metalcon.like.server.core.Like;
import de.metalcon.like.server.core.Node;
import de.metalcon.like.server.core.NodeFactory;
import de.metalcon.like.server.core.PersistentLikeHistory;
import de.metalcon.like.server.core.PersistentMuidSetLevelDB;

/**
 * TODO: implement Vote follows(long from, long to). This method should be O(1)
 * put this to the interface so probably the likeserver needs to store another
 * hashmap
 * 
 * @author Jonas Kunze, rpickhardt
 */
public class LikeService implements LikeGraphApi {

	public LikeService(final String storageDir) throws MetalconException {
		File f = new File(storageDir);

		if (!f.exists()) {
			if (!f.mkdirs()) {
				throw new MetalconException("Unable to create directory "
						+ storageDir);
			}
		}

		LevelDBHandler.initialize(storageDir + "/levelDB");
		PersistentLikeHistory.initialize(storageDir + "/likesDB");
		PersistentMuidSetLevelDB.initialize();
		NodeFactory.initialize(storageDir);
	}

	@Override
	public long[] getCommonNodes(final long uuid1, final long uuid2) {
		Node f = NodeFactory.getNode(uuid1);
		if (f == null) {
			// System.err.println("Unknown Node uuid: " + uuid1);
			return new long[0];
		}
		return f.getCommonNodes(uuid2);
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
	 * Delete the friendship between from and to z@throws IOException
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
	public long[] getLikes(final long nodeMUID, final Direction direction,
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
	public long[] getLikedLikes(final long nodeMUID) {
		final Node n = NodeFactory.getNode(nodeMUID);
		if (n == null) {
			return null;
		}

		HashSet<Long> likedLikedNodes = new HashSet<Long>();

		/*
		 * Iterate through all nodes liked by n
		 */
		for (long likedMUID : n.getLikesOut(Vote.UP)) {
			if (likedMUID == 0) {
				break;
			}

			/*
			 * Iterate through all nodes liked by likedNode and add those liked
			 * nodes to the set
			 */
			final Node likedNode = NodeFactory.getNode(likedMUID);
			for (long likedlikedMUID : likedNode.getLikesOut(Vote.UP)) {
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
	public void clearDataBase(String areYouSure) throws MetalconException {
		if (areYouSure.equals("Yes I am")) {
			try {
				LevelDBHandler.clearDataBase(areYouSure);
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
	public Vote follows(long from, long to) {
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
}
