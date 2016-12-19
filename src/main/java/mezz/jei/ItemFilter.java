package mezz.jei;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

public class ItemFilter {

	private ItemFilterInternals internals;

	public ItemFilter() {
		this.internals = new ItemFilterInternals();
	}

	public void rebuild() {
		this.internals = new ItemFilterInternals();
	}

	public ImmutableList<Object> getIngredientList() {
		return this.internals.getIngredientList();
	}

	public ImmutableList<ItemStack> getItemStacks() {
		ImmutableList.Builder<ItemStack> filteredStacks = ImmutableList.builder();
		for (Object ingredient : getIngredientList()) {
			if (ingredient instanceof ItemStack) {
				filteredStacks.add((ItemStack) ingredient);
			}
		}
		return filteredStacks.build();
	}

	public int size() {
		return getIngredientList().size();
	}
}
