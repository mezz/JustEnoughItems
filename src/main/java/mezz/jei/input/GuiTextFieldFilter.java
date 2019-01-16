package mezz.jei.input;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;

import mezz.jei.Internal;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import org.lwjgl.input.Keyboard;

public class GuiTextFieldFilter extends GuiTextField {
	private static final int MAX_HISTORY = 100;
	private static final int maxSearchLength = 128;
	private static final List<String> history = new LinkedList<>();

	private final HoverChecker hoverChecker;
	private final IngredientFilter ingredientFilter;
	private boolean previousKeyboardRepeatEnabled;

	private final DrawableNineSliceTexture background;

	public GuiTextFieldFilter(int componentId, IngredientFilter ingredientFilter) {
		super(componentId, Minecraft.getMinecraft().fontRenderer, 0, 0, 0, 0);

		setMaxStringLength(maxSearchLength);
		this.hoverChecker = new HoverChecker(0, 0, 0, 0, 0);
		this.ingredientFilter = ingredientFilter;

		this.background = Internal.getHelpers().getGuiHelper().getSearchBackground();
	}

	public void updateBounds(Rectangle area) {
		this.x = area.x;
		this.y = area.y;
		this.width = area.width;
		this.height = area.height;
		this.hoverChecker.updateBounds(area.y, area.y + area.height, area.x, area.x + area.width);
		setSelectionPos(getCursorPosition());
	}

	public void update() {
		String filterText = Config.getFilterText();
		if (!filterText.equals(getText())) {
			setText(filterText);
		}
		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		if (ingredientList.size() == 0) {
			setTextColor(Color.red.getRGB());
		} else {
			setTextColor(Color.white.getRGB());
		}
	}

	@Override
	public boolean textboxKeyTyped(char typedChar, int keyCode) {
		boolean handled = super.textboxKeyTyped(typedChar, keyCode);
		if (!handled && !history.isEmpty()) {
			if (keyCode == Keyboard.KEY_UP) {
				String currentText = getText();
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
					setText(historyString);
					handled = true;
				}
			} else if (keyCode == Keyboard.KEY_DOWN) {
				String currentText = getText();
				int historyIndex = history.indexOf(currentText);
				if (historyIndex >= 0) {
					String historyString;
					if (historyIndex + 1 < history.size()) {
						historyString = history.get(historyIndex + 1);
					} else {
						historyString = "";
					}
					setText(historyString);
					handled = true;
				}
			} else if (KeyBindings.isEnterKey(keyCode)) {
				saveHistory();
			}
		}
		return handled;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return hoverChecker.checkHover(mouseX, mouseY);
	}

	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 1) {
			setText("");
			return Config.setFilterText("");
		} else {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		return false;
	}

	@Override
	public void setFocused(boolean keyboardFocus) {
		final boolean previousFocus = isFocused();
		super.setFocused(keyboardFocus);

		if (previousFocus != keyboardFocus) {
			if (keyboardFocus) {
				previousKeyboardRepeatEnabled = Keyboard.areRepeatEventsEnabled();
				Keyboard.enableRepeatEvents(true);
			} else {
				Keyboard.enableRepeatEvents(previousKeyboardRepeatEnabled);
			}

			saveHistory();
		}
	}

	private boolean saveHistory() {
		String text = getText();
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

	// begin hack to draw our own background texture instead of the ugly default one
	private boolean isDrawing = false;

	@Override
	public boolean getEnableBackgroundDrawing() {
		if (this.isDrawing) {
			GlStateManager.color(1, 1, 1, 1);
			Minecraft minecraft = Minecraft.getMinecraft();
			background.draw(minecraft, x, y, width, height);
		}
		return false;
	}

	@Override
	public int getWidth() {
		return this.width - 8;
	}

	@Override
	public void drawTextBox() {
		this.isDrawing = true;
		super.drawTextBox();
		this.isDrawing = false;
	}
	// end background hack
}
