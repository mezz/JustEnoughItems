package mezz.jei;

import javax.annotation.Nullable;
import java.util.Locale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.IItemRegistry;
import mezz.jei.util.Log;
import mezz.jei.util.ModList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemRegistry implements IItemRegistry {
	private final ImmutableList<ItemStack> itemList;
	private final ImmutableListMultimap<String, ItemStack> itemsByModId;
	private final ImmutableList<ItemStack> potionIngredients;
	private final ImmutableList<ItemStack> fuels;
	private final ModList modList;

	public ItemRegistry(ImmutableList<ItemStack> itemList,
						ImmutableListMultimap<String, ItemStack> itemsByModId,
						ImmutableList<ItemStack> potionIngredients,
						ImmutableList<ItemStack> fuels,
						ModList modList) {
		this.itemList = itemList;
		this.itemsByModId = itemsByModId;
		this.potionIngredients = potionIngredients;
		this.fuels = fuels;
		this.modList = modList;
	}

	@Override
	public ImmutableList<ItemStack> getItemList() {
		return itemList;
	}

	@Override
	public ImmutableList<ItemStack> getFuels() {
		return fuels;
	}

	@Override
	public ImmutableList<ItemStack> getPotionIngredients() {
		return potionIngredients;
	}

	@Override
	public String getModNameForItem(@Nullable Item item) {
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return "";
		}
		return modList.getModNameForItem(item);
	}

	@Override
	public String getModNameForModId(@Nullable String modId) {
		if (modId == null) {
			Log.error("Null modId", new NullPointerException());
			return "";
		}
		return modList.getModNameForModId(modId);
	}

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
