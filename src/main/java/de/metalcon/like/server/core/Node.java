package de.metalcon.like.server.core;

import java.io.IOException;

import de.metalcon.like.server.api.Vote;

/**
 * @author Jonas Kunze
 */
public class Node {

	// Static Variables
	private static final int LastLikeCacheSize = 10;

	// Class Variables
	private final long Muid;

	private final Commons likeCommons;

	private final Commons dislikeCommons;

	/*
	 * lastLikes[lastLikesFirstEntryPointer] is the newest like The list is
	 * ordered by descending timestamps (newest first)
	 */
	private final Like[] lastLikesCache = new Like[LastLikeCacheSize];

	// TODO: Use AtomicInteger instead of synchronized methods for performance
	// boost:
	private int lastLikesFirstEntryPointer = LastLikeCacheSize;

	/*
	 * All out nodes liked/diskliked by this node
	 */
	private final PersistentMuidSetLevelDB likedOut;

	private final PersistentMuidSetLevelDB dislikedOut;

	/*
	 * All out nodes liking/disliking this node
	 */
	private final PersistentMuidSetLevelDB likedIn;

	private final PersistentMuidSetLevelDB dislikedIn;

	private final PersistentLikeHistory likeHistory;

	/**
	 * This constructor may only be called by the NodeFactory class
	 * 
	 * @param muid
	 *            The muid of the node
	 * @param storageDir
	 *            The path to the directory where all node files are stored
	 * @param isNewNode
	 *            If false the corresponding files will be read into memory. If
	 *            true we will not touch the disk.
	 */
	Node(final long muid, boolean isNewNode) {
		Muid = muid;
		// commons = new Commons(this, storageDir,
		// PersistentMuidArrayMap.class);

		// commons = new Commons(this, storageDir,
		// PersistentMuidArrayMapRedis.class);

		// commons = new Commons(this, storageDir, LazyPersistentMuidMap.class);

		likeCommons = new Commons(this, Vote.UP);

		dislikeCommons = new Commons(this, Vote.DOWN);

		// try {
		// friends = new PersistentMuidSet(storageDir + "/" + Muid
		// + "_friends");
		// inNodes = new PersistentMuidSet(storageDir + "/" + Muid
		// + "_inNodes");
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// System.exit(1);
		// }

		likedOut = new PersistentMuidSetLevelDB(Muid + "likedOut");
		likedIn = new PersistentMuidSetLevelDB(Muid + "likedIn");
		dislikedOut = new PersistentMuidSetLevelDB(Muid + "dislikedOut");
		dislikedIn = new PersistentMuidSetLevelDB(Muid + "dislikedIn");

		likeHistory = new PersistentLikeHistory(Muid);
	}

	/**
	 * Removes this Node from the DB
	 * 
	 * @throws IOException
	 */
	public void delete() throws IOException {
		for (long friendMuid : likedOut) {
			Node n = NodeFactory.getNode(friendMuid);
			n.removeFriendship(this);
		}
		likeCommons.delete();
		dislikeCommons.delete();
		likedOut.delete();
		likedIn.delete();

		dislikedOut.delete();
		dislikedIn.delete();

		NodeFactory.removeNodeFromPersistentList(Muid);

		likeHistory.delete();
	}

