package mezz.jei.gui.overlay;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.InventoryEffectRenderer;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.common.input.handlers.InputGroup.Child;
import mezz.jei.common.input.handlers.InputGroup;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.input.DragSource;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.InputArea;
import mezz.jei.gui.input.InputCommands;
import mezz.jei.gui.input.handlers.SearchInputLayer;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryOverlay;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IIngredientListOverlayContents;
import mezz.jei.gui.overlay.ingredients.IngredientGridBackgroundRenderer;
import mezz.jei.gui.search.ISearchCompletionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class IngredientListOverlay implements IIngredientListOverlay, IRecipeFocusSource {
	private final IconButton configButton;
	private final IIngredientListOverlayContents contents;
	private final LookupHistoryOverlay lookupHistoryOverlay;
	private final IClientToggleState toggleState;
	private final IClientConfig clientConfig;
	private final GuiPropertiesCache<Screen> guiPropertiesCache;
	private final IngredientGridBackgroundRenderer backgroundRenderer;
	private final GuiTextFieldFilter searchField;
	private final SearchInputLayer searchInputLayer;
	private final IngredientListOverlayController controller;
	private boolean screenPropertiesDirty;

	public IngredientListOverlay(
		IIngredientGridSource ingredientGridSource,
		IFilterTextSource filterTextSource,
		IScreenHelper screenHelper,
		IIngredientListOverlayContents contents,
		LookupHistoryOverlay lookupHistoryOverlay,
		IngredientGridBackgroundRenderer backgroundRenderer,
		IIngredientGridConfig ingredientGridConfig,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IInternalKeyMappings keyBindings,
		ISearchCompletionProvider searchCompletionProvider
	) {
		this.guiPropertiesCache = new GuiPropertiesCache<>(
			screen -> screenHelper.getGuiProperties(screen)
				.orElse(null)
		);
		this.contents = contents;
		this.lookupHistoryOverlay = lookupHistoryOverlay;
		this.backgroundRenderer = backgroundRenderer;
		this.toggleState = toggleState;
		this.clientConfig = clientConfig;

		this.searchField = new GuiTextFieldFilter(contents::isEmpty, clientConfig, searchCompletionProvider);
		this.searchInputLayer = new SearchInputLayer(this.searchField, this::isSearchDisplayed);
		this.configButton = new IconButton(new ConfigButtonController(this::isSearchDisplayed, toggleState, keyBindings));
		this.controller = IngredientListOverlayController.create(
			this.guiPropertiesCache,
			clientConfig,
			toggleState,
			keyBindings,
			filterTextSource,
			contents,
			contents,
			lookupHistoryOverlay,
			this.searchField,
			configButton::updateBounds
		);
		this.controller.init();
		this.searchField.setResponder(filterTextSource::setFilterText);

		ingredientGridSource.addSourceListChangedListener(this::markScreenPropertiesDirty);
		lookupHistoryOverlay.getLookupHistory().addSourceListChangedListener(this::markScreenPropertiesDirty);

		Internal.registerRuntimeListenerRemoval(clientConfig.searchBarPosition().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(clientConfig.lookupHistoryEnabled().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(clientConfig.maxLookupHistoryRows().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(clientConfig.maxLookupHistoryColumns().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(clientConfig.lookupHistoryDisplaySide().addListener(v -> markScreenPropertiesDirty()));
		addGridConfigListeners(ingredientGridConfig);
	}

	@Override
	public boolean isListDisplayed() {
		updateScreenPropertiesIfDirty();
		return this.controller.isListDisplayed();
	}

	/** Returns whether automatic compacting is enabled and full effect bars would overlap this overlay. */
	public boolean shouldRenderCompactInventoryEffects(AbstractContainerScreen<?> screen) {
		updateScreenPropertiesIfDirty();
		return this.clientConfig.compactInventoryEffects().get() &&
			InventoryEffectRenderer.getEffectAreas(screen, false).stream().anyMatch(this::intersects);
	}

	private boolean intersects(ImmutableRect2i area) {
		if (isListDisplayed() && this.contents.getBackgroundArea().intersects(area)) {
			return true;
		}
		if (isSearchDisplayed() && this.searchField.getArea().intersects(area)) {
			return true;
		}
		if (this.controller.hasValidScreen()) {
			if (this.configButton.isVisible() && this.configButton.getArea().intersects(area)) {
				return true;
			}
			if (this.toggleState.isOverlayEnabled() && this.lookupHistoryOverlay.isListDisplayed()) {
				return this.lookupHistoryOverlay.getBackgroundArea().intersects(area);
			}
		}
		return false;
	}

	private boolean isSearchDisplayed() {
		updateScreenPropertiesIfDirty();
		return this.controller.isSearchDisplayed();
	}

	private void markScreenPropertiesDirty() {
		this.screenPropertiesDirty = true;
	}

	private void addGridConfigListeners(IIngredientGridConfig gridConfig) {
		Internal.registerRuntimeListenerRemoval(gridConfig.maxColumns().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.maxRows().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.backgroundStyle().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.layoutMode().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationMode().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.horizontalAlignment().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.verticalAlignment().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationVisibility().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.shrinkToFit().addListener(v -> markScreenPropertiesDirty()));
	}

	private void updateScreenPropertiesIfDirty() {
		if (this.screenPropertiesDirty) {
			this.screenPropertiesDirty = false;
			Minecraft minecraft = Minecraft.getInstance();
			getScreenPropertiesUpdater()
				.updateScreen(minecraft.gui.screen())
				.forceUpdate();
		}
	}

	public IScreenPropertiesUpdater getScreenPropertiesUpdater() {
		return this.controller.getScreenPropertiesUpdater();
	}

	public SearchInputLayer getSearchInputLayer() {
		return this.searchInputLayer;
	}

	public void drawScreen(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		updateScreenPropertiesIfDirty();
		drawBackground(guiGraphics);
		drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
	}

	public void drawBackground(GuiGraphicsExtractor guiGraphics) {
		updateScreenPropertiesIfDirty();
		boolean contentsDisplayed = isListDisplayed();
		List<IngredientGridBackgroundRenderer.Panel> backgroundPanels = new ArrayList<>(2);
		if (isSearchDisplayed()) {
			this.searchField.extractBackgroundRenderState(guiGraphics);
		}
		if (contentsDisplayed && this.contents.isBackgroundEnabled()) {
			backgroundPanels.add(new IngredientGridBackgroundRenderer.Panel(
				this.contents.getBackgroundArea(),
				this.contents.getSlotBackgroundArea()
			));
		}
		boolean lookupHistoryDisplayed = this.controller.hasValidScreen() &&
			toggleState.isOverlayEnabled() &&
			this.lookupHistoryOverlay.isListDisplayed();
		if (lookupHistoryDisplayed && this.lookupHistoryOverlay.isBackgroundEnabled()) {
			backgroundPanels.add(new IngredientGridBackgroundRenderer.Panel(
				this.lookupHistoryOverlay.getBackgroundArea(),
				this.lookupHistoryOverlay.getSlotBackgroundArea()
			));
		}
		this.backgroundRenderer.draw(
			guiGraphics,
			backgroundPanels,
			this.guiPropertiesCache.getGuiExclusionAreas()
		);
	}

	public void drawForeground(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (isListDisplayed()) {
			this.contents.drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
		}
		if (isSearchDisplayed()) {
			this.searchField.extractForegroundRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		}
		if (this.controller.hasValidScreen()) {
			this.configButton.draw(guiGraphics, mouseX, mouseY, partialTicks);

		}
		if (this.controller.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	public void drawTooltips(Minecraft minecraft, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.contents.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		}
		if (this.controller.hasValidScreen()) {
			this.configButton.drawTooltips(guiGraphics, mouseX, mouseY);
		}
		if (this.controller.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		}
	}

	public void drawOnForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.contents.drawOnForeground(guiGraphics, mouseX, mouseY);
		}
		this.lookupHistoryOverlay.drawOnForeground(guiGraphics, mouseX, mouseY);
	}

	public void tick() {
		this.configButton.tick();
		if (isListDisplayed()) {
			this.contents.tick();
		}
		if (this.controller.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.tick();
		}
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			return Stream.concat(this.contents.getIngredientUnderMouse(mouseX, mouseY), this.lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY));
		}
		if (this.lookupHistoryOverlay.isListDisplayed()) {
			return this.lookupHistoryOverlay.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		updateScreenPropertiesIfDirty();
		if (!this.controller.hasValidScreen()) {
			return false;
		}
		if (this.configButton.isMouseOver(mouseX, mouseY)) {
			return true;
		}
		if (isListDisplayed() && this.contents.isMouseOver(mouseX, mouseY)) {
			return true;
		}
		if (isSearchDisplayed() && this.searchField.isMouseOver(mouseX, mouseY)) {
			return true;
		}
		return toggleState.isOverlayEnabled() && this.lookupHistoryOverlay.isMouseOver(mouseX, mouseY);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			return Stream.concat(this.contents.getDraggableIngredientUnderMouse(mouseX, mouseY), this.lookupHistoryOverlay.getDraggableIngredientUnderMouse(mouseX, mouseY));
		}
		if (this.lookupHistoryOverlay.isListDisplayed()) {
			return this.lookupHistoryOverlay.getDraggableIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	/** Creates this overlay's controls, shortcuts, and ingredient drag sources. */
	public InputArea createInputArea(IInternalKeyMappings keys) {
		InputCommands commands = new InputCommands();
		registerInputCommands(commands, keys);
		return InputArea.builder("Ingredients", this)
			.blockUnhandledMouseInput()
			.controls(createInputHandler())
			.commands(commands)
			.dragSources(createDragSources())
			.build();
	}

	private void registerInputCommands(InputCommands commands, IInternalKeyMappings keys) {
		contents.registerInputCommands(commands, keys, this::isListDisplayed);
		lookupHistoryOverlay.registerInputCommands(commands, keys, this::isHistoryDisplayed);
	}

	private boolean isHistoryDisplayed() {
		updateScreenPropertiesIfDirty();
		return controller.hasValidScreen() && toggleState.isOverlayEnabled() && lookupHistoryOverlay.isListDisplayed();
	}

	private IInputTarget createInputHandler() {
		return new InputGroup(
			"IngredientListOverlay",
			new Child(this::isHistoryDisplayed, lookupHistoryOverlay.getResizeInputHandler()),
			new Child(this::isListDisplayed, contents.getResizeInputHandler()),
			new Child(this::isListDisplayed, contents.createDeleteItemInputHandler()),
			new Child(controller::hasValidScreen, configButton.createInputHandler()),
			new Child(this::isListDisplayed, contents.createInputHandler()),
			new Child(this::isHistoryDisplayed, lookupHistoryOverlay.createInputHandler())
		);
	}

	private List<DragSource> createDragSources() {
		return List.of(
			new DragSource(this::isListDisplayed, contents::beginDrag),
			new DragSource(this::isHistoryDisplayed, lookupHistoryOverlay::beginDrag)
		);
	}

	@Override
	public boolean hasKeyboardFocus() {
		return isSearchDisplayed() && this.searchField.isFocused();
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
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			return this.contents.getVisibleIngredients(ingredientType)
				.toList();
		}
		return Collections.emptyList();
	}
}
