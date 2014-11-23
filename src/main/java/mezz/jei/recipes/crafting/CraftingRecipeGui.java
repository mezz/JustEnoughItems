package mezz.jei.recipes.crafting;

import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipes.IRecipeGuiHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.gui.resource.DrawableResource;
import mezz.jei.gui.resource.IDrawable;
import mezz.jei.util.StackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class CraftingRecipeGui implements IRecipeGuiHelper {

	private static final int craftOutputSlot = 0;
	private static final int craftInputSlot1 = 1;

	@Override
	public void initGuiItemStacks(@Nonnull IGuiItemStacks guiItemStacks) {
		guiItemStacks.initItemStack(craftOutputSlot, 94, 18);

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				int index = craftInputSlot1 + x + (y * 3);
				guiItemStacks.initItemStack(index, x * 18, y * 18);
			}
		}
	}

	@Nonnull
	public IDrawable getBackground() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/crafting_table.png");
		IRecipeType type = RecipeType.CRAFTING_TABLE;
		return new DrawableResource(location, 29, 16, type.displayWidth(), type.displayHeight());
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {

	}

	protected void setOutput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List<ItemStack> output, @Nullable ItemStack focusStack) {
		guiItemStacks.setItemStack(craftOutputSlot, output, focusStack);
	}

	protected void setInput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List input, @Nullable ItemStack focusStack) {
		int width, height;
		if (input.size() > 4)
			width = height = 3;
		else if (input.size() > 1)
			width = height = 2;
		else
			width = height = 1;

		setInput(guiItemStacks, input, focusStack, width, height);
	}

	protected void setInput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List input, @Nullable ItemStack focusStack, int width, int height) {
		for (int i = 0; i < input.size(); i++) {
			Object recipeItem = input.get(i);
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
					if (i > 3)
						index++;
				}
			} else if (height == 2) {
				index = i + 3;
			} else {
				index = i;
			}

			if (recipeItem instanceof ItemStack) {
				List<ItemStack> itemStacks = Collections.singletonList((ItemStack) recipeItem);
				setInput(guiItemStacks, index, itemStacks, focusStack);
			} else if (recipeItem instanceof Iterable) {
				List<ItemStack> itemStacks = StackUtil.toItemStackList((Iterable) recipeItem);
				setInput(guiItemStacks, index, itemStacks, focusStack);
			}
		}
	}

	protected void setInput(@Nonnull IGuiItemStacks guiItemStacks, int inputIndex, @Nonnull ItemStack itemStack, @Nullable ItemStack focusStack) {
		guiItemStacks.setItemStack(craftInputSlot1 + inputIndex, itemStack, focusStack);
	}

	protected void setInput(@Nonnull IGuiItemStacks guiItemStacks, int inputIndex, @Nonnull Iterable<ItemStack> input, @Nullable ItemStack focusStack) {
		guiItemStacks.setItemStack(craftInputSlot1 + inputIndex, input, focusStack);
	}
}
