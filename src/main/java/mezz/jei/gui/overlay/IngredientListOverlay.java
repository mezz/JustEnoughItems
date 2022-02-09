package mezz.jei.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.mouse.ICharTypedHandler;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.CheatInputHandler;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import mezz.jei.input.mouse.handlers.NullInputHandler;
import mezz.jei.input.mouse.handlers.ProxyInputHandler;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Rectangle2dBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Tuple;

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
	private Rect2i displayArea = new Rect2i(0, 0, 0, 0);
	private boolean hasRoom;

	// properties of the gui we're next to
	@Nullable
	private IGuiProperties guiProperties;

	public IngredientListOverlay(
		IIngredientGridSource ingredientGridSource,
		IngredientManager ingredientManager,
		GuiScreenHelper guiScreenHelper,
		IngredientGridWithNavigation contents,
		IClientConfig clientConfig,
		IWorldConfig worldConfig
	) {
		this.guiScreenHelper = guiScreenHelper;
		this.contents = contents;
		this.clientConfig = clientConfig;
		this.worldConfig = worldConfig;
		ingredientGridSource.addListener(() -> onSetFilterText(worldConfig.getFilterText()));
		this.searchField = new GuiTextFieldFilter(ingredientGridSource);
		this.searchField.setResponder(text -> {
			if (this.worldConfig.setFilterText(text)) {
				updateLayout(true);
			}
		});
		this.configButton = ConfigButton.create(this, worldConfig);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.contents, guiScreenHelper, ingredientManager, worldConfig);
		this.searchField.setFocused(false);
	}

	public boolean isListDisplayed() {
		return worldConfig.isOverlayEnabled() && this.guiProperties != null && this.hasRoom;
	}

	private static Rect2i createDisplayArea(IGuiProperties guiProperties) {
		Rect2i screenRectangle = GuiProperties.getScreenRectangle(guiProperties);
		int guiRight = GuiProperties.getGuiRight(guiProperties);
		Tuple<Rect2i, Rect2i> result = MathUtil.splitX(screenRectangle, guiRight);
		Rect2i displayArea = result.getB();
		return new Rectangle2dBuilder(displayArea)
			.insetByPadding(BORDER_PADDING)
			.build();
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

		final boolean searchBarCentered = isSearchBarCentered(this.clientConfig, guiProperties);

		Set<Rect2i> guiExclusionAreas = guiScreenHelper.getGuiExclusionAreas();
		Rect2i availableContentsArea = new Rectangle2dBuilder(this.displayArea)
			.subtractHeight(searchBarCentered ? 0 : SEARCH_HEIGHT + BORDER_PADDING)
			.build();
		this.hasRoom = this.contents.updateBounds(availableContentsArea, guiExclusionAreas);

		// update area to match contents size
		Rect2i contentsArea = this.contents.getArea();
		this.displayArea = new Rectangle2dBuilder(this.displayArea)
			.setX(contentsArea)
			.setWidth(contentsArea)
			.build();

		Tuple<Rect2i, Rect2i> result = getSearchAndConfigArea(searchBarCentered, guiProperties, this.displayArea);
		Rect2i searchAndConfigArea = result.getB();

		result = MathUtil.splitXRight(searchAndConfigArea, BUTTON_SIZE);
		Rect2i searchArea = result.getA();
		Rect2i configButtonArea = result.getB();

		this.searchField.updateBounds(searchArea);
		this.configButton.updateBounds(configButtonArea);

		updateLayout(false);
	}

	private static boolean isSearchBarCentered(IClientConfig clientConfig, IGuiProperties guiProperties) {
		return clientConfig.isCenterSearchBarEnabled() &&
			GuiProperties.getGuiBottom(guiProperties) + SEARCH_HEIGHT < guiProperties.getScreenHeight();
	}

	private static Tuple<Rect2i, Rect2i> getSearchAndConfigArea(boolean searchBarCentered, IGuiProperties guiProperties, Rect2i displayArea) {
		if (searchBarCentered) {
			Rect2i guiRectangle = GuiProperties.getGuiRectangle(guiProperties);
			Rect2i searchRect = new Rect2i(
				guiRectangle.getX(),
				displayArea.getHeight() - SEARCH_HEIGHT,
				guiRectangle.getWidth(),
				SEARCH_HEIGHT
			);
			return new Tuple<>(displayArea, searchRect);
		} else {
			return MathUtil.splitYBottom(displayArea, SEARCH_HEIGHT);
		}
	}

	public void updateLayout(boolean filterChanged) {
		this.contents.updateLayout(filterChanged);
		this.searchField.setValue(this.worldConfig.getFilterText());
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
			poseStack.translate(-gui.getGuiLeft(), -gui.getGuiTop(), 0);
			this.ghostIngredientDragManager.drawOnForeground(minecraft, poseStack, mouseX, mouseY);
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

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(ingredientType)
				.orElse(null);
		}
		return null;
	}

	public void onSetFilterText(String filterText) {
		this.searchField.setValue(filterText);
		updateLayout(true);
	}

	@Override
	public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		if (isListDisplayed()) {
			return this.contents.getVisibleIngredients(ingredientType);
		}
		return Collections.emptyList();
	}

}
