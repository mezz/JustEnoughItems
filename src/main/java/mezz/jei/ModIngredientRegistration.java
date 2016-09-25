package mezz.jei;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.util.Log;

public class ModIngredientRegistration implements IModIngredientRegistration {
	private final Map<Class, Collection> allIngredientsMap = new HashMap<Class, Collection>();
	private final Map<Class, IIngredientHelper> ingredientHelperMap = new HashMap<Class, IIngredientHelper>();
	private final Map<Class, IIngredientRenderer> ingredientRendererMap = new HashMap<Class, IIngredientRenderer>();

	@Override
	public <V> void register(
			@Nullable Class<V> ingredientClass,
			@Nullable Collection<V> allIngredients,
			@Nullable IIngredientHelper<V> ingredientHelper,
			@Nullable IIngredientRenderer<V> ingredientRenderer
	) {
		if (ingredientClass == null) {
			NullPointerException e = new NullPointerException();
			Log.error("Null ingredientClass", e);
			return;
		}

		if (allIngredients == null) {
			NullPointerException e = new NullPointerException();
			Log.error("Null allIngredients", e);
			return;
		}

		if (ingredientHelper == null) {
			NullPointerException e = new NullPointerException();
			Log.error("Null ingredientHelper", e);
			return;
		}

		if (ingredientRenderer == null) {
			NullPointerException e = new NullPointerException();
			Log.error("Null ingredientRendererFactory", e);
			return;
		}

		allIngredientsMap.put(ingredientClass, allIngredients);
		ingredientHelperMap.put(ingredientClass, ingredientHelper);
		ingredientRendererMap.put(ingredientClass, ingredientRenderer);
	}

	public IngredientRegistry createIngredientRegistry() {
		ImmutableMap.Builder<Class, ImmutableList> ingredientsMapBuilder = ImmutableMap.builder();
		for (Class ingredientClass : allIngredientsMap.keySet()) {
			Collection ingredients = allIngredientsMap.get(ingredientClass);
			ImmutableList immutableIngredients = ImmutableList.copyOf(ingredients);
			ingredientsMapBuilder.put(ingredientClass, immutableIngredients);
		}

		return new IngredientRegistry(
				ingredientsMapBuilder.build(),
				ImmutableMap.copyOf(ingredientHelperMap),
				ImmutableMap.copyOf(ingredientRendererMap)
		);
	}
}
