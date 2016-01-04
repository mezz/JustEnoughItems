package mezz.jei.plugins.jei.debug;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.Constants;

public class DebugRecipeCategory implements IRecipeCategory {
	public static final int recipeWidth = 160;
	public static final int recipeHeight = 60;
	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;
	@Nonnull
	private final IDrawable tankBackground;
	@Nonnull
	private final IDrawable tankOverlay;

	public DebugRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		localizedName = "debug";

		ResourceLocation backgroundTexture = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");
		tankBackground = guiHelper.createDrawable(backgroundTexture, 176, 0, 20, 55);
		tankOverlay = guiHelper.createDrawable(backgroundTexture, 176, 55, 12, 47);
	}

	@Nonnull
	@Override
	public String getUid() {
		return "debug";
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		tankBackground.draw(minecraft);
	}

	@Override
	public void drawAnimations(Minecraft minecraft) {

	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(0, false, 70, 0);
		guiItemStacks.set(0, new ItemStack(Items.water_bucket));

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 4, 4, 12, 47, 2000, true, tankOverlay);
		guiFluidStacks.init(1, true, 24, 0, 12, 47, 16000, true, null);
		guiFluidStacks.init(2, true, 50, 0, 24, 24, 2000, true, tankOverlay);
		guiFluidStacks.init(3, true, 90, 0, 12, 47, 100, false, tankOverlay);

		List<FluidStack> fluidInputs = recipeWrapper.getFluidInputs();
		guiFluidStacks.set(0, fluidInputs.get(0));
		guiFluidStacks.set(1, fluidInputs.get(1));
		guiFluidStacks.set(3, fluidInputs.get(0));
	}
}
