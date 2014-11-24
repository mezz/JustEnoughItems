package mezz.jei.gui;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStacks;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class GuiHelper implements IGuiHelper {

	@Nonnull
	@Override
	public IGuiItemStacks makeGuiItemStacks() {
		return new GuiItemStacks();
	}

	@Nonnull
	@Override
	public IDrawable makeDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height) {
		return new DrawableResource(resourceLocation, u, v, width, height);
	}

}
