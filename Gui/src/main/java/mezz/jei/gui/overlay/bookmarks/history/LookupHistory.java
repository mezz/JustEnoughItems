package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.overlay.elements.IElement;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LookupHistory implements IIngredientGridSource {
	private static final int MAX_ELEMENTS = 100;

	private final List<IBookmark> elements = new LinkedList<>();
	private final List<SourceListChangedListener> listeners = new ArrayList<>();

	public void add(IBookmark element) {
		if (elements.isEmpty() || !elements.getFirst().equals(element)) {
			elements.addFirst(element);
		}
		if (elements.size() > MAX_ELEMENTS) {
			elements.removeLast();
		}
		notifyListeners();
	}

	@Override
	public @Unmodifiable List<IElement<?>> getElements() {
		return elements.stream()
			.<IElement<?>>map(IBookmark::getElement)
			.toList();
	}

	@Override
	public void addSourceListChangedListener(SourceListChangedListener listener) {
		listeners.add(listener);
	}

	private void notifyListeners() {
		for (SourceListChangedListener listener : listeners) {
			listener.onSourceListChanged();
		}
	}
}
