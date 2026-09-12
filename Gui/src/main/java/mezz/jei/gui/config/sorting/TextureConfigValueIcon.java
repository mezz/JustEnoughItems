package mezz.jei.gui.config.sorting;

import net.mezzdev.config.gui.api.IConfigValueIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

record TextureConfigValueIcon(Identifier textureLocation, int textureWidth, int textureHeight) implements IConfigValueIcon {
	@Override
	public void draw(GuiGraphics guiGraphics, Rect2i area) {
		int size = Math.min(area.getWidth(), area.getHeight());
		int x = area.getX() + Math.round((area.getWidth() - size) / 2.0f);
		int y = area.getY() + Math.round((area.getHeight() - size) / 2.0f);
		guiGraphics.blit(
			RenderPipelines.GUI_TEXTURED,
			textureLocation,
			x,
			y,
			0,
			0,
			size,
			size,
			textureWidth,
			textureHeight,
			textureWidth,
			textureHeight
		);
	}
}
