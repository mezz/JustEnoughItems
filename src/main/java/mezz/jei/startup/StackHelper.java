package mezz.jei.startup;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;

public class StackHelper implements IStackHelper {
	private final ISubtypeRegistry subtypeRegistry;
	/**
	 * Uids are cached during loading to improve startup performance.
	 */
	private final Map<UidMode, Map<ItemStack, String>> uidCache = new EnumMap<>(UidMode.class);
	private boolean uidCacheEnabled = true;

	public StackHelper(ISubtypeRegistry subtypeRegistry) {
		this.subtypeRegistry = subtypeRegistry;
		for (UidMode mode : UidMode.values()) {
			uidCache.put(mode, new IdentityHashMap<>());
		}
	}

	public void enableUidCache() {
		uidCacheEnabled = true;
	}

	public void disableUidCache() {
		for (UidMode mode : UidMode.values()) {
			uidCache.put(mode, new IdentityHashMap<>());
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
		SortedSet<Integer> keys = new TreeSet<>(ingredientsMap.keySet());
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
				matchingStack.shrink(1);
				if (matchingStack.getCount() == 0) {
					availableItemStacks.remove(matching);
				}
				matchingItemResult.matchingItems.put(recipeSlotNumber, matching);
			}
		}

		return matchingItemResult;
	}

	public boolean containsSameStacks(Collection<ItemStack> stacks, Collection<ItemStack> contains) {
		return containsSameStacks(new MatchingIterable(stacks), new MatchingIterable(contains));
	}

	/**
	 * Returns true if all stacks from "contains" are found in "stacks" and the opposite is true as well.
	 */
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
	public Integer containsAnyStackIndexed(Map<Integer, ItemStack> stacks, Iterable<ItemStack> contains) {
		MatchingIndexed matchingStacks = new MatchingIndexed(stacks);
		MatchingIterable matchingContains = new MatchingIterable(contains);
		return containsStackMatchable(matchingStacks, matchingContains);
	}

	@Nullable
	public ItemStack containsStack(Iterable<ItemStack> stacks, ItemStack contains) {
		List<ItemStack> containsList = Collections.singletonList(contains);
		return containsAnyStack(stacks, containsList);
	}

	@Override
	@Nullable
	public ItemStack containsAnyStack(Iterable<ItemStack> stacks, Iterable<ItemStack> contains) {
		ErrorUtil.checkNotNull(stacks, "stacks");
		ErrorUtil.checkNotNull(contains, "contains");

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
	@Override
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

		String keyLhs = getUniqueIdentifierForStack(lhs, UidMode.NORMAL);
		String keyRhs = getUniqueIdentifierForStack(rhs, UidMode.NORMAL);
		return keyLhs.equals(keyRhs);
	}

	public List<ItemStack> getMatchingStacks(Ingredient ingredient) {
		if (ingredient == Ingredient.EMPTY) {
			return Collections.emptyList();
		}
		ItemStack[] matchingStacks = ingredient.getMatchingStacks();
		//noinspection ConstantConditions
		if (matchingStacks == null) {
			return Collections.emptyList();
		}
		if (matchingStacks.length > 0) {
			return Arrays.asList(matchingStacks);
		}
		return getAllSubtypes(Arrays.asList(ingredient.matchingStacks));
	}

	@Override
	public List<ItemStack> getSubtypes(@Nullable ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) {
			return Collections.emptyList();
		}

		if (itemStack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
			return Collections.singletonList(itemStack);
		}

		NonNullList<ItemStack> subtypes = NonNullList.create();
		addSubtypesToList(subtypes, itemStack);
		return subtypes;
	}

	private void addSubtypesToList(final List<ItemStack> subtypeList, ItemStack itemStack) {
		final Item item = itemStack.getItem();
		final int stackSize = itemStack.getCount();
		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			if (itemTab == null) {
				ItemStack copy = itemStack.copy();
				copy.setItemDamage(0);
				subtypeList.add(copy);
			} else {
				addSubtypesFromCreativeTabToList(subtypeList, item, stackSize, itemTab);
			}
		}
	}

	public void addSubtypesToList(final List<ItemStack> subtypeList, Item item) {
		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			if (itemTab == null) {
				subtypeList.add(new ItemStack(item, 1));
			} else {
				addSubtypesFromCreativeTabToList(subtypeList, item, 1, itemTab);
			}
		}
	}

	private void addSubtypesFromCreativeTabToList(List<ItemStack> subtypeList, Item item, final int stackSize, CreativeTabs itemTab) {
		NonNullList<ItemStack> subItems = NonNullList.create();
		try {
			item.getSubItems(itemTab, subItems);
		} catch (RuntimeException | LinkageError e) {
			Log.get().warn("Caught a crash while getting sub-items of {}", item, e);
		}

		for (ItemStack subItem : subItems) {
			if (subItem.isEmpty()) {
				Log.get().warn("Found an empty subItem of {}", item);
			} else if (subItem.getMetadata() == OreDictionary.WILDCARD_VALUE) {
				String itemStackInfo = ErrorUtil.getItemStackInfo(subItem);
				Log.get().error("Found an subItem of {} with wildcard metadata: {}", item, itemStackInfo);
			} else {
				if (subItem.getCount() != stackSize) {
					ItemStack subItemCopy = subItem.copy();
					subItemCopy.setCount(stackSize);
					subtypeList.add(subItemCopy);
				} else {
					subtypeList.add(subItem);
				}
			}
		}
	}

	@Override
	public List<ItemStack> getAllSubtypes(@Nullable Iterable stacks) {
		if (stacks == null) {
			return Collections.emptyList();
		}

		List<ItemStack> allSubtypes = new ArrayList<>();
		addSubtypesToList(allSubtypes, stacks);

		if (isAllNulls(allSubtypes)) {
			return Collections.emptyList();
		}

		return allSubtypes;
	}

	private static boolean isAllNulls(Iterable<?> iterable) {
		for (Object element : iterable) {
			if (element != null) {
				return false;
			}
		}
		return true;
	}

	private void addSubtypesToList(List<ItemStack> subtypesList, Iterable stacks) {
		for (Object obj : stacks) {
			if (obj instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) obj;
				if (!itemStack.isEmpty()) {
					if (itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
						addSubtypesToList(subtypesList, itemStack);
					} else {
						subtypesList.add(itemStack);
					}
				}
			} else if (obj instanceof Iterable) {
				addSubtypesToList(subtypesList, (Iterable) obj);
			} else if (obj != null) {
				Log.get().error("Unknown object found: {}", obj);
			} else {
				subtypesList.add(null);
			}
		}
	}

	@Override
	public List<List<ItemStack>> expandRecipeItemStackInputs(@Nullable List inputs) {
		if (inputs == null) {
			return Collections.emptyList();
		}

		return expandRecipeItemStackInputs(inputs, true);
	}

	public List<List<ItemStack>> expandRecipeItemStackInputs(List inputs, boolean expandSubtypes) {
		List<List<ItemStack>> expandedInputs = new ArrayList<>();
		for (Object input : inputs) {
			List<ItemStack> expandedInput = toItemStackList(input, expandSubtypes);
			expandedInputs.add(expandedInput);
		}
		return expandedInputs;
	}

	@Override
	public NonNullList<ItemStack> toItemStackList(@Nullable Object stacks) {
		if (stacks == null) {
			return NonNullList.create();
		}

		return toItemStackList(stacks, true);
	}

	public NonNullList<ItemStack> toItemStackList(Object stacks, boolean expandSubtypes) {
		UniqueItemStackListBuilder itemStackListBuilder = new UniqueItemStackListBuilder(this);
		toItemStackList(itemStackListBuilder, stacks, expandSubtypes);
		return itemStackListBuilder.build();
	}

	private void toItemStackList(UniqueItemStackListBuilder itemStackListBuilder, @Nullable Object input, boolean expandSubtypes) {
		if (input instanceof ItemStack) {
			toItemStackList(itemStackListBuilder, (ItemStack) input, expandSubtypes);
		} else if (input instanceof String) {
			List<ItemStack> stacks = OreDictionary.getOres((String) input);
			for (ItemStack stack : stacks) {
				toItemStackList(itemStackListBuilder, stack, expandSubtypes);
			}
		} else if (input instanceof Ingredient) {
			List<ItemStack> stacks = getMatchingStacks((Ingredient) input);
			for (ItemStack stack : stacks) {
				toItemStackList(itemStackListBuilder, stack, expandSubtypes);
			}
		} else if (input instanceof Iterable) {
			for (Object obj : (Iterable) input) {
				toItemStackList(itemStackListBuilder, obj, expandSubtypes);
			}
		} else if (input != null) {
			Log.get().error("Unknown object found: {}", input);
		}
	}

	private void toItemStackList(UniqueItemStackListBuilder itemStackListBuilder, @Nullable ItemStack itemStack, boolean expandSubtypes) {
		if (itemStack != null) {
			if (expandSubtypes && itemStack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
				List<ItemStack> subtypes = getSubtypes(itemStack);
				for (ItemStack subtype : subtypes) {
					itemStackListBuilder.add(subtype);
				}
			} else {
				itemStackListBuilder.add(itemStack);
			}
		}
	}

	public String getUniqueIdentifierForStack(ItemStack stack) {
		return getUniqueIdentifierForStack(stack, UidMode.NORMAL);
	}

	public String getUniqueIdentifierForStack(ItemStack stack, UidMode mode) {
		ErrorUtil.checkNotEmpty(stack, "stack");
		if (uidCacheEnabled) {
			String result = uidCache.get(mode).get(stack);
			if (result != null) {
				return result;
			}
		}

		Item item = stack.getItem();
		ResourceLocation itemName = item.getRegistryName();
		if (itemName == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			throw new IllegalStateException("Item has no registry name: " + stackInfo);
		}

		StringBuilder itemKey = new StringBuilder(itemName.toString());

		int metadata = stack.getMetadata();
		if (mode != UidMode.WILDCARD && metadata != OreDictionary.WILDCARD_VALUE) {
			String subtypeInfo = subtypeRegistry.getSubtypeInfo(stack);
			if (subtypeInfo != null) {
				if (!subtypeInfo.isEmpty()) {
					itemKey.append(':').append(subtypeInfo);
				}
			} else {
				if (mode == UidMode.FULL) {
					itemKey.append(':').append(metadata);

					NBTTagCompound serializedNbt = stack.serializeNBT();
					NBTTagCompound nbtTagCompound = serializedNbt.getCompoundTag("tag").copy();
					if (serializedNbt.hasKey("ForgeCaps")) {
						NBTTagCompound forgeCaps = serializedNbt.getCompoundTag("ForgeCaps");
						if (!forgeCaps.isEmpty()) { // ForgeCaps should never be empty
							nbtTagCompound.setTag("ForgeCaps", forgeCaps);
						}
					}
					if (!nbtTagCompound.isEmpty()) {
						itemKey.append(':').append(nbtTagCompound);
					}
				} else if (stack.getHasSubtypes()) {
					itemKey.append(':').append(metadata);
				}
			}
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

	public static class MatchingItemsResult {
		public final Map<Integer, Integer> matchingItems = new HashMap<>();
		public final List<Integer> missingItems = new ArrayList<>();
	}

	private interface ItemStackMatchable<R> {
		@Nullable
		ItemStack getStack();

		@Nullable
		R getResult();
	}

	private static abstract class DelegateIterator<T, R> implements Iterator<R> {
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
		private final Iterable<ItemStack> list;

		public MatchingIterable(Iterable<ItemStack> list) {
			this.list = list;
		}

		@Override
		public Iterator<ItemStackMatchable<ItemStack>> iterator() {
			Iterator<ItemStack> stacks = list.iterator();
			return new DelegateIterator<ItemStack, ItemStackMatchable<ItemStack>>(stacks) {
				@Override
				public ItemStackMatchable<ItemStack> next() {
					final ItemStack stack = delegate.next();
					return new ItemStackMatchable<ItemStack>() {
						@Nullable
						@Override
						public ItemStack getStack() {
							return stack;
						}

						@Nullable
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
		private final Map<Integer, ItemStack> map;

		public MatchingIndexed(Map<Integer, ItemStack> map) {
			this.map = map;
		}

		@Override
		public Iterator<ItemStackMatchable<Integer>> iterator() {
			return new DelegateIterator<Map.Entry<Integer, ItemStack>, ItemStackMatchable<Integer>>(map.entrySet().iterator()) {
				@Override
				public ItemStackMatchable<Integer> next() {
					final Map.Entry<Integer, ItemStack> entry = delegate.next();
					return new ItemStackMatchable<Integer>() {
						@Override
						public ItemStack getStack() {
							return entry.getValue();
						}

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
