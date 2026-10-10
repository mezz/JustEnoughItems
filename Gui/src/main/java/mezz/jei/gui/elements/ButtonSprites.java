package mezz.jei.gui.elements;

import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.client.gui.GuiGraphics;

public final class ButtonSprites {
	private final Textures textures;

	public ButtonSprites() {
		this.textures = Internal.getTextures();
	}

	public void render(GuiGraphics guiGraphics, boolean enabled, boolean focused, boolean pressed, int x, int y, int width, int height) {
		textures.getButtonBackground(enabled, focused, pressed).draw(guiGraphics, x, y, width, height);
	}
}
