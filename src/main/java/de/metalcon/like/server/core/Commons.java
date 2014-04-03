package de.metalcon.like.server.core;

import de.metalcon.like.api.Vote;

/**
 * A class that is member of each startNode. It contains a map where key is the
 * key and the value consists of all X such that there is a path of length 2
 * from startNode to key
 * 
 * (startNode) -[:like]-> (X) -[:like]-> (key)
 * 
 * The map is persistent based on levelDB
 * 
 * @author Jonas Kunze, rpickhardt
 */
class Commons {

	private final Node node;

	private final Vote likeType;

	private PersistentMuidMapLevelDB persistentCommonsMap = null;

	/**
	 * 
	 * @param persistentFileName
	 *            The path to the persistent commons file
	 */
	public Commons(final Node node, final Vote likeType) {
		this.node = node;
		persistentCommonsMap = new PersistentMuidMapLevelDB(node.getMuid() + ""
				+ likeType);
		this.likeType = likeType;
	}

	/**
	 * Remove all entries in the DB
	 */
	public void delete() {
		persistentCommonsMap.removeAll();
	}

	/**
	 * Reads the persistent PersistentCommonsFile from disk and returns all
	 * muids that have the node muid in common with this node
	 * 
	 * @param muid
	 *            The entity the returned uuids have in common with this node
	 * @return The uuids of the nodes that have the entity uuid with the owner
	 *         of this Commons in common. The last uuids in the list may be 0
	 */
	public long[] getCommonNodes(long muid) {
		long[] commons = persistentCommonsMap.get(muid);

		if (commons != null) {
			/*
			 * Remove the trailing zeros
			 */
			int firstZero = -1;
			while (firstZero != commons.length - 1) {
				if (commons[++firstZero] == 0) {
					long[] tmp = new long[firstZero];
					System.arraycopy(commons, 0, tmp, 0, firstZero);
					return tmp;
				}
			}
		}
		return commons;
	}

	/**
	 * Updates the commons of node and writes the data to disk
	 */
	public void update() {
		final int now = (int) (System.currentTimeMillis() / 1000l);

		/*
		 * Update all outgoing nodes
		 */
		final long[] outNodes = node.getLikesOut(Vote.UP).toArray();
		if (outNodes != null) {
			for (long friendUUID : outNodes) {
				if (friendUUID == 0) {
					break;
				}
				updateFriend(NodeFactory.getNode(friendUUID), false);
			}
		}

		persistentCommonsMap.setUpdateTimeStamp(now);
	}

	/**
	 * Adds the friend to all entities in the commonsMap liked by the friend
	 * node.
	 * 
	 * @param friend
	 *            The friend to be added to the commonsMap
	 */
	public void friendAdded(Node friend) {
		updateFriend(friend, true);
	}

	/**
	 * Removes the friend from all entities in the commonsMap liked by the
	 * friend node.
	 * 
	 * @param friend
	 *            The friend to be removed from the commonsMap
	 */
	public void friendRemoved(Node friend) {
		for (long outID : friend.getLikesOut(Vote.UP)) {
			/*
			 * Remove the friend from the commons list of the liked entity
			 */
			persistentCommonsMap.remove(outID, friend.getMuid());
		}

		for (long outID : friend.getLikesOut(Vote.DOWN)) {
			/*
			 * Remove the friend from the commons list of the liked entity
			 */
			persistentCommonsMap.remove(outID, friend.getMuid());
		}
	}

	/**
	 * Adds the friend to all entities in the commonsMap liked by the friend
	 * node. If ignoreTimstamp is set to false only the entities liked by friend
	 * from the last update of this commons till now will be considered.
	 * 
	 * If a new friend is added to node you should call this method with this
	 * friend and ignoreTimestamp=false to add the friend to all entities liked
	 * by him
	 * 
	 * @param friend
	 *            The friend to be added to the HashMap
	 * @param ignoreTimestamp
	 *            If false only the entities liked by friend from the last
	 *            update of this commons till now will be considered.
	 */
	public void updateFriend(Node friend, boolean ignoreTimestamp) {
		int searchTS = ignoreTimestamp ? 0 : persistentCommonsMap
				.getLastUpdateTimeStamp();
		for (Like like : friend.getLikesFromTimeOn(searchTS)) {
			if (like.getMUID() == node.getMuid()) {
				continue;
			}
			if (like.getVote() == likeType) {
				/*
				 * Q1 node -> friend -> likedNode
				 */
				persistentCommonsMap.append(like.getMUID(), friend.getMuid());

				/*
				 * Q2 node -> likedNode && node -> friend -> likedNode FIXME:
				 * This is a dirty hack as the contains() is very expensive at
				 * the moment. We should sort the out nodes use binary search or
				 * compute the cross-section between node.getLikedNodes() and
				 * friend.getLikedNodes()
				 */
				if (node.getLikesOut(likeType).contains(like.getMUID())) {
					persistentCommonsMap.append(friend.getMuid(),
							like.getMUID());
				}
			} else {
				persistentCommonsMap.remove(like.getMUID(), friend.getMuid());
				persistentCommonsMap.remove(friend.getMuid(), like.getMUID());
			}
		}
	}

	@Override
	public String toString() {
		return node.getMuid() + "(" + likeType + "):\n"
				+ persistentCommonsMap.toString();
	}
}
