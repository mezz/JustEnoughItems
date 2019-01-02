package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.collect.UniqueItemStackListBuilder;

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
			uidCache.get(mode).clear();
		}
		uidCacheEnabled = false;
	}

	@Nullable
	public String getOreDictEquivalent(Collection<ItemStack> itemStacks) {
//		if (itemStacks.size() < 2) {
//			return null;
//		}
//
//		final ItemStack firstStack = itemStacks.iterator().next();
//		if (firstStack != null) {
//			for (final int oreId : OreDictionary.getOreIDs(firstStack)) {
//				final String oreName = OreDictionary.getOreName(oreId);
//				List<ItemStack> ores = OreDictionary.getOres(oreName);
//				ores = getAllSubtypes(ores);
//				if (containsSameStacks(itemStacks, ores)) {
//					return oreName;
//				}
//			}
//		}
		// TODO 1.13 Tags
		return null;
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
					subtypesList.add(itemStack);
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

		List<List<ItemStack>> expandedInputs = new ArrayList<>();
		for (Object input : inputs) {
			List<ItemStack> expandedInput = toItemStackList(input);
			expandedInputs.add(expandedInput);
		}
		return expandedInputs;
	}

	@Override
	public NonNullList<ItemStack> toItemStackList(@Nullable Object stacks) {
		if (stacks == null) {
			return NonNullList.create();
		}

		UniqueItemStackListBuilder itemStackListBuilder = new UniqueItemStackListBuilder(this);
		toItemStackList(itemStackListBuilder, stacks);
		return itemStackListBuilder.build();
	}

	private void toItemStackList(UniqueItemStackListBuilder itemStackListBuilder, @Nullable Object input) {
		if (input instanceof ItemStack) {
			itemStackListBuilder.add((ItemStack) input);
//		} else if (input instanceof String) {
			// TODO 1.13 tags
//			List<ItemStack> stacks = OreDictionary.getOres((String) input);
//			for (ItemStack stack : stacks) {
//				if (stack != null) {
//					itemStackListBuilder.add(stack);
//				}
//			}

		} else if (input instanceof Ingredient) {
			ItemStack[] stacks = ((Ingredient) input).getMatchingStacks();
			for (ItemStack stack : stacks) {
				if (stack != null) {
					itemStackListBuilder.add(stack);
				}
			}
		} else if (input instanceof Iterable) {
			for (Object obj : (Iterable) input) {
				toItemStackList(itemStackListBuilder, obj);
			}
		} else if (input != null) {
			Log.get().error("Unknown object found: {}", input);
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

		if (mode != UidMode.WILDCARD) {
			String subtypeInfo = subtypeRegistry.getSubtypeInfo(stack);
			if (subtypeInfo != null) {
				if (!subtypeInfo.isEmpty()) {
					itemKey.append(':').append(subtypeInfo);
				}
			} else {
				if (mode == UidMode.FULL) {
					NBTTagCompound serializedNbt = stack.serializeNBT();
					NBTTagCompound nbtTagCompound = serializedNbt.getCompound("tag").copy();
					if (serializedNbt.hasKey("ForgeCaps")) {
						NBTTagCompound forgeCaps = serializedNbt.getCompound("ForgeCaps");
						if (!forgeCaps.isEmpty()) { // ForgeCaps should never be empty
							nbtTagCompound.setTag("ForgeCaps", forgeCaps);
						}
					}
					if (!nbtTagCompound.isEmpty()) {
						itemKey.append(':').append(nbtTagCompound);
					}
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
}
