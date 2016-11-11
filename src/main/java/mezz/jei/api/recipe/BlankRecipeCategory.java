package mezz.jei.api.recipe;

import javax.annotation.Nullable;

import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;

/**
 * An {@link IRecipeCategory} that does nothing, inherit from this to avoid implementing methods you don't need.
 */
public abstract class BlankRecipeCategory<T extends IRecipeWrapper> implements IRecipeCategory<T> {
	@Nullable
	@Override
	public IDrawable getIcon() {
		return null;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {

	}
}
