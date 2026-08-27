package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.common.ingredients.TypedIngredientUtil;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.config.EditModeConfig;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IngredientVisibility implements IIngredientVisibility {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IngredientBlacklistInternal blacklist;
	private final IWorldConfig worldConfig;
	private final EditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	private final List<IListener> listeners = new ArrayList<>();

	public IngredientVisibility(
		IngredientBlacklistInternal blacklist,
		IWorldConfig worldConfig,
		EditModeConfig editModeConfig,
		IIngredientManager ingredientManager
	) {
		this.blacklist = blacklist;
		this.worldConfig = worldConfig;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;

		editModeConfig.registerListener(this);
		blacklist.registerListener(this);
	}

	@Override
	public <V> void hideIngredients(
		IIngredientType<V> ingredientType,
		Collection<V> ingredients,
		Collection<UidContext> contexts
	) {
		setIngredientsVisible(ingredientType, ingredients, contexts, false);
	}

	@Override
	public <V> void unhideIngredients(
		IIngredientType<V> ingredientType,
		Collection<V> ingredients,
		Collection<UidContext> contexts
	) {
		setIngredientsVisible(ingredientType, ingredients, contexts, true);
	}

	private <V> void setIngredientsVisible(
		IIngredientType<V> ingredientType,
		Collection<V> ingredients,
		Collection<UidContext> contexts,
		boolean visible
	) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(contexts, "contexts");

		Set<UidContext> checkedContexts = Collections.unmodifiableSet(EnumSet.copyOf(contexts));
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		String operation = getVisibilityOperation(visible);
		LOGGER.info("Ingredients are being {} at runtime in {}: {} {}", operation, checkedContexts, ingredients.size(), ingredientType.getIngredientClass().getName());
		if (LOGGER.isDebugEnabled()) {
			String ingredientStrings = ingredients.stream()
				.map(ingredientHelper::getResourceLocation)
				.map(ResourceLocation::toString)
				.collect(Collectors.joining(", ", "[", "]"));
			LOGGER.debug("Ingredients {} at runtime in {}: {}", operation, checkedContexts, ingredientStrings);
		}

		List<ITypedIngredient<V>> typedIngredients = TypedIngredient.createAndFilterInvalidNonnullList(
			ingredientManager,
			ingredientType,
			ingredients,
			false
		);
		blacklist.onIngredientsVisibilityChanged(typedIngredients, checkedContexts, visible);
	}

	private static String getVisibilityOperation(boolean visible) {
		if (visible) {
			return "unhidden";
		}
		return "hidden";
	}

	@Override
	public <V> boolean isIngredientVisible(ITypedIngredient<V> typedIngredient) {
		return isIngredientVisible(typedIngredient, UidContext.Ingredient);
	}

	@Override
	public <V> boolean isIngredientVisible(
		ITypedIngredient<V> typedIngredient,
		UidContext context
	) {
		ITypedIngredient<V> checkedIngredient = TypedIngredientUtil.checkTypedIngredientFromApi(ingredientManager, typedIngredient);
		IIngredientType<V> ingredientType = checkedIngredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		return isIngredientVisible(checkedIngredient, ingredientHelper, context);
	}

	@Override
	public <V> boolean isIngredientVisible(IIngredientType<V> ingredientType, V ingredient) {
		return isIngredientVisible(ingredientType, ingredient, UidContext.Ingredient);
	}

	@Override
	public <V> boolean isIngredientVisible(
		IIngredientType<V> ingredientType,
		V ingredient,
		UidContext context
	) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		@Nullable ITypedIngredient<V> typedIngredient = TypedIngredient.createAndFilterInvalid(ingredientHelper, ingredientType, ingredient, false);
		if (typedIngredient == null) {
			return false;
		}
		return isIngredientVisible(typedIngredient, ingredientHelper, context);
	}

	public <V> boolean isIngredientVisible(
		ITypedIngredient<V> typedIngredient,
		IIngredientHelper<V> ingredientHelper,
		UidContext context
	) {
		if (blacklist.isIngredientBlacklistedByApi(typedIngredient, ingredientHelper, context)) {
			return false;
		}
		if (ingredientHelper.isHiddenFromRecipeViewersByTags(typedIngredient)) {
			return false;
		}
		return worldConfig.isEditModeEnabled() || !editModeConfig.isIngredientHiddenUsingConfigFile(typedIngredient);
	}

	@Override
	public void registerListener(IListener listener) {
		this.listeners.add(listener);
	}

	public <V> void notifyListeners(Collection<ITypedIngredient<V>> ingredients, boolean visible) {
		notifyListeners(ingredients, Set.of(UidContext.values()), visible);
	}

	public <V> void notifyListeners(
		Collection<ITypedIngredient<V>> ingredients,
		Collection<UidContext> contexts,
		boolean visible
	) {
		listeners.forEach(listener -> listener.onIngredientsVisibilityChanged(ingredients, contexts, visible));
	}

	public void onRuntimeStopped() {
		listeners.clear();
	}
}
