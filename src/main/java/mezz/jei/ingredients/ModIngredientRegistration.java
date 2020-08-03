package mezz.jei.ingredients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.util.ErrorUtil;

public class ModIngredientRegistration implements IModIngredientRegistration {
	private final List<RegisteredIngredient<?>> registeredIngredients = new ArrayList<>();
	private final Set<IIngredientType<?>> registeredIngredientSet = Collections.newSetFromMap(new IdentityHashMap<>());
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
		if (registeredIngredientSet.contains(ingredientType)) {
			throw new IllegalArgumentException("Ingredient type has already been registered: " + ingredientType.getIngredientClass());
		}

		registeredIngredients.add(new RegisteredIngredient<>(ingredientType, allIngredients, ingredientHelper, ingredientRenderer));
		registeredIngredientSet.add(ingredientType);
	}

	@Override
	public ISubtypeManager getSubtypeManager() {
		return subtypeManager;
	}

	public List<RegisteredIngredient<?>> getRegisteredIngredients() {
		return registeredIngredients;
	}
}
