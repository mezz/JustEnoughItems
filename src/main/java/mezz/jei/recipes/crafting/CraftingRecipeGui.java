package mezz.jei.recipes.crafting;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IGuiHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.gui.resource.DrawableResource;
import mezz.jei.gui.resource.IDrawable;
import mezz.jei.recipes.RecipeGui;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class CraftingRecipeGui extends RecipeGui {

	private static final int craftOutputSlot = 0;
	private static final int craftInputSlot1 = 1;

	private static IDrawable getBackground() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/crafting_table.png");
		IRecipeType type = RecipeType.CRAFTING_TABLE;
		return new DrawableResource(location, 29, 16, type.displayWidth(), type.displayHeight());
	}

	protected CraftingRecipeGui() {
		super(getBackground());

		IGuiHelper guiHelper = JEIManager.guiHelper;

		addItem(guiHelper.makeGuiItemStack(94, 18, 1));

		for (int y = 0; y < 3; ++y)
			for (int x = 0; x < 3; ++x)
				addItem(guiHelper.makeGuiItemStack(x * 18, y * 18, 1));
	}

	protected void setOutput(@Nonnull ItemStack output) {
		setItem(craftOutputSlot, output);
	}

	protected void setInput(@Nonnull Object[] recipeItems, @Nullable ItemStack focusStack, int width, int height) {
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

			if (recipeItem instanceof ItemStack) {
				setInput(index, (ItemStack) recipeItem);
			} else if (recipeItem instanceof Iterable) {
				List<ItemStack> itemStacks = StackUtil.getItemStacksRecursive((Iterable)recipeItem);
				setInput(index, itemStacks, focusStack);
			}
		}
	}

	protected void setInput(int inputIndex, @Nonnull ItemStack itemStack) {
		setItem(craftInputSlot1 + inputIndex, itemStack);
	}

	protected void setInput(int inputIndex, @Nonnull Iterable<ItemStack> input, @Nullable ItemStack focusStack) {
		setItems(craftInputSlot1 + inputIndex, input, focusStack);
	}
}
