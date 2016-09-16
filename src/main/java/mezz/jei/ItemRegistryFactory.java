package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.config.Constants;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Java6Helper;
import mezz.jei.util.Log;
import mezz.jei.util.ModList;
import mezz.jei.util.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemRegistryFactory {
	@Nonnull
	private final Set<String> itemNameSet = new HashSet<String>();
	@Nonnull
	private final List<ItemStack> itemList = new ArrayList<ItemStack>();
	@Nonnull
	private final List<ItemStack> fuels = new ArrayList<ItemStack>();
	@Nonnull
	private final List<ItemStack> potionIngredients = new ArrayList<ItemStack>();

	@Nonnull
	private final Set<String> itemWildcardNameSet = new HashSet<String>();
	/** The order that items were added, using wildcard. Used to keep similar items together. */
	@Nonnull
	private final List<String> itemAddedOrder = new ArrayList<String>();

	public ItemRegistry createItemRegistry() {
		final ModList modList = new ModList();

		for (CreativeTabs creativeTab : CreativeTabs.CREATIVE_TAB_ARRAY) {
			List<ItemStack> creativeTabItemStacks = new ArrayList<ItemStack>();
			try {
				creativeTab.displayAllRelevantItems(creativeTabItemStacks);
			} catch (RuntimeException e) {
				Log.error("Creative tab crashed while getting items. Some items from this tab will be missing from the item list. {}", creativeTab, e);
			} catch (LinkageError e) {
				Log.error("Creative tab crashed while getting items. Some items from this tab will be missing from the item list. {}", creativeTab, e);
			}
			for (ItemStack itemStack : creativeTabItemStacks) {
				if (itemStack == null) {
					Log.error("Found a null itemStack in creative tab: {}", creativeTab);
				} else if (itemStack.getItem() == null) {
					Log.error("Found a null item in an itemStack from creative tab: {}", creativeTab);
				} else {
					addItemStack(itemStack);
				}
			}
		}

		for (Block block : ForgeRegistries.BLOCKS) {
			addBlockAndSubBlocks(block);
		}
		for (Item item : ForgeRegistries.ITEMS) {
			addItemAndSubItems(item);
		}

		final StackHelper stackHelper = Internal.getStackHelper();

		Collections.sort(itemList, new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack stack1, ItemStack stack2) {
				final String stack1ModName = modList.getModNameForItem(stack1.getItem());
				final String stack2ModName = modList.getModNameForItem(stack2.getItem());

				if (stack1ModName.equals(stack2ModName)) {
					final String itemUid1 = stackHelper.getUniqueIdentifierForStack(stack1, StackHelper.UidMode.WILDCARD);
					final String itemUid2 = stackHelper.getUniqueIdentifierForStack(stack2, StackHelper.UidMode.WILDCARD);
					final int itemOrderIndex1 = itemAddedOrder.indexOf(itemUid1);
					final int itemOrderIndex2 = itemAddedOrder.indexOf(itemUid2);
					return Java6Helper.compare(itemOrderIndex1, itemOrderIndex2);
				} else if (stack1ModName.equals(Constants.minecraftModName)) {
					return -1;
				} else if (stack2ModName.equals(Constants.minecraftModName)) {
					return 1;
				} else {
					return stack1ModName.compareTo(stack2ModName);
				}
			}
		});

		ImmutableListMultimap.Builder<String, ItemStack> itemsByModIdBuilder = ImmutableListMultimap.builder();
		for (ItemStack itemStack : itemList) {
			Item item = itemStack.getItem();
			if (item != null) {
				String modId = stackHelper.getModId(itemStack).toLowerCase(Locale.ENGLISH);
				itemsByModIdBuilder.put(modId, itemStack);
			}
		}

		return new ItemRegistry(ImmutableList.copyOf(itemList), itemsByModIdBuilder.build(), ImmutableList.copyOf(potionIngredients), ImmutableList.copyOf(fuels), modList);
	}

	private void addItemAndSubItems(@Nullable Item item) {
		if (item == null) {
			return;
		}

		List<ItemStack> items = Internal.getStackHelper().getSubtypes(item, 1);
		for (ItemStack stack : items) {
			if (stack != null) {
				addItemStack(stack);
			}
		}
	}

	private void addBlockAndSubBlocks(@Nullable Block block) {
		if (block == null) {
			return;
		}

		Item item = Item.getItemFromBlock(block);
		if (item == null) {
			return;
		}

		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			List<ItemStack> subBlocks = new ArrayList<ItemStack>();
			block.getSubBlocks(item, itemTab, subBlocks);
			for (ItemStack subBlock : subBlocks) {
				if (subBlock == null) {
					Log.error("Found null subBlock of {}", block);
				} else if (subBlock.getItem() == null) {
					Log.error("Found subBlock of {} with null item", block);
				} else {
					addItemStack(subBlock);
				}
			}
		}
	}

	private void addItemStack(@Nonnull ItemStack stack) {
		StackHelper stackHelper = Internal.getStackHelper();
		try {
			final String itemKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.FULL);

			if (itemNameSet.contains(itemKey)) {
				return;
			}
			itemNameSet.add(itemKey);
			itemList.add(stack);

			final String itemWildcardKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.WILDCARD);
			if (!itemWildcardNameSet.contains(itemWildcardKey)) {
				itemWildcardNameSet.add(itemWildcardKey);
				itemAddedOrder.add(itemWildcardKey);
			}

			if (TileEntityFurnace.isItemFuel(stack)) {
				fuels.add(stack);
			}

			if (PotionHelper.isReagent(stack)) {
				potionIngredients.add(stack);
			}
		} catch (RuntimeException e) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			Log.error("Couldn't create unique name for itemStack {}", stackInfo, e);
		}
	}
}
