package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import mezz.jei.Internal;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class StackHelper implements IStackHelper {
	public static final String nullItemInStack = "Found an itemStack with a null item. This is an error from another mod.";

	/** Uids are cached during loading to improve startup performance. */
	private final Map<UidMode, Map<ItemStack, String>> uidCache = new EnumMap<UidMode, Map<ItemStack, String>>(UidMode.class);
	private boolean uidCacheEnabled = true;

	public StackHelper() {
		for (UidMode mode : UidMode.values()) {
			uidCache.put(mode, new HashMap<ItemStack, String>());
		}
	}

	public void enableUidCache() {
		uidCacheEnabled = true;
	}

	public void disableUidCache() {
		for (UidMode mode : UidMode.values()) {
			uidCache.get(mode).clear();
		}
		uidCacheEnabled = false;
	}

	@Nullable
	public String getOreDictEquivalent(@Nonnull Collection<ItemStack> itemStacks) {
		if (itemStacks.size() < 2) {
			return null;
		}

		final ItemStack firstStack = itemStacks.iterator().next();
		if (firstStack != null) {
			for (final int oreId : OreDictionary.getOreIDs(firstStack)) {
				final String oreName = OreDictionary.getOreName(oreId);
				List<ItemStack> ores = OreDictionary.getOres(oreName);
				ores = getAllSubtypes(ores);
				if (containsSameStacks(itemStacks, ores)) {
					return oreName;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a list of items in slots that complete the recipe defined by requiredStacksList.
	 * Returns a result that contains missingItems if there are not enough items in availableItemStacks.
	 */
	@Nonnull
	public MatchingItemsResult getMatchingItems(@Nonnull Map<Integer, ItemStack> availableItemStacks, @Nonnull Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredientsMap) {
		MatchingItemsResult matchingItemResult = new MatchingItemsResult();

		int recipeSlotNumber = -1;
		SortedSet<Integer> keys = new TreeSet<Integer>(ingredientsMap.keySet());
		for (Integer key : keys) {
			IGuiIngredient<ItemStack> ingredient = ingredientsMap.get(key);
			if (!ingredient.isInput()) {
				continue;
			}
			recipeSlotNumber++;

			List<ItemStack> requiredStacks = ingredient.getAllIngredients();
			if (requiredStacks.isEmpty()) {
				continue;
			}
			
			Integer matching = containsAnyStackIndexed(availableItemStacks, requiredStacks);
			if (matching == null) {
				matchingItemResult.missingItems.add(key);
			} else {
				ItemStack matchingStack = availableItemStacks.get(matching);
				matchingStack.stackSize--;
				if (matchingStack.stackSize == 0) {
					availableItemStacks.remove(matching);
				}
				matchingItemResult.matchingItems.put(recipeSlotNumber, matching);
			}
		}

		return matchingItemResult;
	}

	@Nullable
	public Slot getSlotWithStack(@Nonnull Container container, @Nonnull Iterable<Integer> slotNumbers, @Nonnull ItemStack stack) {
		for (Integer slotNumber : slotNumbers) {
			Slot slot = container.getSlot(slotNumber);
			if (slot != null) {
				ItemStack slotStack = slot.getStack();
				if (isEquivalent(stack, slotStack)) {
					return slot;
				}
			}
		}
		return null;
	}

	public boolean containsSameStacks(@Nonnull Collection<ItemStack> stacks, @Nonnull Collection<ItemStack> contains) {
		return containsSameStacks(new MatchingIterable(stacks), new MatchingIterable(contains));
	}

	/** Returns true if all stacks from "contains" are found in "stacks" and the opposite is true as well. */
	public <R> boolean containsSameStacks(@Nonnull Iterable<ItemStackMatchable<R>> stacks, @Nonnull Iterable<ItemStackMatchable<R>> contains) {
		for (ItemStackMatchable stack : contains) {
			if (containsStack(stacks, stack) == null) {
				return false;
			}
		}

		for (ItemStackMatchable stack : stacks) {
			if (containsStack(contains, stack) == null) {
				return false;
			}
		}

		return true;
	}

	public Integer containsAnyStackIndexed(@Nullable Map<Integer, ItemStack> stacks, @Nullable Iterable<ItemStack> contains) {
		MatchingIndexed matchingStacks = new MatchingIndexed(stacks);
		MatchingIterable matchingContains = new MatchingIterable(contains);
		return containsStackMatchable(matchingStacks, matchingContains);
	}

	public ItemStack containsStack(@Nullable Iterable<ItemStack> stacks, @Nullable ItemStack contains) {
		List<ItemStack> containsList = contains == null ? null : Collections.singletonList(contains);
		return containsAnyStack(stacks, containsList);
	}

	public ItemStack containsAnyStack(@Nullable Iterable<ItemStack> stacks, @Nullable Iterable<ItemStack> contains) {
		MatchingIterable matchingStacks = new MatchingIterable(stacks);
		MatchingIterable matchingContains = new MatchingIterable(contains);
		return containsStackMatchable(matchingStacks, matchingContains);
	}

	/* Returns an ItemStack from "stacks" if it isEquivalent to an ItemStack from "contains" */
	public <R, T> R containsStackMatchable(@Nonnull Iterable<ItemStackMatchable<R>> stacks, @Nonnull Iterable<ItemStackMatchable<T>> contains) {
		for (ItemStackMatchable<?> containStack : contains) {
			R matchingStack = containsStack(stacks, containStack);
			if (matchingStack != null) {
				return matchingStack;
			}
		}

		return null;
	}

	/* Returns an ItemStack from "stacks" if it isEquivalent to "contains" */
	public <R> R containsStack(@Nonnull Iterable<ItemStackMatchable<R>> stacks, @Nonnull ItemStackMatchable<?> contains) {
		for (ItemStackMatchable<R> stack : stacks) {
			if (isEquivalent(contains.getStack(), stack.getStack())) {
				return stack.getResult();
			}
		}
		return null;
	}

	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the INbtIgnoreList
	 */
	public boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs) {
		if (lhs == rhs) {
			return true;
		}

		if (lhs == null || rhs == null) {
			return false;
		}

		if (lhs.getItem() != rhs.getItem()) {
			return false;
		}

		if (lhs.getMetadata() != OreDictionary.WILDCARD_VALUE) {
			if (lhs.getMetadata() != rhs.getMetadata()) {
				return false;
			}
		}

		if (lhs.getHasSubtypes()) {
			String keyLhs = getUniqueIdentifierForStack(lhs, UidMode.NORMAL);
			String keyRhs = getUniqueIdentifierForStack(rhs, UidMode.NORMAL);
			return Java6Helper.equals(keyLhs, keyRhs);
		} else {
			return true;
		}
	}

	@Override
	@Nonnull
	public List<ItemStack> getSubtypes(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return Collections.emptyList();
		}

		Item item = itemStack.getItem();
		if (item == null) {
			Log.error("Null item in itemStack", new NullPointerException());
			return Collections.emptyList();
		}

		if (itemStack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
			return Collections.singletonList(itemStack);
		}

		return getSubtypes(item, itemStack.stackSize);
	}

	@Nonnull
	public List<ItemStack> getSubtypes(@Nonnull final Item item, final int stackSize) {
		List<ItemStack> itemStacks = new ArrayList<ItemStack>();

		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			List<ItemStack> subItems = new ArrayList<ItemStack>();
			try {
				item.getSubItems(item, itemTab, subItems);
			} catch (RuntimeException e) {
				Log.warning("Caught a crash while getting sub-items of {}", item, e);
			} catch (LinkageError e) {
				Log.warning("Caught a crash while getting sub-items of {}", item, e);
			}

			for (ItemStack subItem : subItems) {
				if (subItem == null) {
					Log.warning("Found a null subItem of {}", item);
				} else if (subItem.getItem() == null) {
					Log.warning("Found a subItem of {} with a null item", item);
				} else {
					if (subItem.stackSize != stackSize) {
						ItemStack subItemCopy = subItem.copy();
						subItemCopy.stackSize = stackSize;
						itemStacks.add(subItemCopy);
					} else {
						itemStacks.add(subItem);
					}
				}
			}
		}

		return itemStacks;
	}

	@Override
	@Nonnull
	public List<ItemStack> getAllSubtypes(@Nullable Iterable stacks) {
		if (stacks == null) {
			Log.error("Null stacks", new NullPointerException());
			return Collections.emptyList();
		}

		List<ItemStack> allSubtypes = new ArrayList<ItemStack>();
		getAllSubtypes(allSubtypes, stacks);
		return allSubtypes;
	}

	private void getAllSubtypes(@Nonnull List<ItemStack> subtypesList, @Nonnull Iterable stacks) {
		for (Object obj : stacks) {
			if (obj instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) obj;
				List<ItemStack> subtypes = getSubtypes(itemStack);
				subtypesList.addAll(subtypes);
			} else if (obj instanceof Iterable) {
				getAllSubtypes(subtypesList, (Iterable) obj);
			} else if (obj != null) {
				Log.error("Unknown object found: {}", obj);
			}
		}
	}

	@Override
	@Nonnull
	public List<ItemStack> toItemStackList(@Nullable Object stacks) {
		if (stacks == null) {
			return Collections.emptyList();
		}

		UniqueItemStackListBuilder itemStackListBuilder = new UniqueItemStackListBuilder();
		toItemStackList(itemStackListBuilder, stacks);
		return itemStackListBuilder.build();
	}

	private void toItemStackList(@Nonnull UniqueItemStackListBuilder itemStackListBuilder, @Nullable Object input) {
		if (input instanceof ItemStack) {
			ItemStack stack = (ItemStack) input;
			itemStackListBuilder.add(stack);
		} else if (input instanceof String) {
			List<ItemStack> stacks = OreDictionary.getOres((String) input);
			for (ItemStack stack : stacks) {
				itemStackListBuilder.add(stack);
			}
		} else if (input instanceof Iterable) {
			for (Object obj : (Iterable) input) {
				toItemStackList(itemStackListBuilder, obj);
			}
		} else if (input != null) {
			Log.error("Unknown object found: {}", input);
		}
	}

	@Nonnull
	public String getModId(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		if (item == null) {
			throw new NullPointerException(nullItemInStack);
		}

		ResourceLocation itemName = Item.REGISTRY.getNameForObject(item);
		if (itemName == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			throw new NullPointerException("Item.itemRegistry.getNameForObject returned null for: " + stackInfo);
		}

		return itemName.getResourceDomain();
	}

	@Nonnull
	public String getUniqueIdentifierForStack(@Nonnull ItemStack stack) {
		return getUniqueIdentifierForStack(stack, UidMode.NORMAL);
	}

	@Nonnull
	public String getUniqueIdentifierForStack(@Nonnull ItemStack stack, @Nonnull UidMode mode) {
		if (uidCacheEnabled) {
			String result = uidCache.get(mode).get(stack);
			if (result != null) {
				return result;
			}
		}

		Item item = stack.getItem();
		if (item == null) {
			throw new NullPointerException(nullItemInStack);
		}

		ResourceLocation itemName = item.getRegistryName();
		if (itemName == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			throw new NullPointerException("Item has no registry name: " + stackInfo);
		}

		StringBuilder itemKey = new StringBuilder(itemName.toString());

		ISubtypeRegistry subtypeRegistry = Internal.getHelpers().getSubtypeRegistry();
		String subtypeInfo = subtypeRegistry.getSubtypeInfo(stack);
		if (subtypeInfo != null) {
			itemKey.append(':').append(subtypeInfo);
		}

		int metadata = stack.getMetadata();
		if (mode == UidMode.WILDCARD || metadata == OreDictionary.WILDCARD_VALUE) {
			return itemKey.toString();
		}

		if (mode == UidMode.FULL) {
			itemKey.append(':').append(metadata);

			NBTTagCompound serializedNbt = stack.serializeNBT();
			NBTTagCompound nbtTagCompound = serializedNbt.getCompoundTag("tag");
			if (serializedNbt.hasKey("ForgeCaps")) {
				if (nbtTagCompound == null) {
					nbtTagCompound = new NBTTagCompound();
				}
				nbtTagCompound.setTag("ForgeCaps", serializedNbt.getCompoundTag("ForgeCaps"));
			}
			if (nbtTagCompound != null && !nbtTagCompound.hasNoTags()) {
				itemKey.append(':').append(nbtTagCompound);
			}
		} else if (stack.getHasSubtypes()) {
			itemKey.append(':').append(metadata);
		}

		String result = itemKey.toString();
		if (uidCacheEnabled) {
			uidCache.get(mode).put(stack, result);
		}
		return result;
	}

	public enum UidMode {
		NORMAL, WILDCARD, FULL
	}

	@Nonnull
	public List<String> getUniqueIdentifiersWithWildcard(@Nonnull ItemStack itemStack) {
		String uid = getUniqueIdentifierForStack(itemStack, UidMode.NORMAL);
		String uidWild = getUniqueIdentifierForStack(itemStack, UidMode.WILDCARD);

		if (uid.equals(uidWild)) {
			return Collections.singletonList(uid);
		} else {
			return Arrays.asList(uid, uidWild);
		}
	}

	public int addStack(@Nonnull Container container, @Nonnull Collection<Integer> slotIndexes, @Nonnull ItemStack stack, boolean doAdd) {
		int added = 0;
		// Add to existing stacks first
		for (Integer slotIndex : slotIndexes) {
			Slot slot = container.getSlot(slotIndex);
			if (slot == null) {
				continue;
			}

			ItemStack inventoryStack = slot.getStack();
			if (inventoryStack == null || inventoryStack.getItem() == null) {
				continue;
			}

			// Already occupied by different item, skip this slot.
			if (!inventoryStack.isStackable() || !inventoryStack.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(inventoryStack, stack)) {
				continue;
			}

			int remain = stack.stackSize - added;
			int maxStackSize = Math.min(slot.getItemStackLimit(inventoryStack), inventoryStack.getMaxStackSize());
			int space = maxStackSize - inventoryStack.stackSize;
			if (space <= 0) {
				continue;
			}

			// Enough space
			if (space >= remain) {
				if (doAdd) {
					inventoryStack.stackSize += remain;
				}
				return stack.stackSize;
			}

			// Not enough space
			if (doAdd) {
				inventoryStack.stackSize = inventoryStack.getMaxStackSize();
			}

			added += space;
		}

		if (added >= stack.stackSize) {
			return added;
		}

		for (Integer slotIndex : slotIndexes) {
			Slot slot = container.getSlot(slotIndex);
			if (slot == null) {
				continue;
			}

			ItemStack inventoryStack = slot.getStack();
			if (inventoryStack != null) {
				continue;
			}

			if (doAdd) {
				ItemStack stackToAdd = stack.copy();
				stackToAdd.stackSize = stack.stackSize - added;
				slot.putStack(stackToAdd);
			}
			return stack.stackSize;
		}

		return added;
	}

	public static class MatchingItemsResult {
		@Nonnull
		public final Map<Integer, Integer> matchingItems = new HashMap<Integer, Integer>();
		@Nonnull
		public final List<Integer> missingItems = new ArrayList<Integer>();
	}

	private interface ItemStackMatchable<R> {
		@Nonnull
		ItemStack getStack();
		@Nonnull
		R getResult();
	}

	private static abstract class DelegateIterator<T, R> implements Iterator<R> {
		@Nonnull
		protected final Iterator<T> delegate;

		public DelegateIterator(@Nonnull Iterator<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public void remove() {
			delegate.remove();
		}
	}

	private static class MatchingIterable implements Iterable<ItemStackMatchable<ItemStack>> {
		@Nonnull
		private final Iterable<ItemStack> list;

		public MatchingIterable(@Nullable Iterable<ItemStack> list) {
			if (list == null) {
				this.list = Collections.emptyList();
			} else {
				this.list = list;
			}
		}

		@Nonnull
		@Override
		public Iterator<ItemStackMatchable<ItemStack>> iterator() {
			Iterator<ItemStack> stacks = list.iterator();
			return new DelegateIterator<ItemStack, ItemStackMatchable<ItemStack>>(stacks) {
				@Override
				public ItemStackMatchable<ItemStack> next() {
					final ItemStack stack = delegate.next();
					return new ItemStackMatchable<ItemStack>() {
						@Nonnull
						@Override
						public ItemStack getStack() {
							return stack;
						}

						@Nonnull
						@Override
						public ItemStack getResult() {
							return stack;
						}
					};
				}
			};
		}
	}

	private static class MatchingIndexed implements Iterable<ItemStackMatchable<Integer>> {
		@Nonnull
		private final Map<Integer, ItemStack> map;

		public MatchingIndexed(@Nullable Map<Integer, ItemStack> map) {
			if (map == null) {
				this.map = Collections.emptyMap();
			} else {
				this.map = map;
			}
		}

		@Nonnull
		@Override
		public Iterator<ItemStackMatchable<Integer>> iterator() {
			return new DelegateIterator<Map.Entry<Integer, ItemStack>, ItemStackMatchable<Integer>>(map.entrySet().iterator()) {
				@Override
				public ItemStackMatchable<Integer> next() {
					final Map.Entry<Integer, ItemStack> entry = delegate.next();
					return new ItemStackMatchable<Integer>() {
						@Nonnull
						@Override
						public ItemStack getStack() {
							return entry.getValue();
						}

						@Nonnull
						@Override
						public Integer getResult() {
							return entry.getKey();
						}
					};
				}
			};
		}
	}
}