	/**
	 * Get all likes younger than the given timestamp
	 * 
	 * @param timestamp
	 *            The time all likes have to be younger than
	 * @return An array of the newest likes
	 */
	public Like[] getLikesFromTimeOn(final int timestamp) {
		int arrayLength = 10;
		Like[] likesFoundInCache = new Like[arrayLength];
		int likesFoundInCachePointer = 0;
		int lastLikesPointer = lastLikesFirstEntryPointer;

		Like[] likesFromDisk = null;

		Like nextLike = null;
		while (true) {
			if (likesFoundInCachePointer == arrayLength) {
				// Overflow-> Create new array with twice the length
				arrayLength *= 2;
				Like[] tmp = new Like[arrayLength];
				System.arraycopy(likesFoundInCache, 0, tmp, 0, arrayLength / 2);
				likesFoundInCache = tmp;
			}
			if (lastLikesPointer != lastLikesCache.length) {
				nextLike = lastLikesCache[lastLikesPointer++];
			} else {

				/*
				 * nextLike is now the oldest like we found -> read all likes
				 * from file that are younger than timestamp but older than
				 * nextLike.getTimestamp()
				 */
				if (nextLike == null) {
					likesFromDisk = getLikesFromTimeOnFromDisk(timestamp,
							Integer.MAX_VALUE);
				} else {
					likesFromDisk = getLikesFromTimeOnFromDisk(timestamp,
							nextLike.getTimestamp());
				}
				break;
			}

			if (nextLike.getTimestamp() < timestamp) {
				break;
			}

			likesFoundInCache[likesFoundInCachePointer++] = nextLike;
		} // while (true)

		/*
		 * Merge the two arrays from cache and disk
		 */
		if (likesFromDisk != null) {
			if (lastLikesFirstEntryPointer == LastLikeCacheSize) {
				/*
				 * The cache is still empty, so let's copy the likes read from
				 * disk into it
				 */
				synchronized (lastLikesCache) {
					int elementNumTocopy = likesFromDisk.length > LastLikeCacheSize ? LastLikeCacheSize
							: likesFromDisk.length;
					System.arraycopy(likesFromDisk, 0, lastLikesCache,
							LastLikeCacheSize - elementNumTocopy,
							elementNumTocopy);

					lastLikesFirstEntryPointer = LastLikeCacheSize
							- elementNumTocopy;
				}
				/*
				 * Now we can return likesFromDisk as likesFound is still empty
				 * as the cache was empty before
				 */
				return likesFromDisk;
			} else {
				/*
				 * The cache was not empty so we have to merge the likes from
				 * cache and those from disk
				 */
				Like[] result = new Like[likesFoundInCachePointer
						+ likesFromDisk.length];
				System.arraycopy(likesFoundInCache, 0, result, 0,
						likesFoundInCachePointer);
				System.arraycopy(likesFromDisk, 0, result,
						likesFoundInCachePointer, likesFromDisk.length);

				return result;
			}
		} else {
			/*
			 * We did not access the disk. This means that likesFoundInCache
			 * might have 0 elements at the end. So here we remove these empty
			 * slots in the array by creating a new array with the correct
			 * length and copying all found likes into it
			 */
			if (likesFoundInCachePointer != likesFoundInCache.length) {
				Like[] shortened = new Like[likesFoundInCachePointer];
				System.arraycopy(likesFoundInCache, 0, shortened, 0,
						likesFoundInCachePointer);
				return shortened;
			}
		}

		/*
		 * We come here only if all Likes were found in the lastLikes cache and
		 * the likesFound array was completely filled
		 */
		return likesFoundInCache;
	}

	/**
	 * Seeks the persistent likes file and returns an array of all like found
	 * with the timestamp TS being startTs< TS < stopTS
	 * 
	 * 
	 * @param startTS
	 *            All returned likes will have a higher timestamp
	 * @param stopTS
	 *            All returned likes will have a lower timestamp
	 * @return The array of all found likes or <code>null</code> if no like was
	 *         found or the file was empty. The elements are ordered by
	 *         descending timestamps (newest first).
	 */
	private Like[] getLikesFromTimeOnFromDisk(final int startTS,
			final int stopTS) {
		return likeHistory.getLikesWithinTimePeriod(startTS, stopTS);
	}

	/**
	 * Writes the new like to the cache and the persistent file
	 * 
	 * @param like
	 *            The new like performed by this entity
	 * @return true in case of success, false if a problem with the FS occurred
	 * @throws IOException
	 */
	public void addLike(Like like) throws IOException {
		Node likedNode = NodeFactory.getNode(like.getMUID());
		if (likedNode == null) {
			likedNode = NodeFactory.createNewNode(like.getMUID());
		}

		addOutNode(likedNode.Muid, like.getVote());
		likedNode.addInNode(Muid, like.getVote());

		/*
		 * Update the commons maps by adding likedNode to all out nodes of
		 * likedNode
		 */
		if (like.getVote() == Vote.UP) {
			likeCommons.friendAdded(likedNode);
			dislikeCommons.friendRemoved(likedNode);
		} else if (like.getVote() == Vote.DOWN) {
			likeCommons.friendRemoved(likedNode);
			dislikeCommons.friendAdded(likedNode);
		} else {
			likeCommons.friendRemoved(likedNode);
			dislikeCommons.friendRemoved(likedNode);
		}

		synchronized (lastLikesCache) {
			if (lastLikesFirstEntryPointer == 0) {
				/*
				 * Move all elements one slot up. The last (oldest) element will
				 * be dropped
				 */
				System.arraycopy(lastLikesCache, 0, lastLikesCache, 1,
						LastLikeCacheSize - 1);
			} else {
				lastLikesFirstEntryPointer--;
			}

			lastLikesCache[lastLikesFirstEntryPointer] = like;

			likeHistory.addLike(like);
		}
	}

