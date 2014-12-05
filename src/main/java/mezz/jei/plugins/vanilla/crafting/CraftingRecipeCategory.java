package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class CraftingRecipeCategory implements IRecipeCategory {

	private static final int craftOutputSlot = 0;
	private static final int craftInputSlot1 = 1;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public CraftingRecipeCategory() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/crafting_table.png");
		background = JEIManager.guiHelper.createDrawable(location, 29, 16, 116, 54);
		localizedName = StatCollector.translateToLocal("gui.jei.craftingTableRecipes");
	}

	@Nonnull
	@Override
	public String getCategoryTitle() {
		return localizedName;
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void init(IGuiItemStacks guiItemStacks) {
		guiItemStacks.initItemStack(craftOutputSlot, 94, 18);

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				int index = craftInputSlot1 + x + (y * 3);
				guiItemStacks.initItemStack(index, x * 18, y * 18);
			}
		}
	}

	@Override
	public void setRecipe(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull IRecipeWrapper recipeWrapper) {
		if (recipeWrapper instanceof IShapedCraftingRecipeWrapper) {
			IShapedCraftingRecipeWrapper wrapper = (IShapedCraftingRecipeWrapper)recipeWrapper;
			setInput(guiItemStacks, wrapper.getInputs(), wrapper.getWidth(), wrapper.getHeight());
			setOutput(guiItemStacks, wrapper.getOutputs());
		} else if (recipeWrapper instanceof ICraftingRecipeWrapper) {
			ICraftingRecipeWrapper wrapper = (ICraftingRecipeWrapper)recipeWrapper;
			setInput(guiItemStacks, wrapper.getInputs());
			setOutput(guiItemStacks, wrapper.getOutputs());
		}
	}

	private void setOutput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List<ItemStack> output) {
		guiItemStacks.setItemStack(craftOutputSlot, output);
	}

	private void setInput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List input) {
		int width, height;
		if (input.size() > 4)
			width = height = 3;
		else if (input.size() > 1)
			width = height = 2;
		else
			width = height = 1;

		setInput(guiItemStacks, input, width, height);
	}

	private void setInput(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull List input, int width, int height) {
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
				setInput(guiItemStacks, index, itemStacks);
			} else if (recipeItem instanceof Iterable) {
				List<ItemStack> itemStacks = StackUtil.toItemStackList((Iterable) recipeItem);
				setInput(guiItemStacks, index, itemStacks);
			}
		}
	}

	private void setInput(@Nonnull IGuiItemStacks guiItemStacks, int inputIndex, @Nonnull Iterable<ItemStack> input) {
		guiItemStacks.setItemStack(craftInputSlot1 + inputIndex, input);
	}

}
