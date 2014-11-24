package mezz.jei.recipe.crafting;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeGui;
import mezz.jei.api.recipe.type.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;

public class CraftingRecipeType implements IRecipeType {

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public CraftingRecipeType() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/crafting_table.png");
		background = JEIManager.guiHelper.makeDrawable(location, 29, 16, 116, 54);
		localizedName = StatCollector.translateToLocal("gui.jei.craftingTableRecipes");
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
