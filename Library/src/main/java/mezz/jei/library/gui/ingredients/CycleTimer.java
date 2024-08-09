package mezz.jei.library.gui.ingredients;

import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class CycleTimer {
	private static final CycleTimer ZERO_OFFSET = new CycleTimer(0);
	private static final int MAX_INDEX = 100_000;
	/* the amount of time in ms to display one thing before cycling to the next one */
	private static final int CYCLE_TIME_MS = 1_000;

	public static CycleTimer create(int offset) {
		if (offset == 0) {
			return ZERO_OFFSET;
		}
		return new CycleTimer(offset);
	}

	public static CycleTimer createWithRandomOffset() {
		int cycleOffset = (int) (Math.random() * MAX_INDEX);
		return new CycleTimer(cycleOffset);
	}

	private final int cycleOffset;
	private int index;

	private CycleTimer(int cycleOffset) {
		this.cycleOffset = cycleOffset;
		long now = System.currentTimeMillis();
		this.index = calculateIndex(now, cycleOffset);
	}

	private static int calculateIndex(long now, int cycleOffset) {
		long index = ((now / CYCLE_TIME_MS) % MAX_INDEX) + cycleOffset;
		return Math.toIntExact(index);
	}

	public <T> Optional<T> getCycled(List<Optional<T>> list) {
		if (list.isEmpty()) {
			return Optional.empty();
		}
		int index = this.index % list.size();
		return list.get(index);
	}

	public void update() {
		if (!Screen.hasShiftDown()) {
			long now = System.currentTimeMillis();
			index = calculateIndex(now, cycleOffset);
		}
	}
}
