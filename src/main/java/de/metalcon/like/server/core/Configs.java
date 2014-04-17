package de.metalcon.like.server.core;

import java.lang.invoke.MethodHandles;

import de.metalcon.dbhelper.Options;

public class Configs extends Options {

    public static void initialize(String configFile) {
        try {
            Options.initialize(configFile, MethodHandles.lookup().lookupClass());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String FRONTEND_LISTEN_URI;

    public static String WRITE_WORKER_LISTEN_URI;

    public static String STORAGE_DIR;
}
