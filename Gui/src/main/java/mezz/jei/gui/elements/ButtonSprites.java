package mezz.jei.gui.elements;

import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.client.gui.GuiGraphics;

public final class ButtonSprites {
	private final IScalableDrawable disabled;
	private final IScalableDrawable enabled;
	private final IScalableDrawable highlighted;
	private final IScalableDrawable pressed;
	private final IScalableDrawable pressedFocused;

	public ButtonSprites() {
		Textures textures = Internal.getTextures();
		this.disabled = textures.getButtonDisabled();
		this.enabled = textures.getButtonEnabled();
		this.highlighted = textures.getButtonHighlight();
		this.pressed = textures.getButtonPressed();
		this.pressedFocused = textures.getButtonPressedHighlight();
	}

	public void render(GuiGraphics guiGraphics, boolean enabled, boolean focused, boolean pressed, int x, int y, int width, int height) {
		if (pressed) {
			if (focused) {
				this.pressedFocused.draw(guiGraphics, x, y, width, height);
			} else {
				this.pressed.draw(guiGraphics, x, y, width, height);
			}
		} else if (enabled && focused) {
			this.highlighted.draw(guiGraphics, x, y, width, height);
		} else if (enabled) {
			this.enabled.draw(guiGraphics, x, y, width, height);
		} else {
			this.disabled.draw(guiGraphics, x, y, width, height);
		}
	}
}
