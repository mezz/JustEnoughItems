package mezz.jei.input;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.TextFieldInputHandler;
import mezz.jei.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

import java.util.Optional;

public class GuiTextFieldFilter extends EditBox {
	private static final int maxSearchLength = 128;
	private static final TextHistory history = new TextHistory();

	private final HoverChecker hoverChecker;
	private final DrawableNineSliceTexture background;

	private boolean previousKeyboardRepeatEnabled;

	public GuiTextFieldFilter() {
		// TODO narrator string
		super(Minecraft.getInstance().font, 0, 0, 0, 0, TextComponent.EMPTY);

		setMaxLength(maxSearchLength);
		this.hoverChecker = new HoverChecker();

		this.background = Internal.getTextures().getSearchBackground();
	}

	public void updateBounds(ImmutableRect2i area) {
		this.x = area.getX();
		this.y = area.getY();
		this.width = area.getWidth();
		this.height = area.getHeight();
		this.hoverChecker.updateBounds(area.getY(), area.getY() + area.getHeight(), area.getX(), area.getX() + area.getWidth());
		setHighlightPos(getCursorPosition());
	}

	@Override
	public void setValue(String filterText) {
		if (!filterText.equals(getValue())) {
			super.setValue(filterText);
		}
	}

	public Optional<String> getHistory(TextHistory.Direction direction) {
		String currentText = getValue();
		return history.get(direction, currentText);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return hoverChecker.checkHover(mouseX, mouseY);
	}

	public IUserInputHandler createInputHandler() {
		return new TextFieldInputHandler(this);
	}

	@Override
	public void setFocused(boolean keyboardFocus) {
		final boolean previousFocus = isFocused();
		super.setFocused(keyboardFocus);

		if (previousFocus != keyboardFocus) {
			Minecraft minecraft = Minecraft.getInstance();
			if (keyboardFocus) {
				previousKeyboardRepeatEnabled = minecraft.keyboardHandler.sendRepeatsToGui;
				minecraft.keyboardHandler.setSendRepeatsToGui(true);
			} else {
				minecraft.keyboardHandler.setSendRepeatsToGui(previousKeyboardRepeatEnabled);
			}

			String text = getValue();
			history.add(text);
		}
	}

	// begin hack to draw our own background texture instead of the default one
	private boolean isDrawing = false;

	@Override
	protected boolean isBordered() {
		if (this.isDrawing) {
			return false;
		}
		return super.isBordered();
	}

	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		this.isDrawing = true;
		if (this.isVisible()) {
			RenderSystem.setShaderColor(1, 1, 1, 1);
			background.draw(poseStack, this.x, this.y, this.width, this.height);
		}
		super.renderButton(poseStack, mouseX, mouseY, partialTicks);
		this.isDrawing = false;
	}
	// end background hack

}
