package mezz.jei.util;

import java.util.List;

public class CycleTimer {
	/* the amount of time in ms to display one thing before cycling to the next one */
	private static final int cycleTime = 1000;
	private long drawTime = 0;

	public <T> T getCycledItem(List<T> list) {
		if (list.isEmpty()) {
			return null;
		}
		Long index = (drawTime / cycleTime) % list.size();
		return list.get(index.intValue());
	}

	public void onDraw(boolean cycleEnabled) {
		if (cycleEnabled) {
			drawTime = System.currentTimeMillis();
		}
	}
}
