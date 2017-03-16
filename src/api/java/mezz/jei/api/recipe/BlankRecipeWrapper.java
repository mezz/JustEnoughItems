package mezz.jei.api.recipe;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

/**
 * An {@link IRecipeWrapper} that does nothing, inherit from this to avoid implementing methods you don't need.
 */
public abstract class BlankRecipeWrapper implements IRecipeWrapper {
	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		// override to draw extra info about the recipe
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		return Collections.emptyList();
	}

	@Override
	public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
		return false;
	}
}
