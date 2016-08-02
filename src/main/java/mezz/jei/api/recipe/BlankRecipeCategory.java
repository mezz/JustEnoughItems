package mezz.jei.api.recipe;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

/**
 * An {@link IRecipeCategory} that does nothing, inherit from this to avoid implementing methods you don't need.
 */
public abstract class BlankRecipeCategory<T extends IRecipeWrapper> implements IRecipeCategory<T> {
	@Override
	public void drawExtras(@Nonnull Minecraft minecraft) {

	}

	@Override
	public void drawAnimations(@Nonnull Minecraft minecraft) {

	}
}
