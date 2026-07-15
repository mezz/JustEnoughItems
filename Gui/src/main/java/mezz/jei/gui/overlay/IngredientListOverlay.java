package mezz.jei.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.file.IConfigListener;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.gui.GuiProperties;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.input.ICharTypedHandler;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.MouseUtil;
import mezz.jei.gui.input.handlers.CombinedDragHandler;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.NullDragHandler;
import mezz.jei.gui.input.handlers.NullInputHandler;
import mezz.jei.gui.input.handlers.ProxyDragHandler;
import mezz.jei.gui.input.handlers.ProxyInputHandler;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryOverlay;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IngredientGridWithNavigation;
import net.minecraft.client.Minecraft;
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
	private final LookupHistoryOverlay lookupHistoryOverlay;
	private final IClientConfig clientConfig;
	private final IWorldConfig worldConfig;
	private final GuiTextFieldFilter searchField;
	private final IInternalKeyMappings keyBindings;
	private final ScreenPropertiesCache screenPropertiesCache;
	private final IFilterTextSource filterTextSource;
	private String lastFilterText = "";

	// these need to be stored as strong references here because listeners are weakly stored elsewhere
	@SuppressWarnings("FieldCanBeLocal")
	private final IConfigListener<Boolean> lookupHistoryEnabledListener;
	@SuppressWarnings("FieldCanBeLocal")
	private final IConfigListener<HistoryDisplaySide> lookupHistoryViewSideListener;

	public IngredientListOverlay(
		IIngredientGridSource ingredientGridSource,
		IFilterTextSource filterTextSource,
		IScreenHelper screenHelper,
		IngredientGridWithNavigation contents,
		LookupHistoryOverlay lookupHistoryOverlay,
		IIngredientGridConfig ingredientGridConfig,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		IInternalKeyMappings keyBindings
	) {
		this.screenPropertiesCache = new ScreenPropertiesCache(screenHelper);
		this.contents = contents;
		this.lookupHistoryOverlay = lookupHistoryOverlay;
		this.clientConfig = clientConfig;
		this.worldConfig = worldConfig;

		this.searchField = new GuiTextFieldFilter(contents::isEmpty);
		this.keyBindings = keyBindings;
		this.filterTextSource = filterTextSource;
		this.searchField.setValue(filterTextSource.getFilterText());
		this.lastFilterText = filterTextSource.getFilterText();
		this.searchField.setFocused(false);
		this.searchField.setResponder(filterTextSource::setFilterText);
		filterTextSource.addListener(this::onFilterTextChanged);

		ingredientGridSource.addSourceListChangedListener(() -> {
			Minecraft minecraft = Minecraft.getInstance();
			getScreenPropertiesUpdater()
				.updateScreen(minecraft.screen)
				.update();
		});

		this.configButton = ConfigButton.create(this::isListDisplayed, worldConfig, keyBindings);
		clientConfig.addCenterSearchBarEnabledListener(v -> onScreenPropertiesChanged());
		ingredientGridConfig.addLayoutListener(this::onScreenPropertiesChanged);

		this.lookupHistoryEnabledListener = v -> onScreenPropertiesChanged();
		this.lookupHistoryViewSideListener = v -> onScreenPropertiesChanged();

		clientConfig.addLookupHistoryEnabledListener(lookupHistoryEnabledListener);
		clientConfig.addLookupHistoryDisplaySideListener(lookupHistoryViewSideListener);
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

	public ScreenPropertiesCache.Updater getScreenPropertiesUpdater() {
		return this.screenPropertiesCache.getUpdater(this::onScreenPropertiesChanged);
	}

	private void onScreenPropertiesChanged() {
		screenPropertiesCache.getGuiProperties()
			.ifPresentOrElse(guiProperties -> {
				ImmutableRect2i displayArea = createDisplayArea(guiProperties);
				Set<ImmutableRect2i> guiExclusionAreas = screenPropertiesCache.getGuiExclusionAreas();
				updateBounds(guiProperties, displayArea, guiExclusionAreas);
			}, () -> {
				this.contents.close();
				this.lookupHistoryOverlay.close();
				this.searchField.setFocused(false);
			});
	}

	private void updateBounds(IGuiProperties guiProperties, ImmutableRect2i displayArea, Set<ImmutableRect2i> guiExclusionAreas) {
		final boolean searchBarCentered = isSearchBarCentered(this.clientConfig, guiProperties);

		ImmutableRect2i availableContentsArea = getAvailableContentsArea(displayArea, searchBarCentered);
		IElement<?> pageAnchorElement = this.contents.getPageAnchorElement();
		if (clientConfig.isLookupHistoryEnabled() && lookupHistoryOverlay.isOnSide()) {
			int historyRows = clientConfig.getMaxLookupHistoryRows();
			int historyHeight = historyRows * LookupHistoryOverlay.SLOT_HEIGHT;
			if (historyHeight > 0) {
				ImmutableRect2i historyArea = getLookupHistoryArea(displayArea, searchBarCentered, historyHeight);
				availableContentsArea = cropBottomTo(availableContentsArea, historyArea.y());
				this.lookupHistoryOverlay.updateBounds(historyArea, guiExclusionAreas, null);
				this.lookupHistoryOverlay.updateLayout();
			}
		}
		this.contents.updateBounds(availableContentsArea, guiExclusionAreas, null);
		this.contents.updateLayoutKeepingPageAnchorVisible(pageAnchorElement);

		final ImmutableRect2i searchAndConfigArea = getSearchAndConfigArea(displayArea, searchBarCentered, guiProperties);
		final ImmutableRect2i searchArea = searchAndConfigArea.cropRight(BUTTON_SIZE);
		final ImmutableRect2i configButtonArea = searchAndConfigArea.keepRight(BUTTON_SIZE);

		this.searchField.setValue(filterTextSource.getFilterText());
		this.searchField.updateBounds(searchArea);

		this.configButton.updateBounds(configButtonArea);
	}

	private void onFilterTextChanged(String filterText) {
		this.searchField.setValue(filterText);
		if (!this.lastFilterText.isEmpty() && filterText.isEmpty()) {
			this.contents.updateLayoutToFirstPage();
		}
		this.lastFilterText = filterText;
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

	private static ImmutableRect2i getLookupHistoryArea(ImmutableRect2i displayArea, boolean searchBarCentered, int lookupHistoryHeight) {
		int bottomReservedHeight = searchBarCentered ? 0 : SEARCH_HEIGHT + INNER_PADDING;
		return displayArea
			.insetBy(BORDER_MARGIN)
			.cropBottom(bottomReservedHeight)
			.keepBottom(lookupHistoryHeight);
	}

	private static ImmutableRect2i cropBottomTo(ImmutableRect2i area, int bottomY) {
		int cropAmount = getBottom(area) - bottomY;
		if (cropAmount <= 0) {
			return area;
		}
		return area.cropBottom(cropAmount);
	}

	private static int getBottom(ImmutableRect2i area) {
		return area.y() + area.height();
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
			this.searchField.render(poseStack, mouseX, mouseY, partialTicks);
			this.contents.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
			this.lookupHistoryOverlay.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
		}
		if (this.screenPropertiesCache.hasValidScreen()) {
			this.configButton.draw(poseStack, mouseX, mouseY, partialTicks);
		}
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.contents.drawTooltips(minecraft, poseStack, mouseX, mouseY);
			this.lookupHistoryOverlay.drawTooltips(minecraft, poseStack, mouseX, mouseY);
		}
		if (this.screenPropertiesCache.hasValidScreen()) {
			this.configButton.drawTooltips(poseStack, mouseX, mouseY);
		}
	}

	public void drawOnForeground(PoseStack poseStack, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.contents.drawOnForeground(poseStack, mouseX, mouseY);
			this.lookupHistoryOverlay.drawOnForeground(poseStack, mouseX, mouseY);
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
			return Stream.concat(this.contents.getIngredientUnderMouse(mouseX, mouseY), this.lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY));
		}
		if (this.lookupHistoryOverlay.isListDisplayed()) {
			return this.lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			return Stream.concat(this.contents.getDraggableIngredientUnderMouse(mouseX, mouseY), this.lookupHistoryOverlay.getDraggableIngredientUnderMouse(mouseX, mouseY));
		}
		if (this.lookupHistoryOverlay.isListDisplayed()) {
			return this.lookupHistoryOverlay.getDraggableIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	public IUserInputHandler createInputHandler() {
		final IUserInputHandler displayedInputHandler = new CombinedInputHandler(
			"IngredientListOverlay",
			this.searchField.createInputHandler(),
			this.configButton.createInputHandler(),
			this.contents.createInputHandler()
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

	public IUserInputHandler createDeleteItemInputHandler() {
		final IUserInputHandler deleteItemInputHandler = this.contents.createDeleteItemInputHandler();

		return new ProxyInputHandler(() -> {
			if (isListDisplayed()) {
				return deleteItemInputHandler;
			}
			return NullInputHandler.INSTANCE;
		});
	}

	public IDragHandler createDragHandler() {
		final IDragHandler combinedDragHandler = new CombinedDragHandler(
			this.contents.createDragHandler(),
			this.lookupHistoryOverlay.createDragHandler()
		);

		return new ProxyDragHandler(() -> {
			if (isListDisplayed()) {
				return combinedDragHandler;
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
			return getIngredientUnderMouse(mouseX, mouseY)
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
			return getIngredientUnderMouse(mouseX, mouseY)
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
