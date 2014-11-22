package mezz.jei;

import cpw.mods.fml.common.registry.GameData;
import mezz.jei.util.Log;
import mezz.jei.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemRegistry {

	@Nonnull
	private final Set<String> itemNameSet = new HashSet<String>();

	@Nonnull
	public final List<ItemStack> itemList = new ArrayList<ItemStack>();
	@Nonnull
	public final List<ItemStack> fuels = new ArrayList<ItemStack>();

	public ItemRegistry() {
		for (Block block : GameData.getBlockRegistry().typeSafeIterable())
			addBlockAndSubBlocks(block);

		for (Item item : GameData.getItemRegistry().typeSafeIterable())
			addItemAndSubItems(item);
	}

	private void addItemAndSubItems(@Nullable Item item) {
		if (item == null)
			return;

		if (item.getHasSubtypes()) {
			ItemStack itemStack = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
			List<ItemStack> items = StackUtil.getSubtypes(itemStack);
			addItemStacks(items);
		} else {
			ItemStack itemStack = new ItemStack(item);
			addItemStack(itemStack);
		}
	}

	private void addBlockAndSubBlocks(@Nullable Block block) {
		if (block == null)
			return;

		Item item = Item.getItemFromBlock(block);

		if (item == null) {
			ItemStack stack = new ItemStack(block);
			if (stack.getItem() == null) {
				Log.debug("Couldn't get itemStack for block: " + block.getUnlocalizedName());
				return;
			}
			addItemStack(stack);
			return;
		}

		ArrayList<ItemStack> subItems = new ArrayList<ItemStack>();
		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			subItems.clear();
			block.getSubBlocks(item, itemTab, subItems);
			addItemStacks(subItems);

			if (subItems.isEmpty()) {
				ItemStack stack = new ItemStack(block);
				if (stack.getItem() == null) {
					Log.debug("Couldn't get itemStack for block: " + block.getUnlocalizedName());
					return;
				}
				addItemStack(stack);
			}
		}
	}

	private void addItemStacks(@Nonnull Iterable<ItemStack> stacks) {
		for (ItemStack stack : stacks) {
			if (stack != null)
				addItemStack(stack);
		}
	}

	private void addItemStack(@Nonnull ItemStack stack) {
		String itemKey = uniqueIdentifierForStack(stack);

		if (itemNameSet.contains(itemKey))
			return;
		itemNameSet.add(itemKey);
		itemList.add(stack);

		if (TileEntityFurnace.isItemFuel(stack)) {
			fuels.add(stack);
		}
	}

	@Nonnull
	private String uniqueIdentifierForStack(@Nonnull ItemStack stack) {
		int id = GameData.getItemRegistry().getId(stack.getItem());
		StringBuilder itemKey = new StringBuilder();
		itemKey.append(id).append(":").append(stack.getItemDamage());
		if (stack.hasTagCompound())
			itemKey.append(":").append(stack.getTagCompound());
		return itemKey.toString();
	}

}
