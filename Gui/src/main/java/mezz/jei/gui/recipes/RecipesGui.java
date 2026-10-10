package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.GuiProperties;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IGuiInputLayer;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.handlers.InputGroup;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.common.input.interaction.MouseDrag;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.StringUtil;
import mezz.jei.gui.bookmarks.BookmarkFactory;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.elements.ResizeDrag;
import mezz.jei.gui.elements.ResizeHandle;
import mezz.jei.gui.elements.ScrollbarWidget;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.InputArea;
import mezz.jei.gui.input.InputCommands;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistory;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import mezz.jei.gui.recipes.lookups.StaticFocusedRecipes;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class RecipesGui extends Screen implements IRecipesGui, IRecipeFocusSource {
	private static final int borderPadding = 6;
	private static final int navBarPadding = 2;
	private static final int titleInnerPadding = 14;
	private static final int smallButtonWidth = 13;
	private static final int smallButtonHeight = 13;
	private static final int minGuiWidth = IClientConfig.minRecipeGuiWidth;
	private final ResizeInputHandler resizeInputHandler = new ResizeInputHandler(this);
	private @Nullable ResizeDrag resizeDrag;

	private final IInternalKeyMappings keyBindings;
	private final BookmarkList bookmarks;
	private final IFocusFactory focusFactory;

	private int headerHeight;

	/* Internal logic for the gui, handles finding recipes */
	private final IRecipeGuiLogic logic;

	/* List of RecipeLayout to display */
	private final RecipeGuiLayouts layouts;
	private final ScrollbarWidget scrollbar;

	private String pageString = "1/1";
	private final ScalableDrawable background;

	private final CraftingStations craftingStations;
	private final RecipeGuiTabs recipeGuiTabs;
	private final RecipeOptionButtons optionButtons;
	private final IInputTarget inputHandler;

	private final IconButton nextRecipeCategory;
	private final IconButton previousRecipeCategory;
	private final IconButton nextPage;
	private final IconButton previousPage;
	private final InteractiveIngredientTooltipController interactiveIngredientTooltipController;

	private @Nullable Screen parentScreen;
	/**
	 * The GUI tries to size itself to this ideal area.
	 * This is a stable place to anchor buttons so that
	 * they don't move when the GUI resizes.
	 */
	private ImmutableRect2i idealArea = ImmutableRect2i.EMPTY;
	/**
	 * This is the actual are of the GUI, which temporarily
	 * stretches to fit large recipes.
	 */
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;

	private RecipeCategoryTitle recipeCategoryTitle = new RecipeCategoryTitle();

	private boolean init = false;

	public RecipesGui(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		RecipeTransferService recipeTransferService,
		IInternalKeyMappings keyBindings,
		IFocusFactory focusFactory,
		BookmarkList bookmarks,
		LookupHistory lookupHistory,
		IGuiHelper guiHelper,
		BookmarkFactory bookmarkFactory,
		FocusUtil focusUtil
	) {
		super(Component.literal("Recipes"));
		this.bookmarks = bookmarks;
		this.keyBindings = keyBindings;
		this.logic = new RecipeGuiLogic(
			recipeManager,
			ingredientManager,
			lookupHistory,
			recipeTransferService,
			this::updateLayout,
			focusFactory,
			bookmarkFactory
		);
		this.scrollbar = new ScrollbarWidget(this.logic);
		this.craftingStations = new CraftingStations(guiHelper);
		this.recipeGuiTabs = new RecipeGuiTabs(this.logic, recipeManager, guiHelper);
		this.optionButtons = new RecipeOptionButtons(this.logic::goToFirstPage);
		this.focusFactory = focusFactory;
		RecipeSlotClickTargetFactory clickTargetFactory = new RecipeSlotClickTargetFactory(
			recipeManager,
			keyBindings.getPauseRecipeCycling()::isDown
		);
		this.layouts = new RecipeGuiLayouts(clickTargetFactory);
		this.interactiveIngredientTooltipController = new InteractiveIngredientTooltipController(
			this,
			focusUtil,
			guiHelper,
			ingredientManager,
			clickTargetFactory
		);
		IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
		Internal.registerRuntimeListenerRemoval(clientConfig.searchBarPosition().addListener(v -> reopenIfOpen()));
		Internal.registerRuntimeListenerRemoval(clientConfig.maxRecipeGuiHeight().addListener(v -> reopenIfOpen()));
		Internal.registerRuntimeListenerRemoval(clientConfig.recipeGuiWidth().addListener(v -> reopenIfOpen()));
		Internal.registerRuntimeListenerRemoval(clientConfig.recipeGuiNavigationMode().addListener(v -> reopenIfOpen()));
		Internal.registerRuntimeListenerRemoval(clientConfig.maxRecipeGuiColumns().addListener(v -> reopenIfOpen()));
		Internal.registerRuntimeListenerRemoval(clientConfig.guiResizeEnabled().addListener(v -> resizeInputHandler.resetInput()));

		Textures textures = Internal.getTextures();
		IDrawableStatic arrowNext = textures.getArrowNext();
		IDrawableStatic arrowPrevious = textures.getArrowPrevious();

		ImmutableRect2i buttonSize = new ImmutableRect2i(0, 0, smallButtonWidth, smallButtonHeight);

		nextRecipeCategory = new IconButton(
			new IIconButtonController() {
				@Override
				public boolean onPress(IJeiUserInput input) {
					return input.isSimulate() || logic.nextRecipeCategory();
				}

				@Override
				public void initState(IButtonState state) {
					state.setIcon(arrowNext);
					updateState(state);
				}

				@Override
				public void updateState(IButtonState state) {
					state.setActive(logic.hasMultipleCategories());
				}
			},
			buttonSize
		);
		previousRecipeCategory = new IconButton(
			new IIconButtonController() {
				@Override
				public boolean onPress(IJeiUserInput input) {
					return input.isSimulate() || logic.previousRecipeCategory();
				}

				@Override
				public void initState(IButtonState state) {
					state.setIcon(arrowPrevious);
					updateState(state);
				}

				@Override
				public void updateState(IButtonState state) {
					state.setActive(logic.hasMultipleCategories());
				}
			},
			buttonSize
		);
		nextPage = new IconButton(
			new IIconButtonController() {
				@Override
				public boolean onPress(IJeiUserInput input) {
					return input.isSimulate() || logic.nextPage();
				}

				@Override
				public void initState(IButtonState state) {
					state.setIcon(arrowNext);
					updateState(state);
				}

				@Override
				public void updateState(IButtonState state) {
					state.setVisible(!logic.isScrolling());
					state.setActive(!logic.isScrolling() && logic.hasMultiplePages());
				}
			},
			buttonSize
		);
		previousPage = new IconButton(
			new IIconButtonController() {
				@Override
				public boolean onPress(IJeiUserInput input) {
					return input.isSimulate() || logic.previousPage();
				}

				@Override
				public void initState(IButtonState state) {
					state.setIcon(arrowPrevious);
					updateState(state);
				}

				@Override
				public void updateState(IButtonState state) {
					state.setVisible(!logic.isScrolling());
					state.setActive(!logic.isScrolling() && logic.hasMultiplePages());
				}
			},
			buttonSize
		);

		background = textures.getRecipeGuiBackground();

		inputHandler = new InputGroup(
			"RecipesGui",
			new RecipeCategoryScrollInputHandler(this),
			this.resizeInputHandler,
			this.scrollbar,
			this.layouts,
			new RecipeNavigationInputHandler(this),
			optionButtons.createInputHandler(),
			recipeGuiTabs,
			nextRecipeCategory.createInputHandler(),
			previousRecipeCategory.createInputHandler(),
			nextPage.createInputHandler(),
			previousPage.createInputHandler()
		);
	}

	public RecipeGuiLayouts getRecipeLayouts() {
		return layouts;
	}

	public ImmutableRect2i getArea() {
		return this.area;
	}

	public int getLeftSideExtraWidth() {
		if (craftingStations.isEmpty()) {
			return optionButtons.getWidth();
		}
		return Math.max(craftingStations.getWidth(), optionButtons.getWidth());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	@Override
	public void init() {
		super.init();
		this.resizeDrag = null;
		this.scrollbar.resetInput();
		updateSize();
	}

	private void updateSize() {
		IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
		int maxWidth = Math.max(minGuiWidth, this.width - 2 * Math.max(borderPadding, getLeftSideExtraWidth()));
		int xSize = Math.clamp(clientConfig.recipeGuiWidth().get(), minGuiWidth, maxWidth);
		RecipeGuiSizing.Size recipeGuiSize = RecipeGuiSizing.calculateInitialSize(
			this.height,
			clientConfig.searchBarPosition().get().isCentered(),
			clientConfig.maxRecipeGuiHeight().get()
		);
		int ySize = recipeGuiSize.ySize();
		int extraSpace = recipeGuiSize.extraSpace();

		final int guiLeft = (this.width - xSize) / 2;
		final int guiTop = RecipeGuiTab.TAB_HEIGHT + 21 + (extraSpace / 2);

		this.idealArea = new ImmutableRect2i(guiLeft, guiTop, xSize, ySize);
		this.area = this.idealArea;
		updateNavigation();
		this.init = true;
		updateLayout();
	}

	private void updateNavigation() {
		int guiLeft = idealArea.x();
		int guiTop = idealArea.y();
		int xSize = idealArea.width();
		final int rightButtonX = guiLeft + xSize - borderPadding - smallButtonWidth;
		final int leftButtonX = guiLeft + borderPadding;

		int titleHeight = font.lineHeight + borderPadding;
		int recipeClassButtonTop = guiTop + titleHeight - smallButtonHeight + navBarPadding;
		nextRecipeCategory.updateBounds(nextRecipeCategory.getArea().setPosition(rightButtonX, recipeClassButtonTop));
		previousRecipeCategory.updateBounds(previousRecipeCategory.getArea().setPosition(leftButtonX, recipeClassButtonTop));

		int pageButtonTop = recipeClassButtonTop + smallButtonHeight + navBarPadding;
		nextPage.updateBounds(nextPage.getArea().setPosition(rightButtonX, pageButtonTop));
		previousPage.updateBounds(previousPage.getArea().setPosition(leftButtonX, pageButtonTop));

		int headerBottom = pageButtonTop;
		if (logic.isScrolling()) {
			headerBottom = recipeClassButtonTop;
		}
		this.headerHeight = (headerBottom + smallButtonHeight) - guiTop;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		this.background.draw(graphics, area);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		ResizeHandle resizeHandle = getResizeHandle(mouseX, mouseY);
		if (resizeDrag != null) {
			resizeHandle = resizeDrag.handle();
		}
		resizeHandle.requestCursor(guiGraphics);

		guiGraphics.fill(
			previousRecipeCategory.getX() + previousRecipeCategory.getWidth(),
			previousRecipeCategory.getY(),
			nextRecipeCategory.getX(),
			nextRecipeCategory.getY() + nextRecipeCategory.getHeight(),
			JeiGuiColors.getColor(GuiColor.PAGE_NAVIGATION_BACKGROUND)
		);
		if (!logic.isScrolling()) {
			guiGraphics.fill(
				previousPage.getX() + previousPage.getWidth(),
				previousPage.getY(),
				nextPage.getX(),
				nextPage.getY() + nextPage.getHeight(),
				JeiGuiColors.getColor(GuiColor.PAGE_NAVIGATION_BACKGROUND)
			);
		}

		this.recipeCategoryTitle.draw(guiGraphics, font);

		if (!logic.isScrolling()) {
			ImmutableRect2i pageArea = MathUtil.union(previousPage.getArea(), nextPage.getArea());
			StringUtil.drawCenteredStringWithShadow(guiGraphics, font, pageString, pageArea, JeiGuiColors.getColor(GuiColor.PAGE_NAVIGATION_TEXT));
		}

		nextRecipeCategory.draw(guiGraphics, mouseX, mouseY, partialTicks);
		previousRecipeCategory.draw(guiGraphics, mouseX, mouseY, partialTicks);
		if (!logic.isScrolling()) {
			nextPage.draw(guiGraphics, mouseX, mouseY, partialTicks);
			previousPage.draw(guiGraphics, mouseX, mouseY, partialTicks);
		}
		this.scrollbar.draw(guiGraphics, mouseX, mouseY);

		updateInteractiveIngredientTooltip(mouseX, mouseY);

		Optional<IRecipeLayoutDrawable<?>> hoveredRecipeLayout = this.layouts.draw(guiGraphics, mouseX, mouseY);
		optionButtons.draw(guiGraphics, mouseX, mouseY, partialTicks);
		Optional<IRecipeSlotDrawable> hoveredRecipeCatalyst = craftingStations.draw(guiGraphics, mouseX, mouseY);

		recipeGuiTabs.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);

		if (!interactiveIngredientTooltipController.isVisible()) {
			this.layouts.drawTooltips(guiGraphics, mouseX, mouseY);

			optionButtons.drawTooltips(guiGraphics, mouseX, mouseY);

			hoveredRecipeLayout.ifPresent(l -> l.drawOverlays(guiGraphics, mouseX, mouseY));

			hoveredRecipeCatalyst.ifPresent(h -> {
				h.drawTooltip(guiGraphics, mouseX, mouseY);
			});
		}

		if (!interactiveIngredientTooltipController.isVisible() && recipeCategoryTitle.isMouseOver(mouseX, mouseY)) {
			JeiTooltip tooltip = new JeiTooltip();
			recipeCategoryTitle.getTooltip(tooltip);
			if (!logic.hasAllCategories()) {
				tooltip.addKeyUsageComponent("jei.tooltip.show.all.recipes.hotkey", keyBindings.getLeftClick());
			}
			tooltip.draw(guiGraphics, mouseX, mouseY);
		}

		if (DebugConfig.isDebugGuisEnabled()) {
			guiGraphics.fill(
				idealArea.getX(),
				idealArea.getY(),
				idealArea.getX() + idealArea.getWidth(),
				idealArea.getY() + idealArea.getHeight(),
				JeiGuiColors.getColor(GuiColor.DEBUG_RECIPE_GUI_IDEAL_AREA)
			);

			guiGraphics.fill(
				area.getX(),
				area.getY(),
				area.getX() + area.getWidth(),
				area.getY() + area.getHeight(),
				JeiGuiColors.getColor(GuiColor.DEBUG_RECIPE_GUI_AREA)
			);

			ImmutableRect2i recipeLayoutsArea = getRecipeLayoutsArea(this.area);
			guiGraphics.fill(
				recipeLayoutsArea.getX(),
				recipeLayoutsArea.getY(),
				recipeLayoutsArea.getX() + recipeLayoutsArea.getWidth(),
				recipeLayoutsArea.getY() + recipeLayoutsArea.getHeight(),
				JeiGuiColors.getColor(GuiColor.DEBUG_RECIPE_LAYOUTS_AREA)
			);
		}
	}

	private static ImmutableRect2i calculateAreaToFitLayouts(ImmutableRect2i idealArea, int screenWidth, int recipeWidth) {
		if (recipeWidth == 0) {
			return idealArea;
		}
		final int padding = 2 * borderPadding;
		int width = idealArea.getWidth() - padding;

		width = Math.max(recipeWidth, width);

		final int newWidth = width + padding;
		final int newX = (screenWidth - newWidth) / 2;

		return new ImmutableRect2i(
			newX,
			idealArea.getY(),
			newWidth,
			idealArea.getHeight()
		);
	}

	@Override
	public void tick() {
		super.tick();

		this.layouts.tick();
		this.optionButtons.tick();
		this.logic.tick();
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isOpen()) {
			if (interactiveIngredientTooltipController.isMouseOver(mouseX, mouseY)) {
				return interactiveIngredientTooltipController.getIngredientUnderMouse(mouseX, mouseY);
			}
			return Stream.concat(
				craftingStations.getIngredientUnderMouse(mouseX, mouseY),
				layouts.getIngredientUnderMouse(mouseX, mouseY)
			);
		}
		return Stream.empty();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (!isOpen()) {
			return false;
		}
		return area.contains(mouseX, mouseY) ||
			getResizeHandle(mouseX, mouseY).isPresent() ||
			optionButtons.getArea().contains(mouseX, mouseY) ||
			recipeGuiTabs.isMouseOver(mouseX, mouseY) ||
			craftingStations.isMouseOver(mouseX, mouseY);
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return Stream.empty();
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		updateInteractiveIngredientTooltip(mouseX, mouseY);
		layouts.mouseMoved(mouseX, mouseY);
	}

	private void updateInteractiveIngredientTooltip(double mouseX, double mouseY) {
		if (!keyBindings.getPauseRecipeCycling().isDown()) {
			interactiveIngredientTooltipController.hide();
			return;
		}
		if (!interactiveIngredientTooltipController.isVisible()) {
			openInteractiveIngredientTooltip(mouseX, mouseY);
		}
	}

	/** Creates input handling for the recipe tooltip. */
	public InputArea createTooltipInputArea() {
		return interactiveIngredientTooltipController.createInputArea();
	}

	public IGuiInputLayer getForegroundInputLayer() {
		return this.interactiveIngredientTooltipController;
	}

	private ResizeHandle getResizeHandle(double mouseX, double mouseY) {
		if (!Internal.getClientConfigs().getClientConfig().guiResizeEnabled().get() || !isOpen() || recipeGuiTabs.isMouseOver(mouseX, mouseY) || optionButtons.getArea().contains(mouseX, mouseY)) {
			return ResizeHandle.NONE;
		}
		return ResizeHandle.at(area, mouseX, mouseY);
	}

	private static final class ResizeInputHandler implements IInputTarget {
		private final RecipesGui recipesGui;
		private ImmutableSize2i initialPreferredSize = ImmutableSize2i.EMPTY;

		private ResizeInputHandler(RecipesGui recipesGui) {
			this.recipesGui = recipesGui;
		}

		@Override
		public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			if (!recipesGui.isOpen() || !Internal.getClientConfigs().getClientConfig().guiResizeEnabled().get() || !input.is(keyBindings.getLeftClick())) {
				return Optional.empty();
			}
			ResizeHandle handle = recipesGui.getResizeHandle(input.getMouseX(), input.getMouseY());
			if (!handle.isPresent()) {
				return Optional.empty();
			}
			recipesGui.resizeDrag = new ResizeDrag(handle, input.getMouseX(), input.getMouseY(), recipesGui.area.getSize());
			IClientConfig config = Internal.getClientConfigs().getClientConfig();
			initialPreferredSize = new ImmutableSize2i(config.recipeGuiWidth().get(), config.maxRecipeGuiHeight().get());
			recipesGui.interactiveIngredientTooltipController.hide();
			return Optional.of(MouseDrag.forWidget(this::drag, this::resetInput));
		}

		private void drag(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
			ResizeDrag drag = recipesGui.resizeDrag;
			if (drag == null || mouseKey.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
				return;
			}
			IClientConfig config = Internal.getClientConfigs().getClientConfig();
			int maxHeight = RecipeGuiSizing.calculateInitialSize(recipesGui.height, config.searchBarPosition().get().isCentered(), Integer.MAX_VALUE).ySize();
			int maxWidth = Math.max(minGuiWidth, recipesGui.width - 2 * Math.max(borderPadding, recipesGui.getLeftSideExtraWidth()));
			ImmutableSize2i size = drag.resize(mouseX, mouseY, true, true,
				new ImmutableSize2i(minGuiWidth, IClientConfig.minRecipeGuiHeight), new ImmutableSize2i(maxWidth, maxHeight));
			int preferredWidth = initialPreferredSize.width();
			int preferredHeight = initialPreferredSize.height();
			// Preserve the configured size on untouched axes and when dragging back to the starting size.
			if (drag.handle().horizontal() && size.width() != drag.size().width()) {
				preferredWidth = size.width();
			}
			if (drag.handle().vertical() && size.height() != drag.size().height()) {
				preferredHeight = size.height();
			}
			if (preferredWidth != config.recipeGuiWidth().get() || preferredHeight != config.maxRecipeGuiHeight().get()) {
				config.recipeGuiWidth().set(preferredWidth);
				config.maxRecipeGuiHeight().set(preferredHeight);
				recipesGui.updateSize();
			}
		}

		@Override
		public void resetInput() {
			recipesGui.resizeDrag = null;
		}
	}

	/** Creates the recipe display's controls, shortcuts, and public recipe drag callbacks. */
	public InputArea createInputArea(IInternalKeyMappings keys) {
		InputCommands commands = new InputCommands();
		registerInputCommands(commands, keys);
		return InputArea.builder("Recipes", this)
			.blockUnhandledMouseInput()
			.controls(inputHandler)
			.commands(commands)
			.apiDragHandler(layouts::handleApiMouseDragged)
			.build();
	}

	private void registerInputCommands(InputCommands commands, IInternalKeyMappings keys) {
		interactiveIngredientTooltipController.registerInputCommands(commands, keys);
		commands.add("Close Recipe Gui", keys.getCloseRecipeGui(), this::isOpen, this::onClose);
		commands.add("Close inventory", input -> isOpen() && input.is(Minecraft.getInstance().options.keyInventory),
			(screen, input) -> Optional.of(InputAction.run(this::onClose)));
		commands.add("Recipe Back", keys.getRecipeBack(), this::isOpen, this::back);
		commands.add("Recipe Forward", keys.getRecipeForward(), this::isOpen, this::forward);
		commands.add("Next Category", keys.getNextCategory(), this::isOpen, logic::nextRecipeCategory);
		commands.add("Previous Category", keys.getPreviousCategory(), this::isOpen, logic::previousRecipeCategory);
		commands.add("Next Recipe Page", keys.getNextRecipePage(), this::isOpen, logic::nextPage);
		commands.add("Previous Recipe Page", keys.getPreviousRecipePage(), this::isOpen, logic::previousPage);
	}

	public boolean isOpen() {
		return minecraft.gui.screen() == this;
	}

	private void reopenIfOpen() {
		if (resizeDrag == null && isOpen() && minecraft != null) {
			Screen currentParentScreen = parentScreen;
			minecraft.gui.setScreen(currentParentScreen);
			parentScreen = currentParentScreen;
			open();
		}
	}

	private void open() {
		if (!isOpen()) {
			parentScreen = minecraft.gui.screen();
		}
		minecraft.gui.setScreen(this);
	}

	@Override
	public void removed() {
		resizeInputHandler.resetInput();
		inputHandler.resetInput();
		interactiveIngredientTooltipController.hide();
		super.removed();
	}

	@Override
	public void onClose() {
		resizeDrag = null;
		inputHandler.resetInput();
		if (isOpen()) {
			minecraft.gui.setScreen(parentScreen);
			parentScreen = null;
			logic.clearHistory();
			return;
		}
		super.onClose();
	}

	@Override
	public void show(List<IFocus<?>> focuses) {
		IFocusGroup checkedFocuses = focusFactory.createFocusGroup(focuses);
		if (logic.showFocus(checkedFocuses)) {
			open();
		}
	}

	@Override
	public void showTypes(List<IRecipeType<?>> recipeTypes) {
		ErrorUtil.checkNotEmpty(recipeTypes, "recipeTypes");

		if (logic.showCategories(recipeTypes)) {
			open();
		}
	}

	@Override
	public <T> void showRecipes(IRecipeCategory<T> recipeCategory, List<T> recipes, List<IFocus<?>> focuses) {
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");
		ErrorUtil.checkNotEmpty(recipes, "recipes");
		IFocusGroup checkedFocuses = focusFactory.createFocusGroup(focuses);

		IFocusedRecipes<T> focusedRecipes = new StaticFocusedRecipes<>(recipeCategory, recipes);
		if (logic.showRecipes(focusedRecipes, checkedFocuses)) {
			open();
		}
	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		double x = MouseUtil.getX();
		double y = MouseUtil.getY();

		return getIngredientUnderMouse(x, y)
			.map(IClickableIngredientInternal::getTypedIngredient)
			.flatMap(i -> i.getIngredient(ingredientType).stream())
			.findFirst();
	}

	public Optional<IRecipeLayoutWithButtons<?>> getRecipeLayoutUnderMouse(double mouseX, double mouseY) {
		if (!isOpen() || interactiveIngredientTooltipController.isMouseOver(mouseX, mouseY)) {
			return Optional.empty();
		}
		return layouts.getRecipeLayoutUnderMouse(mouseX, mouseY);
	}

	public void back() {
		logic.back();
	}

	public void forward() {
		logic.forward();
	}

	private boolean openInteractiveIngredientTooltip(double mouseX, double mouseY) {
		Optional<RecipeSlotUnderMouse> craftingStation = craftingStations.getSlotUnderMouse(mouseX, mouseY);
		if (craftingStation.isPresent()) {
			RecipeSlotUnderMouse slotUnderMouse = craftingStation.get();
			return interactiveIngredientTooltipController.show(
				slotUnderMouse,
				slotUnderMouse::isMouseOver,
				mouseX,
				mouseY
			);
		}
		return getRecipeLayoutUnderMouse(mouseX, mouseY)
			.map(IRecipeLayoutWithButtons::getRecipeLayout)
			.flatMap(layout -> layout.getSlotUnderMouse(mouseX, mouseY)
				.map(slotUnderMouse -> interactiveIngredientTooltipController.show(
					slotUnderMouse,
					(x, y) -> layouts.isInsideViewport(x, y) && RecipeSlotClickTargetFactory.createMouseOverable(layout, slotUnderMouse).isMouseOver(x, y),
					mouseX,
					mouseY
				)))
			.orElse(false);
	}

	private void updateLayout() {
		if (!init) {
			return;
		}

		this.interactiveIngredientTooltipController.hide();

		ImmutableRect2i titleArea = MathUtil.union(previousRecipeCategory.getArea(), nextRecipeCategory.getArea())
			.cropLeft(previousRecipeCategory.getWidth() + titleInnerPadding)
			.cropRight(nextRecipeCategory.getWidth() + titleInnerPadding);
		IRecipeCategory<?> recipeCategory = logic.getSelectedRecipeCategory();
		this.recipeCategoryTitle = RecipeCategoryTitle.create(recipeCategory, font, titleArea);

		// Base column capacity on the preferred area, without any expansion for the previous category.
		ImmutableRect2i recipeLayoutsArea = getRecipeLayoutsArea(this.idealArea);

		AbstractContainerMenu containerMenu = getParentContainerMenu();
		List<IRecipeLayoutWithButtons<?>> recipeLayoutsWithButtons = logic.getVisibleRecipeLayoutsWithButtons(
			recipeLayoutsArea.getSize(),
			containerMenu,
			bookmarks,
			this
		);
		RecipeGuiGrid grid = this.logic.getRecipeGuiGrid();

		this.layouts.setRecipeLayoutsWithButtons(recipeLayoutsWithButtons);
		this.layouts.tick();
		int scrollbarSpace = getScrollbarSpace();
		this.area = calculateAreaToFitLayouts(this.idealArea, this.width, this.layouts.getWidth() + scrollbarSpace);
		recipeLayoutsArea = getRecipeLayoutsArea(this.area);

		RecipeGuiScrollState scrollState = null;
		if (logic.isScrolling()) {
			scrollState = logic.getScrollState();
		}
		this.layouts.updateLayout(recipeLayoutsArea, grid, scrollState);
		ImmutableRect2i scrollbarArea = ImmutableRect2i.EMPTY;
		if (logic.isScrolling()) {
			scrollbarArea = new ImmutableRect2i(recipeLayoutsArea.x() + recipeLayoutsArea.width() + navBarPadding + 1, recipeLayoutsArea.y(), Scrollbar.WIDTH, recipeLayoutsArea.height());
		}
		this.scrollbar.updateBounds(scrollbarArea);

		this.nextRecipeCategory.tick();
		this.previousRecipeCategory.tick();
		this.nextPage.tick();
		this.previousPage.tick();

		pageString = logic.getPageString();

		optionButtons.updateLayout(this.area);
		ImmutableRect2i optionButtonsArea = optionButtons.getArea();
		List<Consumer<IIngredientAcceptor<?>>> craftingStations = logic.getCraftingStations().toList();
		this.craftingStations.updateLayout(craftingStations, this.area, optionButtonsArea);
		recipeGuiTabs.initLayout(this.idealArea);
	}

	private int getScrollbarSpace() {
		if (logic.isScrolling()) {
			return Scrollbar.WIDTH + navBarPadding;
		}
		return 0;
	}

	private ImmutableRect2i getRecipeLayoutsArea(ImmutableRect2i area) {
		return new ImmutableRect2i(
			area.getX() + borderPadding,
			area.getY() + headerHeight + navBarPadding,
			area.getWidth() - (2 * borderPadding) - getScrollbarSpace(),
			area.getHeight() - (headerHeight + borderPadding + navBarPadding)
		);
	}

	@Nullable
	public AbstractContainerMenu getParentContainerMenu() {
		AbstractContainerScreen<?> containerScreen = getParentContainerScreen();
		if (containerScreen == null) {
			return null;
		}
		return containerScreen.getMenu();
	}

	@Nullable
	public AbstractContainerScreen<?> getParentContainerScreen() {
		Screen screen;
		if (parentScreen == null) {
			screen = Minecraft.getInstance().gui.screen();
		} else {
			screen = parentScreen;
		}
		if (screen instanceof AbstractContainerScreen<?> containerScreen) {
			return containerScreen;
		}
		return null;
	}

	@Override
	public Optional<Screen> getParentScreen() {
		return Optional.ofNullable(parentScreen);
	}

	@Nullable
	public IGuiProperties getProperties() {
		if (width <= 0 || height <= 0) {
			return null;
		}
		int extraWidth = getLeftSideExtraWidth();
		ImmutableRect2i recipeArea = getArea();
		int guiXSize = recipeArea.getWidth() + extraWidth;
		int guiYSize = recipeArea.getHeight();
		if (guiXSize <= 0 || guiYSize <= 0) {
			return null;
		}
		return new GuiProperties(
			getClass(),
			recipeArea.getX() - extraWidth,
			recipeArea.getY(),
			guiXSize,
			guiYSize,
			width,
			height
		);
	}

	private static final class RecipeCategoryScrollInputHandler implements IInputTarget {
		private final RecipesGui recipesGui;

		private RecipeCategoryScrollInputHandler(RecipesGui recipesGui) {
			this.recipesGui = recipesGui;
		}

		@Override
		public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			return Optional.empty();
		}

		@Override
		public boolean scroll(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			if (!recipesGui.isMouseOver(mouseX, mouseY) || !Minecraft.getInstance().hasShiftDown()) {
				return false;
			}
			if (scrollDeltaY < 0) {
				recipesGui.logic.nextRecipeCategory();
				return true;
			} else if (scrollDeltaY > 0) {
				recipesGui.logic.previousRecipeCategory();
				return true;
			}
			return false;
		}
	}

	private static final class RecipeNavigationInputHandler implements IInputTarget {
		private final RecipesGui recipesGui;

		private RecipeNavigationInputHandler(RecipesGui recipesGui) {
			this.recipesGui = recipesGui;
		}

		@Override
		public Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			double mouseX = input.getMouseX();
			double mouseY = input.getMouseY();
			if (recipesGui.isMouseOver(mouseX, mouseY) &&
				recipesGui.recipeCategoryTitle.isMouseOver(mouseX, mouseY) &&
				input.is(keyBindings.getLeftClick())
			) {
				return Optional.of(InputAction.run(recipesGui.logic::showAllRecipes)
					.within(recipesGui.recipeCategoryTitle::isMouseOver));
			}
			return Optional.empty();
		}

		@Override
		public boolean scroll(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			if (recipesGui.isMouseOver(mouseX, mouseY)) {
				if (recipesGui.logic.isScrolling() && scrollDeltaY != 0) {
					int scrollRate = Internal.getClientConfigs().getClientConfig().smoothScrollRate().get();
					recipesGui.logic.scrollRecipes(-scrollDeltaY * scrollRate);
					return true;
				} else {
					if (scrollDeltaY < 0) {
						recipesGui.logic.nextPage();
						return true;
					} else if (scrollDeltaY > 0) {
						recipesGui.logic.previousPage();
						return true;
					}
				}
			}

			return false;
		}
	}

}
