package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.config.ILookupHistoryConfig;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.overlay.elements.IElement;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LookupHistory implements IIngredientGridSource {
	private final List<IBookmark> elements = new LinkedList<>();
	private final List<SourceListChangedListener> listeners = new ArrayList<>();
	private final IRecipeManager recipeManager;
	private final IIngredientManager ingredientManager;
	private final IFocusFactory focusFactory;
	private final IClientConfig clientConfig;
	private final ILookupHistoryConfig lookupHistoryConfig;

	public LookupHistory(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		IFocusFactory focusFactory,
		IClientConfig clientConfig,
		ILookupHistoryConfig lookupHistoryConfig
	) {
		this.recipeManager = recipeManager;
		this.ingredientManager = ingredientManager;
		this.focusFactory = focusFactory;
		this.clientConfig = clientConfig;
		this.lookupHistoryConfig = lookupHistoryConfig;

		List<IBookmark> loaded = lookupHistoryConfig.load(recipeManager, ingredientManager, focusFactory);
		this.elements.addAll(loaded);
		clientConfig.addMaxLookupHistoryIngredientsListener(v -> trimToMaxElements());
		trimToMaxElements();
	}

	public void add(IBookmark element) {
		elements.remove(element);
		elements.add(0, element);
		if (elements.size() > clientConfig.getMaxLookupHistoryIngredients()) {
			elements.remove(elements.size() - 1);
		}
		notifyListeners();
		save();
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

	private void trimToMaxElements() {
		boolean changed = false;
		while (elements.size() > clientConfig.getMaxLookupHistoryIngredients()) {
			elements.remove(elements.size() - 1);
			changed = true;
		}
		if (changed) {
			notifyListeners();
			save();
		}
	}

	private void save() {
		lookupHistoryConfig.save(recipeManager, ingredientManager, focusFactory, elements);
	}
}
