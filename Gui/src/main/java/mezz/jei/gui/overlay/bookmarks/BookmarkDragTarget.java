package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.ingredients.IngredientListSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A drop area and the bookmark's destination index after it is removed from its old position. */
public record BookmarkDragTarget(ImmutableRect2i area, int index) {
	static List<BookmarkDragTarget> createSlotTargets(List<IngredientListSlot> slots, List<IElement<?>> elements, IBookmark draggedBookmark) {
		List<BookmarkDragTarget> targets = new ArrayList<>();
		int draggedIndex = elements.indexOf(draggedBookmark.getElement());
		int nextIndex = -1;
		// Walk backwards so gaps can use the next occupied slot as their insertion point.
		for (int slotIndex = slots.size() - 1; slotIndex >= 0; slotIndex--) {
			IngredientListSlot slot = slots.get(slotIndex);
			IElement<?> element = slot.getOptionalElement().orElse(null);
			if (element != null) {
				int index = elements.indexOf(element);
				targets.add(new BookmarkDragTarget(slot.getArea(), index));
				nextIndex = index;
				if (draggedIndex < index) {
					nextIndex--;
				}
			} else if (nextIndex >= 0) {
				targets.add(new BookmarkDragTarget(slot.getArea(), nextIndex));
			}
		}
		Collections.reverse(targets);
		return targets;
	}
}
