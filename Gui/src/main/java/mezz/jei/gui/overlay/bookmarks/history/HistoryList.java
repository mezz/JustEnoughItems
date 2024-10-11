package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.overlay.elements.IElement;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryList implements IIngredientGridSource {

    private final List<IElement<?>> elements = new ArrayList<>();
    private final List<SourceListChangedListener> listeners = new ArrayList<>();
    private int maxSize;

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void add(IElement<?> element) {
        if (elements.contains(element)) {
            return;
        }
        elements.addFirst(element);
        if (elements.size() > maxSize) {
            elements.removeLast();
        }
        notifyListeners();
    }

    public void remove(IElement<?> element) {
        elements.remove(element);
        notifyListeners();
    }

    @Override
    public @Unmodifiable List<IElement<?>> getElements() {
        return Collections.unmodifiableList(elements);
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
