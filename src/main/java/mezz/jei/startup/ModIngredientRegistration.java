package mezz.jei.startup;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.util.ErrorUtil;

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

	public IngredientRegistry createIngredientRegistry() {
		Map<Class, List> ingredientsMap = new IdentityHashMap<>();
		for (Class ingredientClass : allIngredientsMap.keySet()) {
			Collection ingredients = allIngredientsMap.get(ingredientClass);
			ingredientsMap.put(ingredientClass, Lists.newArrayList(ingredients));
		}

		return new IngredientRegistry(
				ingredientsMap,
				ImmutableMap.copyOf(ingredientHelperMap),
				ImmutableMap.copyOf(ingredientRendererMap)
		);
	}
}
