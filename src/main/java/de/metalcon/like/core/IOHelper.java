package de.metalcon.like.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class IOHelper {
	/**
	 * Recursively deletes the given file which may be a directory
	 */
	public static void deleteFile(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles()) {
				deleteFile(c);
			}
		}
		if (!f.delete()) {
			throw new FileNotFoundException("Failed to delete file: " + f);
		}
	}
}
