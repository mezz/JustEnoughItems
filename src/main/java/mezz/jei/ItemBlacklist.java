package mezz.jei;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
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
		Preconditions.checkNotNull(itemStack, "Null itemStack");
		Preconditions.checkNotNull(itemStack.getItem(), "Null item in itemStack");

		ingredientBlacklist.addIngredientToBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public void removeItemFromBlacklist(@Nullable ItemStack itemStack) {
		Preconditions.checkNotNull(itemStack, "Null itemStack");
		Preconditions.checkNotNull(itemStack.getItem(), "Null item in itemStack");

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
