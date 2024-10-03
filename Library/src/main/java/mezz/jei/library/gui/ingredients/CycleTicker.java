package mezz.jei.library.gui.ingredients;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CycleTicker implements ICycler {
	private static final int MAX_INDEX = 100_000;
	private static final int TICKS_PER_UPDATE = 20;

	public static CycleTicker createWithRandomOffset() {
		int cycleOffset = (int) (Math.random() * MAX_INDEX);
		return new CycleTicker(cycleOffset);
	}

	private int tick = 0;
	private int index;

	private CycleTicker(int cycleOffset) {
		this.index = cycleOffset;
	}

	@Override
	@Nullable
	public <T> T getCycled(List<@Nullable T> list) {
		if (list.isEmpty()) {
			return null;
		}
		int index = this.index % list.size();
		return list.get(index);
	}

	public boolean tick() {
		if (Screen.hasShiftDown()) {
			return false;
		}
		tick++;
		if (tick >= TICKS_PER_UPDATE) {
			tick = 0;
			index++;
			return true;
		}
		return false;
	}
}
