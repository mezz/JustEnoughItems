package mezz.jei.api.gui;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Helps with the implementation of GUIs.
 * The instance is in JEIManager.
 */
public interface IGuiHelper {

	@Nonnull
	IGuiItemStacks makeGuiItemStacks();

	@Nonnull
	IDrawable makeDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height);

}
