package mezz.jei.gui.overlay;

import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.api.IIngredientListOverlay;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.SessionData;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.MouseHelper;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import mezz.jei.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IngredientListOverlay implements IItemListOverlay, IIngredientListOverlay, IPaged, IMouseHandler, IShowsRecipeFocuses {
	private static final int BORDER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int NAVIGATION_HEIGHT = 20;
	private static final int SEARCH_HEIGHT = 16;

	/**
	 * @return true if this can be displayed next to the gui with the given guiProperties
	 */
	private static boolean hasRoom(IGuiProperties guiProperties) {
		return guiProperties.getScreenWidth() - (guiProperties.getGuiLeft() + guiProperties.getGuiXSize()) >= 72;
	}

	private static boolean isSearchBarCentered(IGuiProperties guiProperties) {
		return Config.isCenterSearchBarEnabled() &&
				guiProperties.getGuiTop() + guiProperties.getGuiYSize() + SEARCH_HEIGHT < guiProperties.getScreenHeight();
	}

	private final IngredientFilter ingredientFilter;
	private final NonNullList<ItemStack> highlightedStacks = NonNullList.create();
	private final ConfigButton configButton;
	private final PageNavigation navigation;
	private final IngredientGrid contents;
	private final GuiTextFieldFilter searchField;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private Set<Rectangle> guiExclusionAreas = Collections.emptySet();
	private Rectangle displayArea = new Rectangle();

	// properties of the gui we're beside
	@Nullable
	private IGuiProperties guiProperties;

	public IngredientListOverlay(IngredientFilter ingredientFilter) {
		this.ingredientFilter = ingredientFilter;

		this.contents = new IngredientGridAll(ingredientFilter);
		this.searchField = new GuiTextFieldFilter(0, ingredientFilter);
		this.navigation = new PageNavigation(this, false);
		this.configButton = new ConfigButton(this);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.contents);
		this.setKeyboardFocus(false);
	}

	public void rebuildItemFilter() {
		Log.get().info("Updating ingredient filter...");
		long start_time = System.currentTimeMillis();
		this.ingredientFilter.modesChanged();
		Log.get().info("Updated  ingredient filter in {} ms", System.currentTimeMillis() - start_time);
		SessionData.setFirstItemIndex(0);
		updateLayout();
	}

	@Override
	public String getFilterText() {
		return Config.getFilterText();
	}

	@Override
	public ImmutableList<ItemStack> getFilteredStacks() {
		List<IIngredientListElement> elements = ingredientFilter.getIngredientList();
		ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
		for (IIngredientListElement element : elements) {
			Object ingredient = element.getIngredient();
			if (ingredient instanceof ItemStack) {
				builder.add((ItemStack) ingredient);
			}
		}
		return builder.build();
	}

	@Override
	public void highlightStacks(Collection<ItemStack> stacks) {
		highlightedStacks.clear();
		highlightedStacks.addAll(stacks);
	}

	public boolean isEnabled() {
		return Config.isOverlayEnabled() && this.guiProperties != null && hasRoom(this.guiProperties);
	}

	private static boolean areGuiPropertiesEqual(IGuiProperties guiProperties1, IGuiProperties guiProperties2) {
		return guiProperties1.getGuiClass().equals(guiProperties2.getGuiClass()) &&
			guiProperties1.getGuiLeft() == guiProperties2.getGuiLeft() &&
			guiProperties1.getGuiXSize() == guiProperties2.getGuiXSize() &&
			guiProperties1.getScreenWidth() == guiProperties2.getScreenWidth() &&
			guiProperties1.getScreenHeight() == guiProperties2.getScreenHeight();
	}

	public void updateScreen(@Nullable GuiScreen guiScreen) {
		JeiRuntime runtime = Internal.getRuntime();
		if (runtime == null) {
			return;
		}
		IGuiProperties guiProperties = runtime.getGuiProperties(guiScreen);
		if (guiProperties == null) {
			this.guiProperties = null;
			setKeyboardFocus(false);
		} else if (this.guiProperties == null || !areGuiPropertiesEqual(this.guiProperties, guiProperties)) {
			this.guiProperties = guiProperties;

			final int x = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + BORDER_PADDING;
			final int y = BORDER_PADDING;
			final int width = guiProperties.getScreenWidth() - x - (2 * BORDER_PADDING);
			final int height = guiProperties.getScreenHeight() - y - (2 * BORDER_PADDING);
			displayArea = new Rectangle(x, y, width, height);

			final boolean searchBarCentered = isSearchBarCentered(guiProperties);
			final int searchHeight = searchBarCentered ? 0 : SEARCH_HEIGHT + 4;

			Rectangle contentsArea = new Rectangle(displayArea.x, displayArea.y + NAVIGATION_HEIGHT + 2, displayArea.width, displayArea.height - NAVIGATION_HEIGHT - searchHeight);
			this.contents.updateBounds(contentsArea, this.guiExclusionAreas);

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

			int configButtonX = this.searchField.x + this.searchField.width + 1;
			int configButtonY = this.searchField.y - BORDER_PADDING;
			this.configButton.updateBounds(new Rectangle(configButtonX, configButtonY, BUTTON_SIZE, BUTTON_SIZE));

			updateLayout();
		}
	}

	private void updateLayout() {
		this.contents.updateLayout(this.guiExclusionAreas);

		int pageNum = this.contents.getPageNum();
		int pageCount = this.contents.getPageCount();
		this.navigation.updatePageState(pageNum, pageCount);

		this.searchField.update();
	}

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		if (this.updateGuiExclusionAreas()) {
			updateLayout();
		}

		GlStateManager.disableLighting();

		this.navigation.draw(minecraft, mouseX, mouseY, partialTicks);
		this.searchField.drawTextBox();
		this.contents.draw(minecraft, mouseX, mouseY);
		this.configButton.draw(minecraft, mouseX, mouseY, partialTicks);
	}

	public boolean updateGuiExclusionAreas() {
		final Set<Rectangle> guiAreas = getGuiAreas();
		if (!guiAreas.equals(this.guiExclusionAreas)) {
			this.guiExclusionAreas = guiAreas;
			return true;
		}
		return false;
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		this.contents.drawTooltips(minecraft, mouseX, mouseY);
		this.configButton.drawTooltips(minecraft, mouseX, mouseY);
		this.ghostIngredientDragManager.drawTooltips(minecraft, mouseX, mouseY);
	}

	public void drawOnForeground(Minecraft minecraft, int mouseX, int mouseY) {
		this.ghostIngredientDragManager.drawOnForeground(minecraft, mouseX, mouseY);
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
		if (displayArea.contains(mouseX, mouseY) || searchField.isMouseOver(mouseX, mouseY) || configButton.isMouseOver(mouseX, mouseY)) {
			return !MathUtil.contains(guiExclusionAreas, mouseX, mouseY);
		}
		return false;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		IClickedIngredient<?> clicked = this.contents.getIngredientUnderMouse(mouseX, mouseY);
		if (clicked != null) {
			clicked.setOnClickHandler(() -> setKeyboardFocus(false));
		}
		return clicked;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return this.isEnabled() && this.contents.canSetFocusWithMouse();
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (this.ghostIngredientDragManager.handleMouseClicked(mouseX, mouseY)) {
			return true;
		} // if the ghost ingredient drag failed, fall through to let the click be handled by something else

		if (!isMouseOver(mouseX, mouseY)) {
			setKeyboardFocus(false);
			return false;
		}

		if (this.contents.handleMouseClicked(mouseX, mouseY)) {
			setKeyboardFocus(false);
			return true;
		}

		if (this.navigation.handleMouseClickedButtons(mouseX, mouseY)) {
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

		if (this.configButton.handleMouseClick(Minecraft.getMinecraft(), mouseX, mouseY)) {
			return true;
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		GuiScreen currentScreen = minecraft.currentScreen;
		if (currentScreen != null && !(currentScreen instanceof RecipesGui) &&
			(mouseButton == 0 || mouseButton == 1 || minecraft.gameSettings.keyBindPickBlock.isActiveAndMatches(mouseButton - 100)))
		{
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
				if (this.ghostIngredientDragManager.handleClickGhostIngredient(currentScreen, clicked)) {
					return true;
				}
			}
		}
		return false;
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
		return checkHotbarKeys(keyCode);
	}

	/**
	 * Modeled after {@link GuiContainer#checkHotbarKeys(int)}
	 * Sets the stack in a hotbar slot to the one that's hovered over.
	 */
	protected boolean checkHotbarKeys(int keyCode) {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (Config.isCheatItemsEnabled() && guiScreen != null && !(guiScreen instanceof RecipesGui)) {
			final int mouseX = MouseHelper.getX();
			final int mouseY = MouseHelper.getY();
			if (isMouseOver(mouseX, mouseY)) {
				GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
				for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
					if (gameSettings.keyBindsHotbar[hotbarSlot].isActiveAndMatches(keyCode)) {
						IClickedIngredient<?> ingredientUnderMouse = getIngredientUnderMouse(mouseX, mouseY);
						if (ingredientUnderMouse != null) {
							ItemStack itemStack = ingredientUnderMouse.getCheatItemStack();
							if (!itemStack.isEmpty()) {
								CommandUtil.setHotbarStack(itemStack, hotbarSlot);
							}
							ingredientUnderMouse.onClickHandled();
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	@Nullable
	public ItemStack getStackUnderMouse() {
		Object ingredient = getIngredientUnderMouse();
		if (ingredient instanceof ItemStack) {
			return (ItemStack) ingredient;
		}
		return null;
	}

	@Nullable
	@Override
	public Object getIngredientUnderMouse() {
		IIngredientListElement elementUnderMouse = this.contents.getElementUnderMouse();
		if (elementUnderMouse != null) {
			return elementUnderMouse.getIngredient();
		}
		return null;
	}

	@Override
	public void setFilterText(String filterText) {
		ErrorUtil.checkNotNull(filterText, "filterText");
		if (Config.setFilterText(filterText)) {
			onSetFilterText(filterText);
		}
	}

	public void onSetFilterText(String filterText) {
		this.searchField.setText(filterText);
		SessionData.setFirstItemIndex(0);
		updateLayout();
	}

	@Override
	public ImmutableList<ItemStack> getVisibleStacks() {
		ImmutableList.Builder<ItemStack> visibleStacks = ImmutableList.builder();
		List<IIngredientListElement> visibleElements = this.contents.getVisibleElements();
		for (IIngredientListElement element : visibleElements) {
			Object ingredient = element.getIngredient();
			if (ingredient instanceof ItemStack) {
				visibleStacks.add((ItemStack) ingredient);
			}
		}

		return visibleStacks.build();
	}

	@Override
	public ImmutableList<Object> getVisibleIngredients() {
		ImmutableList.Builder<Object> visibleIngredients = ImmutableList.builder();
		List<IIngredientListElement> visibleElements = this.contents.getVisibleElements();
		for (IIngredientListElement element : visibleElements) {
			Object ingredient = element.getIngredient();
			visibleIngredients.add(ingredient);
		}

		return visibleIngredients.build();
	}

	private static Set<Rectangle> getGuiAreas() {
		final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen instanceof GuiContainer) {
			final GuiContainer guiContainer = (GuiContainer) currentScreen;
			final JeiRuntime jeiRuntime = Internal.getRuntime();
			if (jeiRuntime != null) {
				final Set<Rectangle> allGuiExtraAreas = new HashSet<>();
				final List<IAdvancedGuiHandler<GuiContainer>> activeAdvancedGuiHandlers = jeiRuntime.getActiveAdvancedGuiHandlers(guiContainer);
				for (IAdvancedGuiHandler<GuiContainer> advancedGuiHandler : activeAdvancedGuiHandlers) {
					final List<Rectangle> guiExtraAreas = advancedGuiHandler.getGuiExtraAreas(guiContainer);
					if (guiExtraAreas != null) {
						allGuiExtraAreas.addAll(guiExtraAreas);
					}
				}
				return allGuiExtraAreas;
			}
		}
		return Collections.emptySet();
	}
}
