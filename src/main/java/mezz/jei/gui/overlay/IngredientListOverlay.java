package mezz.jei.gui.overlay;

import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.api.IIngredientListOverlay;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.List;

public class IngredientListOverlay implements IIngredientListOverlay, IMouseHandler, IShowsRecipeFocuses {
	private static final int BORDER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int SEARCH_HEIGHT = 20;

	/**
	 * @return true if this can be displayed next to the gui with the given guiProperties
	 */
	private static boolean hasRoom(IGuiProperties guiProperties) {
		Rectangle displayArea = getDisplayArea(guiProperties);
		return displayArea.width >= getMinWidth();
	}

	private static int getMinWidth() {
		return Math.max(4 * BUTTON_SIZE, Config.smallestNumColumns * IngredientGrid.INGREDIENT_WIDTH);
	}

	private static boolean isSearchBarCentered(IGuiProperties guiProperties) {
		return Config.isCenterSearchBarEnabled() &&
				guiProperties.getGuiTop() + guiProperties.getGuiYSize() + SEARCH_HEIGHT < guiProperties.getScreenHeight();
	}

	private final IngredientFilter ingredientFilter;
	private final ConfigButton configButton;
	private final IngredientGridAll contents;
	private final GuiTextFieldFilter searchField;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private Rectangle displayArea = new Rectangle();

	// properties of the gui we're beside
	@Nullable
	private IGuiProperties guiProperties;

