package mezz.jei.ingredients;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.collect.IngredientSet;
import mezz.jei.util.ErrorUtil;

public class ModIngredientRegistration implements IModIngredientRegistration {
	private final Map<IIngredientType, Collection> allIngredientsMap = new IdentityHashMap<>();
	private final Map<IIngredientType, IIngredientHelper> ingredientHelperMap = new IdentityHashMap<>();
	private final Map<IIngredientType, IIngredientRenderer> ingredientRendererMap = new IdentityHashMap<>();

	@Override
	public <V> void register(IIngredientType<V> ingredientType, Collection<V> allIngredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(allIngredients, "allIngredients");
		ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		allIngredientsMap.put(ingredientType, allIngredients);
		ingredientHelperMap.put(ingredientType, ingredientHelper);
		ingredientRendererMap.put(ingredientType, ingredientRenderer);
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
