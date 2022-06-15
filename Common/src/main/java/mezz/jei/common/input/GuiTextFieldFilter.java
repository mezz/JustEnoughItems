package mezz.jei.common.input;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.common.gui.HoverChecker;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.handlers.TextFieldInputHandler;
import mezz.jei.common.platform.IPlatformInputHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.core.util.TextHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

import java.util.Optional;

public class GuiTextFieldFilter extends EditBox {
	private static final int maxSearchLength = 128;
	private static final TextHistory history = new TextHistory();

	private final HoverChecker hoverChecker;
	private final DrawableNineSliceTexture background;
	private ImmutableRect2i backgroundBounds;

	private boolean previousKeyboardRepeatEnabled;

	public GuiTextFieldFilter(Textures textures) {
		// TODO narrator string
		super(Minecraft.getInstance().font, 0, 0, 0, 0, TextComponent.EMPTY);

		setMaxLength(maxSearchLength);
		this.hoverChecker = new HoverChecker();

		this.background = textures.getSearchBackground();
		this.backgroundBounds = ImmutableRect2i.EMPTY;
		setBordered(false);
	}

	public void updateBounds(ImmutableRect2i area) {
		this.backgroundBounds = area;
		this.x = area.getX() + 4;
		this.y = area.getY() + (area.getHeight() - 8) / 2;
		this.width = area.getWidth() - 12;
		this.height = area.getHeight();
		this.hoverChecker.updateBounds(area);
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
				IPlatformInputHelper inputHelper = Services.PLATFORM.getInputHelper();
				previousKeyboardRepeatEnabled = inputHelper.isSendRepeatsToGui(minecraft.keyboardHandler);
				minecraft.keyboardHandler.setSendRepeatsToGui(true);
			} else {
				minecraft.keyboardHandler.setSendRepeatsToGui(previousKeyboardRepeatEnabled);
			}

			String text = getValue();
			history.add(text);
		}
	}

	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (this.isVisible()) {
			RenderSystem.setShaderColor(1, 1, 1, 1);
			background.draw(poseStack, this.backgroundBounds);
		}
		super.renderButton(poseStack, mouseX, mouseY, partialTicks);
	}

}
