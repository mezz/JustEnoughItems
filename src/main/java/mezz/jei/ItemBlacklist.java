package mezz.jei;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;

@Deprecated
public class ItemBlacklist implements IItemBlacklist {
	private final IngredientBlacklist ingredientBlacklist;

	public ItemBlacklist(IngredientBlacklist ingredientBlacklist) {
		this.ingredientBlacklist = ingredientBlacklist;
	}

	@Override
	@Deprecated
	public void addItemToBlacklist(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);
		ingredientBlacklist.addIngredientToBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public void removeItemFromBlacklist(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);
		ingredientBlacklist.removeIngredientFromBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public boolean isItemBlacklisted(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);
		return ingredientBlacklist.isIngredientBlacklisted(itemStack);
	}

	@Deprecated
	public boolean isItemBlacklistedByApi(ItemStack itemStack) {
		return ingredientBlacklist.isIngredientBlacklistedByApi(itemStack);
	}
}
