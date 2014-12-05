package mezz.jei.gui;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class GuiHelper implements IGuiHelper {

	@Nonnull
	@Override
	public IDrawable createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height) {
		return new DrawableResource(resourceLocation, u, v, width, height);
	}

	@Nonnull
	@Override
	public ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot) {
		return new CraftingGridHelper(craftInputSlot1, craftOutputSlot);
	}

}
