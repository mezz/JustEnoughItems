package mezz.jei.ingredients;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mezz.jei.Internal;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;

public class IngredientManager implements IIngredientManager {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IModIdHelper modIdHelper;
	private final IngredientBlacklistInternal blacklist;

	private final ImmutableMap<IIngredientType<?>, IngredientInfo<?>> ingredientsMap;
	private final ImmutableList<IIngredientType<?>> registeredIngredientTypes;

	private final boolean enableDebugLogs;
	private final ImmutableMap<Class<?>, IIngredientType<?>> ingredientTypeMap;

	public IngredientManager(
		IModIdHelper modIdHelper,
		IngredientBlacklistInternal blacklist,
		List<IngredientInfo<?>> ingredientInfos,
		boolean enableDebugLogs
	) {
		this.modIdHelper = modIdHelper;
		this.blacklist = blacklist;

		ImmutableMap.Builder<IIngredientType<?>, IngredientInfo<?>> ingredientsMapBuilder = ImmutableMap.builder();
		for (IngredientInfo<?> ingredientInfo : ingredientInfos) {
			IIngredientType<?> ingredientType = ingredientInfo.getIngredientType();
			ingredientsMapBuilder.put(ingredientType, ingredientInfo);
		}
		this.ingredientsMap = ingredientsMapBuilder.build();

		//noinspection UnstableApiUsage
		this.registeredIngredientTypes = ingredientInfos.stream()
			.map(IngredientInfo::getIngredientType)
			.collect(ImmutableList.toImmutableList());

		this.enableDebugLogs = enableDebugLogs;
		ImmutableMap.Builder<Class<?>, IIngredientType<?>> ingredientTypeBuilder = ImmutableMap.builder();
		for (IIngredientType<?> ingredientType : ingredientsMap.keySet()) {
			ingredientTypeBuilder.put(ingredientType.getIngredientClass(), ingredientType);
		}
		this.ingredientTypeMap = ingredientTypeBuilder.build();
	}

	public <V> IngredientInfo<V> getIngredientInfo(IIngredientType<V> ingredientType) {
		@SuppressWarnings("unchecked")
		IngredientInfo<V> ingredientInfo = (IngredientInfo<V>) ingredientsMap.get(ingredientType);
		if (ingredientInfo == null) {
			throw new IllegalArgumentException("Unknown ingredient type: " + ingredientType.getIngredientClass());
		}
		return ingredientInfo;
	}

	@Override
	public <V> Collection<V> getAllIngredients(IIngredientType<V> ingredientType) {
		IngredientInfo<V> ingredientInfo = getIngredientInfo(ingredientType);
		return ingredientInfo.getAllIngredients();
	}

	@Nullable
	public <V> V getIngredientByUid(IIngredientType<V> ingredientType, String uid) {
		IngredientInfo<V> ingredientInfo = getIngredientInfo(ingredientType);
		return ingredientInfo.getIngredientByUid(uid);
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
		IngredientInfo<V> ingredientInfo = getIngredientInfo(ingredientType);
		return ingredientInfo.getIngredientHelper();
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
		IngredientInfo<V> ingredientInfo = getIngredientInfo(ingredientType);
		return ingredientInfo.getIngredientRenderer();
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

		IngredientInfo<V> ingredientInfo = getIngredientInfo(ingredientType);

		LOGGER.info("Ingredients are being added at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		ingredientInfo.addIngredients(ingredients);

		IIngredientHelper<V> ingredientHelper = ingredientInfo.getIngredientHelper();

		for (V ingredient : ingredients) {
			List<IIngredientListElementInfo<V>> matchingElementInfos = ingredientFilter.findMatchingElements(ingredientHelper, ingredient);
			if (!matchingElementInfos.isEmpty()) {
				for (IIngredientListElementInfo<V> matchingElementInfo : matchingElementInfos) {
					IIngredientListElement<V> matchingElement = matchingElementInfo.getElement();
					blacklist.removeIngredientFromBlacklist(matchingElement.getIngredient(), ingredientHelper);
					ingredientFilter.updateHiddenState(matchingElement);
				}
				if (enableDebugLogs) {
					LOGGER.debug("Updated ingredient: {}", ingredientHelper.getErrorInfo(ingredient));
				}
			} else {
				IIngredientListElement<V> element = IngredientListElementFactory.createOrderedElement(this, ingredientType, ingredient);
				IIngredientListElementInfo<V> info = IngredientListElementInfo.create(element, this, modIdHelper);
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

		IngredientInfo<V> ingredientInfo = getIngredientInfo(ingredientType);

		LOGGER.info("Ingredients are being removed at runtime: {} {}", ingredients.size(), ingredientType.getIngredientClass().getName());

		ingredientInfo.removeIngredients(ingredients);

		IIngredientHelper<V> ingredientHelper = getIngredientHelper(ingredientType);

		for (V ingredient : ingredients) {
			List<IIngredientListElementInfo<V>> matchingElementInfos = ingredientFilter.findMatchingElements(ingredientHelper, ingredient);
			if (matchingElementInfos.isEmpty()) {

				String errorInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.error("Could not find any matching ingredients to remove: {}", errorInfo);
			} else if (enableDebugLogs) {
				LOGGER.debug("Removed ingredient: {}", ingredientHelper.getErrorInfo(ingredient));
			}
			for (IIngredientListElementInfo<V> matchingElementInfo : matchingElementInfos) {
				IIngredientListElement<V> matchingElement = matchingElementInfo.getElement();
				blacklist.addIngredientToBlacklist(matchingElement.getIngredient(), ingredientHelper);
				matchingElement.setVisible(false);
			}
		}
		ingredientFilter.invalidateCache();
	}

}
