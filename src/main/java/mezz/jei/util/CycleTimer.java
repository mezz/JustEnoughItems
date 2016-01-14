package mezz.jei.util;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;

public class CycleTimer {
	/* the amount of time in ms to display one thing before cycling to the next one */
	private static final int cycleTime = 1000;
	private long startTime = 0;
	private long drawTime = 0;
	private long pausedDuration = 0;

	public CycleTimer(int offset) {
		this.startTime = System.currentTimeMillis() - (offset * cycleTime);
	}

	public <T> T getCycledItem(List<T> list) {
		if (list.isEmpty()) {
			return null;
		}
		Long index = ((drawTime - startTime) / cycleTime) % list.size();
		return list.get(index.intValue());
	}

	public void onDraw() {
		if (!GuiScreen.isShiftKeyDown()) {
			if (pausedDuration > 0) {
				startTime += pausedDuration;
				pausedDuration = 0;
			}
			drawTime = System.currentTimeMillis();
		} else {
			pausedDuration = System.currentTimeMillis() - drawTime;
		}
	}
}
