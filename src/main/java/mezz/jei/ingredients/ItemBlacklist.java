package mezz.jei.ingredients;

import net.minecraft.item.ItemStack;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.ingredients.IIngredientBlacklist;

@Deprecated
public class ItemBlacklist implements IItemBlacklist {
	private final IIngredientBlacklist ingredientBlacklist;

	public ItemBlacklist(IIngredientBlacklist ingredientBlacklist) {
		this.ingredientBlacklist = ingredientBlacklist;
	}

	@Override
	public void addItemToBlacklist(ItemStack itemStack) {
		ingredientBlacklist.addIngredientToBlacklist(itemStack);
	}

	@Override
	public void removeItemFromBlacklist(ItemStack itemStack) {
		ingredientBlacklist.removeIngredientFromBlacklist(itemStack);
	}

	@Override
	public boolean isItemBlacklisted(ItemStack itemStack) {
		return ingredientBlacklist.isIngredientBlacklisted(itemStack);
	}
}
