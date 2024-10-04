package mezz.jei.gui.recipes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
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
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.IClientConfig;
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
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.InputType;
import mezz.jei.gui.input.MouseUtil;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.UserInputRouter;
import mezz.jei.gui.recipes.lookups.IFocusedRecipes;
import mezz.jei.gui.recipes.lookups.StaticFocusedRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
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
	private final BookmarkList bookmarks;
	private final IFocusFactory focusFactory;
	private final IIngredientManager ingredientManager;

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
		IRecipeTransferManager recipeTransferManager,
		IIngredientManager ingredientManager,
		IInternalKeyMappings keyBindings,
		IFocusFactory focusFactory,
		BookmarkList bookmarks,
		IGuiHelper guiHelper
	) {
		super(Component.literal("Recipes"));
		this.bookmarks = bookmarks;
		this.ingredientManager = ingredientManager;
		this.keyBindings = keyBindings;
		this.logic = new RecipeGuiLogic(
			recipeManager,
			recipeTransferManager,
			this::updateLayout,
			focusFactory
		);
		this.recipeCatalysts = new RecipeCatalysts(recipeManager);
		this.recipeGuiTabs = new RecipeGuiTabs(this.logic, recipeManager, guiHelper);
		this.optionButtons = new RecipeOptionButtons(this.logic::goToFirstPage);
		this.focusFactory = focusFactory;
		this.minecraft = Minecraft.getInstance();
		this.layouts = new RecipeGuiLayouts();

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
		int ySize;
		IClientConfig clientConfig = Internal.getJeiClientConfigs().getClientConfig();
		if (clientConfig.isCenterSearchBarEnabled()) {
			ySize = this.height - 76;
		} else {
			ySize = this.height - 58;
		}
		int extraSpace = 0;
		final int maxHeight = clientConfig.getMaxRecipeGuiHeight();
		if (ySize > maxHeight) {
			extraSpace = ySize - maxHeight;
			ySize = maxHeight;
		}

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
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (minecraft == null) {
			return;
		}
		renderTransparentBackground(guiGraphics);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		this.background.draw(guiGraphics, area);

		RenderSystem.disableBlend();

		guiGraphics.fill(
			RenderType.gui(),
			previousRecipeCategory.getX() + previousRecipeCategory.getWidth(),
			previousRecipeCategory.getY(),
			nextRecipeCategory.getX(),
			nextRecipeCategory.getY() + nextRecipeCategory.getHeight(),
			0x30000000
		);
		guiGraphics.fill(
			RenderType.gui(),
			previousPage.getX() + previousPage.getWidth(),
			previousPage.getY(),
			nextPage.getX(),
			nextPage.getY() + nextPage.getHeight(),
			0x30000000
		);

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		this.recipeCategoryTitle.draw(guiGraphics, font);

		ImmutableRect2i pageArea = MathUtil.union(previousPage.getArea(), nextPage.getArea());
		StringUtil.drawCenteredStringWithShadow(guiGraphics, font, pageString, pageArea);

		nextRecipeCategory.render(guiGraphics, mouseX, mouseY, partialTicks);
		previousRecipeCategory.render(guiGraphics, mouseX, mouseY, partialTicks);
		nextPage.render(guiGraphics, mouseX, mouseY, partialTicks);
		previousPage.render(guiGraphics, mouseX, mouseY, partialTicks);

		Optional<IRecipeLayoutDrawable<?>> hoveredRecipeLayout = this.layouts.draw(guiGraphics, mouseX, mouseY);
		optionButtons.draw(guiGraphics, mouseX, mouseY, partialTicks);
		Optional<IRecipeSlotDrawable> hoveredRecipeCatalyst = recipeCatalysts.draw(guiGraphics, mouseX, mouseY);

		recipeGuiTabs.draw(minecraft, guiGraphics, mouseX, mouseY, partialTicks);

		this.layouts.drawTooltips(guiGraphics, mouseX, mouseY);

		optionButtons.drawTooltips(guiGraphics, mouseX, mouseY);
		RenderSystem.disableBlend();

		hoveredRecipeLayout.ifPresent(l -> l.drawOverlays(guiGraphics, mouseX, mouseY));
		hoveredRecipeCatalyst.ifPresent(h -> h.drawHoverOverlays(guiGraphics));

		hoveredRecipeCatalyst.ifPresent(h -> {
			JeiTooltip tooltip = new JeiTooltip();
			h.getTooltip(tooltip);
			tooltip.draw(guiGraphics, mouseX, mouseY);
		});
		RenderSystem.enableDepthTest();

		if (recipeCategoryTitle.isMouseOver(mouseX, mouseY)) {
			JeiTooltip tooltip = new JeiTooltip();
			recipeCategoryTitle.getTooltip(tooltip);
			if (!logic.hasAllCategories()) {
				tooltip.addKeyUsageComponent("jei.tooltip.show.all.recipes.hotkey", keyBindings.getLeftClick());
			}
			tooltip.draw(guiGraphics, mouseX, mouseY);
		}

		if (DebugConfig.isDebugGuisEnabled()) {
			guiGraphics.fill(
				RenderType.gui(),
				idealArea.getX(),
				idealArea.getY(),
				idealArea.getX() + idealArea.getWidth(),
				idealArea.getY() + idealArea.getHeight(),
				0x4400FF00
			);

			guiGraphics.fill(
				RenderType.gui(),
				area.getX(),
				area.getY(),
				area.getX() + area.getWidth(),
				area.getY() + area.getHeight(),
				0x44990044
			);

			ImmutableRect2i recipeLayoutsArea = getRecipeLayoutsArea();
			guiGraphics.fill(
				RenderType.gui(),
				recipeLayoutsArea.getX(),
				recipeLayoutsArea.getY(),
				recipeLayoutsArea.getX() + recipeLayoutsArea.getWidth(),
				recipeLayoutsArea.getY() + recipeLayoutsArea.getHeight(),
				0x44228844
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
			return area.contains(mouseX, mouseY) ||
				optionButtons.getArea().contains(mouseX, mouseY);
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
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.inputHandler.handleMouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
		List<RecipeLayoutWithButtons<?>> recipeLayoutsWithButtons = logic.getVisibleRecipeLayoutsWithButtons(
			availableHeight,
			minRecipePadding,
			containerMenu,
			bookmarks,
			this
		);
		int recipesPerPage = this.logic.getRecipesPerPage();

		this.layouts.setRecipeLayoutsWithButtons(recipeLayoutsWithButtons);
		this.layouts.tick(containerMenu);
		this.area = calculateAreaToFitLayouts(this.idealArea, this.width, this.layouts.getWidth());
		recipeLayoutsArea = getRecipeLayoutsArea();

		this.layouts.updateLayout(recipeLayoutsArea, recipesPerPage);

		nextPage.active = previousPage.active = logic.hasMultiplePages();
		nextRecipeCategory.active = previousRecipeCategory.active = logic.hasMultipleCategories();

		pageString = logic.getPageString();

		optionButtons.updateLayout(this.area);
		ImmutableRect2i optionButtonsArea = optionButtons.getArea();
		List<ITypedIngredient<?>> recipeCatalystIngredients = logic.getRecipeCatalysts().toList();
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

	@Nullable
	public AbstractContainerMenu getParentContainerMenu() {
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
					if (input.is(keyBindings.getLeftClick()))
						if (input.isSimulate() || recipesGui.logic.showAllRecipes()) {
							return Optional.of(this);
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
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
			if (recipesGui.isMouseOver(mouseX, mouseY)) {
				if (scrollDeltaY < 0) {
					recipesGui.logic.nextPage();
					return Optional.of(this);
				} else if (scrollDeltaY > 0) {
					recipesGui.logic.previousPage();
					return Optional.of(this);
				}
			}

			return Optional.empty();
		}
	}
}
