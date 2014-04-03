package de.metalcon.like.server.core;

import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.factory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;

import de.metalcon.exceptions.MetalconException;
import de.metalcon.exceptions.MetalconRuntimeException;

public class LevelDBHandler {
	private static DB db = null;

	private final byte[] keyPrefix;
	private final long keyPrefixID;

	private static String DBPath_;

	public static void initialize(final String DBPath) throws MetalconException {
		File f = new File(DBPath);

		if (!f.exists()) {
			if (!f.mkdirs()) {
				throw new MetalconException("Unable to create directory "
						+ DBPath);
			}
		}

		if (db == null) {
			try {
				Options options = new Options();
				options.createIfMissing(true);

				// options.logger(new Logger() {
				// public void log(String message) {
				// System.out.println(message);
				// }
				// });

				db = factory.open(new File(DBPath), options);
			} catch (IOException e) {
				throw new MetalconException("Unable to instanciate levelDB on "
						+ DBPath);
			}
		} else {
			throw new MetalconException(
					"LevelDBHandler has already been Initialized");
		}
		DBPath_ = DBPath;
	}

	public LevelDBHandler(final long keyPrefix) {
		keyPrefixID = keyPrefix;
		this.keyPrefix = new byte[8];
		this.keyPrefix[0] = (byte) (keyPrefix >> 56);
		this.keyPrefix[1] = (byte) (keyPrefix >> 48);
		this.keyPrefix[2] = (byte) (keyPrefix >> 40);
		this.keyPrefix[3] = (byte) (keyPrefix >> 32);
		this.keyPrefix[4] = (byte) (keyPrefix >> 24);
		this.keyPrefix[5] = (byte) (keyPrefix >> 16);
		this.keyPrefix[6] = (byte) (keyPrefix >> 8);
		this.keyPrefix[7] = (byte) (keyPrefix);
	}

	public LevelDBHandler(final String keyPrefix) {
		this(keyPrefix.hashCode() + 0xFFFFFFFFL * keyPrefix.hashCode());
	}

	public static void clearDataBase(String areYouSure) throws IOException {
		if (areYouSure.equals("Yes I am") && db != null) {
			db.close();
			IOHelper.deleteFile(new File(DBPath_));
			db = null;
		}
	}

	/**
	 * Associates the specified value with the specified key in the DB. If the
	 * DB previously contained a mapping for the key, the old value is replaced.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 */
	public void put(final String key, final int value) {
		db.put(generateKey(key), Serialize(value));
	}

	/**
	 * Associates the specified value with the specified key in the DB. If the
	 * DB previously contained a mapping for the key, the old value is replaced.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 */
	public void put(final long key, final long value) {
		db.put(generateKey(key), Serialize(value));
	}

	/**
	 * Adds value to the array associated with the specified key in the DB if it
	 * is not already included.
	 * 
	 * @param key
	 *            key associated with the array to which the specified value is
	 *            to be added
	 * @param value
	 *            value to be added to the array
	 */
	public void setAdd(final byte[] key, final long value) {
		long[] valueArray = getLongs(key);

		if (valueArray == null) {
			valueArray = new long[1];
		} else {
			/*
			 * Check if the long is already stored
			 */
			for (long current : valueArray) {
				if (current == value) {
					return;
				}
			}

			long[] tmp = new long[valueArray.length + 1];
			System.arraycopy(valueArray, 0, tmp, 0, valueArray.length);
			valueArray = tmp;
		}
		valueArray[valueArray.length - 1] = value;
		put(key, valueArray);
	}

	/**
	 * Removes value from the array associated with the specified key in the DB.
	 * 
	 * @param key
	 *            key associated with the array from which the specified value
	 *            has to be removed
	 * @param value
	 *            value to be removed from the array
	 * @return boolean true if the element has been found and removed
	 */
	public boolean setRemove(final byte[] key, final long value) {
		long[] valueArray = getLongs(key);

		if (valueArray == null) {
			return false;
		} else {
			/*
			 * Seek the element
			 */
			int pos = -1;
			for (long current : valueArray) {
				pos++;
				if (current == value) {
					break;
				}
			}
			if (pos == -1) {
				return false;
			}

			long[] tmp = new long[valueArray.length - 1];
			System.arraycopy(valueArray, 0, tmp, 0, pos);
			System.arraycopy(valueArray, pos + 1, tmp, pos, tmp.length - pos);
			valueArray = tmp;
		}
		put(key, valueArray);
		return true;
	}

