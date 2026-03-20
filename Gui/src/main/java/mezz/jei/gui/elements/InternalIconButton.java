package mezz.jei.gui.elements;

import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.ARGB;

/**
 * A button that has an {@link IDrawable} instead of a string label.
 * This internal class is used for re-using vanilla render code and to override behavior.
 * See {@link IconButton} for the class that uses this.
 */
class InternalIconButton extends Button implements IButtonState {
	private static final ButtonSprites SPRITES = new ButtonSprites();

	private IDrawable icon = DrawableBlank.EMPTY;
	private boolean pressed = false;
	private boolean forcePressed = false;

	public InternalIconButton() {
		super(0, 0, 0, 0, CommonComponents.EMPTY, b -> {}, Button.DEFAULT_NARRATION);
	}

	public void updateBounds(ImmutableRect2i area) {
		setX(area.getX());
		setY(area.getY());
		this.width = area.getWidth();
		this.height = area.getHeight();
	}

	@Override
	public void setHeight(int value) {
		this.height = value;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		SPRITES.render(
			guiGraphics,
			this.active,
			this.isHoveredOrFocused(),
			this.pressed || this.forcePressed,
			this.getX(),
			this.getY(),
			this.getWidth(),
			this.getHeight(),
			ARGB.white(this.alpha)
		);

		float xOffset = getX() + (width - icon.getWidth()) / 2.0f;
		float yOffset = getY() + (height - icon.getHeight()) / 2.0f;
		if (this.pressed || this.forcePressed) {
			xOffset += 0.5f;
			yOffset += 0.5f;
		}
		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(xOffset, yOffset);
			icon.draw(guiGraphics);
		}
		poseStack.popMatrix();
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	@Override
	public void setForcePressed(boolean forcePressed) {
		this.forcePressed = forcePressed;
	}

	@Override
	public boolean isValidClickButton(MouseButtonInfo p_447020_) {
		return super.isValidClickButton(p_447020_);
	}

	@Override
	public void setIcon(IDrawable icon) {
		this.icon = icon;
	}

	@Override
	public void setActive(boolean value) {
		this.active = value;
	}

	@Override
	public void setVisible(boolean value) {
		this.visible = value;
	}
}