	/**
	 * Creates and writes the new like to the cache and the persistent file
	 * 
	 * @return true in case of success, false if a problem with the FS occurred
	 * @throws IOException
	 * @see Like#Like
	 */
	public void addLike(final long muid, final int timestamp, final Vote vote)
			throws IOException {
		addLike(new Like(muid, timestamp, vote));
	}

	/**
	 * Removes the given Node from the friend list
	 * 
	 * @param friend
	 *            The node to be deleted
	 * @return <code>true</code> if the node has been found, <code>false</code>
	 *         if not
	 * @throws IOException
	 */
	public void removeFriendship(Node friend) throws IOException {
		addLike(new Like(friend.getMuid(),
				(int) System.currentTimeMillis() / 1000, Vote.NEUTRAL));
	}

	/**
	 * Adds a new node to the out-list
	 * 
	 * @param inNode
	 *            The node to be added
	 */
	private void addOutNode(final long outNodeMuid, final Vote v) {
		synchronized (dislikedOut) {
			synchronized (likedOut) {
				synchronized (likedOut) {
					if (v == Vote.UP) {
						likedOut.add(outNodeMuid);
						dislikedOut.remove(outNodeMuid);
					} else if (v == Vote.DOWN) {
						likedOut.remove(outNodeMuid);
						dislikedOut.add(outNodeMuid);
					} else {
						likedOut.remove(outNodeMuid);
						dislikedOut.remove(outNodeMuid);
					}
				}
			}
		}
	}

	/**
	 * Adds a new node to the in-list
	 * 
	 * @param inNode
	 *            The node to be added
	 */
	private void addInNode(final long inNodeMuid, final Vote v) {
		synchronized (likedIn) {
			synchronized (dislikedIn) {
				if (v == Vote.UP) {
					likedIn.add(inNodeMuid);
					dislikedIn.remove(inNodeMuid);
				} else if (v == Vote.DOWN) {
					likedIn.remove(inNodeMuid);
					dislikedIn.add(inNodeMuid);
				} else {
					likedIn.remove(inNodeMuid);
					dislikedIn.remove(inNodeMuid);
				}
			}
		}
	}

	/**
	 * @param vote
	 *            If this parameter equals Vote.UP all nodes liked by this node
	 *            will be returned. If it's set to Vote.DOWN all disliked nodes
	 *            instead. Vote.NEUTRAL will trigger a return null;
	 * @return All MUIDs liked/disliked by this node, sorted by MUID
	 */
	public PersistentMuidSetLevelDB getLikesOut(final Vote vote) {
		if (vote == Vote.UP) {
			return likedOut;
		}
		if (vote == Vote.DOWN) {
			return dislikedOut;
		}
		return null;
	}

	public PersistentMuidSetLevelDB getLikesIn(final Vote vote) {
		if (vote == Vote.UP) {
			return likedIn;
		}
		if (vote == Vote.DOWN) {
			return dislikedIn;
		}
		return null;
	}

	/**
	 * Returns all MUIDs that are liked by this node and liked the node with the
	 * given muid
	 * 
	 * @param muid
	 *            The entity the returned muids have in common with this node
	 * @return The nodes that have the entity muid with this node in common
	 */
	public long[] getCommonNodes(long muid) {
		return likeCommons.getCommonNodes(muid);
	}

	/**
	 * Returns all MUIDs that are liked by this node and disliked the node with
	 * the given muid
	 * 
	 * @param muid
	 *            The entity the returned muids have in common with this node
	 * @return The nodes that have the entity muid with this node in common
	 */
	public long[] getCommonDislikedNodes(long muid) {
		return dislikeCommons.getCommonNodes(muid);
	}

	public final long getMuid() {
		return Muid;
	}

	protected Commons getCommons() {
		return likeCommons;
	}

	public void updateCommons() {
		likeCommons.update();
	}
}
