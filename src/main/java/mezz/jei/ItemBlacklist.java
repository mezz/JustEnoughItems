package mezz.jei;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import mezz.jei.api.IItemBlacklist;
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
		Preconditions.checkNotNull(itemStack, "itemStack cannot be null");
		Preconditions.checkArgument(!itemStack.isEmpty(), "itemStack cannot be empty");

		ingredientBlacklist.addIngredientToBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public void removeItemFromBlacklist(@Nullable ItemStack itemStack) {
		Preconditions.checkNotNull(itemStack, "itemStack cannot be null");
		Preconditions.checkArgument(!itemStack.isEmpty(), "itemStack cannot be empty");

		ingredientBlacklist.removeIngredientFromBlacklist(itemStack);
	}

	@Override
	@Deprecated
	public boolean isItemBlacklisted(@Nullable ItemStack itemStack) {
		Preconditions.checkNotNull(itemStack, "itemStack cannot be null");
		Preconditions.checkArgument(!itemStack.isEmpty(), "itemStack cannot be empty");

		return ingredientBlacklist.isIngredientBlacklisted(itemStack);
	}
	
	@Deprecated
	public boolean isItemBlacklistedByApi(ItemStack itemStack) {
		return ingredientBlacklist.isIngredientBlacklistedByApi(itemStack);
	}
}
