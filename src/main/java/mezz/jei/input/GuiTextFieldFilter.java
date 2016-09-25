package mezz.jei.input;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import mezz.jei.ItemFilter;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.client.config.HoverChecker;
import org.lwjgl.input.Keyboard;

public class GuiTextFieldFilter extends GuiTextField {
	private static final int MAX_HISTORY = 100;
	private static final int maxSearchLength = 128;

	private final List<String> history = new LinkedList<String>();
	private final HoverChecker hoverChecker;
	private ItemFilter itemFilter;
	private boolean previousKeyboardRepeatEnabled;

	public GuiTextFieldFilter(int componentId, FontRenderer fontRenderer, int x, int y, int width, int height) {
		super(componentId, fontRenderer, x, y, width, height);
		setMaxStringLength(maxSearchLength);
		this.hoverChecker = new HoverChecker(y, y + height, x, x + width, 0);
	}

	public void setItemFilter(ItemFilter itemFilter) {
		this.itemFilter = itemFilter;
		setText(Config.getFilterText());
	}

	public void update() {
		List<IIngredientListElement> itemList = itemFilter.getIngredientList();
		if (itemList.size() == 0) {
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
			} else if (keyCode == Keyboard.KEY_RETURN) {
				saveHistory();
			}
		}
		return handled && Config.setFilterText(getText());
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

			if (!keyboardFocus) {
				saveHistory();
			}
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
}
