package mezz.jei.startup;

import com.google.common.collect.ImmutableMap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.IngredientSet;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public class ModIngredientRegistration implements IModIngredientRegistration {
	private final Map<Class, Collection> allIngredientsMap = new IdentityHashMap<>();
	private final Map<Class, IIngredientHelper> ingredientHelperMap = new IdentityHashMap<>();
	private final Map<Class, IIngredientRenderer> ingredientRendererMap = new IdentityHashMap<>();

	@Override
	public <V> void register(Class<V> ingredientClass, Collection<V> allIngredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		ErrorUtil.checkNotNull(ingredientClass, "ingredientClass");
		ErrorUtil.checkNotNull(allIngredients, "allIngredients");
		ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
		ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");

		allIngredientsMap.put(ingredientClass, allIngredients);
		ingredientHelperMap.put(ingredientClass, ingredientHelper);
		ingredientRendererMap.put(ingredientClass, ingredientRenderer);
	}

	public IngredientRegistry createIngredientRegistry(IModIdHelper modIdHelper, IngredientBlacklistInternal blacklist) {
		Map<Class, IngredientSet> ingredientsMap = new IdentityHashMap<>();
		for (Map.Entry<Class, Collection> entry : allIngredientsMap.entrySet()) {
			Class ingredientClass = entry.getKey();
			Collection ingredients = entry.getValue();
			IIngredientHelper ingredientHelper = ingredientHelperMap.get(ingredientClass);
			IngredientSet ingredientSet = IngredientSet.create(ingredientClass, ingredientHelper);
			ingredientSet.addAll(ingredients);
			ingredientsMap.put(ingredientClass, ingredientSet);
		}

		return new IngredientRegistry(
				modIdHelper,
				blacklist,
				ingredientsMap,
				ImmutableMap.copyOf(ingredientHelperMap),
				ImmutableMap.copyOf(ingredientRendererMap)
		);
	}
}
