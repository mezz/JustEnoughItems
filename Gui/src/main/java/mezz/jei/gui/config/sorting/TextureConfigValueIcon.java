package mezz.jei.gui.config.sorting;

import net.mezzdev.config.gui.api.IConfigValueIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

record TextureConfigValueIcon(ResourceLocation textureLocation, int textureWidth, int textureHeight) implements IConfigValueIcon {
	@Override
	public void draw(GuiGraphics guiGraphics, Rect2i area) {
		int size = Math.min(area.getWidth(), area.getHeight());
		int x = area.getX() + Math.round((area.getWidth() - size) / 2.0f);
		int y = area.getY() + Math.round((area.getHeight() - size) / 2.0f);
		guiGraphics.blit(
			textureLocation,
			x,
			y,
			size,
			size,
			0,
			0,
			textureWidth,
			textureHeight,
			textureWidth,
			textureHeight
		);
	}
}
