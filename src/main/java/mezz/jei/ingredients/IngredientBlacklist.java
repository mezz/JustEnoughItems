package mezz.jei.ingredients;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;

public class IngredientBlacklist implements IIngredientBlacklist, IItemBlacklist {
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

	@Override
	@Deprecated
	public void addItemToBlacklist(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);
		addIngredientToBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public void removeItemFromBlacklist(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);
		removeIngredientFromBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public boolean isItemBlacklisted(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);
		return isIngredientBlacklisted(itemStack);
	}
}
