package mezz.jei;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.config.Config;
import mezz.jei.util.StackUtil;

public class ItemBlacklist implements IItemBlacklist {
	@Nonnull
	private final Set<String> itemBlacklist = new HashSet<>();

	@Override
	public void addItemToBlacklist(ItemStack itemStack) {
		if (itemStack == null) {
			return;
		}
		String uid = StackUtil.getUniqueIdentifierForStack(itemStack);
		itemBlacklist.add(uid);
	}

	@Override
	public void removeItemFromBlacklist(ItemStack itemStack) {
		if (itemStack == null) {
			return;
		}
		String uid = StackUtil.getUniqueIdentifierForStack(itemStack);
		itemBlacklist.remove(uid);
	}

	@Override
	public boolean isItemBlacklisted(ItemStack itemStack) {
		List<String> uids = StackUtil.getUniqueIdentifiersWithWildcard(itemStack);
		for (String uid : uids) {
			if (itemBlacklist.contains(uid) || Config.itemBlacklist.contains(uid)) {
				return true;
			}
		}
		return false;
	}
}
