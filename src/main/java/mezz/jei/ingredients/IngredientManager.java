package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
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

	private final ImmutableMap<IIngredientType<?>, RegisteredIngredient<?>> ingredientsMap;
	private final ImmutableList<IIngredientType<?>> registeredIngredientTypes;

	private final boolean enableDebugLogs;
	private final ImmutableMap<Class<?>, IIngredientType<?>> ingredientTypeMap;

	public IngredientManager(
		IModIdHelper modIdHelper,
		IngredientBlacklistInternal blacklist,
		List<RegisteredIngredient<?>> registeredIngredients,
		boolean enableDebugLogs
	) {
		this.modIdHelper = modIdHelper;
		this.blacklist = blacklist;

		ImmutableMap.Builder<IIngredientType<?>, RegisteredIngredient<?>> ingredientsMapBuilder = ImmutableMap.builder();
		for (RegisteredIngredient<?> registeredIngredient : registeredIngredients) {
			ingredientsMapBuilder.put(registeredIngredient.getIngredientType(), registeredIngredient);
		}
		this.ingredientsMap = ingredientsMapBuilder.build();

		//noinspection UnstableApiUsage
		this.registeredIngredientTypes = registeredIngredients.stream()
			.map(RegisteredIngredient::getIngredientType)
			.collect(ImmutableList.toImmutableList());

		this.enableDebugLogs = enableDebugLogs;
		ImmutableMap.Builder<Class<?>, IIngredientType<?>> ingredientTypeBuilder = ImmutableMap.builder();
		for (IIngredientType<?> ingredientType : ingredientsMap.keySet()) {
			ingredientTypeBuilder.put(ingredientType.getIngredientClass(), ingredientType);
		}
		this.ingredientTypeMap = ingredientTypeBuilder.build();
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <V> RegisteredIngredient<V> getRegisteredIngredient(IIngredientType<V> ingredientType) {
		return (RegisteredIngredient<V>) ingredientsMap.get(ingredientType);
	}

	@Override
	public <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		RegisteredIngredient<V> registeredIngredient = getRegisteredIngredient(ingredientType);
		if (registeredIngredient == null) {
			return Collections.emptySet();
		} else {
			IngredientSet<V> ingredients = registeredIngredient.getIngredientSet();
			return Collections.unmodifiableCollection(ingredients);
		}
	}

	@Nullable
	public <V> V getIngredientByUid(IIngredientType<V> ingredientType, String uid) {
		RegisteredIngredient<V> registeredIngredient = getRegisteredIngredient(ingredientType);
		if (registeredIngredient == null) {
			return null;
		} else {
			IngredientSet<V> ingredients = registeredIngredient.getIngredientSet();
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
		RegisteredIngredient<V> registeredIngredient = getRegisteredIngredient(ingredientType);
		if (registeredIngredient != null) {
			return registeredIngredient.getIngredientHelper();
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
		RegisteredIngredient<V> registeredIngredient = getRegisteredIngredient(ingredientType);
		if (registeredIngredient == null) {
			throw new IllegalArgumentException("Could not find ingredient for " + ingredientType);
		}
		return registeredIngredient.getIngredientRenderer();
	}

	@Override
	public Collection<IIngredientType<?>> getRegisteredIngredientTypes() {
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

		RegisteredIngredient<V> registeredIngredient = getRegisteredIngredient(ingredientType);
		if (registeredIngredient == null) {
			throw new IllegalArgumentException("Unknown ingredient type: " + ingredientType.getIngredientClass());
		}

		LOGGER.info("Ingredients are being added at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		Set<V> set = registeredIngredient.getIngredientSet();
		set.addAll(ingredients);

		IIngredientHelper<V> ingredientHelper = registeredIngredient.getIngredientHelper();

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
		IIngredientType<V> ingredientType = (IIngredientType<V>) this.ingredientTypeMap.get(ingredientClass);
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

		RegisteredIngredient<V> registeredIngredient = getRegisteredIngredient(ingredientType);
		if (registeredIngredient == null) {
			throw new IllegalArgumentException("Unknown ingredient type: " + ingredientType.getIngredientClass());
		}

		LOGGER.info("Ingredients are being removed at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		IngredientSet<V> set = registeredIngredient.getIngredientSet();
		set.removeAll(ingredients);

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
		for (IIngredientListElement<?> matchingElement : matchingElements) {
			if (matchingElement.isVisible()) {
				return true;
			}
		}
		return false;
	}
}
