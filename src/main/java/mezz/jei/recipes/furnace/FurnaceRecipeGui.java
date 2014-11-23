package mezz.jei.recipes.furnace;

import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipes.IRecipeGuiHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.gui.resource.DrawableResource;
import mezz.jei.gui.resource.IDrawable;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public abstract class FurnaceRecipeGui implements IRecipeGuiHelper {

	protected static final int inputSlot = 0;
	protected static final int fuelSlot = 1;
	protected static final int outputSlot = 2;

	@Override
	@Nonnull
	public IDrawable getBackground() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/furnace.png");
		IRecipeType type = RecipeType.FURNACE;
		return new DrawableResource(location, 55, 16, type.displayWidth(), type.displayHeight());
	}

	@Override
	public void initGuiItemStacks(@Nonnull IGuiItemStacks guiItemStacks) {
		guiItemStacks.initItemStack(inputSlot, 0, 0);
		guiItemStacks.initItemStack(fuelSlot, 0, 36);
		guiItemStacks.initItemStack(outputSlot, 60, 18);
	}

}
