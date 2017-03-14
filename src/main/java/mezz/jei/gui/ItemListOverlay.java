package mezz.jei.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mezz.jei.ItemFilter;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.config.SessionData;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

public class ItemListOverlay implements IItemListOverlay, IMouseHandler, IShowsRecipeFocuses {
	private static final int BORDER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;

	/**
	 * @return true if this can be displayed next to the gui with the given guiProperties
	 */
	private static boolean hasRoom(GuiProperties guiProperties) {
		return guiProperties.getScreenWidth() - (guiProperties.getGuiLeft() + guiProperties.getGuiXSize()) >= 72;
	}

	private final ItemFilter itemFilter;
	private final Set<ItemStack> highlightedStacks = new HashSet<ItemStack>();
	private final PagedSearchDisplay display;
	private final ConfigButton configButton;

	// properties of the gui we're beside
	@Nullable
	private GuiProperties guiProperties;

	public ItemListOverlay(ItemFilter itemFilter, IIngredientRegistry ingredientRegistry) {
		this.itemFilter = itemFilter;

		IngredientGridAll ingredientGridAll = new IngredientGridAll(ingredientRegistry, itemFilter);
		GuiTextFieldFilter searchField = new GuiTextFieldFilter(0, itemFilter);
		this.display = new PagedSearchDisplay(ingredientGridAll, searchField);
		this.configButton = new ConfigButton(this);
	}

	public void rebuildItemFilter() {
		this.itemFilter.rebuild();
		SessionData.setFirstItemIndex(0);
		updateLayout();
	}

	@Override
	public String getFilterText() {
		return Config.getFilterText();
	}

	@Override
	public ImmutableList<ItemStack> getFilteredStacks() {
		return itemFilter.getItemStacks();
	}

	@Override
	public void highlightStacks(Collection<ItemStack> stacks) {
		highlightedStacks.clear();
		highlightedStacks.addAll(stacks);
	}

	public Set<ItemStack> getHighlightedStacks() {
		return highlightedStacks;
	}

	public boolean isEnabled() {
		return Config.isOverlayEnabled() && this.guiProperties != null && hasRoom(this.guiProperties);
	}

	public void updateScreen(@Nullable GuiScreen guiScreen) {
		GuiProperties guiProperties = GuiProperties.create(guiScreen);
		if (guiProperties == null) {
			this.guiProperties = null;
			this.display.setKeyboardFocus(false);
		} else if (this.guiProperties == null || !this.guiProperties.equals(guiProperties)) {
			this.guiProperties = guiProperties;

			final int footerSize = BUTTON_SIZE + (2 * BORDER_PADDING);
			final int x = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + BORDER_PADDING;
			final int y = BORDER_PADDING;
			final int width = guiProperties.getScreenWidth() - x - (2 * BORDER_PADDING);
			final int height = guiProperties.getScreenHeight() - y - footerSize;
			Rectangle displayArea = new Rectangle(x, y, width, height);
			displayArea = this.display.updateBounds(guiProperties, displayArea);

			int configButtonX = displayArea.x + displayArea.width - BUTTON_SIZE;
			int configButtonY = guiProperties.getScreenHeight() - BUTTON_SIZE - BORDER_PADDING;
			this.configButton.updateBounds(new Rectangle(configButtonX, configButtonY, BUTTON_SIZE, BUTTON_SIZE));

			updateLayout();
		}
	}

	private void updateLayout() {
		this.display.updateLayout();
	}

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY) {
		GlStateManager.disableLighting();

		this.display.drawScreen(minecraft, mouseX, mouseY);
		this.configButton.draw(minecraft, mouseX, mouseY);
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		this.display.drawTooltips(minecraft, mouseX, mouseY);
		this.configButton.drawTooltips(minecraft, mouseX, mouseY);
	}

	public void handleTick() {
		this.display.handleTick();
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return this.display.isMouseOver(mouseX, mouseY) ||
				this.configButton.isMouseOver(mouseX, mouseY);
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		return this.display.getIngredientUnderMouse(mouseX, mouseY);
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return this.display.canSetFocusWithMouse();
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		return this.display.handleMouseClicked(mouseX, mouseY, mouseButton) ||
				this.configButton.handleMouseClick(Minecraft.getMinecraft(), mouseX, mouseY);
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		return this.display.handleMouseScrolled(mouseX, mouseY, scrollDelta);
	}

	@Override
	public boolean hasKeyboardFocus() {
		return this.display.hasKeyboardFocus();
	}

	public void setKeyboardFocus(boolean keyboardFocus) {
		this.display.setKeyboardFocus(keyboardFocus);
	}

	public boolean onKeyPressed(char typedChar, int keyCode) {
		return this.display.onKeyPressed(typedChar, keyCode);
	}

	@Override
	@Nullable
	public ItemStack getStackUnderMouse() {
		return this.display.getStackUnderMouse();
	}

	@Override
	public void setFilterText(String filterText) {
		Preconditions.checkNotNull(filterText, "filterText cannot be null");

		Config.setFilterText(filterText);
		this.display.setFilterText(filterText);
	}

	@Override
	public ImmutableList<ItemStack> getVisibleStacks() {
		return this.display.getVisibleStacks();
	}

}
