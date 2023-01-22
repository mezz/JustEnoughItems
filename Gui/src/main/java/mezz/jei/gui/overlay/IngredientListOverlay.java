package mezz.jei.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IClickableIngredientInternal;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.gui.GuiProperties;
import mezz.jei.gui.config.IClientConfig;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.input.ICharTypedHandler;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.MouseUtil;
import mezz.jei.gui.input.handlers.CheatInputHandler;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.NullDragHandler;
import mezz.jei.gui.input.handlers.NullInputHandler;
import mezz.jei.gui.input.handlers.ProxyDragHandler;
import mezz.jei.gui.input.handlers.ProxyInputHandler;
import mezz.jei.gui.util.CheatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class IngredientListOverlay implements IIngredientListOverlay, IRecipeFocusSource, ICharTypedHandler {
	private static final int BORDER_MARGIN = 6;
	private static final int INNER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int SEARCH_HEIGHT = BUTTON_SIZE;

	private final GuiIconToggleButton configButton;
	private final IngredientGridWithNavigation contents;
	private final IClientConfig clientConfig;
	private final IWorldConfig worldConfig;
	private final IConnectionToServer serverConnection;
	private final GuiTextFieldFilter searchField;
	private final IInternalKeyMappings keyBindings;
	private final CheatUtil cheatUtil;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private final ScreenPropertiesCache screenPropertiesCache;

	public IngredientListOverlay(
		IIngredientGridSource ingredientGridSource,
		IFilterTextSource filterTextSource,
		IIngredientManager ingredientManager,
		IScreenHelper screenHelper,
		IngredientGridWithNavigation contents,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		IConnectionToServer serverConnection,
		Textures textures,
		IInternalKeyMappings keyBindings,
		CheatUtil cheatUtil
	) {
		this.screenPropertiesCache = new ScreenPropertiesCache(screenHelper);
		this.contents = contents;
		this.clientConfig = clientConfig;
		this.worldConfig = worldConfig;
		this.serverConnection = serverConnection;

		this.searchField = new GuiTextFieldFilter(textures);
		this.keyBindings = keyBindings;
		this.cheatUtil = cheatUtil;
		this.searchField.setValue(filterTextSource.getFilterText());
		this.searchField.setFocused(false);
		this.searchField.setResponder(filterTextSource::setFilterText);
		filterTextSource.addListener(this.searchField::setValue);

		ingredientGridSource.addSourceListChangedListener(() -> {
			Minecraft minecraft = Minecraft.getInstance();
			Screen screen = minecraft.screen;
			updateScreen(screen, null);
		});

		this.configButton = ConfigButton.create(this::isListDisplayed, worldConfig, textures, keyBindings);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.contents, screenHelper, ingredientManager, worldConfig);
	}

	@Override
	public boolean isListDisplayed() {
		// if there is no key binding to toggle it, force the overlay to display if possible
		return (worldConfig.isOverlayEnabled() || keyBindings.getToggleOverlay().isUnbound()) &&
			screenPropertiesCache.hasValidScreen() &&
			contents.hasRoom();
	}

	private static ImmutableRect2i createDisplayArea(IGuiProperties guiProperties) {
		ImmutableRect2i screenRectangle = GuiProperties.getScreenRectangle(guiProperties);
		int guiRight = GuiProperties.getGuiRight(guiProperties);
		return screenRectangle.cropLeft(guiRight);
	}

	public void updateScreen(@Nullable Screen guiScreen, @Nullable Set<ImmutableRect2i> updatedGuiExclusionAreas) {
		screenPropertiesCache.updateScreen(guiScreen, updatedGuiExclusionAreas, this::onScreenPropertiesChanged);
	}

	private void onScreenPropertiesChanged() {
		screenPropertiesCache.getGuiProperties()
			.ifPresentOrElse(guiProperties -> {
				ImmutableRect2i displayArea = createDisplayArea(guiProperties);
				Set<ImmutableRect2i> guiExclusionAreas = screenPropertiesCache.getGuiExclusionAreas();
				updateBounds(guiProperties, displayArea, guiExclusionAreas);
			}, () -> {
				this.ghostIngredientDragManager.stopDrag();
				this.searchField.setFocused(false);
			});
	}

	private void updateBounds(IGuiProperties guiProperties, ImmutableRect2i displayArea, Set<ImmutableRect2i> guiExclusionAreas) {
		final boolean searchBarCentered = isSearchBarCentered(this.clientConfig, guiProperties);

		final ImmutableRect2i availableContentsArea = getAvailableContentsArea(displayArea, searchBarCentered);
		this.contents.updateBounds(availableContentsArea, guiExclusionAreas);
		this.contents.updateLayout(false);

		final ImmutableRect2i searchAndConfigArea = getSearchAndConfigArea(displayArea, searchBarCentered, guiProperties);
		final ImmutableRect2i searchArea = searchAndConfigArea.cropRight(BUTTON_SIZE);
		final ImmutableRect2i configButtonArea = searchAndConfigArea.keepRight(BUTTON_SIZE);

		int searchTextColor = this.contents.isEmpty() ? 0xFFFF0000 : 0xFFFFFFFF;
		this.searchField.setTextColor(searchTextColor);
		this.searchField.updateBounds(searchArea);

		this.configButton.updateBounds(configButtonArea);
	}

	private static boolean isSearchBarCentered(IClientConfig clientConfig, IGuiProperties guiProperties) {
		return clientConfig.isCenterSearchBarEnabled() &&
			GuiProperties.getGuiBottom(guiProperties) + SEARCH_HEIGHT < guiProperties.getScreenHeight();
	}

	private ImmutableRect2i getAvailableContentsArea(ImmutableRect2i displayArea, boolean searchBarCentered) {
		if (searchBarCentered) {
			return displayArea;
		}
		return displayArea.cropBottom(SEARCH_HEIGHT + INNER_PADDING);
	}

	private ImmutableRect2i getSearchAndConfigArea(ImmutableRect2i displayArea, boolean searchBarCentered, IGuiProperties guiProperties) {
		displayArea = displayArea.insetBy(BORDER_MARGIN);
		if (searchBarCentered) {
			ImmutableRect2i guiRectangle = GuiProperties.getGuiRectangle(guiProperties);
			return displayArea
				.keepBottom(SEARCH_HEIGHT)
				.matchWidthAndX(guiRectangle);
		} else if (this.contents.hasRoom()) {
			final ImmutableRect2i contentsArea = this.contents.getBackgroundArea();
			return displayArea
				.keepBottom(SEARCH_HEIGHT)
				.matchWidthAndX(contentsArea);
		} else {
			return displayArea.keepBottom(SEARCH_HEIGHT);
		}
	}

	public void drawScreen(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (isListDisplayed()) {
			this.searchField.renderButton(poseStack, mouseX, mouseY, partialTicks);
			this.contents.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
		}
		if (this.screenPropertiesCache.hasValidScreen()) {
			this.configButton.draw(poseStack, mouseX, mouseY, partialTicks);
		}
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.ghostIngredientDragManager.drawTooltips(minecraft, poseStack, mouseX, mouseY);
			this.contents.drawTooltips(minecraft, poseStack, mouseX, mouseY);
		}
		if (this.screenPropertiesCache.hasValidScreen()) {
			this.configButton.drawTooltips(poseStack, mouseX, mouseY);
		}
	}

	public void drawOnForeground(Minecraft minecraft, PoseStack poseStack, AbstractContainerScreen<?> gui, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			poseStack.pushPose();
			{
				IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
				poseStack.translate(-screenHelper.getGuiLeft(gui), -screenHelper.getGuiTop(gui), 0);
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

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	public IUserInputHandler createInputHandler() {
		final IUserInputHandler displayedInputHandler = new CombinedInputHandler(
			this.searchField.createInputHandler(),
			this.configButton.createInputHandler(),
			this.contents.createInputHandler(),
			new CheatInputHandler(this.contents, worldConfig, clientConfig, serverConnection, cheatUtil)
		);

		final IUserInputHandler configButtonInputHandler = this.configButton.createInputHandler();

		return new ProxyInputHandler(() -> {
			if (isListDisplayed()) {
				return displayedInputHandler;
			}
			if (this.screenPropertiesCache.hasValidScreen()) {
				return configButtonInputHandler;
			}
			return NullInputHandler.INSTANCE;
		});
	}

	public IDragHandler createDragHandler() {
		final IDragHandler displayedDragHandler = this.ghostIngredientDragManager.createDragHandler();

		return new ProxyDragHandler(() -> {
			if (isListDisplayed()) {
				return displayedDragHandler;
			}
			return NullDragHandler.INSTANCE;
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
			double mouseX = MouseUtil.getX();
			double mouseY = MouseUtil.getY();
			return this.contents.getIngredientUnderMouse(mouseX, mouseY)
				.<ITypedIngredient<?>>map(IClickableIngredientInternal::getTypedIngredient)
				.findFirst();
		}
		return Optional.empty();
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		if (isListDisplayed()) {
			double mouseX = MouseUtil.getX();
			double mouseY = MouseUtil.getY();
			return this.contents.getIngredientUnderMouse(mouseX, mouseY)
				.map(IClickableIngredientInternal::getTypedIngredient)
				.map(i -> i.getIngredient(ingredientType))
				.flatMap(Optional::stream)
				.findFirst()
				.orElse(null);
		}
		return null;
	}

	@Override
	public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		if (isListDisplayed()) {
			return this.contents.getVisibleIngredients(ingredientType)
				.toList();
		}
		return Collections.emptyList();
	}
}
