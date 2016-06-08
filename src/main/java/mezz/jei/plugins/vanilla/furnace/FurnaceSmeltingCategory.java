package mezz.jei.plugins.vanilla.furnace;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class FurnaceSmeltingCategory extends FurnaceRecipeCategory<SmeltingRecipe> {
	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
		super(guiHelper);
		ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
		background = guiHelper.createDrawable(location, 55, 16, 82, 54);
		localizedName = Translator.translateToLocal("gui.jei.category.smelting");
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawAnimations(@Nonnull Minecraft minecraft) {
		flame.draw(minecraft, 2, 20);
		arrow.draw(minecraft, 24, 18);
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.SMELTING;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull SmeltingRecipe recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(inputSlot, true, 0, 0);
		guiItemStacks.init(outputSlot, false, 60, 18);

		guiItemStacks.setFromRecipe(inputSlot, recipeWrapper.getInputs());
		guiItemStacks.setFromRecipe(outputSlot, recipeWrapper.getOutputs());
	}
}
