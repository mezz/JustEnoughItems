package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class FurnaceFuelCategory extends FurnaceRecipeCategory {
	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public FurnaceFuelCategory() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/furnace.png");
		background = JEIManager.guiHelper.createDrawable(location, 55, 38, 18, 32, 0, 0, 0, 80);
		localizedName = StatCollector.translateToLocal("gui.jei.fuelRecipes");
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Nonnull
	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public void init(@Nonnull IRecipeLayout recipeLayout) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(fuelSlot, true, 0, 14);
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.setFromRecipe(fuelSlot, recipeWrapper.getInputs());
	}
}
