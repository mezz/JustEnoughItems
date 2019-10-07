package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.collect.IngredientSet;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngredientManager implements IIngredientManager {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IModIdHelper modIdHelper;
	private final IngredientBlacklistInternal blacklist;
	private final List<IIngredientType> registeredIngredientTypes;
	private final Map<IIngredientType, IngredientSet> ingredientsMap;
	private final ImmutableMap<IIngredientType, IIngredientHelper> ingredientHelperMap;
	private final ImmutableMap<IIngredientType, IIngredientRenderer> ingredientRendererMap;
	private final boolean enableDebugLogs;
	private final ImmutableMap<Class, IIngredientType> ingredientTypeMap;

	public IngredientManager(
		IModIdHelper modIdHelper,
		IngredientBlacklistInternal blacklist,
		List<IIngredientType> registeredIngredientTypes,
		Map<IIngredientType, IngredientSet> ingredientsMap,
		ImmutableMap<IIngredientType, IIngredientHelper> ingredientHelperMap,
		ImmutableMap<IIngredientType, IIngredientRenderer> ingredientRendererMap,
		boolean enableDebugLogs
	) {
		this.modIdHelper = modIdHelper;
		this.blacklist = blacklist;
		this.registeredIngredientTypes = Collections.unmodifiableList(registeredIngredientTypes);
		this.ingredientsMap = ingredientsMap;
		this.ingredientHelperMap = ingredientHelperMap;
		this.ingredientRendererMap = ingredientRendererMap;
		this.enableDebugLogs = enableDebugLogs;
		ImmutableMap.Builder<Class, IIngredientType> ingredientTypeBuilder = ImmutableMap.builder();
		for (IIngredientType ingredientType : ingredientsMap.keySet()) {
			ingredientTypeBuilder.put(ingredientType.getIngredientClass(), ingredientType);
		}
		this.ingredientTypeMap = ingredientTypeBuilder.build();
	}

	@Override
	public <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		@SuppressWarnings("unchecked")
		IngredientSet<V> ingredients = ingredientsMap.get(ingredientType);
		if (ingredients == null) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableCollection(ingredients);
		}
	}

	@Nullable
	public <V> V getIngredientByUid(IIngredientType<V> ingredientType, String uid) {
		@SuppressWarnings("unchecked")
		IngredientSet<V> ingredients = ingredientsMap.get(ingredientType);
		if (ingredients == null) {
			return null;
		} else {
			return ingredients.getByUid(uid);
		}
	}

	public <V> boolean isValidIngredient(V ingredient) {
		try {
			IIngredientHelper<V> ingredientHelper = getIngredientHelper(ingredient);
			return ingredientHelper.isValidIngredient(ingredient);
		} catch (RuntimeException ignored) {
			return false;
		}
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IIngredientType<V> ingredientType = getIngredientType(ingredient);
		return getIngredientHelper(ingredientType);
	}

	@Override
	public <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		@SuppressWarnings("unchecked")
		IIngredientHelper<V> ingredientHelper = ingredientHelperMap.get(ingredientType);
		if (ingredientHelper != null) {
			return ingredientHelper;
		}
		throw new IllegalArgumentException("Unknown ingredient type: " + ingredientType.getIngredientClass());
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		IIngredientType<V> ingredientType = getIngredientType(ingredient);
		return getIngredientRenderer(ingredientType);
	}

	@Override
	public <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		@SuppressWarnings("unchecked")
		IIngredientRenderer<V> ingredientRenderer = ingredientRendererMap.get(ingredientType);
		if (ingredientRenderer == null) {
			throw new IllegalArgumentException("Could not find ingredient renderer for " + ingredientType);
		}
		return ingredientRenderer;
	}

	@Override
	public Collection<IIngredientType> getRegisteredIngredientTypes() {
		return this.registeredIngredientTypes;
	}

	@Override
	public <V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		addIngredientsAtRuntime(ingredientType, ingredients, Internal.getIngredientFilter());
	}

	public <V> void addIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients, IngredientFilter ingredientFilter) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		LOGGER.info("Ingredients are being added at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		IIngredientHelper<V> ingredientHelper = getIngredientHelper(ingredientType);
		//noinspection unchecked
		Set<V> set = ingredientsMap.computeIfAbsent(ingredientType, k -> IngredientSet.create(ingredientHelper));
		set.addAll(ingredients);

		for (V ingredient : ingredients) {
			List<IIngredientListElement<V>> matchingElements = ingredientFilter.findMatchingElements(ingredientHelper, ingredient);
			if (!matchingElements.isEmpty()) {
				for (IIngredientListElement<V> matchingElement : matchingElements) {
					blacklist.removeIngredientFromBlacklist(matchingElement.getIngredient(), ingredientHelper);
					ingredientFilter.updateHiddenState(matchingElement);
				}
				if (enableDebugLogs) {
					LOGGER.debug("Updated ingredient: {}", ingredientHelper.getErrorInfo(ingredient));
				}
			} else {
				IIngredientListElement<V> element = IngredientListElementFactory.createOrderedElement(this, ingredientType, ingredient);
				IngredientListElementInfo<V> info = IngredientListElementInfo.create(element, this, modIdHelper);
				if (info != null) {
					blacklist.removeIngredientFromBlacklist(ingredient, ingredientHelper);
					ingredientFilter.addIngredient(info);
					if (enableDebugLogs) {
						LOGGER.debug("Added ingredient: {}", ingredientHelper.getErrorInfo(ingredient));
					}
				}
			}
		}
		ingredientFilter.invalidateCache();
	}

	@Override
	public <V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients) {
		removeIngredientsAtRuntime(ingredientType, ingredients, Internal.getIngredientFilter());
	}

	@Override
	public <V> IIngredientType<V> getIngredientType(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		@SuppressWarnings("unchecked")
		Class<? extends V> ingredientClass = (Class<? extends V>) ingredient.getClass();
		return getIngredientType(ingredientClass);
	}

	@Override
	public <V> IIngredientType<V> getIngredientType(Class<? extends V> ingredientClass) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		@SuppressWarnings("unchecked")
		IIngredientType<V> ingredientType = this.ingredientTypeMap.get(ingredientClass);
		if (ingredientType != null) {
			return ingredientType;
		}
		for (IIngredientType<?> type : this.registeredIngredientTypes) {
			if (type.getIngredientClass().isAssignableFrom(ingredientClass)) {
				@SuppressWarnings("unchecked")
				IIngredientType<V> castType = (IIngredientType<V>) type;
				return castType;
			}
		}
		throw new IllegalArgumentException("Unknown ingredient class: " + ingredientClass);
	}

	public <V> void removeIngredientsAtRuntime(IIngredientType<V> ingredientType, Collection<V> ingredients, IngredientFilter ingredientFilter) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");

		LOGGER.info("Ingredients are being removed at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		@SuppressWarnings("unchecked")
		IngredientSet<V> set = ingredientsMap.get(ingredientType);
		if (set != null) {
			set.removeAll(ingredients);
		}

		IIngredientHelper<V> ingredientHelper = getIngredientHelper(ingredientType);

		for (V ingredient : ingredients) {
			List<IIngredientListElement<V>> matchingElements = ingredientFilter.findMatchingElements(ingredientHelper, ingredient);
			if (matchingElements.isEmpty()) {

				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.error("Could not find any matching ingredients to remove: {}", errorInfo);
			} else if (enableDebugLogs) {
				LOGGER.debug("Removed ingredient: {}", ingredientHelper.getErrorInfo(ingredient));
			}
			for (IIngredientListElement<V> matchingElement : matchingElements) {
				blacklist.addIngredientToBlacklist(matchingElement.getIngredient(), ingredientHelper);
				matchingElement.setVisible(false);
			}
		}
		ingredientFilter.invalidateCache();
	}

	public <V> boolean isIngredientVisible(V ingredient, IngredientFilter ingredientFilter) {
		IIngredientType<V> ingredientType = getIngredientType(ingredient);
		IIngredientHelper<V> ingredientHelper = getIngredientHelper(ingredientType);
		List<IIngredientListElement<V>> matchingElements = ingredientFilter.findMatchingElements(ingredientHelper, ingredient);
		if (matchingElements.isEmpty()) {
			return true;
		}
		for (IIngredientListElement matchingElement : matchingElements) {
			if (matchingElement.isVisible()) {
				return true;
			}
		}
		return false;
	}
}
