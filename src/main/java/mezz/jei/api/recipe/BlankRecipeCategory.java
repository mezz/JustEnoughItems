package mezz.jei.api.recipe;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public abstract class BlankRecipeCategory<T extends IRecipeWrapper> implements IRecipeCategory<T> {
	@Override
	public void drawExtras(@Nonnull Minecraft minecraft) {

	}

	@Override
	public void drawAnimations(@Nonnull Minecraft minecraft) {

	}
}
