package mezz.jei.ingredients;

import java.util.List;

import com.google.common.collect.ImmutableList;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.item.ItemStack;

public class IngredientFilter {

	private IngredientFilterInternals internals;

	public IngredientFilter(List<IIngredientListElement> ingredientList) {
		this.internals = new IngredientFilterInternals(ingredientList);
	}

	public void rebuild(List<IIngredientListElement> ingredientList) {
		this.internals = new IngredientFilterInternals(ingredientList);
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
