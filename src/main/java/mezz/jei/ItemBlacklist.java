package mezz.jei;

import javax.annotation.Nullable;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;

@Deprecated
public class ItemBlacklist implements IItemBlacklist {
	private final IngredientBlacklist ingredientBlacklist;

	public ItemBlacklist(IngredientBlacklist ingredientBlacklist) {
		this.ingredientBlacklist = ingredientBlacklist;
	}

	@Override
	@Deprecated
	public void addItemToBlacklist(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return;
		}
		if (itemStack.getItem() == null) {
			Log.error("Null item in itemStack", new NullPointerException());
			return;
		}

		ingredientBlacklist.addIngredientToBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public void removeItemFromBlacklist(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return;
		}
		if (itemStack.getItem() == null) {
			Log.error("Null item in itemStack", new NullPointerException());
			return;
		}

		ingredientBlacklist.removeIngredientFromBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public boolean isItemBlacklisted(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return false;
		}
		if (itemStack.getItem() == null) {
			Log.error("Null item in itemStack", new NullPointerException());
			return false;
		}

		return ingredientBlacklist.isIngredientBlacklisted(itemStack);
	}
	
	@Deprecated
	public boolean isItemBlacklistedByApi(ItemStack itemStack) {
		return ingredientBlacklist.isIngredientBlacklistedByApi(itemStack);
	}
}
