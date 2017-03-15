package mezz.jei.startup;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.ingredients.IngredientRegistry;

public class ModIngredientRegistration implements IModIngredientRegistration {
	private final Map<Class, Collection> allIngredientsMap = new IdentityHashMap<Class, Collection>();
	private final Map<Class, IIngredientHelper> ingredientHelperMap = new IdentityHashMap<Class, IIngredientHelper>();
	private final Map<Class, IIngredientRenderer> ingredientRendererMap = new IdentityHashMap<Class, IIngredientRenderer>();

	@Override
	public <V> void register(Class<V> ingredientClass, Collection<V> allIngredients, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer) {
		Preconditions.checkNotNull(ingredientClass, "ingredientClass cannot be null");
		Preconditions.checkNotNull(allIngredients, "allIngredients cannot be null");
		Preconditions.checkNotNull(ingredientHelper, "ingredientHelper cannot be null");
		Preconditions.checkNotNull(ingredientRenderer, "ingredientRenderer cannot be null");

		allIngredientsMap.put(ingredientClass, allIngredients);
		ingredientHelperMap.put(ingredientClass, ingredientHelper);
		ingredientRendererMap.put(ingredientClass, ingredientRenderer);
	}

	public IngredientRegistry createIngredientRegistry() {
		Map<Class, List> ingredientsMap = new IdentityHashMap<Class, List>();
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
