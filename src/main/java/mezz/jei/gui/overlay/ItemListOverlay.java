package mezz.jei.gui.overlay;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.SessionData;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientBaseListFactory;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemListOverlay implements IItemListOverlay, IPaged, IMouseHandler, IShowsRecipeFocuses {
	private static final int BORDER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int NAVIGATION_HEIGHT = 20;
	private static final int SEARCH_HEIGHT = 16;

	/**
	 * @return true if this can be displayed next to the gui with the given guiProperties
	 */
	private static boolean hasRoom(GuiProperties guiProperties) {
		return guiProperties.getScreenWidth() - (guiProperties.getGuiLeft() + guiProperties.getGuiXSize()) >= 72;
	}

	private static boolean isSearchBarCentered(GuiProperties guiProperties) {
		return Config.isCenterSearchBarEnabled() &&
				guiProperties.getGuiTop() + guiProperties.getGuiYSize() + SEARCH_HEIGHT < guiProperties.getScreenHeight();
	}

	private final IngredientFilter ingredientFilter;
	private final NonNullList<ItemStack> highlightedStacks = NonNullList.create();
	private final ConfigButton configButton;
	private final PageNavigation navigation;
	private final IngredientGrid contents;
	private final GuiTextFieldFilter searchField;

	// properties of the gui we're beside
	@Nullable
	private GuiProperties guiProperties;

	public ItemListOverlay(IngredientFilter ingredientFilter, IIngredientRegistry ingredientRegistry) {
		this.ingredientFilter = ingredientFilter;

		this.contents = new IngredientGridAll(ingredientRegistry, ingredientFilter);
		this.searchField = new GuiTextFieldFilter(0, ingredientFilter);
		this.navigation = new PageNavigation(this, false);
		this.configButton = new ConfigButton(this);
		this.setKeyboardFocus(false);
	}

	public void rebuildItemFilter() {
		List<IIngredientListElement> ingredientList = IngredientBaseListFactory.create();
		this.ingredientFilter.rebuild(ingredientList);
		SessionData.setFirstItemIndex(0);
		updateLayout();
	}

	@Override
	public String getFilterText() {
		return Config.getFilterText();
	}

	@Override
	public ImmutableList<ItemStack> getFilteredStacks() {
		return ingredientFilter.getItemStacks();
	}

	@Override
	public void highlightStacks(Collection<ItemStack> stacks) {
		highlightedStacks.clear();
		highlightedStacks.addAll(stacks);
	}

	public NonNullList<ItemStack> getHighlightedStacks() {
		return highlightedStacks;
	}

	public boolean isEnabled() {
		return Config.isOverlayEnabled() && this.guiProperties != null && hasRoom(this.guiProperties);
	}

	public void updateScreen(@Nullable GuiScreen guiScreen) {
		GuiProperties guiProperties = GuiProperties.create(guiScreen);
		if (guiProperties == null) {
			this.guiProperties = null;
			setKeyboardFocus(false);
		} else if (this.guiProperties == null || !this.guiProperties.equals(guiProperties)) {
			this.guiProperties = guiProperties;

			final int x = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + BORDER_PADDING;
			final int y = BORDER_PADDING;
			final int width = guiProperties.getScreenWidth() - x - (2 * BORDER_PADDING);
			final int height = guiProperties.getScreenHeight() - y - (2 * BORDER_PADDING);
			Rectangle displayArea = new Rectangle(x, y, width, height);

			final boolean searchBarCentered = isSearchBarCentered(guiProperties);
			final int searchHeight = searchBarCentered ? 0 : SEARCH_HEIGHT + 4;

			Rectangle contentsArea = new Rectangle(displayArea.x, displayArea.y + NAVIGATION_HEIGHT + 2, displayArea.width, displayArea.height - NAVIGATION_HEIGHT - searchHeight);
			this.contents.updateBounds(contentsArea);

			// update area to match contents size
			contentsArea = this.contents.getArea();
			displayArea.x = contentsArea.x;
			displayArea.width = contentsArea.width;

			Rectangle navigationArea = new Rectangle(displayArea.x, displayArea.y, displayArea.width, NAVIGATION_HEIGHT);
			this.navigation.updateBounds(navigationArea);

			if (searchBarCentered) {
				Rectangle searchArea = new Rectangle(guiProperties.getGuiLeft() + 2, guiProperties.getScreenHeight() - 4 - SEARCH_HEIGHT, guiProperties.getGuiXSize() - 3 - BUTTON_SIZE, SEARCH_HEIGHT);
				this.searchField.updateBounds(searchArea);
			} else {
				Rectangle searchArea = new Rectangle(displayArea.x + 2, displayArea.y + displayArea.height - SEARCH_HEIGHT, displayArea.width - 3 - BUTTON_SIZE, SEARCH_HEIGHT);
				this.searchField.updateBounds(searchArea);
			}

			int configButtonX = this.searchField.xPosition + this.searchField.width + 1;
			int configButtonY = this.searchField.yPosition - BORDER_PADDING;
			this.configButton.updateBounds(new Rectangle(configButtonX, configButtonY, BUTTON_SIZE, BUTTON_SIZE));

			updateLayout();
		}
	}

	private void updateLayout() {
		this.contents.updateLayout();

		int pageNum = this.contents.getPageNum();
		int pageCount = this.contents.getPageCount();
		this.navigation.updatePageState(pageNum, pageCount);

		this.searchField.update();
	}

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY) {
		if (this.contents.updateGuiAreas()) {
			updateLayout();
		}

		GlStateManager.disableLighting();

		this.navigation.draw(minecraft, mouseX, mouseY);
		this.searchField.drawTextBox();
		this.contents.draw(minecraft, mouseX, mouseY);
		this.configButton.draw(minecraft, mouseX, mouseY);
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		this.contents.drawTooltips(minecraft, mouseX, mouseY);
		this.configButton.drawTooltips(minecraft, mouseX, mouseY);
	}

	public void handleTick() {
		this.searchField.updateCursorCounter();
	}

	@Override
	public boolean nextPage() {
		if (this.contents.nextPage()) {
			updateLayout();
			return true;
		}
		return false;
	}

	@Override
	public boolean previousPage() {
		if (this.contents.previousPage()) {
			updateLayout();
			return true;
		}
		return false;
	}

	@Override
	public boolean hasNext() {
		return this.contents.hasNext();
	}

	@Override
	public boolean hasPrevious() {
		return this.contents.hasPrevious();
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return this.navigation.isMouseOver(mouseX, mouseY) ||
				this.contents.isMouseOver(mouseX, mouseY) ||
				this.searchField.isMouseOver(mouseX, mouseY) ||
				this.configButton.isMouseOver(mouseX, mouseY);
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
		return this.isEnabled() && this.contents.canSetFocusWithMouse();
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
			return true;
		}

		return this.configButton.handleMouseClick(Minecraft.getMinecraft(), mouseX, mouseY);
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

	@Override
	public boolean hasKeyboardFocus() {
		return this.searchField.isFocused();
	}

	public void setKeyboardFocus(boolean keyboardFocus) {
		this.searchField.setFocused(keyboardFocus);
	}

	public boolean onKeyPressed(char typedChar, int keyCode) {
		if (hasKeyboardFocus() &&
			searchField.textboxKeyTyped(typedChar, keyCode)) {
			boolean changed = Config.setFilterText(searchField.getText());
			if (changed) {
				SessionData.setFirstItemIndex(0);
				updateLayout();
			}
			return true;
		} else if (KeyBindings.nextPage.isActiveAndMatches(keyCode)) {
			nextPage();
			return true;
		} else if (KeyBindings.previousPage.isActiveAndMatches(keyCode)) {
			previousPage();
			return true;
		}
		return false;
	}

	@Override
	@Nullable
	public ItemStack getStackUnderMouse() {
		return this.contents.getStackUnderMouse();
	}

	@Override
	public void setFilterText(String filterText) {
		ErrorUtil.checkNotNull(filterText, "filterText");
		if (Config.setFilterText(filterText)) {
			this.searchField.setText(filterText);
			SessionData.setFirstItemIndex(0);
			updateLayout();
		}
	}

	@Override
	public ImmutableList<ItemStack> getVisibleStacks() {
		return this.contents.getVisibleStacks();
	}

}
