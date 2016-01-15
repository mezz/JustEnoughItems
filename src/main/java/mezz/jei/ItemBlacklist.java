package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.config.Config;
import mezz.jei.util.Log;

public class ItemBlacklist implements IItemBlacklist {
	@Nonnull
	private final Set<String> itemBlacklist = new HashSet<>();

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
			if (itemBlacklist.contains(uid) || Config.getItemBlacklist().contains(uid)) {
				return true;
			}
		}
		return false;
	}
}
