package mezz.jei.recipe.crafting;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipe.IRecipeGui;
import mezz.jei.api.recipe.type.IRecipeType;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.util.StackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CraftingRecipeGui implements IRecipeGui {

	private static final int craftOutputSlot = 0;
	private static final int craftInputSlot1 = 1;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final IGuiItemStacks guiItemStacks;
	@Nullable
	private IRecipeWrapper recipeWrapper;

	public CraftingRecipeGui(@Nonnull IRecipeType recipeType) {
		background = recipeType.getBackground();

		guiItemStacks = JEIManager.guiHelper.makeGuiItemStacks();
		guiItemStacks.initItemStack(craftOutputSlot, 94, 18);

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				int index = craftInputSlot1 + x + (y * 3);
				guiItemStacks.initItemStack(index, x * 18, y * 18);
			}
		}
	}

	@Override
	public void setRecipe(@Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack) {
		this.recipeWrapper = recipeWrapper;
		guiItemStacks.clearItemStacks();
		if (recipeWrapper instanceof IShapedCraftingRecipeWrapper) {
			IShapedCraftingRecipeWrapper wrapper = (IShapedCraftingRecipeWrapper)recipeWrapper;
			setInput(wrapper.getInputs(), focusStack, wrapper.getWidth(), wrapper.getHeight());
			setOutput(wrapper.getOutputs(), focusStack);
		} else if (recipeWrapper instanceof ICraftingRecipeWrapper) {
			ICraftingRecipeWrapper wrapper = (ICraftingRecipeWrapper)recipeWrapper;
			setInput(wrapper.getInputs(), focusStack);
			setOutput(wrapper.getOutputs(), focusStack);
		}
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (recipeWrapper == null)
			return;

		background.draw(minecraft);
		recipeWrapper.drawInfo(minecraft, mouseX, mouseY);
		guiItemStacks.draw(minecraft, mouseX, mouseY);
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse(int mouseX, int mouseY) {
		return guiItemStacks.getStackUnderMouse(mouseX, mouseY);
	}

	private void setOutput(@Nonnull List<ItemStack> output, @Nullable ItemStack focusStack) {
		guiItemStacks.setItemStack(craftOutputSlot, output, focusStack);
	}

	private void setInput(@Nonnull List input, @Nullable ItemStack focusStack) {
		int width, height;
		if (input.size() > 4)
			width = height = 3;
		else if (input.size() > 1)
			width = height = 2;
		else
			width = height = 1;

		setInput(input, focusStack, width, height);
	}

	private void setInput(@Nonnull List input, @Nullable ItemStack focusStack, int width, int height) {
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
				setInput(index, itemStacks, focusStack);
			} else if (recipeItem instanceof Iterable) {
				List<ItemStack> itemStacks = StackUtil.toItemStackList((Iterable) recipeItem);
				setInput(index, itemStacks, focusStack);
			}
		}
	}

	private void setInput(int inputIndex, @Nonnull Iterable<ItemStack> input, @Nullable ItemStack focusStack) {
		guiItemStacks.setItemStack(craftInputSlot1 + inputIndex, input, focusStack);
	}

}