	public IngredientListOverlay(IngredientFilter ingredientFilter) {
		this.ingredientFilter = ingredientFilter;

		this.contents = new IngredientGridAll(ingredientFilter);
		this.searchField = new GuiTextFieldFilter(0, ingredientFilter);
		this.configButton = new ConfigButton(this);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.contents);
		this.setKeyboardFocus(false);
	}

	public void rebuildItemFilter() {
		Log.get().info("Updating ingredient filter...");
		long start_time = System.currentTimeMillis();
		this.ingredientFilter.modesChanged();
		Log.get().info("Updated  ingredient filter in {} ms", System.currentTimeMillis() - start_time);
		updateLayout(true);
	}

	public boolean isListDisplayed() {
		return Config.isOverlayEnabled() && this.guiProperties != null && hasRoom(this.guiProperties);
	}

	private static boolean areGuiPropertiesEqual(IGuiProperties guiProperties1, IGuiProperties guiProperties2) {
		return guiProperties1.getGuiClass().equals(guiProperties2.getGuiClass()) &&
			guiProperties1.getGuiLeft() == guiProperties2.getGuiLeft() &&
			guiProperties1.getGuiXSize() == guiProperties2.getGuiXSize() &&
			guiProperties1.getScreenWidth() == guiProperties2.getScreenWidth() &&
			guiProperties1.getScreenHeight() == guiProperties2.getScreenHeight();
	}

	private static Rectangle getDisplayArea(IGuiProperties guiProperties) {
		final int x = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + BORDER_PADDING;
		final int y = BORDER_PADDING;
		final int width = guiProperties.getScreenWidth() - x - BORDER_PADDING;
		final int height = guiProperties.getScreenHeight() - y - BORDER_PADDING;
		return new Rectangle(x, y, width, height);
	}

	public void updateScreen(@Nullable GuiScreen guiScreen) {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime == null) {
			return;
		}
		final boolean wasDisplayed = isListDisplayed();
		IGuiProperties guiProperties = runtime.getGuiProperties(guiScreen);
		if (guiProperties == null) {
			if (this.guiProperties != null) {
				this.guiProperties = null;
				setKeyboardFocus(false);
				this.ghostIngredientDragManager.stopDrag();
			}
		} else if (this.guiProperties == null || !areGuiPropertiesEqual(this.guiProperties, guiProperties)) {
			this.guiProperties = guiProperties;
			this.displayArea = getDisplayArea(guiProperties);

			final boolean searchBarCentered = isSearchBarCentered(guiProperties);
			final int searchHeight = searchBarCentered ? 0 : SEARCH_HEIGHT + BORDER_PADDING;

			Rectangle availableContentsArea = new Rectangle(
				displayArea.x,
				displayArea.y,
				displayArea.width,
				displayArea.height - searchHeight
			);
			final int minWidth = getMinWidth();
			this.contents.updateBounds(availableContentsArea, minWidth);

			// update area to match contents size
			Rectangle contentsArea = this.contents.getArea();
			displayArea.x = contentsArea.x;
			displayArea.width = contentsArea.width;

			if (searchBarCentered && isListDisplayed()) {
				searchField.updateBounds(new Rectangle(
					guiProperties.getGuiLeft(),
					guiProperties.getScreenHeight() - SEARCH_HEIGHT - BORDER_PADDING,
					guiProperties.getGuiXSize() - BUTTON_SIZE + 1,
					SEARCH_HEIGHT
				));
			} else {
				searchField.updateBounds(new Rectangle(
					displayArea.x,
					displayArea.y + displayArea.height - SEARCH_HEIGHT - BORDER_PADDING,
					displayArea.width - BUTTON_SIZE + 1,
					SEARCH_HEIGHT
				));
			}

			this.configButton.updateBounds(new Rectangle(
				searchField.x + searchField.width - 1,
				searchField.y,
				BUTTON_SIZE,
				BUTTON_SIZE
			));

			updateLayout(false);
		}
		if (wasDisplayed && !isListDisplayed()) {
			Config.saveFilterText();
		}
	}

	public void updateLayout(boolean filterChanged) {
		this.contents.updateLayout(filterChanged);
		this.searchField.update();
	}

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		if (this.guiProperties != null) {
			if (isListDisplayed()) {
				GlStateManager.disableLighting();
				this.searchField.drawTextBox();
				this.contents.draw(minecraft, mouseX, mouseY, partialTicks);
				this.configButton.draw(minecraft, mouseX, mouseY, partialTicks);
			} else {
				this.configButton.draw(minecraft, mouseX, mouseY, partialTicks);
			}
		}
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.configButton.drawTooltips(minecraft, mouseX, mouseY, true);
			this.ghostIngredientDragManager.drawTooltips(minecraft, mouseX, mouseY);
			this.contents.drawTooltips(minecraft, mouseX, mouseY);
		} else if (this.guiProperties != null) {
			this.configButton.drawTooltips(minecraft, mouseX, mouseY, false);
		}
	}

	public void drawOnForeground(GuiContainer gui, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(-gui.getGuiLeft(), -gui.getGuiTop(), 0);
			this.ghostIngredientDragManager.drawOnForeground(gui.mc, mouseX, mouseY);
			GlStateManager.popMatrix();
		}
	}

	public void handleTick() {
		if (this.isListDisplayed()) {
			this.searchField.updateCursorCounter();
		}
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		if (isListDisplayed()) {
			if (Config.isCenterSearchBarEnabled() && searchField.isMouseOver(mouseX, mouseY)) {
				return true;
			}
			return displayArea.contains(mouseX, mouseY);
		} else if (this.guiProperties != null) {
			return this.configButton.isMouseOver(mouseX, mouseY);
		}
		return false;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		if (isListDisplayed()) {
			IClickedIngredient<?> clicked = this.contents.getIngredientUnderMouse(mouseX, mouseY);
			if (clicked != null) {
				clicked.setOnClickHandler(() -> setKeyboardFocus(false));
				return clicked;
			}
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return this.isListDisplayed() && this.contents.canSetFocusWithMouse();
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (isListDisplayed()) {
			if (this.ghostIngredientDragManager.handleMouseClicked(mouseX, mouseY)) {
				return true;
			}

			if (this.configButton.handleMouseClick(mouseX, mouseY)) {
				return true;
			}

			if (!isMouseOver(mouseX, mouseY)) {
				setKeyboardFocus(false);
				return false;
			}

			if (this.contents.handleMouseClicked(mouseX, mouseY, mouseButton)) {
				setKeyboardFocus(false);
				return true;
			}

			boolean searchClicked = this.searchField.isMouseOver(mouseX, mouseY);
			setKeyboardFocus(searchClicked);
			if (searchClicked) {
				final boolean updated = this.searchField.handleMouseClicked(mouseX, mouseY, mouseButton);
				if (updated) {
					updateLayout(false);
				}
				return true;
			}

			Minecraft minecraft = Minecraft.getMinecraft();
			GuiScreen currentScreen = minecraft.currentScreen;
			if (currentScreen != null && !(currentScreen instanceof RecipesGui) &&
				(mouseButton == 0 || mouseButton == 1 || minecraft.gameSettings.keyBindPickBlock.isActiveAndMatches(mouseButton - 100))) {
				IClickedIngredient<?> clicked = getIngredientUnderMouse(mouseX, mouseY);
				if (clicked != null) {
					if (Config.isCheatItemsEnabled()) {
						ItemStack itemStack = clicked.getCheatItemStack();
						if (!itemStack.isEmpty()) {
							CommandUtil.giveStack(itemStack, mouseButton);
						}
						clicked.onClickHandled();
						return true;
					}
					EntityPlayerSP player = minecraft.player;
					if (player != null) {
						ItemStack mouseItem = player.inventory.getItemStack();
						if (mouseItem.isEmpty() && this.ghostIngredientDragManager.handleClickGhostIngredient(currentScreen, clicked)) {
							return true;
						}
					}
				}
			}
		} else if (this.guiProperties != null) {
			return this.configButton.handleMouseClick(mouseX, mouseY);
		}
		return false;
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		return isListDisplayed() &&
			isMouseOver(mouseX, mouseY) &&
			this.contents.handleMouseScrolled(mouseX, mouseY, scrollDelta);
	}

	@Override
	public boolean hasKeyboardFocus() {
		return isListDisplayed() && this.searchField.isFocused();
	}

	public void setKeyboardFocus(boolean keyboardFocus) {
		this.searchField.setFocused(keyboardFocus);
	}

	public boolean onGlobalKeyPressed(int eventKey) {
		if (isListDisplayed()) {
			if (KeyBindings.toggleCheatMode.isActiveAndMatches(eventKey)) {
				Config.toggleCheatItemsEnabled();
				return true;
			}
			if (KeyBindings.focusSearch.isActiveAndMatches(eventKey)) {
				setKeyboardFocus(true);
				return true;
			}
		}
		return false;
	}

	public boolean onKeyPressed(char typedChar, int eventKey) {
		if (isListDisplayed()) {
			if (hasKeyboardFocus() &&
					searchField.textboxKeyTyped(typedChar, eventKey)) {
				boolean changed = Config.setFilterText(searchField.getText());
				if (changed) {
					updateLayout(true);
				}
				return true;
			}
			return this.contents.onKeyPressed(typedChar, eventKey);
		}
		return false;
	}

	@Nullable
	@Override
	public Object getIngredientUnderMouse() {
		if (isListDisplayed()) {
			IIngredientListElement elementUnderMouse = this.contents.getElementUnderMouse();
			if (elementUnderMouse != null) {
				return elementUnderMouse.getIngredient();
			}
		}
		return null;
	}

	public void onSetFilterText(String filterText) {
		this.searchField.setText(filterText);
		updateLayout(true);
	}

	@Override
	public ImmutableList<Object> getVisibleIngredients() {
		if (isListDisplayed()) {
			ImmutableList.Builder<Object> visibleIngredients = ImmutableList.builder();
			List<IIngredientListElement> visibleElements = this.contents.getVisibleElements();
			for (IIngredientListElement element : visibleElements) {
				Object ingredient = element.getIngredient();
				visibleIngredients.add(ingredient);
			}

			return visibleIngredients.build();
		}
		return ImmutableList.of();
	}

}
