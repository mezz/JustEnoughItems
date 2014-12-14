package mezz.jei.plugins.forestry.centrifuge;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

public class CentrifugeRecipeCategory implements IRecipeCategory {

	private static final int inputSlot = 0;
	private static final int outputSlot1 = 1;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public CentrifugeRecipeCategory() {
		ResourceLocation location = new ResourceLocation("forestry:textures/gui/centrifuge.png");
		background = JEIManager.guiHelper.createDrawable(location, 29, 18, 122, 54);
		localizedName = StatCollector.translateToLocal("gui.jei.forestry.centrifugeRecipes");
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
		// Resource
		guiItemStacks.initItemStack(inputSlot, 4, 18);

		// Product Inventory
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				guiItemStacks.initItemStack(outputSlot1 + x + (y * 3), 68 + x * 18, y * 18);
	}

	@Override
	public void setRecipe(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull IRecipeWrapper recipeWrapper) {
		CentrifugeRecipeWrapper wrapper = (CentrifugeRecipeWrapper)recipeWrapper;

		guiItemStacks.setItemStack(inputSlot, wrapper.getInputs());

		List<ItemStack> outputs = wrapper.getOutputs();
		for (int i = 0; i < outputs.size(); i++) {
			guiItemStacks.setItemStack(outputSlot1 + i, outputs.get(i));
		}
	}

}
