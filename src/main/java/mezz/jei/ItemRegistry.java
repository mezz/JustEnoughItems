package mezz.jei;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mezz.jei.api.IItemRegistry;
import mezz.jei.util.Log;
import mezz.jei.util.ModList;

public class ItemRegistry implements IItemRegistry {
	@Nonnull
	private final ImmutableList<ItemStack> itemList;
	@Nonnull
	private final ImmutableListMultimap<String, ItemStack> itemsByModId;
	@Nonnull
	private final ImmutableList<ItemStack> potionIngredients;
	@Nonnull
	private final ImmutableList<ItemStack> fuels;
	@Nonnull
	private final ModList modList;

	public ItemRegistry(@Nonnull ImmutableList<ItemStack> itemList,
			@Nonnull ImmutableListMultimap<String, ItemStack> itemsByModId,
			@Nonnull ImmutableList<ItemStack> potionIngredients,
			@Nonnull ImmutableList<ItemStack> fuels,
			@Nonnull ModList modList) {
		this.itemList = itemList;
		this.itemsByModId = itemsByModId;
		this.potionIngredients = potionIngredients;
		this.fuels = fuels;
		this.modList = modList;
	}

	@Override
	@Nonnull
	public ImmutableList<ItemStack> getItemList() {
		return itemList;
	}

	@Override
	@Nonnull
	public ImmutableList<ItemStack> getFuels() {
		return fuels;
	}

	@Override
	@Nonnull
	public ImmutableList<ItemStack> getPotionIngredients() {
		return potionIngredients;
	}

	@Nonnull
	@Override
	public String getModNameForItem(@Nullable Item item) {
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return "";
		}
		return modList.getModNameForItem(item);
	}

	@Nonnull
	public String getModNameForModId(@Nonnull String modId) {
		return modList.getModNameForModId(modId);
	}

	@Nonnull
	@Override
	public ImmutableList<ItemStack> getItemListForModId(@Nullable String modId) {
		if (modId == null) {
			Log.error("Null modId", new NullPointerException());
			return ImmutableList.of();
		}
		String lowerCaseModId = modId.toLowerCase(Locale.ENGLISH);
		return itemsByModId.get(lowerCaseModId);
	}
}
