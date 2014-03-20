package de.metalcon.like.core;

import java.io.IOException;
import java.util.HashMap;

import de.metalcon.exceptions.MetalconException;

/**
 * @author Jonas Kunze
 */
public class NodeFactory {
	/*
	 * The file with all existing node muids. The muids are stored as
	 * concatenated longs where some might be 0 (fragmentation)
	 */
	private static PersistentMuidSetLevelDB AllNodes = null;

	/*
	 * This Map stores all nodes that are alive.
	 */
	private static HashMap<Long, Node> AllNodesAliveCache = new HashMap<Long, Node>();

	/**
	 * Dumps the Map with all existing nodes to fileName
	 * 
	 * @param fileName
	 *            The file to be written to
	 * @return true in case of success
	 */
	// private static boolean saveNodeListToFile(final String nodeFilePath) {
	// FileChannel file = raf.getChannel();
	// ByteBuffer buf = file.map(FileChannel.MapMode.READ_WRITE, 0,
	// 8 * AllExistingNodemuids.size());
	// for (Long muid : AllExistingNodemuids.keySet()) {
	// buf.putLong(muid);
	// }
	// return false;
	// }

	/**
	 * Reads all nodes from the given file
	 * 
	 * @param fileName
	 *            The file to be read
	 */
	public static void initialize(final String storDir) {
		if (AllNodes != null) {
			throw new RuntimeException(
					"NodeFactory has already been initialized.");
		}
		AllNodes = new PersistentMuidSetLevelDB(storDir);
		System.out.println("Finished reading " + storDir + ":");
		System.out.println(AllNodes.size() + " muids");
	}

	/**
	 * Initializes Node objects for all muids that were found in the persistent
	 * node list file
	 */
	public static void pushAllNodesToCache() {
		for (long muid : AllNodes) {
			getNode(muid);
		}
	}

	/**
	 * If a node with this muid is already alive it will be returned form the
	 * cache. If not and if the muid occurs in the AllExistingNodemuids (set of
	 * all muids in the db) it will be created
	 * 
	 * @param muid
	 *            The muid of the requested node
	 * @return A node object with the given muid or null if the muid doesnt
	 *         exist in the DB
	 */
	public static final Node getNode(final long muid) {
		Node n = AllNodesAliveCache.get(muid);
		if (n == null && AllNodes.contains(muid)) {
			synchronized (NodeFactory.class) {
				n = new Node(muid, false);
				AllNodesAliveCache.put(muid, n);
				return n;
			}
		}
		return n;
	}

	/**
	 * Factory method. If a Node with the same muid already exists no node will
	 * be created an null will be returned
	 * 
	 * This method is thread safe
	 * 
	 * @param muid
	 *            The muid of the new node
	 * @return A node object with the given muid
	 * @throws IOException
	 */
	public static final Node createNewNode(final long muid) throws IOException {
		if (AllNodes.contains(muid)) {
			throw new RuntimeException(
					"Calling Node.createNode with an muid that already exists in the DB");
		}
		synchronized (NodeFactory.class) {
			AllNodes.add(muid);

			Node n = new Node(muid, true);
			AllNodesAliveCache.put(muid, n);
			return n;
		}
	}

	static void removeNodeFromPersistentList(long muid) throws IOException {
		AllNodesAliveCache.remove(muid);
		AllNodes.remove(muid);
	}

	public static long[] getAllNodeMuids() {
		return AllNodes.toArray();
	}

	public static void clearDataBase(String areYouSure)
			throws MetalconException {
		if (areYouSure.equals("Yes I am")) {
			AllNodes = null;
			AllNodesAliveCache = new HashMap<Long, Node>();
		}
	}

	public static boolean nodeExists(final long muid) {
		Node n = AllNodesAliveCache.get(muid);
		if (n == null) {
			return AllNodes.contains(muid);
		}
		return true;
	}
}
