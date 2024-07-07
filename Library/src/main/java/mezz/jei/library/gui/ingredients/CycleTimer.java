package mezz.jei.library.gui.ingredients;

import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

public class CycleTimer {
	/* the amount of time in ms to display one thing before cycling to the next one */
	private static final int cycleTime = 1000;
	private long startTime;
	private int drawDuration = 0;
	private int pausedDuration = 0;

	public CycleTimer(int offset) {
		long time = System.currentTimeMillis();
		this.startTime = time - ((long) offset * cycleTime);
	}

	public Optional<ITypedIngredient<?>> getCycledItem(List<Optional<ITypedIngredient<?>>> list) {
		if (list.isEmpty()) {
			return Optional.empty();
		}
		int index = (drawDuration / cycleTime) % list.size();
		return list.get(index);
	}

	public void onDraw() {
		if (!Screen.hasShiftDown()) {
			if (pausedDuration > 0) {
				startTime += pausedDuration;
				pausedDuration = 0;
			}
			drawDuration = Math.toIntExact(System.currentTimeMillis() - startTime);
		} else {
			long drawTime = startTime + drawDuration;
			pausedDuration = Math.toIntExact(System.currentTimeMillis() - drawTime);
		}
	}
}
