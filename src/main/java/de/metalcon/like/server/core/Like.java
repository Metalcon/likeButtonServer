package de.metalcon.like.server.core;

import de.metalcon.like.api.Vote;


/**
 * Stores a like edge between two MUIDs together with a timestamp
 * 
 * This class is not persistent
 * 
 * @author Jonas Kunze
 */
public class Like {

    private final int Timestamp;

    private final long MUID;

    private final Vote Vote;

    /**
     * 
     * @param Timestamp
     *            The time this like has been performed
     * @param MUID
     *            The UUID of the liked entity
     * @param Flags
     *            An integer containing flags like unlike
     */
    public Like(
            final long MUID,
            final int timestamp,
            final Vote vote) {
        Timestamp = timestamp;
        this.MUID = MUID;
        Vote = vote;
    }

    /**
     * 
     * @return The time this like has been performed
     */
    public int getTimestamp() {
        return Timestamp;
    }

    /**
     * 
     * @return The UUID of the liked entity
     */
    public long getMUID() {
        return MUID;
    }

    /**
     * 
     * @return An integer containing flags like unlike
     */
    public Vote getVote() {
        return Vote;
    }
}
