package mezz.jei.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public class GuiTextFieldFilter extends EditBox {
	private static final int maxSearchLength = 128;
	private static final TextHistory history = new TextHistory();

	private final HoverChecker hoverChecker;
	private final IIngredientGridSource ingredientSource;
	private final DrawableNineSliceTexture background;

	private boolean previousKeyboardRepeatEnabled;

	public GuiTextFieldFilter(IIngredientGridSource ingredientSource) {
		// TODO narrator string
		super(Minecraft.getInstance().font, 0, 0, 0, 0, TextComponent.EMPTY);

		setMaxLength(maxSearchLength);
		this.hoverChecker = new HoverChecker();
		this.ingredientSource = ingredientSource;

		this.background = Internal.getTextures().getSearchBackground();
	}

	public void updateBounds(Rect2i area) {
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
		List<IIngredientListElement<?>> ingredientList = ingredientSource.getIngredientList(filterText);
		if (ingredientList.size() == 0) {
			setTextColor(0xFFFF0000);
		} else {
			setTextColor(0xFFFFFFFF);
		}
	}

	private boolean navigateHistory(UserInput userInput) {
		final String currentText = getValue();
		String newText = null;
		if (userInput.is(KeyBindings.previousSearch)) {
			newText = history.getPrevious(currentText);
		} else if (userInput.is(KeyBindings.nextSearch)) {
			newText = history.getNext(currentText);
		}

		if (newText != null) {
			if (!userInput.isSimulate()) {
				setValue(newText);
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return hoverChecker.checkHover(mouseX, mouseY);
	}

	public IUserInputHandler createInputHandler() {
		return new UserInputHandler();
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

	private class UserInputHandler implements IUserInputHandler {
		@Override
		public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
			if (isFocused()) {
				if (input.isEscapeKey() || input.isEnterKey()) {
					setFocused(false);
					return this;
				}
			}

			if (input.is(KeyBindings.focusSearch)) {
				if (!input.isSimulate()) {
					setFocused(true);
				}
				return this;
			}

			if (isMouseOver(input.getMouseX(), input.getMouseY())) {
				if (input.is(KeyBindings.hoveredClearSearchBar)) {
					if (!input.isSimulate()) {
						setValue("");
						setFocused(true);
					}
					return this;
				}
			}

			if (input.callVanilla(
				GuiTextFieldFilter.this::isMouseOver,
				GuiTextFieldFilter.this::mouseClicked,
				GuiTextFieldFilter.this::keyPressed
			)) {
				return this;
			}

			if (navigateHistory(input)) {
				return this;
			}

			// If we can handle this input as a typed char,
			// treat it as handled to prevent other handlers from using it.
			if (canConsumeInput() && input.isAllowedChatCharacter()) {
				return this;
			}

			return null;
		}

		@Override
		public void handleMouseClickedOut(InputConstants.Key input) {
			setFocused(false);
		}
	}
}
