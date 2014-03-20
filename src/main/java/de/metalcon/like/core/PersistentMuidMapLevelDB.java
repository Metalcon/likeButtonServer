package de.metalcon.like.core;

import java.util.HashMap;

/**
 * @author Jonas Kunze
 */
public class PersistentMuidMapLevelDB {
	private static final int InitialArrayLength = 4;

	LevelDBHandler dbHandler;

	public PersistentMuidMapLevelDB(final long keyPrefix) {
		dbHandler = new LevelDBHandler(keyPrefix);
	}

	/**
	 * @return The timestamp of the last update
	 */
	public int getLastUpdateTimeStamp() {
		return dbHandler.getInt("UpdateTS");
	}

	/**
	 * 
	 * @param updateTimeStamp
	 *            The timestamp of the last update
	 */
	public void setUpdateTimeStamp(final int updateTimeStamp) {
		dbHandler.put("UpdateTS", updateTimeStamp);
	}

	/**
	 * 
	 * @param keyUUID
	 * @param valueUUID
	 */
	public void append(final long keyUUID, final long valueUUID) {
		dbHandler.setAdd(keyUUID, valueUUID);
	}

	/**
	 * Returns the long[] to which the keyUUID is mapped
	 * 
	 * @param keyUUID
	 *            The key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or null if this
	 *         map contains no mapping for the key
	 * @see HashMap#get(Object)
	 */
	public long[] get(final long keyUUID) {
		return dbHandler.getLongs(keyUUID);
	}

	/**
	 * Removes all elements associated with the given keyUUID
	 * 
	 * @param keyUUID
	 *            The key of the list
	 */
	public void removeKey(final long keyUUID) {
		dbHandler.removeKey(keyUUID);
	}

	/**
	 * Removes the element valueUUID from the list associated with the given
	 * keyUUID
	 * 
	 * @param keyUUID
	 *            The key of the list
	 * @param valueUUID
	 *            The element to be deleted from the list
	 */
	public void remove(final long keyUUID, final long valueUUID) {
		/*
		 * TODO To be implemented
		 */
	}

	/**
	 * Will delete all elements in this map
	 */
	public void removeAll() {
		/*
		 * TODO To be implemented
		 */
	}

}
