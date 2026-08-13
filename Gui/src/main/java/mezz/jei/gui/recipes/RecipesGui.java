package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeButtonControllerFactory;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.StringUtil;
import mezz.jei.gui.GuiProperties;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.InputType;
import mezz.jei.gui.input.MouseUtil;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.UserInputRouter;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistory;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import mezz.jei.gui.recipes.lookups.StaticFocusedRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipesGui extends Screen implements IRecipesGui, IRecipeFocusSource {
	private static final int borderPadding = 6;
	private static final int minRecipePadding = 4;
	private static final int navBarPadding = 2;
	private static final int titleInnerPadding = 14;
	private static final int smallButtonWidth = 13;
	private static final int smallButtonHeight = 13;
	private static final int minGuiWidth = 198;

	private final IInternalKeyMappings keyBindings;
	private final IFocusFactory focusFactory;
	private final IRecipeManager recipeManager;
	private final IIngredientManager ingredientManager;
	private final IGuiHelper guiHelper;
	private final BookmarkList bookmarkList;
	private final List<IRecipeButtonControllerFactory> recipeButtonControllerFactories;

	private int headerHeight;

	/* Internal logic for the gui, handles finding recipes */
	private final IRecipeGuiLogic logic;

	/* List of RecipeLayout to display */
	private final RecipeGuiLayouts layouts;

	private String pageString = "1/1";
	private final DrawableNineSliceTexture background;

	private final RecipeCatalysts recipeCatalysts;
	private final RecipeGuiTabs recipeGuiTabs;
	private final RecipeOptionButtons optionButtons;
	private final UserInputRouter inputHandler;

	private final GuiIconButton nextRecipeCategory;
	private final GuiIconButton previousRecipeCategory;
	private final GuiIconButton nextPage;
	private final GuiIconButton previousPage;

	@Nullable
	private Screen parentScreen;
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
		IInternalKeyMappings keyBindings,
		IFocusFactory focusFactory,
		IGuiHelper guiHelper,
		BookmarkList bookmarkList,
		LookupHistory lookupHistory
	) {
		super(Component.literal("Recipes"));
		this.recipeButtonControllerFactories = recipeManager.getRecipeButtonControllerFactories();
		this.keyBindings = keyBindings;
		this.recipeManager = recipeManager;
		this.ingredientManager = ingredientManager;
		this.guiHelper = guiHelper;
		this.bookmarkList = bookmarkList;
		this.logic = new RecipeGuiLogic(
			recipeManager,
			ingredientManager,
			lookupHistory,
			guiHelper,
			this::updateLayout,
			focusFactory,
			bookmarkList,
			this::createRecipeLayoutWithButtons
		);
		this.recipeCatalysts = new RecipeCatalysts(recipeManager);
		this.recipeGuiTabs = new RecipeGuiTabs(this.logic, recipeManager, guiHelper);
		this.optionButtons = new RecipeOptionButtons(this.logic::goToFirstPage);
		this.focusFactory = focusFactory;
		this.minecraft = Minecraft.getInstance();
		this.layouts = new RecipeGuiLayouts();
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		clientConfig.addCenterSearchBarEnabledListener(v -> reopenIfOpen());
		clientConfig.addMaxRecipeGuiHeightListener(v -> reopenIfOpen());

		Textures textures = Internal.getTextures();
		IDrawableStatic arrowNext = textures.getArrowNext();
		IDrawableStatic arrowPrevious = textures.getArrowPrevious();

		nextRecipeCategory = new GuiIconButton(0, 0, smallButtonWidth, smallButtonHeight, arrowNext, b -> logic.nextRecipeCategory());
		previousRecipeCategory = new GuiIconButton(0, 0, smallButtonWidth, smallButtonHeight, arrowPrevious, b -> logic.previousRecipeCategory());
		nextPage = new GuiIconButton(0, 0, smallButtonWidth, smallButtonHeight, arrowNext, b -> logic.nextPage());
		previousPage = new GuiIconButton(0, 0, smallButtonWidth, smallButtonHeight, arrowPrevious, b -> logic.previousPage());

		background = textures.getRecipeGuiBackground();

		inputHandler = new UserInputRouter(
			"RecipesGui",
			layouts.createInputHandler(),
			new UserInputHandler(this),
			optionButtons.createInputHandler(),
			recipeGuiTabs.createInputHandler(),
			nextRecipeCategory.createInputHandler(),
			previousRecipeCategory.createInputHandler(),
			nextPage.createInputHandler(),
			previousPage.createInputHandler()
		);
	}

	public ImmutableRect2i getArea() {
		return this.area;
	}

	public int getLeftSideExtraWidth() {
		if (recipeCatalysts.isEmpty()) {
			return optionButtons.getWidth();
		}
		return Math.max(recipeCatalysts.getWidth(), optionButtons.getWidth());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void init() {
		super.init();

		final int xSize = minGuiWidth;
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		RecipeGuiSizing.Size recipeGuiSize = RecipeGuiSizing.calculateInitialSize(
			this.height,
			clientConfig.isCenterSearchBarEnabled(),
			clientConfig.getMaxRecipeGuiHeight()
		);
		int ySize = recipeGuiSize.ySize();
		int extraSpace = recipeGuiSize.extraSpace();

		final int guiLeft = (this.width - xSize) / 2;
		final int guiTop = RecipeGuiTab.TAB_HEIGHT + 21 + (extraSpace / 2);

		this.idealArea = new ImmutableRect2i(guiLeft, guiTop, xSize, ySize);
		this.area = this.idealArea;

		final int rightButtonX = guiLeft + xSize - borderPadding - smallButtonWidth;
		final int leftButtonX = guiLeft + borderPadding;

		int titleHeight = font.lineHeight + borderPadding;
		int recipeClassButtonTop = guiTop + titleHeight - smallButtonHeight + navBarPadding;
		nextRecipeCategory.setX(rightButtonX);
		nextRecipeCategory.setY(recipeClassButtonTop);
		previousRecipeCategory.setX(leftButtonX);
		previousRecipeCategory.setY(recipeClassButtonTop);

		int pageButtonTop = recipeClassButtonTop + smallButtonHeight + navBarPadding;
		nextPage.setX(rightButtonX);
		nextPage.setY(pageButtonTop);
		previousPage.setX(leftButtonX);
		previousPage.setY(pageButtonTop);

		this.headerHeight = (pageButtonTop + smallButtonHeight) - guiTop;

		this.init = true;
		updateLayout();
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (minecraft == null) {
			return;
		}
		super.render(poseStack, mouseX, mouseY, partialTicks);

		renderBackground(poseStack);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		this.background.draw(poseStack, area);

		RenderSystem.disableBlend();

		GuiComponent.fill(
			poseStack,
			previousRecipeCategory.getX() + previousRecipeCategory.getWidth(),
			previousRecipeCategory.getY(),
			nextRecipeCategory.getX(),
			nextRecipeCategory.getY() + nextRecipeCategory.getHeight(),
			JeiGuiColors.getColor(GuiColor.PAGE_NAVIGATION_BACKGROUND)
		);
		GuiComponent.fill(
			poseStack,
			previousPage.getX() + previousPage.getWidth(),
			previousPage.getY(),
			nextPage.getX(),
			nextPage.getY() + nextPage.getHeight(),
			JeiGuiColors.getColor(GuiColor.PAGE_NAVIGATION_BACKGROUND)
		);

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		this.recipeCategoryTitle.draw(poseStack, font);

		ImmutableRect2i pageArea = MathUtil.union(previousPage.getArea(), nextPage.getArea());
		StringUtil.drawCenteredStringWithShadow(poseStack, font, pageString, pageArea, JeiGuiColors.getColor(GuiColor.PAGE_NAVIGATION_TEXT));

		nextRecipeCategory.render(poseStack, mouseX, mouseY, partialTicks);
		previousRecipeCategory.render(poseStack, mouseX, mouseY, partialTicks);
		nextPage.render(poseStack, mouseX, mouseY, partialTicks);
		previousPage.render(poseStack, mouseX, mouseY, partialTicks);

		Optional<IRecipeLayoutDrawable<?>> hoveredRecipeLayout = this.layouts.draw(poseStack, mouseX, mouseY);
		optionButtons.draw(poseStack, mouseX, mouseY, partialTicks);
		Optional<IRecipeSlotDrawable> hoveredRecipeCatalyst = recipeCatalysts.draw(poseStack, mouseX, mouseY);

		recipeGuiTabs.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);

		this.layouts.drawTooltips(poseStack, mouseX, mouseY);
		optionButtons.drawTooltips(poseStack, mouseX, mouseY);

		RenderSystem.disableBlend();

		hoveredRecipeLayout.ifPresent(l -> l.drawOverlays(poseStack, mouseX, mouseY));

		hoveredRecipeCatalyst.ifPresent(h -> {
			h.drawTooltip(poseStack, mouseX, mouseY);
		});
		RenderSystem.enableDepthTest();

		if (recipeCategoryTitle.isMouseOver(mouseX, mouseY)) {
			JeiTooltip tooltip = new JeiTooltip();
			recipeCategoryTitle.getTooltip(tooltip);
			if (!logic.hasAllCategories()) {
				tooltip.addKeyUsageComponent("jei.tooltip.show.all.recipes.hotkey", keyBindings.getLeftClick());
			}
			tooltip.draw(poseStack, mouseX, mouseY);
		}

		if (DebugConfig.isDebugGuisEnabled()) {
			GuiComponent.fill(
				poseStack,
				idealArea.getX(),
				idealArea.getY(),
				idealArea.getX() + idealArea.getWidth(),
				idealArea.getY() + idealArea.getHeight(),
				JeiGuiColors.getColor(GuiColor.DEBUG_RECIPE_GUI_IDEAL_AREA)
			);

			GuiComponent.fill(
				poseStack,
				area.getX(),
				area.getY(),
				area.getX() + area.getWidth(),
				area.getY() + area.getHeight(),
				JeiGuiColors.getColor(GuiColor.DEBUG_RECIPE_GUI_AREA)
			);

			ImmutableRect2i recipeLayoutsArea = getRecipeLayoutsArea();
			GuiComponent.fill(
				poseStack,
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
		int width = minGuiWidth - padding;

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

		AbstractContainerMenu container = getParentContainerMenu();
		this.layouts.tick(container);
		this.optionButtons.tick();

		this.logic.tick(container);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (minecraft != null && minecraft.screen == this) {
			return area.contains(mouseX, mouseY);
		}
		return false;
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isOpen()) {
			return Stream.concat(
				recipeCatalysts.getIngredientUnderMouse(mouseX, mouseY),
				layouts.getIngredientUnderMouse(mouseX, mouseY)
			);
		}
		return Stream.empty();
	}

	@Override
	public Stream<IDraggableIngredientInternal<?>> getDraggableIngredientUnderMouse(double mouseX, double mouseY) {
		return Stream.empty();
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		layouts.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
		InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(mouseButton);
		return layouts.mouseDragged(mouseX, mouseY, input, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		if (this.inputHandler.handleMouseScrolled(mouseX, mouseY, scrollDelta)) {
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, scrollDelta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		boolean handled = UserInput.fromVanilla(mouseX, mouseY, mouseButton, InputType.SIMULATE)
			.map(this::handleInput)
			.orElse(false);

		if (handled) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
		boolean handled = UserInput.fromVanilla(mouseX, mouseY, mouseButton, InputType.EXECUTE)
			.map(this::handleInput)
			.orElse(false);

		if (handled) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		UserInput input = UserInput.fromVanilla(keyCode, scanCode, modifiers, InputType.IMMEDIATE);
		return handleInput(input);
	}

	private boolean handleInput(UserInput input) {
		return this.inputHandler.handleUserInput(this, input, keyBindings);
	}

	public boolean isOpen() {
		return minecraft != null && minecraft.screen == this;
	}

	private void reopenIfOpen() {
		if (isOpen() && minecraft != null) {
			Screen currentParentScreen = parentScreen;
			minecraft.setScreen(currentParentScreen);
			parentScreen = currentParentScreen;
			open();
		}
	}

	private void open() {
		if (minecraft != null) {
			if (!isOpen()) {
				parentScreen = minecraft.screen;
			}
			minecraft.setScreen(this);
		}
	}

	@Override
	public void onClose() {
		if (isOpen() && minecraft != null) {
			minecraft.setScreen(parentScreen);
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
	public void showTypes(List<RecipeType<?>> recipeTypes) {
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

	public void back() {
		logic.back();
	}

	public void forward() {
		logic.forward();
	}

	private void updateLayout() {
		if (!init) {
			return;
		}

		ImmutableRect2i titleArea = MathUtil.union(previousRecipeCategory.getArea(), nextRecipeCategory.getArea())
			.cropLeft(previousRecipeCategory.getWidth() + titleInnerPadding)
			.cropRight(nextRecipeCategory.getWidth() + titleInnerPadding);
		IRecipeCategory<?> recipeCategory = logic.getSelectedRecipeCategory();
		this.recipeCategoryTitle = RecipeCategoryTitle.create(recipeCategory, font, titleArea);

		ImmutableRect2i recipeLayoutsArea = getRecipeLayoutsArea();
		final int availableHeight = recipeLayoutsArea.getHeight();

		AbstractContainerMenu containerMenu = getParentContainerMenu();
		List<IRecipeLayoutWithButtons<?>> recipeLayoutsWithButtons = logic.getVisibleRecipeLayoutsWithButtons(availableHeight, minRecipePadding, containerMenu);
		int recipesPerPage = this.logic.getRecipesPerPage();

		this.layouts.setRecipeLayoutsWithButtons(recipeLayoutsWithButtons);
		this.layouts.tick(containerMenu);
		this.area = calculateAreaToFitLayouts(this.idealArea, this.width, this.layouts.getWidth());
		recipeLayoutsArea = getRecipeLayoutsArea();

		this.layouts.updateLayout(recipeLayoutsArea, recipesPerPage);

		nextPage.active = previousPage.active = logic.hasMultiplePages();
		nextRecipeCategory.active = previousRecipeCategory.active = logic.hasMultipleCategories();

		pageString = logic.getPageString();

		List<ITypedIngredient<?>> recipeCatalystIngredients = logic.getRecipeCatalysts().toList();
		optionButtons.updateLayout(this.area);
		ImmutableRect2i optionButtonsArea = optionButtons.getArea();
		recipeCatalysts.updateLayout(recipeCatalystIngredients, this.area, optionButtonsArea);
		recipeGuiTabs.initLayout(this.idealArea);
	}

	private ImmutableRect2i getRecipeLayoutsArea() {
		return new ImmutableRect2i(
			area.getX() + borderPadding,
			area.getY() + headerHeight + navBarPadding,
			area.getWidth() - (2 * borderPadding),
			area.getHeight() - (headerHeight + borderPadding + navBarPadding)
		);
	}

	private <T> IRecipeLayoutWithButtons<T> createRecipeLayoutWithButtons(
		IRecipeLayoutDrawable<T> recipeLayoutDrawable,
		@Nullable RecipeBookmark<?, ?> recipeBookmark
	) {
		RecipeTransferButton transferButton = RecipeTransferButton.create(
			recipeLayoutDrawable,
			this::onClose
		);

		RecipeBookmarkButton bookmarkButton;
		if (recipeBookmark == null) {
			bookmarkButton = RecipeBookmarkButton.create(
				recipeLayoutDrawable,
				ingredientManager,
				bookmarkList,
				recipeManager,
				guiHelper
			).orElse(null);
		} else {
			bookmarkButton = RecipeBookmarkButton.create(
				recipeLayoutDrawable,
				bookmarkList,
				recipeBookmark
			);
		}

		return RecipeLayoutWithButtons.create(
			recipeLayoutDrawable,
			transferButton,
			bookmarkButton,
			recipeButtonControllerFactories
		);
	}

	@Nullable
	private AbstractContainerMenu getParentContainerMenu() {
		Screen screen;
		if (parentScreen == null) {
			screen = Minecraft.getInstance().screen;
		} else {
			screen = parentScreen;
		}
		if (screen instanceof AbstractContainerScreen<?> containerScreen) {
			return containerScreen.getMenu();
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

	private static class UserInputHandler implements IUserInputHandler {
		private final RecipesGui recipesGui;

		public UserInputHandler(RecipesGui recipesGui) {
			this.recipesGui = recipesGui;
		}

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			double mouseX = input.getMouseX();
			double mouseY = input.getMouseY();
			if (recipesGui.isMouseOver(mouseX, mouseY)) {
				if (recipesGui.recipeCategoryTitle.isMouseOver(mouseX, mouseY)) {
					if (input.is(keyBindings.getLeftClick())) {
						if (input.isSimulate() || recipesGui.logic.showAllRecipes()) {
							return Optional.of(this);
						}
					}
				}
			}

			Minecraft minecraft = Minecraft.getInstance();
			if (input.is(keyBindings.getCloseRecipeGui()) || input.is(minecraft.options.keyInventory)) {
				if (!input.isSimulate()) {
					recipesGui.onClose();
				}
				return Optional.of(this);
			} else if (input.is(keyBindings.getRecipeBack())) {
				if (!input.isSimulate()) {
					recipesGui.back();
				}
				return Optional.of(this);
			} else if (input.is(keyBindings.getRecipeForward())) {
				if (!input.isSimulate()) {
					recipesGui.forward();
				}
				return Optional.of(this);
			} else if (input.is(keyBindings.getNextCategory())) {
				if (!input.isSimulate()) {
					recipesGui.logic.nextRecipeCategory();
				}
				return Optional.of(this);
			} else if (input.is(keyBindings.getPreviousCategory())) {
				if (!input.isSimulate()) {
					recipesGui.logic.previousRecipeCategory();
				}
				return Optional.of(this);
			} else if (input.is(keyBindings.getNextRecipePage())) {
				if (!input.isSimulate()) {
					recipesGui.logic.nextPage();
				}
				return Optional.of(this);
			} else if (input.is(keyBindings.getPreviousRecipePage())) {
				if (!input.isSimulate()) {
					recipesGui.logic.previousPage();
				}
				return Optional.of(this);
			}

			return Optional.empty();
		}

		@Override
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
			if (recipesGui.isMouseOver(mouseX, mouseY)) {
				if (hasShiftDown()) {
					if (scrollDelta < 0) {
						recipesGui.logic.nextRecipeCategory();
						return Optional.of(this);
					} else if (scrollDelta > 0) {
						recipesGui.logic.previousRecipeCategory();
						return Optional.of(this);
					}
				} else {
					if (scrollDelta < 0) {
						recipesGui.logic.nextPage();
						return Optional.of(this);
					} else if (scrollDelta > 0) {
						recipesGui.logic.previousPage();
						return Optional.of(this);
					}
				}
			}

			return Optional.empty();
		}
	}
}
