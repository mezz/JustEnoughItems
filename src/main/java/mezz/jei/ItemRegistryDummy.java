package mezz.jei;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mezz.jei.api.IItemRegistry;
import mezz.jei.util.ModList;

public class ItemRegistryDummy implements IItemRegistry {
	@Nonnull
	private final ModList modList = new ModList();

	@Nonnull
	@Override
	public ImmutableList<ItemStack> getItemList() {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<ItemStack> getFuels() {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<ItemStack> getPotionIngredients() {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public String getModNameForItem(@Nullable Item item) {
		if (item == null) {
			return "";
		}
		return modList.getModNameForItem(item);
	}

	@Nonnull
	@Override
	public ImmutableList<ItemStack> getItemListForModId(@Nullable String modId) {
		return ImmutableList.of();
	}
}
