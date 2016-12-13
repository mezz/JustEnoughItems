package mezz.jei;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.util.IngredientUtil;
import net.minecraft.item.ItemStack;

public class ItemBlacklist implements IItemBlacklist {
	private final IIngredientHelper<ItemStack> ingredientHelper;
	private final Set<String> itemBlacklist = new HashSet<String>();

	public ItemBlacklist(IIngredientRegistry ingredientRegistry) {
		this.ingredientHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
	}

	@Override
	public void addItemToBlacklist(@Nullable ItemStack itemStack) {
		Preconditions.checkNotNull(itemStack, "itemStack cannot be null");
		Preconditions.checkArgument(!itemStack.isEmpty(), "itemStack cannot be empty");

		String uid = ingredientHelper.getUniqueId(itemStack);
		itemBlacklist.add(uid);
	}

	@Override
	public void removeItemFromBlacklist(@Nullable ItemStack itemStack) {
		Preconditions.checkNotNull(itemStack, "itemStack cannot be null");
		Preconditions.checkArgument(!itemStack.isEmpty(), "itemStack cannot be empty");

		String uid = ingredientHelper.getUniqueId(itemStack);
		itemBlacklist.remove(uid);
	}

	@Override
	public boolean isItemBlacklisted(@Nullable ItemStack itemStack) {
		Preconditions.checkNotNull(itemStack, "itemStack cannot be null");
		Preconditions.checkArgument(!itemStack.isEmpty(), "itemStack cannot be empty");

		return isItemBlacklistedByApi(itemStack) ||
				Config.isIngredientOnConfigBlacklist(itemStack, ingredientHelper);
	}

	public boolean isItemBlacklistedByApi(ItemStack itemStack) {
		List<String> uids = IngredientUtil.getUniqueIdsWithWildcard(ingredientHelper, itemStack);
		for (String uid : uids) {
			if (itemBlacklist.contains(uid)) {
				return true;
			}
		}
		return false;
	}
}