	/**
	 * Removes value from the array associated with the specified key in the DB
	 * 
	 * @param key
	 *            key associated with the array from which the specified value
	 *            is to be removed
	 * @param value
	 *            value to be removed from the array
	 */
	public void removeFromSet(final byte[] key, final long value) {
		long[] valueArray = getLongs(key);

		if (valueArray == null) {
			return;
		}

		int elementPointer = 0;

		/*
		 * Seek the element
		 */
		while (elementPointer != valueArray.length) {
			long current = valueArray[elementPointer];
			if (current == value) {
				break;
			}
			elementPointer++;
		}

		/*
		 * Element has been found
		 */
		if (elementPointer != valueArray.length) {
			long[] tmp = new long[valueArray.length - 1];

			System.arraycopy(valueArray, 0, tmp, 0, elementPointer);
			System.arraycopy(valueArray, elementPointer + 1, tmp,
					elementPointer, valueArray.length - elementPointer - 1);

			// System.arraycopy(valueArray, elementPointer + 1, valueArray,
			// elementPointer, valueArray.length - elementPointer - 1);
			put(key, tmp);
		}
	}

	/**
	 * Associates the specified value with the specified key in the DB. If the
	 * DB previously contained a mapping for the key, the old value is replaced.
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 */
	public void put(final byte[] key, final long[] value) {
		db.put(key, Serialize(value));
	}

	/**
	 * Returns the integer to which the specified key is mapped, or
	 * Integer.MIN_VALUE if the DB contains no mapping for the key.
	 * 
	 * @param key
	 *            The key whose associated value is to be returned
	 * @return The integer to which the specified key is mapped, or
	 *         Integer.MIN_VALUE if the DB contains no mapping for the key.
	 */
	public int getInt(final String key) {
		try {
			byte[] bytes = db.get(generateKey(key.hashCode()));
			if (bytes == null) {
				return Integer.MIN_VALUE;
			}
			return (int) DeSerialize(bytes);
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Returns the long[] to which the specified key is mapped, or null if the
	 * DB contains no mapping for the key.
	 * 
	 * @param key
	 *            The key whose associated value is to be returned
	 * @return The long[] to which the specified key is mapped, or null if the
	 *         DB contains no mapping for the key.
	 */
	public long[] getLongs(final byte[] key) {
		byte[] bytes = db.get(key);
		if (bytes == null) {
			return null;
		}
		return (long[]) DeSerialize(bytes);
	}

	/**
	 * Removes the mapping for a key from this DB if it is present
	 * 
	 * @param keyUUID
	 *            The key to be removed
	 */
	public void removeKey(final long keyUUID) {
		db.delete(generateKey(keyUUID));
	}

	/**
	 * Try to avoid using this method and use get() instead!
	 * 
	 * @param keyUUID
	 * @return
	 */
	public boolean containsKey(final long keyUUID) {
		return db.get(generateKey(keyUUID)) != null;
	}

	/**
	 * Try to avoid using this method and use get() instead!
	 * 
	 * FIXME: Sort the Set and use binary search
	 * 
	 * @param keyUUID
	 * @return
	 */
	public boolean setContainsElement(final byte[] keyUUID, final long valueUUID) {
		if (getLongs(keyUUID) == null) {
			return false;
		}
		for (long l : getLongs(keyUUID)) {
			if (l == valueUUID) {
				return true;
			}
		}
		return false;
	}

	public byte[] generateKey(final String keySuffix) {
		// return bytes(keyPrefixID + keySuffix);
		return generateKey(keySuffix.hashCode());
	}

	public byte[] generateKey(final long keySuffix) {
		byte[] key = new byte[16];
		System.arraycopy(keyPrefix, 0, key, 0, 8);
		key[8] = (byte) (keySuffix >> 56);
		key[9] = (byte) (keySuffix >> 48);
		key[10] = (byte) (keySuffix >> 40);
		key[11] = (byte) (keySuffix >> 32);
		key[12] = (byte) (keySuffix >> 24);
		key[13] = (byte) (keySuffix >> 16);
		key[14] = (byte) (keySuffix >> 8);
		key[15] = (byte) (keySuffix);

		return key;
	}

	private static byte[] Serialize(Object obj) {
		byte[] out = null;
		if (obj != null) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(obj);
				out = baos.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return out;
	}

	private static Object DeSerialize(byte[] str) {
		Object out = null;
		if (str != null) {
			try {
				ByteArrayInputStream bios = new ByteArrayInputStream(str);
				ObjectInputStream ois = new ObjectInputStream(bios);
				out = ois.readObject();
			} catch (Exception e) {
				throw new MetalconRuntimeException(e.getMessage());
			}
		}
		return out;
	}

	@Override
	public String toString() {
		if (db == null) {
			return "DB is Empty";
		}
		StringBuilder builder = new StringBuilder();
		DBIterator iterator = db.iterator();
		outer: for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
			byte[] key = iterator.peekNext().getKey();
			// for (int i = 0; i != keyPrefix.length; i++) {
			// if (key[i] != keyPrefix[i]) {
			// continue outer;
			// }
			// }
			System.out.println(asString(iterator.peekNext().getKey()) + ":");
			builder.append(asString(iterator.peekNext().getKey()) + ":");
			builder.append("\t"
					+ Arrays.toString((long[]) DeSerialize(iterator.peekNext()
							.getValue())));
		}
		try {
			iterator.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}
}
