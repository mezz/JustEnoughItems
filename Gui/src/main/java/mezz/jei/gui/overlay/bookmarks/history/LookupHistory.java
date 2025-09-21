package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.config.ILookupHistoryConfig;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class LookupHistory implements IIngredientGridSource {
	private final List<IBookmark> elements = new LinkedList<>();
	private final List<SourceListChangedListener> listeners = new ArrayList<>();
	private final IRecipeManager recipeManager;
	private final IIngredientManager ingredientManager;
	private final RegistryAccess registryAccess;
	private final ICodecHelper codecHelper;
	private final Supplier<Integer> maxElements;
	private final ILookupHistoryConfig lookupHistoryConfig;

	public LookupHistory(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess,
		ICodecHelper codecHelper,
		Supplier<Integer> maxElements,
		ILookupHistoryConfig lookupHistoryConfig
	) {
		this.recipeManager = recipeManager;
		this.ingredientManager = ingredientManager;
		this.registryAccess = registryAccess;
		this.codecHelper = codecHelper;
		this.maxElements = maxElements;
		this.lookupHistoryConfig = lookupHistoryConfig;

		List<IBookmark> loaded = lookupHistoryConfig.load(recipeManager, ingredientManager, registryAccess, codecHelper);
		this.elements.addAll(loaded);
	}

	public void add(IBookmark element) {
		elements.remove(element);
		elements.addFirst(element);
		if (elements.size() > maxElements.get()) {
			elements.removeLast();
		}
		notifyListeners();
		lookupHistoryConfig.save(recipeManager, ingredientManager, registryAccess, codecHelper, elements);
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
