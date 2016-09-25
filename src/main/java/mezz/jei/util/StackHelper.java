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
	public String getOreDictEquivalent(Collection<ItemStack> itemStacks) {
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
	public MatchingItemsResult getMatchingItems(Map<Integer, ItemStack> availableItemStacks, Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredientsMap) {
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

	/**
	 * Get the slot which contains a specific itemStack.
	 *
	 * @param container   the container to search
	 * @param slotNumbers the slots in the container to search
	 * @param itemStack   the itemStack to find
	 * @return the slot that contains the itemStack. returns null if no slot contains the itemStack.
	 */
	@Nullable
	public Slot getSlotWithStack(Container container, Iterable<Integer> slotNumbers, ItemStack itemStack) {
		for (Integer slotNumber : slotNumbers) {
			if (slotNumber >= 0 && slotNumber < container.inventorySlots.size()) {
				Slot slot = container.getSlot(slotNumber);
				ItemStack slotStack = slot.getStack();
				if (ItemStack.areItemsEqual(itemStack, slotStack) && ItemStack.areItemStackTagsEqual(itemStack, slotStack)) {
					return slot;
				}
			}
		}
		return null;
	}

	public boolean containsSameStacks(Collection<ItemStack> stacks, Collection<ItemStack> contains) {
		return containsSameStacks(new MatchingIterable(stacks), new MatchingIterable(contains));
	}

	/** Returns true if all stacks from "contains" are found in "stacks" and the opposite is true as well. */
	public <R> boolean containsSameStacks(Iterable<ItemStackMatchable<R>> stacks, Iterable<ItemStackMatchable<R>> contains) {
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

	@Nullable
	public Integer containsAnyStackIndexed(@Nullable Map<Integer, ItemStack> stacks, @Nullable Iterable<ItemStack> contains) {
		MatchingIndexed matchingStacks = new MatchingIndexed(stacks);
		MatchingIterable matchingContains = new MatchingIterable(contains);
		return containsStackMatchable(matchingStacks, matchingContains);
	}

	@Nullable
	public ItemStack containsStack(@Nullable Iterable<ItemStack> stacks, @Nullable ItemStack contains) {
		List<ItemStack> containsList = contains == null ? null : Collections.singletonList(contains);
		return containsAnyStack(stacks, containsList);
	}

	@Nullable
	public ItemStack containsAnyStack(@Nullable Iterable<ItemStack> stacks, @Nullable Iterable<ItemStack> contains) {
		MatchingIterable matchingStacks = new MatchingIterable(stacks);
		MatchingIterable matchingContains = new MatchingIterable(contains);
		return containsStackMatchable(matchingStacks, matchingContains);
	}

	/* Returns an ItemStack from "stacks" if it isEquivalent to an ItemStack from "contains" */
	@Nullable
	public <R, T> R containsStackMatchable(Iterable<ItemStackMatchable<R>> stacks, Iterable<ItemStackMatchable<T>> contains) {
		for (ItemStackMatchable<?> containStack : contains) {
			R matchingStack = containsStack(stacks, containStack);
			if (matchingStack != null) {
				return matchingStack;
			}
		}

		return null;
	}

	/* Returns an ItemStack from "stacks" if it isEquivalent to "contains" */
	@Nullable
	public <R> R containsStack(Iterable<ItemStackMatchable<R>> stacks, ItemStackMatchable<?> contains) {
		for (ItemStackMatchable<R> stack : stacks) {
			if (isEquivalent(contains.getStack(), stack.getStack())) {
				return stack.getResult();
			}
		}
		return null;
	}

	/**
	 * Similar to ItemStack.areItemStacksEqual but ignores NBT on items without subtypes, and uses the {@link ISubtypeRegistry}
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

	public List<ItemStack> getSubtypes(final Item item, final int stackSize) {
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
	public List<ItemStack> getAllSubtypes(@Nullable Iterable stacks) {
		if (stacks == null) {
			Log.error("Null stacks", new NullPointerException());
			return Collections.emptyList();
		}

		List<ItemStack> allSubtypes = new ArrayList<ItemStack>();
		getAllSubtypes(allSubtypes, stacks);
		return allSubtypes;
	}

	private void getAllSubtypes(List<ItemStack> subtypesList, Iterable stacks) {
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
	public List<List<ItemStack>> expandRecipeItemStackInputs(@Nullable List inputs) {
		if (inputs == null) {
			return Collections.emptyList();
		}

		List<List<ItemStack>> expandedInputs = new ArrayList<List<ItemStack>>();
		for (Object input : inputs) {
			List<ItemStack> expandedInput = toItemStackList(input);
			expandedInputs.add(expandedInput);
		}
		return expandedInputs;
	}

	@Override
	public List<ItemStack> toItemStackList(@Nullable Object stacks) {
		if (stacks == null) {
			return Collections.emptyList();
		}

		UniqueIngredientListBuilder<ItemStack> ingredientListBuilder = new UniqueIngredientListBuilder<ItemStack>(ItemStack.class);
		toItemStackList(ingredientListBuilder, stacks);
		return ingredientListBuilder.build();
	}

	private void toItemStackList(UniqueIngredientListBuilder<ItemStack> ingredientListBuilder, @Nullable Object input) {
		if (input instanceof ItemStack) {
			ItemStack stack = (ItemStack) input;
			if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
				List<ItemStack> subtypes = getSubtypes(stack);
				for (ItemStack subtype : subtypes) {
					ingredientListBuilder.add(subtype);
				}
			} else {
				ingredientListBuilder.add(stack);
			}
		} else if (input instanceof String) {
			List<ItemStack> stacks = OreDictionary.getOres((String) input);
			for (ItemStack stack : stacks) {
				ingredientListBuilder.add(stack);
			}
		} else if (input instanceof Iterable) {
			for (Object obj : (Iterable) input) {
				toItemStackList(ingredientListBuilder, obj);
			}
		} else if (input != null) {
			Log.error("Unknown object found: {}", input);
		}
	}

	public String getUniqueIdentifierForStack(ItemStack stack) {
		return getUniqueIdentifierForStack(stack, UidMode.NORMAL);
	}

	public String getUniqueIdentifierForStack(ItemStack stack, UidMode mode) {
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

		if (mode != UidMode.WILDCARD) {
			ISubtypeRegistry subtypeRegistry = Internal.getHelpers().getSubtypeRegistry();
			String subtypeInfo = subtypeRegistry.getSubtypeInfo(stack);
			if (subtypeInfo != null) {
				itemKey.append(':').append(subtypeInfo);
			}
		}

		int metadata = stack.getMetadata();
		if (mode == UidMode.WILDCARD || metadata == OreDictionary.WILDCARD_VALUE) {
			return itemKey.toString();
		}

		if (mode == UidMode.FULL) {
			itemKey.append(':').append(metadata);

			NBTTagCompound serializedNbt = stack.serializeNBT();
			NBTTagCompound nbtTagCompound = serializedNbt.getCompoundTag("tag").copy();
			if (serializedNbt.hasKey("ForgeCaps")) {
				NBTTagCompound forgeCaps = serializedNbt.getCompoundTag("ForgeCaps");
				if (!forgeCaps.hasNoTags()) { // ForgeCaps should never be empty
					nbtTagCompound.setTag("ForgeCaps", forgeCaps);
				}
			}
			if (!nbtTagCompound.hasNoTags()) {
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

	public List<String> getUniqueIdentifiersWithWildcard(ItemStack itemStack) {
		String uid = getUniqueIdentifierForStack(itemStack, UidMode.NORMAL);
		String uidWild = getUniqueIdentifierForStack(itemStack, UidMode.WILDCARD);

		if (uid.equals(uidWild)) {
			return Collections.singletonList(uid);
		} else {
			return Arrays.asList(uid, uidWild);
		}
	}

	public int addStack(Container container, Collection<Integer> slotIndexes, ItemStack stack, boolean doAdd) {
		int added = 0;
		// Add to existing stacks first
		for (final Integer slotIndex : slotIndexes) {
			if (slotIndex >= 0 && slotIndex < container.inventorySlots.size()) {
				final Slot slot = container.getSlot(slotIndex);
				final ItemStack inventoryStack = slot.getStack();
				// Check that the slot's contents are stackable with this stack
				if (inventoryStack != null &&
						inventoryStack.getItem() != null &&
						inventoryStack.isStackable() &&
						inventoryStack.isItemEqual(stack) &&
						ItemStack.areItemStackTagsEqual(inventoryStack, stack)) {

					final int remain = stack.stackSize - added;
					final int maxStackSize = Math.min(slot.getItemStackLimit(inventoryStack), inventoryStack.getMaxStackSize());
					final int space = maxStackSize - inventoryStack.stackSize;
					if (space > 0) {

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
				}
			}
		}

		if (added >= stack.stackSize) {
			return added;
		}

		for (final Integer slotIndex : slotIndexes) {
			if (slotIndex >= 0 && slotIndex < container.inventorySlots.size()) {
				final Slot slot = container.getSlot(slotIndex);
				final ItemStack inventoryStack = slot.getStack();
				if (inventoryStack == null) {
					if (doAdd) {
						ItemStack stackToAdd = stack.copy();
						stackToAdd.stackSize = stack.stackSize - added;
						slot.putStack(stackToAdd);
					}
					return stack.stackSize;
				}
			}
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

		public DelegateIterator(Iterator<T> delegate) {
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
