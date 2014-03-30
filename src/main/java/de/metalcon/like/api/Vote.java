package de.metalcon.like.api;

public enum Vote {
	UP((byte) 1), DOWN((byte) 2), NEUTRAL((byte) 3);

	public byte value;

	private Vote(byte val) {
		value = val;
	}

	public static Vote getByFlag(final int voteFlag) {
		if (voteFlag == 1) {
			return UP;
		}
		if (voteFlag == 2) {
			return DOWN;
		}
		if (voteFlag == 3) {
			return NEUTRAL;
		}
		throw new RuntimeException("Bad vote flag: " + voteFlag);
	}
}