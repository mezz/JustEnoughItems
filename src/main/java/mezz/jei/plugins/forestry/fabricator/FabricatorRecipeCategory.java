package mezz.jei.plugins.forestry.fabricator;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.plugins.forestry.crafting.ForestryShapedRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;
import java.util.List;

public class FabricatorRecipeCategory implements IRecipeCategory {

	private static final short SLOT_METAL = 0;
	private static final short SLOT_PLAN = 1;
	private static final short SLOT_RESULT = 2;
	private static final short SLOT_CRAFTING_1 = 3;

	@Nonnull private final IDrawable background;
	@Nonnull private final String categoryTitle;
	@Nonnull private final ICraftingGridHelper craftingGridHelper;

	public FabricatorRecipeCategory() {
		ResourceLocation location = new ResourceLocation("forestry:textures/gui/fabricator.png");
		background = JEIManager.guiHelper.createDrawable(location, 20, 16, 136, 54);
		categoryTitle = StatCollector.translateToLocal("gui.jei.forestry.fabricatorRecipes");
		craftingGridHelper = JEIManager.guiHelper.createCraftingGridHelper(SLOT_CRAFTING_1, SLOT_RESULT);
	}

	@Nonnull
	@Override
	public String getCategoryTitle() {
		return categoryTitle;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void init(IGuiItemStacks guiItemStacks) {
		// Molten resource
		guiItemStacks.initItemStack(SLOT_METAL, 5, 4);

		// Plan
		guiItemStacks.initItemStack(SLOT_PLAN, 118, 0);

		// Result
		guiItemStacks.initItemStack(SLOT_RESULT, 118, 36);

		// Crafting matrix
		for (int l = 0; l < 3; l++)
			for (int k = 0; k < 3; k++)
				guiItemStacks.initItemStack(SLOT_CRAFTING_1 + k + l * 3, 46 + k * 18, l * 18);
	}

	@Override
	public void setRecipe(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull IRecipeWrapper recipeWrapper) {
		if (recipeWrapper instanceof FabricatorCraftingRecipeWrapper) {
			FabricatorCraftingRecipeWrapper wrapper = (FabricatorCraftingRecipeWrapper) recipeWrapper;
			ForestryShapedRecipeWrapper internal = wrapper.getInternal();
			craftingGridHelper.setInput(guiItemStacks, internal.getInputs(), internal.getWidth(), internal.getHeight());
			craftingGridHelper.setOutput(guiItemStacks, internal.getOutputs());

			ItemStack plan = wrapper.getPlan();
			if (plan != null) {
				guiItemStacks.setItemStack(SLOT_PLAN, plan);
			}
		} else if (recipeWrapper instanceof FabricatorSmeltingRecipeWrapper) {
			FabricatorSmeltingRecipeWrapper wrapper = (FabricatorSmeltingRecipeWrapper) recipeWrapper;
			List<ItemStack> inputs = wrapper.getInputs();
			guiItemStacks.setItemStack(SLOT_METAL, inputs);
			// TODO liquid amount, power required
		}
	}

}
