package mezz.jei.ingredients;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

public class IngredientFilter {

	private IngredientFilterInternals internals;

	public IngredientFilter() {
		this.internals = new IngredientFilterInternals();
	}

	public void rebuild() {
		this.internals = new IngredientFilterInternals();
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
