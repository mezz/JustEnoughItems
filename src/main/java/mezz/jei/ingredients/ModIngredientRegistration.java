package mezz.jei.ingredients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.collect.IngredientSet;
import mezz.jei.util.ErrorUtil;

public class ModIngredientRegistration implements IModIngredientRegistration {
	private final Map<IIngredientType, Collection> allIngredientsMap = new IdentityHashMap<>();
	private final Map<IIngredientType, IIngredientHelper> ingredientHelperMap = new IdentityHashMap<>();
	private final Map<IIngredientType, IIngredientRenderer> ingredientRendererMap = new IdentityHashMap<>();
	/** to preserve the order that types were registered in */
	private final List<IIngredientType> registeredIngredientTypes = new ArrayList<>();
	private final ISubtypeManager subtypeManager;

	public ModIngredientRegistration(ISubtypeManager subtypeManager) {
		this.subtypeManager = subtypeManager;
	}

	@Override
	public <V> void register(IIngredientType<V> ingredientType, Collection<V> allIngredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(allIngredients, "allIngredients");
		ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");
		if (allIngredientsMap.containsKey(ingredientType)) {
			throw new IllegalArgumentException("Ingredient type has already been registered: " + ingredientType.getIngredientClass());
		}

		allIngredientsMap.put(ingredientType, allIngredients);
		ingredientHelperMap.put(ingredientType, ingredientHelper);
		ingredientRendererMap.put(ingredientType, ingredientRenderer);
		registeredIngredientTypes.add(ingredientType);
	}

	@Override
	public ISubtypeManager getSubtypeManager() {
		return subtypeManager;
	}

	public IngredientManager createIngredientManager(IModIdHelper modIdHelper, IngredientBlacklistInternal blacklist, boolean enableDebugLogs) {
		Map<IIngredientType, IngredientSet> ingredientsMap = new IdentityHashMap<>();
		for (Map.Entry<IIngredientType, Collection> entry : allIngredientsMap.entrySet()) {
			IIngredientType ingredientType = entry.getKey();
			@SuppressWarnings("unchecked")
			IngredientSet ingredientSet = createIngredientSet(ingredientType, entry.getValue());
			ingredientsMap.put(ingredientType, ingredientSet);
		}

		return new IngredientManager(
			modIdHelper,
			blacklist,
			registeredIngredientTypes,
			ingredientsMap,
			ImmutableMap.copyOf(ingredientHelperMap),
			ImmutableMap.copyOf(ingredientRendererMap),
			enableDebugLogs
		);
	}

	private <T> IngredientSet<T> createIngredientSet(IIngredientType<T> ingredientType, Collection<T> ingredients) {
		@SuppressWarnings("unchecked")
		IIngredientHelper<T> ingredientHelper = ingredientHelperMap.get(ingredientType);
		IngredientSet<T> ingredientSet = IngredientSet.create(ingredientHelper);
		ingredientSet.addAll(ingredients);
		return ingredientSet;
	}
}
