package mezz.jei.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.MouseUtil;
import mezz.jei.input.mouse.ICharTypedHandler;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.CheatInputHandler;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import mezz.jei.input.mouse.handlers.NullInputHandler;
import mezz.jei.input.mouse.handlers.ProxyInputHandler;
import mezz.jei.util.MathUtil;
import mezz.jei.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class IngredientListOverlay implements IIngredientListOverlay, IRecipeFocusSource, ICharTypedHandler {
	private static final int BORDER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int SEARCH_HEIGHT = 20;

	private final GuiIconToggleButton configButton;
	private final IngredientGridWithNavigation contents;
	private final IClientConfig clientConfig;
	private final IWorldConfig worldConfig;
	private final GuiScreenHelper guiScreenHelper;
	private final GuiTextFieldFilter searchField;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private ImmutableRect2i displayArea = ImmutableRect2i.EMPTY;
	private boolean hasRoom;

	// properties of the gui we're next to
	@Nullable
	private IGuiProperties guiProperties;

	public IngredientListOverlay(
		IIngredientGridSource ingredientGridSource,
		RegisteredIngredients registeredIngredients,
		GuiScreenHelper guiScreenHelper,
		IngredientGridWithNavigation contents,
		IClientConfig clientConfig,
		IWorldConfig worldConfig
	) {
		this.guiScreenHelper = guiScreenHelper;
		this.contents = contents;
		this.clientConfig = clientConfig;
		this.worldConfig = worldConfig;

		this.searchField = new GuiTextFieldFilter(ingredientGridSource);
		this.searchField.setResponder(text -> {
			if (this.worldConfig.setFilterText(text)) {
				updateBounds(true);
			}
		});
		ingredientGridSource.addListener(() -> {
			this.searchField.setValue(worldConfig.getFilterText());
			updateBounds(true);
		});

		this.configButton = ConfigButton.create(this, worldConfig);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.contents, guiScreenHelper, registeredIngredients, worldConfig);
		this.searchField.setFocused(false);
	}

	public boolean isListDisplayed() {
		return worldConfig.isOverlayEnabled() && this.guiProperties != null && this.hasRoom;
	}

	private static ImmutableRect2i createDisplayArea(IGuiProperties guiProperties) {
		ImmutableRect2i screenRectangle = GuiProperties.getScreenRectangle(guiProperties);
		int guiRight = GuiProperties.getGuiRight(guiProperties);
		return screenRectangle
			.cropLeft(guiRight)
			.insetByPadding(BORDER_PADDING);
	}

	public void updateScreen(@Nullable Screen guiScreen, boolean exclusionAreasChanged) {
		final boolean wasDisplayed = isListDisplayed();
		IGuiProperties guiProperties = guiScreenHelper.getGuiProperties(guiScreen);
		if (guiProperties == null) {
			if (this.guiProperties != null) {
				this.guiProperties = null;
				this.searchField.setFocused(false);
				this.ghostIngredientDragManager.stopDrag();
			}
		} else {
			final boolean guiPropertiesChanged = this.guiProperties == null || !GuiProperties.areEqual(this.guiProperties, guiProperties);
			if (exclusionAreasChanged || guiPropertiesChanged) {
				updateNewScreen(guiProperties, guiPropertiesChanged);
			}
		}

		if (wasDisplayed && !isListDisplayed()) {
			worldConfig.saveFilterText();
		}
	}

	private void updateNewScreen(IGuiProperties guiProperties, boolean guiPropertiesChanged) {
		this.guiProperties = guiProperties;
		this.displayArea = createDisplayArea(guiProperties);
		if (guiPropertiesChanged) {
			this.ghostIngredientDragManager.stopDrag();
		}
		updateBounds(false);
	}

	private void updateBounds(boolean filterChanged) {
		if (this.guiProperties == null) {
			return;
		}
		final boolean searchBarCentered = isSearchBarCentered(this.clientConfig, this.guiProperties);

		final ImmutableRect2i availableContentsArea = getAvailableContentsArea(searchBarCentered);
		final Set<ImmutableRect2i> guiExclusionAreas = guiScreenHelper.getGuiExclusionAreas();
		this.hasRoom = this.contents.updateBounds(availableContentsArea, guiExclusionAreas);

		final ImmutableRect2i searchAndConfigArea = getSearchAndConfigArea(searchBarCentered, guiProperties);
		final ImmutableRect2i searchArea = searchAndConfigArea.cropRight(BUTTON_SIZE);
		final ImmutableRect2i configButtonArea = searchAndConfigArea.keepRight(BUTTON_SIZE);

		this.searchField.updateBounds(searchArea);
		this.configButton.updateBounds(configButtonArea);

		if (this.hasRoom) {
			this.contents.updateLayout(filterChanged);
		}
	}

	private static boolean isSearchBarCentered(IClientConfig clientConfig, IGuiProperties guiProperties) {
		return clientConfig.isCenterSearchBarEnabled() &&
			GuiProperties.getGuiBottom(guiProperties) + SEARCH_HEIGHT < guiProperties.getScreenHeight();
	}

	private ImmutableRect2i getAvailableContentsArea(boolean searchBarCentered) {
		if (searchBarCentered) {
			return this.displayArea;
		}
		return this.displayArea.cropBottom(SEARCH_HEIGHT + BORDER_PADDING);
	}

	private ImmutableRect2i getSearchAndConfigArea(boolean searchBarCentered, IGuiProperties guiProperties) {
		if (searchBarCentered) {
			ImmutableRect2i guiRectangle = GuiProperties.getGuiRectangle(guiProperties);
			return this.displayArea
				.keepBottom(SEARCH_HEIGHT)
				.matchWidthAndX(guiRectangle);
		} else if (this.hasRoom) {
			final ImmutableRect2i contentsArea = this.contents.getBackgroundArea();
			return this.displayArea
				.keepBottom(SEARCH_HEIGHT)
				.matchWidthAndX(contentsArea);
		} else {
			return this.displayArea.keepBottom(SEARCH_HEIGHT);
		}
	}

	public void drawScreen(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (isListDisplayed()) {
			this.searchField.renderButton(poseStack, mouseX, mouseY, partialTicks);
			this.contents.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
		}
		if (this.guiProperties != null) {
			this.configButton.draw(poseStack, mouseX, mouseY, partialTicks);
		}
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.ghostIngredientDragManager.drawTooltips(minecraft, poseStack, mouseX, mouseY);
			this.contents.drawTooltips(minecraft, poseStack, mouseX, mouseY);
		}
		if (this.guiProperties != null) {
			this.configButton.drawTooltips(poseStack, mouseX, mouseY);
		}
	}

	public void drawOnForeground(Minecraft minecraft, PoseStack poseStack, AbstractContainerScreen<?> gui, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			poseStack.pushPose();
			{
				poseStack.translate(-gui.getGuiLeft(), -gui.getGuiTop(), 0);
				this.ghostIngredientDragManager.drawOnForeground(minecraft, poseStack, mouseX, mouseY);
			}
			poseStack.popPose();
		}
	}

	public void handleTick() {
		if (this.isListDisplayed()) {
			this.searchField.tick();
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			if (this.clientConfig.isCenterSearchBarEnabled() && searchField.isMouseOver(mouseX, mouseY)) {
				return true;
			}
			return MathUtil.contains(displayArea, mouseX, mouseY) &&
				!guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY);
		}
		if (this.guiProperties != null) {
			return this.configButton.isMouseOver(mouseX, mouseY);
		}
		return false;
	}

	@Override
	public Optional<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Optional.empty();
	}

	public IUserInputHandler createInputHandler() {
		final IUserInputHandler displayedInputHandler = new CombinedInputHandler(
			this.ghostIngredientDragManager.createInputHandler(),
			this.searchField.createInputHandler(),
			this.configButton.createInputHandler(),
			this.contents.createInputHandler(),
			new CheatInputHandler(this.contents, worldConfig, clientConfig)
		);

		final IUserInputHandler hiddenInputHandler = this.configButton.createInputHandler();

		return new ProxyInputHandler(() -> {
			if (this.guiProperties == null) {
				return NullInputHandler.INSTANCE;
			}
			if (isListDisplayed()) {
				return displayedInputHandler;
			}
			return hiddenInputHandler;
		});
	}

	@Override
	public boolean hasKeyboardFocus() {
		return isListDisplayed() && this.searchField.isFocused();
	}

	@Override
	public boolean onCharTyped(char codePoint, int modifiers) {
		return searchField.charTyped(codePoint, modifiers);
	}

	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(MouseUtil.getX(), MouseUtil.getY())
				.map(IClickedIngredient::getValue);
		}
		return Optional.empty();
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(ingredientType)
				.map(ITypedIngredient::getIngredient)
				.orElse(null);
		}
		return null;
	}

	@Override
	public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		if (isListDisplayed()) {
			return this.contents.getVisibleIngredients(ingredientType)
				.map(ITypedIngredient::getIngredient)
				.toList();
		}
		return Collections.emptyList();
	}

}
