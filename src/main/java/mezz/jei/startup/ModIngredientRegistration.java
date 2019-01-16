package mezz.jei.startup;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.IngredientSet;

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

	@Override
	@Deprecated
	public <V> void register(Class<V> ingredientClass, Collection<V> allIngredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		register(() -> ingredientClass, allIngredients, ingredientHelper, ingredientRenderer);
	}

	public IngredientRegistry createIngredientRegistry(IModIdHelper modIdHelper, IngredientBlacklistInternal blacklist) {
		Map<IIngredientType, IngredientSet> ingredientsMap = new IdentityHashMap<>();
		for (Map.Entry<IIngredientType, Collection> entry : allIngredientsMap.entrySet()) {
			IIngredientType ingredientType = entry.getKey();
			@SuppressWarnings("unchecked")
			IngredientSet ingredientSet = createIngredientSet(ingredientType, entry.getValue());
			ingredientsMap.put(ingredientType, ingredientSet);
		}

		return new IngredientRegistry(
			modIdHelper,
			blacklist,
			ingredientsMap,
			ImmutableMap.copyOf(ingredientHelperMap),
			ImmutableMap.copyOf(ingredientRendererMap)
		);
	}

	private <T> IngredientSet<T> createIngredientSet(IIngredientType<T> ingredientType, Collection<T> ingredients) {
		@SuppressWarnings("unchecked")
		IIngredientHelper<T> ingredientHelper = ingredientHelperMap.get(ingredientType);
		IngredientSet<T> ingredientSet = IngredientSet.create(ingredientType, ingredientHelper);
		ingredientSet.addAll(ingredients);
		return ingredientSet;
	}
}
