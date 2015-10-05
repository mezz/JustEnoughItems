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
import mezz.jei.util.StackUtil;

class ItemRegistry implements IItemRegistry {

	@Nonnull
	private final Set<String> itemNameSet = new HashSet<String>();
	@Nonnull
	private final ImmutableList<ItemStack> itemList;
	@Nonnull
	private final ImmutableList<ItemStack> fuels;

	public ItemRegistry() {
		List<ItemStack> itemList = new ArrayList<ItemStack>();
		List<ItemStack> fuels = new ArrayList<ItemStack>();

		for (Block block : GameData.getBlockRegistry().typeSafeIterable()) {
			addBlockAndSubBlocks(block, itemList, fuels);
		}

		for (Item item : GameData.getItemRegistry().typeSafeIterable()) {
			addItemAndSubItems(item, itemList, fuels);
		}

		this.itemList = ImmutableList.copyOf(itemList);
		this.fuels = ImmutableList.copyOf(fuels);
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
				Log.debug("Couldn't get itemStack for block: " + block.getUnlocalizedName());
				return;
			}
			addItemStack(stack, itemList, fuels);
			return;
		}

		List<ItemStack> subItems = new ArrayList<ItemStack>();
		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			subItems.clear();
			block.getSubBlocks(item, itemTab, subItems);
			addItemStacks(subItems, itemList, fuels);

			if (subItems.isEmpty()) {
				ItemStack stack = new ItemStack(block);
				if (stack.getItem() == null) {
					Log.debug("Couldn't get itemStack for block: " + block.getUnlocalizedName());
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
		String itemKey = uniqueIdentifierForStack(stack);

		if (itemNameSet.contains(itemKey)) {
			return;
		}
		itemNameSet.add(itemKey);
		itemList.add(stack);

		if (TileEntityFurnace.isItemFuel(stack)) {
			fuels.add(stack);
		}
	}

	@Nonnull
	private String uniqueIdentifierForStack(@Nonnull ItemStack stack) {
		StringBuilder itemKey = new StringBuilder();
		itemKey.append(stack.getUnlocalizedName()).append(':').append(stack.getItemDamage());
		if (stack.hasTagCompound()) {
			itemKey.append(':').append(stack.getTagCompound());
		}
		return itemKey.toString();
	}

}
