package mezz.jei.input;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.LinkedList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;

import mezz.jei.Internal;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiTextFieldFilter extends EditBox {
	private static final int MAX_HISTORY = 100;
	private static final int maxSearchLength = 128;
	private static final List<String> history = new LinkedList<>();

	private final HoverChecker hoverChecker;
	private final IIngredientGridSource ingredientSource;
	private final IWorldConfig worldConfig;
	private final IMouseHandler mouseHandler;
	private boolean previousKeyboardRepeatEnabled;

	private final DrawableNineSliceTexture background;

	public GuiTextFieldFilter(IIngredientGridSource ingredientSource, IWorldConfig worldConfig) {
		// TODO narrator string
		super(Minecraft.getInstance().font, 0, 0, 0, 0, TextComponent.EMPTY);
		this.worldConfig = worldConfig;

		setMaxLength(maxSearchLength);
		this.hoverChecker = new HoverChecker();
		this.ingredientSource = ingredientSource;

		this.background = Internal.getTextures().getSearchBackground();
		this.mouseHandler = new MouseHandler();
	}

	public void updateBounds(Rect2i area) {
		this.x = area.getX();
		this.y = area.getY();
		this.width = area.getWidth();
		this.height = area.getHeight();
		this.hoverChecker.updateBounds(area.getY(), area.getY() + area.getHeight(), area.getX(), area.getX() + area.getWidth());
		setHighlightPos(getCursorPosition());
	}

	public void update() {
		String filterText = worldConfig.getFilterText();
		if (!filterText.equals(getValue())) {
			setValue(filterText);
		}
		List<IIngredientListElement<?>> ingredientList = ingredientSource.getIngredientList(filterText);
		if (ingredientList.size() == 0) {
			setTextColor(0xFFFF0000);
		} else {
			setTextColor(0xFFFFFFFF);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
		if (!handled && !history.isEmpty()) {
			if (keyCode == GLFW.GLFW_KEY_UP) {
				String currentText = getValue();
				int historyIndex = history.indexOf(currentText);
				if (historyIndex < 0) {
					if (saveHistory()) {
						historyIndex = history.size() - 1;
					} else {
						historyIndex = history.size();
					}
				}
				if (historyIndex > 0) {
					String historyString = history.get(historyIndex - 1);
					setValue(historyString);
					handled = true;
				}
			} else if (keyCode == GLFW.GLFW_KEY_DOWN) {
				String currentText = getValue();
				int historyIndex = history.indexOf(currentText);
				if (historyIndex >= 0) {
					String historyString;
					if (historyIndex + 1 < history.size()) {
						historyString = history.get(historyIndex + 1);
					} else {
						historyString = "";
					}
					setValue(historyString);
					handled = true;
				}
			} else if (KeyBindings.isEnterKey(keyCode)) {
				saveHistory();
			}
		}
		return handled;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return hoverChecker.checkHover(mouseX, mouseY);
	}

	public IMouseHandler getMouseHandler() {
		return mouseHandler;
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

			saveHistory();
		}
	}

	private boolean saveHistory() {
		String text = getValue();
		if (text.length() > 0) {
			history.remove(text);
			history.add(text);
			if (history.size() > MAX_HISTORY) {
				history.remove(0);
			}
			return true;
		}
		return false;
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

	@SuppressWarnings("deprecation")
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

	private class MouseHandler implements IMouseHandler {
		@Override
		public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
			if (!isMouseOver(mouseX, mouseY)) {
				return null;
			}
			if (mouseButton == 1) {
				if (!clickState.isSimulate()) {
					setValue("");
					worldConfig.setFilterText("");
				}
				return this;
			}
			if (!clickState.isSimulate()) {
				if (GuiTextFieldFilter.super.mouseClicked(mouseX, mouseY, mouseButton)) {
					return this;
				}
				return null;
			} else {
				return this; // can't easily simulate the click, just say we could handle it
			}
		}

		@Override
		public void handleMouseClickedOut(int mouseButton) {
			setFocused(false);
		}
	}
}
