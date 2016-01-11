package mezz.jei;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import mezz.jei.api.INbtIgnoreList;
import mezz.jei.config.Config;
import mezz.jei.util.Log;

public class NbtIgnoreList implements INbtIgnoreList {
	private final Set<String> nbtTagNameBlacklist = new HashSet<>();
	private final HashMultimap<Item, String> itemNbtTagNameBlacklist = HashMultimap.create();

	@Deprecated
	@Override
	public void ignoreNbtTagNames(String... nbtTagNames) {
		Collections.addAll(nbtTagNameBlacklist, nbtTagNames);
	}

	@Override
	public void ignoreNbtTagNames(@Nullable Item item, String... nbtTagNames) {
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return;
		}
		Collection<String> ignoredNbtTagNames = itemNbtTagNameBlacklist.get(item);
		Collections.addAll(ignoredNbtTagNames, nbtTagNames);
	}

	@Override
	public boolean isNbtTagIgnored(@Nullable String nbtTagName) {
		if (nbtTagName == null) {
			Log.error("Null nbtTagName", new NullPointerException());
			return false;
		}
		return Config.getNbtKeyIgnoreList().contains(nbtTagName) || nbtTagNameBlacklist.contains(nbtTagName);
	}

	@Override
	@Nonnull
	public Set<String> getIgnoredNbtTags(@Nullable Set<String> nbtTagNames) {
		if (nbtTagNames == null) {
			Log.error("Null nbtTagNames", new NullPointerException());
			return Collections.emptySet();
		}
		Set<String> ignoredKeysConfig = Sets.intersection(nbtTagNames, Config.getNbtKeyIgnoreList());
		Set<String> ignoredKeysApi = Sets.intersection(nbtTagNames, nbtTagNameBlacklist);
		return Sets.union(ignoredKeysConfig, ignoredKeysApi);
	}

	@Nullable
	@Override
	public NBTTagCompound getNbt(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return null;
		}

		NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
		if (nbtTagCompound == null) {
			return null;
		}

		Set<String> keys = nbtTagCompound.getKeySet();

		Set<String> allIgnoredKeysForItem = itemNbtTagNameBlacklist.get(itemStack.getItem());

		Set<String> ignoredKeysConfig = Sets.intersection(keys, Config.getNbtKeyIgnoreList());
		Set<String> ignoredKeysApi = Sets.intersection(keys, nbtTagNameBlacklist);
		Set<String> ignoredKeysApiForItem = Sets.intersection(keys, allIgnoredKeysForItem);

		Set<String> ignoredKeys = Sets.union(ignoredKeysConfig, ignoredKeysApi);
		ignoredKeys = Sets.union(ignoredKeys, ignoredKeysApiForItem);

		if (ignoredKeys.isEmpty()) {
			return nbtTagCompound;
		}

		NBTTagCompound nbtTagCompoundCopy = (NBTTagCompound) nbtTagCompound.copy();
		for (String ignoredKey : ignoredKeys) {
			nbtTagCompoundCopy.removeTag(ignoredKey);
		}
		return nbtTagCompoundCopy;
	}
}
