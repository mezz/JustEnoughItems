package mezz.jei.gui.overlay;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.MathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IngredientListOverlay implements IIngredientListOverlay, IMouseHandler, IShowsRecipeFocuses {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int BORDER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int SEARCH_HEIGHT = 20;
	private boolean hasRoom;

	private static boolean isSearchBarCentered(IGuiProperties guiProperties) {
		return ClientConfig.getInstance().isCenterSearchBarEnabled() &&
			guiProperties.getGuiTop() + guiProperties.getGuiYSize() + SEARCH_HEIGHT < guiProperties.getScreenHeight();
	}

	private final IngredientFilter ingredientFilter;
	private final GuiIconToggleButton configButton;
	private final IngredientGridWithNavigation contents;
	private final IWorldConfig worldConfig;
	private final GuiScreenHelper guiScreenHelper;
	private final GuiTextFieldFilter searchField;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private Rectangle2d displayArea = new Rectangle2d(0, 0, 0, 0);

	// properties of the gui we're beside
	@Nullable
	private IGuiProperties guiProperties;

	public IngredientListOverlay(
		IngredientFilter ingredientFilter,
		IngredientManager ingredientManager,
		GuiScreenHelper guiScreenHelper,
		IngredientGridWithNavigation contents,
		IWorldConfig worldConfig
	) {
		this.ingredientFilter = ingredientFilter;
		this.guiScreenHelper = guiScreenHelper;
		this.contents = contents;
		this.worldConfig = worldConfig;
		ingredientFilter.addListener(() -> onSetFilterText(worldConfig.getFilterText()));
		this.searchField = new GuiTextFieldFilter(ingredientFilter, worldConfig);
		this.configButton = ConfigButton.create(this, worldConfig);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.contents, guiScreenHelper, ingredientManager, worldConfig);
		this.setKeyboardFocus(false);
	}

	public void rebuildIngredientFilter() {
		LOGGER.info("Updating ingredient filter...");
		long start_time = System.currentTimeMillis();
		this.ingredientFilter.modesChanged();
		LOGGER.info("Updated  ingredient filter in {} ms", System.currentTimeMillis() - start_time);
		updateLayout(true);
	}

	public boolean isListDisplayed() {
		return worldConfig.isOverlayEnabled() && this.guiProperties != null && this.hasRoom;
	}

	private static Rectangle2d getDisplayArea(IGuiProperties guiProperties) {
		final int x = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + BORDER_PADDING;
		final int y = BORDER_PADDING;
		final int width = guiProperties.getScreenWidth() - x - BORDER_PADDING;
		final int height = guiProperties.getScreenHeight() - y - BORDER_PADDING;
		return new Rectangle2d(x, y, width, height);
	}

	public void updateScreen(@Nullable Screen guiScreen, boolean forceUpdate) {
		final boolean wasDisplayed = isListDisplayed();
		IGuiProperties guiProperties = guiScreenHelper.getGuiProperties(guiScreen);
		if (guiProperties == null) {
			if (this.guiProperties != null) {
				this.guiProperties = null;
				setKeyboardFocus(false);
				this.ghostIngredientDragManager.stopDrag();
			}
		} else {
			if (forceUpdate || this.guiProperties == null || !GuiProperties.areEqual(this.guiProperties, guiProperties)) {
				this.guiProperties = guiProperties;
				this.displayArea = getDisplayArea(guiProperties);

				final boolean searchBarCentered = isSearchBarCentered(guiProperties);
				final int searchHeight = searchBarCentered ? 0 : SEARCH_HEIGHT + BORDER_PADDING;

				Set<Rectangle2d> guiExclusionAreas = guiScreenHelper.getGuiExclusionAreas();
				Rectangle2d availableContentsArea = new Rectangle2d(
					displayArea.getX(),
					displayArea.getY(),
					displayArea.getWidth(),
					displayArea.getHeight() - searchHeight
				);
				hasRoom = this.contents.updateBounds(availableContentsArea, guiExclusionAreas, 4 * BUTTON_SIZE);

				// update area to match contents size
				Rectangle2d contentsArea = this.contents.getArea();
				displayArea = new Rectangle2d(
					contentsArea.getX(),
					displayArea.getY(),
					contentsArea.getWidth(),
					displayArea.getHeight()
				);

				if (searchBarCentered && isListDisplayed()) {
					searchField.updateBounds(new Rectangle2d(
						guiProperties.getGuiLeft(),
						guiProperties.getScreenHeight() - SEARCH_HEIGHT - BORDER_PADDING,
						guiProperties.getGuiXSize() - BUTTON_SIZE + 1,
						SEARCH_HEIGHT
					));
				} else {
					searchField.updateBounds(new Rectangle2d(
						displayArea.getX(),
						displayArea.getY() + displayArea.getHeight() - SEARCH_HEIGHT - BORDER_PADDING,
						displayArea.getWidth() - BUTTON_SIZE + 1,
						SEARCH_HEIGHT
					));
				}

				this.configButton.updateBounds(new Rectangle2d(
					searchField.x + searchField.getWidth() - 1,
					searchField.y,
					BUTTON_SIZE,
					BUTTON_SIZE
				));

				updateLayout(false);
			}
		}
		if (wasDisplayed && !isListDisplayed()) {
			worldConfig.saveFilterText();
		}
	}

	public void updateLayout(boolean filterChanged) {
		this.contents.updateLayout(filterChanged);
		this.searchField.update();
	}

	public void drawScreen(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (this.guiProperties != null) {
			if (isListDisplayed()) {
				RenderSystem.disableLighting();
				this.searchField.renderButton(matrixStack, mouseX, mouseY, partialTicks);
				this.contents.draw(minecraft, matrixStack, mouseX, mouseY, partialTicks);
				this.configButton.draw(matrixStack, mouseX, mouseY, partialTicks);
			} else {
				this.configButton.draw(matrixStack, mouseX, mouseY, partialTicks);
			}
		}
	}

	public void drawTooltips(Minecraft minecraft, MatrixStack matrixStack, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.configButton.drawTooltips(matrixStack, mouseX, mouseY);
			this.ghostIngredientDragManager.drawTooltips(minecraft, matrixStack, mouseX, mouseY);
			this.contents.drawTooltips(minecraft, matrixStack, mouseX, mouseY);
		} else if (this.guiProperties != null) {
			this.configButton.drawTooltips(matrixStack, mouseX, mouseY);
		}
	}

	public void drawOnForeground(Minecraft minecraft, MatrixStack matrixStack, ContainerScreen<?> gui, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			matrixStack.push();
			matrixStack.translate(-gui.getGuiLeft(), -gui.getGuiTop(), 0);
			this.ghostIngredientDragManager.drawOnForeground(minecraft, matrixStack, mouseX, mouseY);
			matrixStack.pop();
		}
	}

	public void handleTick() {
		if (this.isListDisplayed()) {
			this.searchField.tick();
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			if (ClientConfig.getInstance().isCenterSearchBarEnabled() && searchField.isMouseOver(mouseX, mouseY)) {
				return true;
			}
			return MathUtil.contains(displayArea, mouseX, mouseY) &&
				!guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY);
		} else if (this.guiProperties != null) {
			return this.configButton.isMouseOver(mouseX, mouseY);
		}
		return false;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
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
	public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (isListDisplayed()) {
			if (this.ghostIngredientDragManager.handleMouseClicked(mouseX, mouseY)) {
				return true;
			}

			if (this.configButton.handleMouseClick(mouseX, mouseY, mouseButton)) {
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

			Minecraft minecraft = Minecraft.getInstance();
			Screen currentScreen = minecraft.currentScreen;
			InputMappings.Input input = InputMappings.Type.MOUSE.getOrMakeInput(mouseButton);
			if (currentScreen != null && !(currentScreen instanceof RecipesGui) &&
				(mouseButton == 0 || mouseButton == 1 || minecraft.gameSettings.keyBindPickBlock.isActiveAndMatches(input))) {
				IClickedIngredient<?> clicked = getIngredientUnderMouse(mouseX, mouseY);
				if (clicked != null) {
					if (worldConfig.isCheatItemsEnabled()) {
						ItemStack itemStack = clicked.getCheatItemStack();
						if (!itemStack.isEmpty()) {
							CommandUtil.giveStack(itemStack, input);
						}
						clicked.onClickHandled();
						return true;
					}
					ClientPlayerEntity player = minecraft.player;
					if (player != null) {
						ItemStack mouseItem = player.inventory.getItemStack();
						if (mouseItem.isEmpty() && this.ghostIngredientDragManager.handleClickGhostIngredient(currentScreen, clicked)) {
							return true;
						}
					}
				}
			}
		} else if (this.guiProperties != null) {
			return this.configButton.handleMouseClick(mouseX, mouseY, mouseButton);
		}
		return false;
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
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

	public boolean onGlobalKeyPressed(InputMappings.Input input) {
		if (isListDisplayed()) {
			if (KeyBindings.toggleCheatMode.isActiveAndMatches(input)) {
				worldConfig.toggleCheatItemsEnabled();
				return true;
			}
			if (KeyBindings.toggleEditMode.isActiveAndMatches(input)) {
				worldConfig.toggleEditModeEnabled();
				return true;
			}
			if (KeyBindings.focusSearch.isActiveAndMatches(input)) {
				setKeyboardFocus(true);
				return true;
			}
		}
		return false;
	}

	public boolean onCharTyped(char codePoint, int modifiers) {
		if (isListDisplayed() &&
			hasKeyboardFocus() &&
			searchField.charTyped(codePoint, modifiers)) {
			boolean changed = worldConfig.setFilterText(searchField.getText());
			if (changed) {
				updateLayout(true);
			}
			return true;
		}
		return false;
	}

	public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
		if (isListDisplayed()) {
			if (hasKeyboardFocus() &&
				searchField.keyPressed(keyCode, scanCode, modifiers)) {
				boolean changed = worldConfig.setFilterText(searchField.getText());
				if (changed) {
					updateLayout(true);
				}
				return true;
			}
			return this.contents.onKeyPressed(keyCode, scanCode, modifiers);
		}
		return false;
	}

	@Nullable
	@Override
	public Object getIngredientUnderMouse() {
		if (isListDisplayed()) {
			IIngredientListElement<?> elementUnderMouse = this.contents.getElementUnderMouse();
			if (elementUnderMouse != null) {
				return elementUnderMouse.getIngredient();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		Object ingredient = getIngredientUnderMouse();
		if (ingredientType.getIngredientClass().isInstance(ingredient)) {
			return (T) ingredient;
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
			List<IIngredientListElement<?>> visibleElements = this.contents.getVisibleElements();
			for (IIngredientListElement<?> element : visibleElements) {
				Object ingredient = element.getIngredient();
				visibleIngredients.add(ingredient);
			}

			return visibleIngredients.build();
		}
		return ImmutableList.of();
	}

}
