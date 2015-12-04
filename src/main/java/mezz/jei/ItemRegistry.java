package mezz.jei;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.oredict.OreDictionary;

import mezz.jei.api.IItemRegistry;
import mezz.jei.util.Log;
import mezz.jei.util.ModList;
import mezz.jei.util.StackUtil;

class ItemRegistry implements IItemRegistry {

	@Nonnull
	private final Set<String> itemNameSet = new HashSet<>();
	@Nonnull
	private final ImmutableList<ItemStack> itemList;
	@Nonnull
	private final ImmutableList<ItemStack> potionIngredients;
	@Nonnull
	private final ImmutableList<ItemStack> fuels;
	@Nonnull
	private final ModList modList;

	public ItemRegistry() {
		this.modList = new ModList();
		List<ItemStack> itemList = new ArrayList<>();
		List<ItemStack> fuels = new ArrayList<>();

		for (Block block : GameData.getBlockRegistry().typeSafeIterable()) {
			addBlockAndSubBlocks(block, itemList, fuels);
		}

		for (Item item : GameData.getItemRegistry().typeSafeIterable()) {
			addItemAndSubItems(item, itemList, fuels);
		}

		this.itemList = ImmutableList.copyOf(itemList);
		this.fuels = ImmutableList.copyOf(fuels);

		ImmutableList.Builder<ItemStack> potionIngredientBuilder = ImmutableList.builder();
		for (ItemStack itemStack : this.itemList) {
			if (itemStack.getItem().isPotionIngredient(itemStack)) {
				potionIngredientBuilder.add(itemStack);
			}
		}
		this.potionIngredients = potionIngredientBuilder.build();
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
			return "";
		}
		return modList.getModNameForItem(item);
	}

	private void addItemAndSubItems(@Nullable Item item, @Nonnull List<ItemStack> itemList, @Nonnull List<ItemStack> fuels) {
		if (item == null) {
			return;
		}

		if (item.getHasSubtypes()) {
			ItemStack itemStack = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
			List<ItemStack> items = StackUtil.getSubtypes(itemStack);
			addItemStacks(items, itemList, fuels);
		} else {
			ItemStack itemStack = new ItemStack(item);
			addItemStack(itemStack, itemList, fuels);
		}
	}

	private void addBlockAndSubBlocks(@Nullable Block block, @Nonnull List<ItemStack> itemList, @Nonnull List<ItemStack> fuels) {
		if (block == null) {
			return;
		}

		Item item = Item.getItemFromBlock(block);

		if (item == null) {
			ItemStack stack = new ItemStack(block);
			if (stack.getItem() == null) {
				Log.debug("Couldn't get itemStack for block: {}", block.getUnlocalizedName());
				return;
			}
			addItemStack(stack, itemList, fuels);
			return;
		}

		List<ItemStack> subItems = new ArrayList<>();
		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			subItems.clear();
			block.getSubBlocks(item, itemTab, subItems);
			addItemStacks(subItems, itemList, fuels);

			if (subItems.isEmpty()) {
				ItemStack stack = new ItemStack(block);
				if (stack.getItem() == null) {
					Log.debug("Couldn't get itemStack for block: {}", block.getUnlocalizedName());
					return;
				}
				addItemStack(stack, itemList, fuels);
			}
		}
	}

	private void addItemStacks(@Nonnull Iterable<ItemStack> stacks, @Nonnull List<ItemStack> itemList, @Nonnull List<ItemStack> fuels) {
		for (ItemStack stack : stacks) {
			if (stack != null) {
				addItemStack(stack, itemList, fuels);
			}
		}
	}

	private void addItemStack(@Nonnull ItemStack stack, @Nonnull List<ItemStack> itemList, @Nonnull List<ItemStack> fuels) {
		try {
			String itemKey = StackUtil.getUniqueIdentifierForStack(stack);

			if (itemNameSet.contains(itemKey)) {
				return;
			}
			itemNameSet.add(itemKey);
			itemList.add(stack);

			if (TileEntityFurnace.isItemFuel(stack)) {
				fuels.add(stack);
			}
		} catch (RuntimeException e) {
			try {
				Log.error("Couldn't create unique name for itemStack {}. Exception: {}", stack, e);
			} catch (RuntimeException ignored) {

			}
		}
	}

}
