package mezz.jei.api;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;

/**
 * Helps with the implementation of GUIs.
 */
public interface IGuiHelper {

	@Nonnull
	IDrawable createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height);

	@Nonnull
	ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot);

}
