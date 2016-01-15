package mezz.jei;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.GameData;

import mezz.jei.api.IItemRegistry;
import mezz.jei.util.Log;
import mezz.jei.util.ModList;

public class ItemRegistry implements IItemRegistry {

	@Nonnull
	private final Set<String> itemNameSet = new HashSet<>();
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

	public ItemRegistry() {
		this.modList = new ModList();
		List<ItemStack> itemListMutable = new ArrayList<>();
		List<ItemStack> fuelsMutable = new ArrayList<>();

		for (Block block : GameData.getBlockRegistry().typeSafeIterable()) {
			addBlockAndSubBlocks(block, itemListMutable, fuelsMutable);
		}

		for (Item item : GameData.getItemRegistry().typeSafeIterable()) {
			addItemAndSubItems(item, itemListMutable, fuelsMutable);
		}

		addEnchantedBooks(itemListMutable);

		this.itemList = ImmutableList.copyOf(itemListMutable);
		this.fuels = ImmutableList.copyOf(fuelsMutable);

		ImmutableListMultimap.Builder<String, ItemStack> itemsByModIdBuilder = ImmutableListMultimap.builder();
		for (ItemStack itemStack : itemListMutable) {
			Item item = itemStack.getItem();
			if (item != null) {
				ResourceLocation itemResourceLocation = GameData.getItemRegistry().getNameForObject(itemStack.getItem());
				String modId = itemResourceLocation.getResourceDomain().toLowerCase(Locale.ENGLISH);
				itemsByModIdBuilder.put(modId, itemStack);
			}
		}
		this.itemsByModId = itemsByModIdBuilder.build();

		ImmutableList.Builder<ItemStack> potionIngredientBuilder = ImmutableList.builder();
		for (ItemStack itemStack : this.itemList) {
			if (itemStack.getItem().isPotionIngredient(itemStack)) {
				potionIngredientBuilder.add(itemStack);
			}
		}
		this.potionIngredients = potionIngredientBuilder.build();
	}

	private void addEnchantedBooks(List<ItemStack> itemList) {
		for (Enchantment enchantment : Enchantment.enchantmentsBookList) {
			if (enchantment != null && enchantment.type != null) {
				EnchantmentData enchantmentData = new EnchantmentData(enchantment, enchantment.getMaxLevel());
				ItemStack enchantedBook = Items.enchanted_book.getEnchantedItemStack(enchantmentData);
				itemList.add(enchantedBook);
			}
		}
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
	@Override
	public ImmutableList<ItemStack> getItemListForModId(@Nullable String modId) {
		if (modId == null) {
			Log.error("Null modId", new NullPointerException());
			return ImmutableList.of();
		}
		String lowerCaseModId = modId.toLowerCase(Locale.ENGLISH);
		return itemsByModId.get(lowerCaseModId);
	}

	private void addItemAndSubItems(@Nullable Item item, @Nonnull List<ItemStack> itemList, @Nonnull List<ItemStack> fuels) {
		if (item == null) {
			return;
		}

		List<ItemStack> items = Internal.getStackHelper().getSubtypes(item, 1);
		addItemStacks(items, itemList, fuels);
	}

	private void addBlockAndSubBlocks(@Nullable Block block, @Nonnull List<ItemStack> itemList, @Nonnull List<ItemStack> fuels) {
		if (block == null) {
			return;
		}

		Item item = Item.getItemFromBlock(block);

		if (item == null) {
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
			String itemKey = Internal.getStackHelper().getUniqueIdentifierForStack(stack);

			if (itemNameSet.contains(itemKey)) {
				return;
			}
			itemNameSet.add(itemKey);
			itemList.add(stack);

			if (TileEntityFurnace.isItemFuel(stack)) {
				fuels.add(stack);
			}
		} catch (RuntimeException e) {
			Log.error("Couldn't create unique name for itemStack {}.", stack.getClass(), e);
		}
	}
}
