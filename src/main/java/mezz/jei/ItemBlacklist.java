package mezz.jei;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.config.Config;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;

public class ItemBlacklist implements IItemBlacklist {
	private final Set<String> itemBlacklist = new HashSet<String>();

	@Override
	public void addItemToBlacklist(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return;
		}
		String uid = Internal.getStackHelper().getUniqueIdentifierForStack(itemStack);
		itemBlacklist.add(uid);

		JustEnoughItems.getProxy().resetItemFilter();
	}

	@Override
	public void removeItemFromBlacklist(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return;
		}
		String uid = Internal.getStackHelper().getUniqueIdentifierForStack(itemStack);
		itemBlacklist.remove(uid);

		JustEnoughItems.getProxy().resetItemFilter();
	}

	@Override
	public boolean isItemBlacklisted(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return false;
		}
		List<String> uids = Internal.getStackHelper().getUniqueIdentifiersWithWildcard(itemStack);
		for (String uid : uids) {
			if (itemBlacklist.contains(uid)) {
				return true;
			}
		}
		return Config.isIngredientOnConfigBlacklist(itemStack);
	}
}
