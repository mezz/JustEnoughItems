package mezz.jei.common.config;

public enum HistoryViewSide {
	LEFT,RIGHT,BOTH;

	public boolean isSide(HistoryViewSide side) {
		return this == side || side == BOTH;
	}
}
