package mezz.jei.gui.resource;

import mezz.jei.api.recipes.RecipeType;
import mezz.jei.config.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/** Helps working with the compound texture recipes.png */
public class DrawableRecipePng implements IDrawable {

	private DrawableResource resource;

	public DrawableRecipePng(@Nonnull RecipeType recipeType) {
		int u = 0;
		int v = 0;

		switch (recipeType) {
			case FURNACE:
				v += RecipeType.CRAFTING_TABLE.displayHeight();
			case CRAFTING_TABLE:
				break;
			default:
				throw new IllegalArgumentException("Gui is not part of recipes.png: " + recipeType);
		}

		ResourceLocation resourceLocation = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipes.png");
		this.resource = new DrawableResource(resourceLocation, u, v, recipeType.displayWidth(), recipeType.displayHeight());
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int x, int y) {
		resource.draw(minecraft, x, y);
	}
}
