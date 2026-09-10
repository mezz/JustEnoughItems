package mezz.jei.gui.config.sorting;

import mezz.jei.api.gui.drawable.IDrawable;
import net.mezzdev.config.gui.api.IConfigValueIcon;
import net.mezzdev.config.gui.api.LegacyGuiGraphics;
import net.minecraft.client.renderer.Rect2i;

record DrawableConfigValueIcon(IDrawable drawable) implements IConfigValueIcon {
	@Override
	public void draw(LegacyGuiGraphics guiGraphics, Rect2i area) {
		int x = area.getX() + Math.round((area.getWidth() - drawable.getWidth()) / 2.0f);
		int y = area.getY() + Math.round((area.getHeight() - drawable.getHeight()) / 2.0f);
		drawable.draw(guiGraphics.pose(), x, y);
	}
}
