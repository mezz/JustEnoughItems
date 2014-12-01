package mezz.jei.plugins.forestry.centrifuge;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeGui;
import mezz.jei.api.recipe.IRecipeType;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;

public class CentrifugeRecipeType implements IRecipeType {

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public CentrifugeRecipeType() {
		ResourceLocation location = new ResourceLocation("forestry:textures/gui/centrifuge.png");
		background = JEIManager.guiHelper.makeDrawable(location, 29, 18, 122, 54);
		localizedName = StatCollector.translateToLocal("gui.jei.forestry.centrifugeRecipes");
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Nonnull
	@Override
	public String getLocalizedName() {
		return localizedName;
	}

	@Nonnull
	public IRecipeGui createGui() {
		return new CraftingRecipeGui(this);
	}

}
