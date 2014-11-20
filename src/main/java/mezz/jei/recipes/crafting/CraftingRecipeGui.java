package mezz.jei.recipes.crafting;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IGuiHelper;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.gui.resource.DrawableRecipePng;
import mezz.jei.recipes.RecipeGui;
import net.minecraft.item.ItemStack;

public abstract class CraftingRecipeGui extends RecipeGui {

	private static final int craftOutputSlot = 0;
	private static final int craftInputSlot1 = 1;
	private static final int craftInputSlotCount = 9;

	public CraftingRecipeGui() {
		super(new DrawableRecipePng(RecipeType.CRAFTING_TABLE));

		IGuiHelper guiHelper = JEIManager.guiHelper;

		addItem(guiHelper.makeGuiItemStack(94, 18, 1));

		for (int y = 0; y < 3; ++y)
			for (int x = 0; x < 3; ++x)
				addItem(guiHelper.makeGuiItemStack(x * 18, y * 18, 1));
	}

	protected void setOutput(Object output) {
		setItem(craftOutputSlot, output, null);
	}

	protected void setInput(Object[] recipeItems, ItemStack focusStack, int width, int height) {
		for (int i = 0; i < recipeItems.length; i++) {
			Object recipeItem = recipeItems[i];
			int index;
			if (width == 1) {
				if (height == 3)
					index = (i * 3) + 1;
				else if (height == 2)
					index = (i * 3) + 4;
				else
					index = 4;
			} else if (height == 1) {
				index = i + 6;
			} else if (width == 2) {
				index = i;
				if (i > 1) {
					index++;
				if (i > 4)
					index++;
				}
			} else if (height == 2) {
				index = i + 3;
			} else {
				index = i;
			}
			setInput(index, recipeItem, focusStack);
		}
	}

	protected void setInput(int inputIndex, Object input, ItemStack focusStack) {
		setItem(craftInputSlot1 + inputIndex, input, focusStack);
	}
}
