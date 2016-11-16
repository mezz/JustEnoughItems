package mezz.jei;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.util.IngredientUtil;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;

public class ItemBlacklist implements IItemBlacklist {
	private final IIngredientHelper<ItemStack> ingredientHelper;
	private final Set<String> itemBlacklist = new HashSet<String>();

	public ItemBlacklist(IIngredientRegistry ingredientRegistry) {
		this.ingredientHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
	}

	@Override
	public void addItemToBlacklist(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return;
		}
		if (itemStack.func_190926_b()) {
			Log.error("Invalid itemStack", new IllegalArgumentException());
			return;
		}

		String uid = ingredientHelper.getUniqueId(itemStack);
		itemBlacklist.add(uid);
	}

	@Override
	public void removeItemFromBlacklist(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return;
		}
		if (itemStack.func_190926_b()) {
			Log.error("Invalid itemStack", new IllegalArgumentException());
			return;
		}

		String uid = ingredientHelper.getUniqueId(itemStack);
		itemBlacklist.remove(uid);
	}

	@Override
	public boolean isItemBlacklisted(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return false;
		}
		if (itemStack.func_190926_b()) {
			Log.error("Invalid itemStack", new IllegalArgumentException());
			return false;
		}

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
