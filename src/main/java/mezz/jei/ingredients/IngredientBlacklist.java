package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.util.ErrorUtil;

public class IngredientBlacklist implements IIngredientBlacklist {
	private final IIngredientRegistry ingredientRegistry;
	private final IngredientBlacklistInternal internal;

	public IngredientBlacklist(IIngredientRegistry ingredientRegistry, IngredientBlacklistInternal internal) {
		this.ingredientRegistry = ingredientRegistry;
		this.internal = internal;
	}

	@Override
	public <V> void addIngredientToBlacklist(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		internal.addIngredientToBlacklist(ingredient, ingredientHelper);
	}

	@Override
	public <V> void removeIngredientFromBlacklist(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		internal.removeIngredientFromBlacklist(ingredient, ingredientHelper);
	}

	@Override
	public <V> boolean isIngredientBlacklisted(V ingredient) {
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		return internal.isIngredientBlacklisted(ingredient, ingredientHelper);
	}
}
