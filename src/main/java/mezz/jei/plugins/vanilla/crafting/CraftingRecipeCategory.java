package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;

public class CraftingRecipeCategory implements IRecipeCategory {

	private static final int craftOutputSlot = 0;
	private static final int craftInputSlot1 = 1;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;
	@Nonnull
	private final ICraftingGridHelper craftingGridHelper;

	public CraftingRecipeCategory() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/crafting_table.png");
		background = JEIManager.guiHelper.createDrawable(location, 29, 16, 116, 54);
		localizedName = StatCollector.translateToLocal("gui.jei.craftingTableRecipes");
		craftingGridHelper = JEIManager.guiHelper.createCraftingGridHelper(craftInputSlot1, craftOutputSlot);
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
	public void init(@Nonnull IGuiItemStacks guiItemStacks) {
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
			IShapedCraftingRecipeWrapper wrapper = (IShapedCraftingRecipeWrapper) recipeWrapper;
			craftingGridHelper.setInput(guiItemStacks, wrapper.getInputs(), wrapper.getWidth(), wrapper.getHeight());
			craftingGridHelper.setOutput(guiItemStacks, wrapper.getOutputs());
		} else if (recipeWrapper instanceof ICraftingRecipeWrapper) {
			ICraftingRecipeWrapper wrapper = (ICraftingRecipeWrapper) recipeWrapper;
			craftingGridHelper.setInput(guiItemStacks, wrapper.getInputs());
			craftingGridHelper.setOutput(guiItemStacks, wrapper.getOutputs());
		}
	}


}
