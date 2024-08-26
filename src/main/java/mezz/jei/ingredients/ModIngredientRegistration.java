package mezz.jei.ingredients;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.color.ColorGetter;
import mezz.jei.util.ErrorUtil;

public class ModIngredientRegistration implements IModIngredientRegistration, IIngredientAliasRegistration {
	private final List<RegisteredIngredient<?>> registeredIngredients = new ArrayList<>();
	private final IdentityHashMap<IIngredientType<?>, RegisteredIngredient<?>> registeredIngredientsByType = new IdentityHashMap<>();
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

		RegisteredIngredient<V> registeredIngredient = new RegisteredIngredient<>(ingredientType, allIngredients, ingredientHelper, ingredientRenderer);
		registeredIngredients.add(registeredIngredient);
		registeredIngredientsByType.put(ingredientType, registeredIngredient);
		registeredIngredientSet.add(ingredientType);
	}

	@Override
	public <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
		addAliases(type, ingredient, Collections.singleton(alias));
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases) {
		ErrorUtil.checkNotNull(type, "type");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		ErrorUtil.checkNotNull(aliases, "aliases");
		getRegisteredIngredient(type).addAliases(ingredient, aliases);
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, String alias) {
		addAliases(type, ingredients, Collections.singleton(alias));
	}

	@Override
	public <I> void addAliases(IIngredientType<I> type, Collection<I> ingredients, Collection<String> aliases) {
		ErrorUtil.checkNotNull(ingredients, "ingredients");
		for (I ingredient : ingredients) {
			addAliases(type, ingredient, aliases);
		}
	}

	@SuppressWarnings("unchecked")
	private <I> RegisteredIngredient<I> getRegisteredIngredient(IIngredientType<I> type) {
		RegisteredIngredient<I> registeredIngredient = (RegisteredIngredient<I>) registeredIngredientsByType.get(type);
		if (registeredIngredient == null) {
			throw new IllegalArgumentException("Unknown ingredient type: " + type.getIngredientClass());
		}
		return registeredIngredient;
	}

	@Override
	public ISubtypeManager getSubtypeManager() {
		return subtypeManager;
	}

	@Override
	public IColorHelper getColorHelper() {
		return ColorGetter.INSTANCE;
	}

	public List<RegisteredIngredient<?>> getRegisteredIngredients() {
		return registeredIngredients;
	}
}
