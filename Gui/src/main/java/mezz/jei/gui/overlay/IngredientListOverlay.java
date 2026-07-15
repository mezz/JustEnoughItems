package mezz.jei.gui.overlay;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IScreenHelper;
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
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.MouseUtil;
import mezz.jei.gui.input.handlers.CombinedDragHandler;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.NullDragHandler;
import mezz.jei.gui.input.handlers.NullInputHandler;
import mezz.jei.gui.input.handlers.ProxyDragHandler;
import mezz.jei.gui.input.handlers.ProxyInputHandler;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistoryOverlay;
import mezz.jei.gui.overlay.ingredients.IIngredientGridSource;
import mezz.jei.gui.overlay.ingredients.IIngredientListOverlayContents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class IngredientListOverlay implements IIngredientListOverlay, IRecipeFocusSource, ICharTypedHandler {
	private final IconButton configButton;
	private final IIngredientListOverlayContents contents;
	private final LookupHistoryOverlay lookupHistoryOverlay;
	private final IClientToggleState toggleState;
	private final GuiTextFieldFilter searchField;
	private final IngredientListOverlayController controller;
	private boolean screenPropertiesDirty;

	public IngredientListOverlay(
		IIngredientGridSource ingredientGridSource,
		IFilterTextSource filterTextSource,
		IScreenHelper screenHelper,
		IIngredientListOverlayContents contents,
		LookupHistoryOverlay lookupHistoryOverlay,
		IIngredientGridConfig ingredientGridConfig,
		IClientConfig clientConfig,
		IClientToggleState toggleState,
		IInternalKeyMappings keyBindings
	) {
		GuiPropertiesCache<Screen> guiPropertiesCache = new GuiPropertiesCache<>(
			screen -> screenHelper.getGuiProperties(screen)
				.orElse(null)
		);
		this.contents = contents;
		this.lookupHistoryOverlay = lookupHistoryOverlay;
		this.toggleState = toggleState;

		this.searchField = new GuiTextFieldFilter(contents::isEmpty);
		this.configButton = new IconButton(new ConfigButtonController(this::isListDisplayed, toggleState, keyBindings));
		this.controller = IngredientListOverlayController.create(
			guiPropertiesCache,
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

		clientConfig.centerSearchBarEnabled().addListener(v -> markScreenPropertiesDirty());
		clientConfig.lookupHistoryEnabled().addListener(v -> markScreenPropertiesDirty());
		clientConfig.maxLookupHistoryRows().addListener(v -> markScreenPropertiesDirty());
		clientConfig.lookupHistoryDisplaySide().addListener(v -> markScreenPropertiesDirty());
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
		gridConfig.maxColumns().addListener(v -> markScreenPropertiesDirty());
		gridConfig.maxRows().addListener(v -> markScreenPropertiesDirty());
		gridConfig.drawBackground().addListener(v -> markScreenPropertiesDirty());
		gridConfig.layoutMode().addListener(v -> markScreenPropertiesDirty());
		gridConfig.navigationMode().addListener(v -> markScreenPropertiesDirty());
		gridConfig.horizontalAlignment().addListener(v -> markScreenPropertiesDirty());
		gridConfig.verticalAlignment().addListener(v -> markScreenPropertiesDirty());
		gridConfig.navigationVisibility().addListener(v -> markScreenPropertiesDirty());
	}

	private void updateScreenPropertiesIfDirty() {
		if (this.screenPropertiesDirty) {
			this.screenPropertiesDirty = false;
			Minecraft minecraft = Minecraft.getInstance();
			getScreenPropertiesUpdater()
				.updateScreen(minecraft.screen)
				.forceUpdate();
		}
	}

	public IScreenPropertiesUpdater getScreenPropertiesUpdater() {
		return this.controller.getScreenPropertiesUpdater();
	}

	public void drawScreen(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		updateScreenPropertiesIfDirty();
		drawBackground(guiGraphics);
		drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
	}

	public void drawBackground(GuiGraphics guiGraphics) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.searchField.drawBackground(guiGraphics);
			this.contents.drawBackground(guiGraphics);
		}
		if (this.controller.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.drawBackground(guiGraphics);
		}
	}

	public void drawForeground(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.searchField.drawForeground(guiGraphics, mouseX, mouseY, partialTicks);
			this.contents.drawForeground(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
		}
		if (this.controller.hasValidScreen()) {
			this.configButton.draw(guiGraphics, mouseX, mouseY, partialTicks);

		}
		if (this.controller.hasValidScreen() && toggleState.isOverlayEnabled()) {
			this.lookupHistoryOverlay.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	public void drawTooltips(Minecraft minecraft, GuiGraphics guiGraphics, int mouseX, int mouseY) {
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

	public void drawOnForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		updateScreenPropertiesIfDirty();
		if (isListDisplayed()) {
			this.contents.drawOnForeground(guiGraphics, mouseX, mouseY);
		}
		this.lookupHistoryOverlay.drawOnForeground(guiGraphics, mouseX, mouseY);
	}

	public void tick() {
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
			if (this.controller.hasValidScreen()) {
				return configButtonInputHandler;
			}
			return NullInputHandler.INSTANCE;
		});
	}

	public IDragHandler createDragHandler() {
		final IDragHandler combinedDragHandlers = new CombinedDragHandler(
			this.contents.createDragHandler(),
			this.lookupHistoryOverlay.createDragHandler()
		);

		return new ProxyDragHandler(() -> {
			if (isListDisplayed()) {
				return combinedDragHandlers;
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
