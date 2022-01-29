package mezz.jei.gui;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeLayoutSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.ArrayList;
import java.util.List;

public class CraftingGridHelper implements ICraftingGridHelper {
	private final int craftInputSlot1;
	private final int craftOutputSlot;

	public CraftingGridHelper(int craftInputSlot1, int craftOutputSlot) {
		this.craftInputSlot1 = craftInputSlot1;
		this.craftOutputSlot = craftOutputSlot;
	}

	@Override
	public <T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs) {
		int width;
		int height;
		width = height = getShapelessSize(inputs.size());
		setInputs(ingredientGroup, inputs, width, height);
	}

	@Override
	public <T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs, int width, int height) {
		for (int i = 0; i < inputs.size(); i++) {
			List<T> recipeItem = inputs.get(i);
			int index = getCraftingIndex(i, width, height);
			ingredientGroup.set(craftInputSlot1 + index, recipeItem);
		}
	}

	@Override
	public <T> void setInputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, List<List<T>> inputs, int width, int height) {
		if (width <= 0 || height <= 0) {
			builder.setShapeless();
			width = height = getShapelessSize(inputs.size());
		}
		List<IRecipeLayoutSlotBuilder> inputSlots = new ArrayList<>();
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				int index = craftInputSlot1 + x + (y * 3);
				IRecipeLayoutSlotBuilder slot = builder.addSlot(index, RecipeIngredientRole.INPUT, x * 18, y * 18);
				inputSlots.add(slot);
			}
		}

		for (int i = 0; i < inputs.size(); i++) {
			int index = getCraftingIndex(i, width, height);
			IRecipeLayoutSlotBuilder slot = inputSlots.get(index);

			List<T> ingredients = inputs.get(i);
			if (ingredients != null) {
				slot.addIngredients(ingredientType, ingredients);
			}
		}
	}

	@Override
	public <T> void setOutputs(IRecipeLayoutBuilder builder, IIngredientType<T> ingredientType, List<T> outputs) {
		builder.addSlot(craftOutputSlot, RecipeIngredientRole.OUTPUT, 94, 18)
			.addIngredients(ingredientType, outputs);
	}

	private static int getShapelessSize(int total) {
		if (total > 4) {
			return 3;
		} else if (total > 1) {
			return 2;
		} else {
			return 1;
		}
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
