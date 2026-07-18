package mezz.jei.gui.overlay;

import mezz.jei.common.util.ImmutableRect2i;

import java.util.stream.Stream;

final class IngredientGridRoom {
	private IngredientGridRoom() {

	}

	static boolean hasRoom(ImmutableRect2i area, Stream<IngredientListSlot> availableSlots) {
		return !area.isEmpty() &&
			availableSlots.findAny()
				.isPresent();
	}
}
