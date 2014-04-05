package de.metalcon.like.server.core;

import java.util.HashMap;

import de.metalcon.dbhelper.ElementNotFoundException;
import de.metalcon.dbhelper.LevelDbHandler;
import de.metalcon.exceptions.MetalconRuntimeException;

/**
 * @author Jonas Kunze
 */
public class PersistentMuidMapLevelDB {
	LevelDbHandler dbHandler;

	public PersistentMuidMapLevelDB(final String keyPrefix) {
		dbHandler = new LevelDbHandler(keyPrefix);
	}

	/**
	 * @return The timestamp of the last update
	 */
	public int getLastUpdateTimeStamp() {
		try {
			return dbHandler.getInt("UpdateTS");
		} catch (ElementNotFoundException e) {
			return 0;
		}
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
		dbHandler.addToSet(keyUUID, valueUUID);
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
		dbHandler.removeFromSet(keyUUID, valueUUID);
	}

	/**
	 * Will delete all elements in this map
	 */
	public void removeAll() {
		/*
		 * TODO To be implemented
		 */
		throw new MetalconRuntimeException("Not yet implemented");
	}

	@Override
	public String toString() {
		return dbHandler.toString();
	}

}
