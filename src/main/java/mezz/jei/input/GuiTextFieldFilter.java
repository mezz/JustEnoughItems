package mezz.jei.input;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import net.minecraftforge.fml.client.config.HoverChecker;

import org.lwjgl.input.Keyboard;

import mezz.jei.ItemFilter;
import mezz.jei.util.ItemStackElement;

public class GuiTextFieldFilter extends GuiTextField {
	private static final int MAX_HISTORY = 100;
	private static final int maxSearchLength = 128;

	private final List<String> history = new LinkedList<>();
	private final HoverChecker hoverChecker;
	private ItemFilter itemFilter;

	public GuiTextFieldFilter(int componentId, FontRenderer fontRenderer, int x, int y, int width, int height) {
		super(componentId, fontRenderer, x, y, width, height);
		setMaxStringLength(maxSearchLength);
		this.hoverChecker = new HoverChecker(y, y + height, x, x + width, 0);
	}

	public void setItemFilter(ItemFilter itemFilter) {
		this.itemFilter = itemFilter;
		setText(itemFilter.getFilterText());
	}

	public void update() {
		List<ItemStackElement> itemList = itemFilter.getItemList();
		if (itemList.size() == 0) {
			setTextColor(Color.red.getRGB());
		} else {
			setTextColor(Color.white.getRGB());
		}
	}

	@Override
	public boolean textboxKeyTyped(char character, int keyCode) {
		boolean handled = super.textboxKeyTyped(character, keyCode);
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
		return handled && ItemFilter.setFilterText(getText());
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return hoverChecker.checkHover(mouseX, mouseY);
	}

	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 1) {
			setText("");
			return ItemFilter.setFilterText("");
		} else {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		return false;
	}

	@Override
	public void setFocused(boolean keyboardFocus) {
		super.setFocused(keyboardFocus);
		Keyboard.enableRepeatEvents(keyboardFocus);

		if (!keyboardFocus) {
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
}
