package mezz.jei.startup;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.IngredientSet;

public class ModIngredientRegistration implements IModIngredientRegistration {
	private final Map<Class, Collection> allIngredientsMap = new IdentityHashMap<Class, Collection>();
	private final Map<Class, IIngredientHelper> ingredientHelperMap = new IdentityHashMap<Class, IIngredientHelper>();
	private final Map<Class, IIngredientRenderer> ingredientRendererMap = new IdentityHashMap<Class, IIngredientRenderer>();

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
	
	public IngredientRegistry createIngredientRegistry(IModIdHelper modIdHelper) {
		Map<Class, IngredientSet> ingredientsMap = new IdentityHashMap<Class, IngredientSet>();
		for (Class ingredientClass : allIngredientsMap.keySet()) {
			IIngredientHelper ingredientHelper = ingredientHelperMap.get(ingredientClass);
			Collection ingredients = allIngredientsMap.get(ingredientClass);
			IngredientSet ingredientSet = new IngredientSet(ingredientHelper);
			ingredientSet.addAll(ingredients);
			ingredientsMap.put(ingredientClass, ingredientSet);
		}

		return new IngredientRegistry(
				modIdHelper,
				ingredientsMap,
				ImmutableMap.copyOf(ingredientHelperMap),
				ImmutableMap.copyOf(ingredientRendererMap)
		);
	}
}
