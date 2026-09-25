package mezz.jei.gui.overlay;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.input.ICharTypedHandler;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDragHandler;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.gui.input.handlers.CombinedDragHandler;
import mezz.jei.common.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.NullDragHandler;
import mezz.jei.gui.input.handlers.NullInputHandler;
import mezz.jei.gui.input.handlers.ProxyDragHandler;
import mezz.jei.gui.input.handlers.ProxyInputHandler;
import mezz.jei.gui.input.handlers.SearchInputLayer;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryOverlay;
import mezz.jei.gui.overlay.ingredients.IngredientGridBackgroundRenderer;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IIngredientListOverlayContents;
import mezz.jei.gui.search.ISearchCompletionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class IngredientListOverlay implements IIngredientListOverlay, IRecipeFocusSource, ICharTypedHandler {
	private final IconButton configButton;
	private final IIngredientListOverlayContents contents;
	private final LookupHistoryOverlay lookupHistoryOverlay;
	private final IClientToggleState toggleState;
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

		this.searchField = new GuiTextFieldFilter(contents::isEmpty, clientConfig, searchCompletionProvider);
		this.searchInputLayer = new SearchInputLayer(this.searchField, this::isListDisplayed);
		this.configButton = new IconButton(new ConfigButtonController(this::isListDisplayed, toggleState, keyBindings));
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

	private void markScreenPropertiesDirty() {
		this.screenPropertiesDirty = true;
	}

	private void addGridConfigListeners(IIngredientGridConfig gridConfig) {
		Internal.registerRuntimeListenerRemoval(gridConfig.maxColumns().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.maxRows().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.drawBackground().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.layoutMode().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationMode().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.horizontalAlignment().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.verticalAlignment().addListener(v -> markScreenPropertiesDirty()));
		Internal.registerRuntimeListenerRemoval(gridConfig.navigationVisibility().addListener(v -> markScreenPropertiesDirty()));
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
		if (contentsDisplayed) {
			this.searchField.extractBackgroundRenderState(guiGraphics);
			if (this.contents.isBackgroundEnabled()) {
				backgroundPanels.add(new IngredientGridBackgroundRenderer.Panel(
					this.contents.getBackgroundArea(),
					this.contents.getSlotBackgroundArea()
				));
			}
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

	public IUserInputHandler createInputHandler() {
		final IUserInputHandler lookupHistoryInputHandler = this.lookupHistoryOverlay.createInputHandler();
		final IUserInputHandler displayedLookupHistoryInputHandler = new ProxyInputHandler(() -> {
			if (this.controller.hasValidScreen() &&
				toggleState.isOverlayEnabled() &&
				this.lookupHistoryOverlay.isListDisplayed()
			) {
				return lookupHistoryInputHandler;
			}
			return NullInputHandler.INSTANCE;
		});
		final IUserInputHandler displayedInputHandler = new CombinedInputHandler(
			"IngredientListOverlay",
			this.configButton.createInputHandler(),
			this.contents.createInputHandler(),
			displayedLookupHistoryInputHandler
		);

		final IUserInputHandler configAndLookupHistoryInputHandler = new CombinedInputHandler(
			"IngredientListOverlayControls",
			this.configButton.createInputHandler(),
			displayedLookupHistoryInputHandler
		);

		return new ProxyInputHandler(() -> {
			if (isListDisplayed()) {
				return displayedInputHandler;
			}
			if (this.controller.hasValidScreen()) {
				return configAndLookupHistoryInputHandler;
			}
			return NullInputHandler.INSTANCE;
		});
	}

	public IUserInputHandler getResizeInputHandler() {
		IUserInputHandler historyResize = new ProxyInputHandler(() -> {
			updateScreenPropertiesIfDirty();
			if (controller.hasValidScreen() && toggleState.isOverlayEnabled() && lookupHistoryOverlay.isListDisplayed()) {
				return lookupHistoryOverlay.getResizeInputHandler();
			}
			return NullInputHandler.INSTANCE;
		});
		IUserInputHandler contentsResize = new ProxyInputHandler(() -> {
			if (isListDisplayed()) {
				return contents.getResizeInputHandler();
			}
			return NullInputHandler.INSTANCE;
		});
		return new CombinedInputHandler("IngredientListResize", historyResize, contentsResize);
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
		final IDragHandler lookupHistoryDragHandler = this.lookupHistoryOverlay.createDragHandler();
		final IDragHandler combinedDragHandlers = new CombinedDragHandler(
			this.contents.createDragHandler(),
			lookupHistoryDragHandler
		);

		return new ProxyDragHandler(() -> {
			if (isListDisplayed()) {
				return combinedDragHandlers;
			}
			if (this.controller.hasValidScreen() &&
				toggleState.isOverlayEnabled() &&
				this.lookupHistoryOverlay.isListDisplayed()
			) {
				return lookupHistoryDragHandler;
			}
			return NullDragHandler.INSTANCE;
		});
	}

	@Override
	public boolean hasKeyboardFocus() {
		return isListDisplayed() && this.searchField.isFocused();
	}

	@Override
	public boolean onCharTyped(CharacterEvent event) {
		return searchField.charTyped(event);
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
