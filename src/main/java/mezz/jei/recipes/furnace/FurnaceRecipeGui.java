package mezz.jei.recipes.furnace;

import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.gui.resource.DrawableResource;
import mezz.jei.gui.resource.IDrawable;
import mezz.jei.recipes.RecipeGui;
import net.minecraft.util.ResourceLocation;

public abstract class FurnaceRecipeGui extends RecipeGui {

	protected static final int inputSlot = 0;
	protected static final int fuelSlot = 1;
	protected static final int outputSlot = 2;

	private static IDrawable getBackground() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/furnace.png");
		IRecipeType type = RecipeType.FURNACE;
		return new DrawableResource(location, 55, 16, type.displayWidth(), type.displayHeight());
	}

	protected FurnaceRecipeGui() {
		super(getBackground());

		addItem(inputSlot, 0, 0);
		addItem(fuelSlot, 0, 36);
		addItem(outputSlot, 60, 18);
	}

}
