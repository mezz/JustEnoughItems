package mezz.jei.api;

import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Helps with the implementation of GUIs.
 */
public interface IGuiHelper {

	@Nonnull IDrawable createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height);
	@Nonnull ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot);

}
