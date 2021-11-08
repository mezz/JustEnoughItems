package mezz.jei.gui;

import java.util.List;

import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;

public class CraftingGridHelper implements ICraftingGridHelper {
	private final int craftInputSlot1;

	public CraftingGridHelper(int craftInputSlot1) {
		this.craftInputSlot1 = craftInputSlot1;
	}

	@Override
	public <T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs) {
		int width, height;
		if (inputs.size() > 4) {
			width = height = 3;
		} else if (inputs.size() > 1) {
			width = height = 2;
		} else {
			width = height = 1;
		}

		setInputs(ingredientGroup, inputs, width, height);
	}

	@Override
	public <T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs, int width, int height) {
		for (int i = 0; i < inputs.size(); i++) {
			List<T> recipeItem = inputs.get(i);
			int index = getCraftingIndex(i, width, height);

			setInput(ingredientGroup, index, recipeItem);
		}
	}

	private <T> void setInput(IGuiIngredientGroup<T> guiIngredients, int inputIndex, List<T> input) {
		guiIngredients.set(craftInputSlot1 + inputIndex, input);
	}

	private static int getCraftingIndex(int i, int width, int height) {
		int index;
		if (width == 1) {
			if (height == 3) {
				index = (i * 3) + 1;
			} else if (height == 2) {
				index = (i * 3) + 1;
			} else {
				index = 4;
			}
		} else if (height == 1) {
			index = i + 3;
		} else if (width == 2) {
			index = i;
			if (i > 1) {
				index++;
				if (i > 3) {
					index++;
				}
			}
		} else if (height == 2) {
			index = i + 3;
		} else {
			index = i;
		}
		return index;
	}

}
