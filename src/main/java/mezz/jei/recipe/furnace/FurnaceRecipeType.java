package mezz.jei.recipe.furnace;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeGui;
import mezz.jei.api.recipe.type.IRecipeType;
import mezz.jei.gui.resource.DrawableResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;

public class FurnaceRecipeType implements IRecipeType {

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public FurnaceRecipeType() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/furnace.png");
		background = new DrawableResource(location, 55, 16, 82, 54);
		localizedName = StatCollector.translateToLocal("gui.jei.furnaceRecipes");
	}

	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Nonnull
	@Override
	public IRecipeGui createGui() {
		return new FurnaceRecipeGui(this);
	}

	@Nonnull
	@Override
	public String getLocalizedName() {
		return localizedName;
	}
}
