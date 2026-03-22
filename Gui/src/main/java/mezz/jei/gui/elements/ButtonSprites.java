package mezz.jei.gui.elements;

import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class ButtonSprites {
	private final WidgetSprites widgetSprites;
	private final IScalableDrawable pressed;
	private final IScalableDrawable pressedFocused;

	public ButtonSprites() {
		this.widgetSprites = new WidgetSprites(
			Identifier.withDefaultNamespace("widget/button"),
			Identifier.withDefaultNamespace("widget/button_disabled"),
			Identifier.withDefaultNamespace("widget/button_highlighted")
		);
		Textures textures = Internal.getTextures();
		this.pressed = textures.getButtonPressed();
		this.pressedFocused = textures.getButtonPressedHighlight();
	}

	public void render(GuiGraphicsExtractor guiGraphics, boolean enabled, boolean focused, boolean pressed, int x, int y, int width, int height, int alpha) {
		if (pressed) {
			if (focused) {
				this.pressedFocused.draw(guiGraphics, x, y, width, height);
			} else {
				this.pressed.draw(guiGraphics, x, y, width, height);
			}
		} else {
			Identifier spriteLocation = widgetSprites.get(enabled, focused);
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteLocation, x, y, width, height, ARGB.white(alpha));
		}
	}
}
