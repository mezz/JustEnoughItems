package mezz.jei.recipes.crafting;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.gui.resource.DrawableResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;

public class RecipeTypeCrafting implements IRecipeType {

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public RecipeTypeCrafting() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/crafting_table.png");
		this.background = new DrawableResource(location, 29, 16, 116, 54);
		this.localizedName = StatCollector.translateToLocal("gui.jei.craftingTableRecipes");
	}

	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Nonnull
	@Override
	public String getLocalizedName() {
		return localizedName;
	}

}
