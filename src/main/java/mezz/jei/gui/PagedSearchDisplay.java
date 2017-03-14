package mezz.jei.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;

import com.google.common.collect.ImmutableList;
import mezz.jei.config.Config;
import mezz.jei.config.SessionData;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

public class PagedSearchDisplay implements IPaged, IMouseHandler, IShowsRecipeFocuses {
	private static final int NAVIGATION_HEIGHT = 20;
	private static final int SEARCH_HEIGHT = 16;

	private static boolean isSearchBarCentered(GuiProperties guiProperties) {
		return Config.isCenterSearchBarEnabled() &&
				guiProperties.getGuiTop() + guiProperties.getGuiYSize() + SEARCH_HEIGHT < guiProperties.getScreenHeight();
	}

	private final PageNavigation navigation;
	private final IngredientGrid contents;
	private final GuiTextFieldFilter searchField;

	public PagedSearchDisplay(IngredientGrid contents, GuiTextFieldFilter searchField) {
		this.navigation = new PageNavigation(this, false);
		this.contents = contents;
		this.searchField = searchField;
		this.setKeyboardFocus(false);
	}

	public Rectangle updateBounds(GuiProperties guiProperties, Rectangle area) {
		final boolean searchBarCentered = isSearchBarCentered(guiProperties);
		final int searchHeight = searchBarCentered ? 0 : SEARCH_HEIGHT + 4;

		Rectangle contentsArea = new Rectangle(area.x, area.y + NAVIGATION_HEIGHT + 2, area.width, area.height - NAVIGATION_HEIGHT - searchHeight);
		this.contents.updateBounds(contentsArea);

		// update area to match contents size
		contentsArea = this.contents.getArea();
		area.x = contentsArea.x;
		area.width = contentsArea.width;

		Rectangle navigationArea = new Rectangle(area.x, area.y, area.width, NAVIGATION_HEIGHT);
		this.navigation.updateBounds(navigationArea);

		if (searchBarCentered) {
			Rectangle searchArea = new Rectangle(guiProperties.getGuiLeft() + 2, guiProperties.getScreenHeight() - 4 - SEARCH_HEIGHT, guiProperties.getGuiXSize() - 4, SEARCH_HEIGHT);
			this.searchField.updateBounds(searchArea);
		} else {
			Rectangle searchArea = new Rectangle(area.x + 2, area.y + area.height - SEARCH_HEIGHT, area.width - 4, SEARCH_HEIGHT);
			this.searchField.updateBounds(searchArea);
		}

		return area;
	}

	public void updateLayout() {
		this.contents.updateLayout();

		int pageNum = this.contents.getPageNum();
		int pageCount = this.contents.getPageCount();
		this.navigation.updatePageState(pageNum, pageCount);

		this.searchField.update();
	}

	@Override
	public boolean nextPage() {
		if (contents.nextPage()) {
			updateLayout();
			return true;
		}
		return false;
	}

	@Override
	public boolean previousPage() {
		if (contents.previousPage()) {
			updateLayout();
			return true;
		}
		return false;
	}

	@Override
	public boolean hasNext() {
		return contents.hasNext();
	}

	@Override
	public boolean hasPrevious() {
		return contents.hasPrevious();
	}

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY) {
		if (this.contents.updateGuiAreas()) {
			updateLayout();
		}

		GlStateManager.disableLighting();

		this.navigation.draw(minecraft, mouseX, mouseY);
		this.searchField.drawTextBox();
		this.contents.draw(minecraft, mouseX, mouseY);
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		this.contents.drawTooltips(minecraft, mouseX, mouseY);
	}

	public void handleTick() {
		this.searchField.updateCursorCounter();
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return this.navigation.isMouseOver(mouseX, mouseY) ||
				this.contents.isMouseOver(mouseX, mouseY) ||
				this.searchField.isMouseOver(mouseX, mouseY);
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		IClickedIngredient<?> clicked = this.contents.getIngredientUnderMouse(mouseX, mouseY);
		if (clicked != null) {
			setKeyboardFocus(false);
		}
		return clicked;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return this.contents.canSetFocusWithMouse();
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isMouseOver(mouseX, mouseY)) {
			setKeyboardFocus(false);
			return false;
		}

		if (this.contents.handleMouseClicked(mouseX, mouseY)) {
			setKeyboardFocus(false);
			return true;
		}

		boolean buttonClicked = this.navigation.handleMouseClickedButtons(mouseX, mouseY);
		if (buttonClicked) {
			setKeyboardFocus(false);
			return true;
		}

		boolean searchClicked = this.searchField.isMouseOver(mouseX, mouseY);
		setKeyboardFocus(searchClicked);
		if (searchClicked) {
			final boolean updated = this.searchField.handleMouseClicked(mouseX, mouseY, mouseButton);
			if (updated) {
				updateLayout();
			}
		}
		return searchClicked;
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (scrollDelta < 0) {
			nextPage();
			return true;
		} else if (scrollDelta > 0) {
			previousPage();
			return true;
		}
		return false;
	}

	public boolean hasKeyboardFocus() {
		return searchField.isFocused();
	}

	public void setKeyboardFocus(boolean keyboardFocus) {
		searchField.setFocused(keyboardFocus);
	}

	public boolean onKeyPressed(char typedChar, int keyCode) {
		if (hasKeyboardFocus()) {
			boolean handled = searchField.textboxKeyTyped(typedChar, keyCode);
			if (handled) {
				boolean changed = Config.setFilterText(searchField.getText());
				if (changed) {
					SessionData.setFirstItemIndex(0);
					updateLayout();
				}
			}
			return handled;
		}
		return false;
	}

	@Nullable
	public ItemStack getStackUnderMouse() {
		return this.contents.getStackUnderMouse();
	}

	public void setFilterText(String filterText) {
		this.searchField.setText(filterText);
		SessionData.setFirstItemIndex(0);
		updateLayout();
	}

	public ImmutableList<ItemStack> getVisibleStacks() {
		return this.contents.getVisibleStacks();
	}
}
