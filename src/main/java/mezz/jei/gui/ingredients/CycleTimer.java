package mezz.jei.gui.ingredients;

import org.jetbrains.annotations.Nullable;
import java.util.List;

import net.minecraft.client.gui.screens.Screen;

public class CycleTimer {
	/* the amount of time in ms to display one thing before cycling to the next one */
	private static final int cycleTime = 1000;
	private long startTime;
	private long drawTime;
	private long pausedDuration = 0;

	public CycleTimer(int offset) {
		long time = System.currentTimeMillis();
		this.startTime = time - ((long) offset * cycleTime);
		this.drawTime = time;
	}

	@Nullable
	public <T> T getCycledItem(List<T> list) {
		if (list.isEmpty()) {
			return null;
		}
		long index = ((drawTime - startTime) / cycleTime) % list.size();
		return list.get(Math.toIntExact(index));
	}

	public void onDraw() {
		if (!Screen.hasShiftDown()) {
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
