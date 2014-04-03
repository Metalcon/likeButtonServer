package de.metalcon.like.server.api;

public enum Vote {
	UP((byte) 1), DOWN((byte) 2), NEUTRAL((byte) 3);

	public final byte flag;

	private Vote(byte val) {
		flag = val;
	}

	public static Vote getByFlag(final int voteFlag) {
		if (voteFlag == UP.flag) {
			return UP;
		}
		if (voteFlag == DOWN.flag) {
			return DOWN;
		}
		if (voteFlag == NEUTRAL.flag) {
			return NEUTRAL;
		}
		throw new RuntimeException("Bad vote flag: " + voteFlag);
	}
}