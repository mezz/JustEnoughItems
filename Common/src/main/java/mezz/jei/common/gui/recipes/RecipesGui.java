package mezz.jei.common.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.focus.FocusGroup;
import mezz.jei.common.gui.GuiProperties;
import mezz.jei.common.gui.HoverChecker;
import mezz.jei.common.gui.TooltipRenderer;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.elements.GuiIconButtonSmall;
import mezz.jei.common.gui.ingredients.RecipeSlot;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.input.ClickedIngredient;
import mezz.jei.common.input.IClickedIngredient;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.input.IRecipeFocusSource;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.InputType;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.StringUtil;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.common.gui.recipes.layout.RecipeLayout;
import mezz.jei.common.gui.recipes.layout.RecipeTransferButton;
import mezz.jei.common.recipes.RecipeManager;
import mezz.jei.common.recipes.RecipeTransferManager;
import mezz.jei.common.transfer.RecipeTransferUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipesGui extends Screen implements IRecipesGui, IRecipeFocusSource, IRecipeLogicStateListener {
	private static final int borderPadding = 6;
	private static final int innerPadding = 14;
	private static final int buttonWidth = 13;
	private static final int buttonHeight = 13;

	private final RecipeTransferManager recipeTransferManager;
	private final RegisteredIngredients registeredIngredients;
	private final IModIdHelper modIdHelper;
	private final IClientConfig clientConfig;
	private final IKeyBindings keyBindings;
	private final RecipeManager recipeManager;

	private int headerHeight;

	/* Internal logic for the gui, handles finding recipes */
	private final IRecipeGuiLogic logic;

	/* List of RecipeLayout to display */
	private final List<RecipeLayout<?>> recipeLayouts = new ArrayList<>();

	private String pageString = "1/1";
	private Component title = TextComponent.EMPTY;
	private final DrawableNineSliceTexture background;

	private final RecipeCatalysts recipeCatalysts;
	private final RecipeGuiTabs recipeGuiTabs;

	private final HoverChecker titleHoverChecker = new HoverChecker();

	private final GuiIconButtonSmall nextRecipeCategory;
	private final GuiIconButtonSmall previousRecipeCategory;
	private final GuiIconButtonSmall nextPage;
	private final GuiIconButtonSmall previousPage;

	@Nullable
	private Screen parentScreen;
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private ImmutableRect2i titleArea = ImmutableRect2i.EMPTY;

	private boolean init = false;

	public RecipesGui(
		RecipeManager recipeManager,
		RecipeTransferManager recipeTransferManager,
		RegisteredIngredients registeredIngredients,
		IModIdHelper modIdHelper,
		IClientConfig clientConfig,
		Textures textures,
		IIngredientVisibility ingredientVisibility,
		IKeyBindings keyBindings
	) {
		super(new TextComponent("Recipes"));
		this.recipeTransferManager = recipeTransferManager;
		this.registeredIngredients = registeredIngredients;
		this.modIdHelper = modIdHelper;
		this.clientConfig = clientConfig;
		this.keyBindings = keyBindings;
		this.logic = new RecipeGuiLogic(recipeManager, recipeTransferManager, this, registeredIngredients, modIdHelper, textures, ingredientVisibility);
		this.recipeCatalysts = new RecipeCatalysts(textures, ingredientVisibility);
		this.recipeGuiTabs = new RecipeGuiTabs(this.logic, textures);
		this.recipeManager = recipeManager;
		this.minecraft = Minecraft.getInstance();

		IDrawableStatic arrowNext = textures.getArrowNext();
		IDrawableStatic arrowPrevious = textures.getArrowPrevious();

		nextRecipeCategory = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowNext, b -> logic.nextRecipeCategory(), textures);
		previousRecipeCategory = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowPrevious, b -> logic.previousRecipeCategory(), textures);
		nextPage = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowNext, b -> logic.nextPage(), textures);
		previousPage = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowPrevious, b -> logic.previousPage(), textures);

		background = textures.getRecipeGuiBackground();
	}

	private static void drawCenteredStringWithShadow(PoseStack poseStack, Font font, String string, ImmutableRect2i area) {
		ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, string);
		font.drawShadow(poseStack, string, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}

	private static void drawCenteredStringWithShadow(PoseStack poseStack, Font font, Component text, ImmutableRect2i area) {
		ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, text);
		font.drawShadow(poseStack, text, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}

	public ImmutableRect2i getArea() {
		return this.area;
	}

	public int getRecipeCatalystExtraWidth() {
		if (recipeCatalysts.isEmpty()) {
			return 0;
		}
		return recipeCatalysts.getWidth();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void init() {
		super.init();

		final int xSize = 198;
		int ySize = this.height - 68;
		int extraSpace = 0;
		final int maxHeight = this.clientConfig.getMaxRecipeGuiHeight();
		if (ySize > maxHeight) {
			extraSpace = ySize - maxHeight;
			ySize = maxHeight;
		}

		final int guiLeft = (this.width - xSize) / 2;
		final int guiTop = RecipeGuiTab.TAB_HEIGHT + 21 + (extraSpace / 2);

		this.area = new ImmutableRect2i(guiLeft, guiTop, xSize, ySize);

		final int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		final int leftButtonX = guiLeft + borderPadding;

		int titleHeight = font.lineHeight + borderPadding;
		int recipeClassButtonTop = guiTop + titleHeight - buttonHeight + 2;
		nextRecipeCategory.x = rightButtonX;
		nextRecipeCategory.y = recipeClassButtonTop;
		previousRecipeCategory.x = leftButtonX;
		previousRecipeCategory.y = recipeClassButtonTop;

		int pageButtonTop = recipeClassButtonTop + buttonHeight + 2;
		nextPage.x = rightButtonX;
		nextPage.y = pageButtonTop;
		previousPage.x = leftButtonX;
		previousPage.y = pageButtonTop;

		this.headerHeight = (pageButtonTop + buttonHeight) - guiTop;
		this.titleArea = MathUtil.union(previousRecipeCategory.getArea(), nextRecipeCategory.getArea())
			.cropLeft(previousRecipeCategory.getWidth() + innerPadding)
			.cropRight(nextRecipeCategory.getWidth() + innerPadding);

		this.addRenderableWidget(nextRecipeCategory);
		this.addRenderableWidget(previousRecipeCategory);
		this.addRenderableWidget(nextPage);
		this.addRenderableWidget(previousPage);

		this.init = true;
		updateLayout();
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (minecraft == null) {
			return;
		}
		renderBackground(poseStack);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		final int x = area.getX();
		final int y = area.getY();
		final int width = area.getWidth();
		final int height = area.getHeight();
		this.background.draw(poseStack, x, y, width, height);

		RenderSystem.disableBlend();

		fill(poseStack,
			x + borderPadding + buttonWidth,
			nextRecipeCategory.y,
			x + width - borderPadding - buttonWidth,
			nextRecipeCategory.y + buttonHeight,
			0x30000000);
		fill(poseStack,
			x + borderPadding + buttonWidth,
			nextPage.y,
			x + width - borderPadding - buttonWidth,
			nextPage.y + buttonHeight,
			0x30000000);

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		drawCenteredStringWithShadow(poseStack, font, title, titleArea);

		ImmutableRect2i pageArea = MathUtil.union(previousPage.getArea(), nextPage.getArea());
		drawCenteredStringWithShadow(poseStack, font, pageString, pageArea);

		nextRecipeCategory.render(poseStack, mouseX, mouseY, partialTicks);
		previousRecipeCategory.render(poseStack, mouseX, mouseY, partialTicks);
		nextPage.render(poseStack, mouseX, mouseY, partialTicks);
		previousPage.render(poseStack, mouseX, mouseY, partialTicks);

		RecipeLayout<?> hoveredLayout = null;
		for (RecipeLayout<?> recipeLayout : recipeLayouts) {
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hoveredLayout = recipeLayout;
			}
			recipeLayout.drawRecipe(poseStack, mouseX, mouseY);
		}

		RecipeSlot hoveredRecipeCatalyst = recipeCatalysts.draw(poseStack, mouseX, mouseY);

		recipeGuiTabs.draw(minecraft, poseStack, mouseX, mouseY);

		if (hoveredLayout != null) {
			hoveredLayout.drawOverlays(poseStack, mouseX, mouseY);
		}
		if (hoveredRecipeCatalyst != null) {
			hoveredRecipeCatalyst.drawOverlays(poseStack, 0, 0, mouseX, mouseY, modIdHelper);
		}

		if (titleHoverChecker.checkHover(mouseX, mouseY) && !logic.hasAllCategories()) {
			TranslatableComponent showAllRecipesString = new TranslatableComponent("jei.tooltip.show.all.recipes");
			TooltipRenderer.drawHoveringText(poseStack, List.of(showAllRecipesString), mouseX, mouseY);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (minecraft != null && minecraft.screen == this) {
			if (this.area.contains(mouseX, mouseY)) {
				return true;
			}
			for (RecipeLayout<?> recipeLayout : this.recipeLayouts) {
				if (recipeLayout.isMouseOver(mouseX, mouseY)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isOpen()) {
			return Stream.concat(
				recipeCatalysts.getIngredientUnderMouse(mouseX, mouseY),
				getRecipeLayoutsIngredientUnderMouse(mouseX, mouseY)
			);
		}
		return Stream.empty();
	}

	private Stream<IClickedIngredient<?>> getRecipeLayoutsIngredientUnderMouse(double mouseX, double mouseY) {
		return this.recipeLayouts.stream()
			.map(recipeLayout -> getRecipeLayoutIngredientUnderMouse(recipeLayout, mouseX, mouseY))
			.flatMap(Optional::stream);
	}

	private static Optional<IClickedIngredient<?>> getRecipeLayoutIngredientUnderMouse(RecipeLayout<?> recipeLayout, double mouseX, double mouseY) {
		return recipeLayout.getRecipeSlotUnderMouse(mouseX, mouseY)
			.flatMap(recipeSlot -> getClickedIngredient(recipeLayout, recipeSlot));
	}

	private static Optional<IClickedIngredient<?>> getClickedIngredient(RecipeLayout<?> recipeLayout, RecipeSlot recipeSlot) {
		return recipeSlot.getDisplayedIngredient()
			.map(displayedIngredient -> {
				ImmutableRect2i area = absoluteClickedArea(recipeLayout, recipeSlot.getRect());
				return new ClickedIngredient<>(displayedIngredient, area, false, true);
			});
	}

	/**
	 * Converts from relative recipeLayout coordinates to absolute screen coordinates
	 */
	private static ImmutableRect2i absoluteClickedArea(RecipeLayout<?> recipeLayout, ImmutableRect2i area) {
		return area.addOffset(recipeLayout.getPosX(), recipeLayout.getPosY());
	}

	@Override
	public boolean mouseScrolled(double scrollX, double scrollY, double scrollDelta) {
		final double x = MouseUtil.getX();
		final double y = MouseUtil.getY();
		if (isMouseOver(x, y)) {
			if (scrollDelta < 0) {
				logic.nextPage();
				return true;
			} else if (scrollDelta > 0) {
				logic.previousPage();
				return true;
			}
		}
		return super.mouseScrolled(scrollX, scrollY, scrollDelta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		boolean handled = UserInput.fromVanilla(mouseX, mouseY, mouseButton, InputType.IMMEDIATE)
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
		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();
		if (isMouseOver(mouseX, mouseY)) {
			if (titleHoverChecker.checkHover(mouseX, mouseY)) {
				if (input.is(keyBindings.getLeftClick()) && logic.setCategoryFocus()) {
					return true;
				}
			} else {
				for (RecipeLayout<?> recipeLayout : recipeLayouts) {
					if (recipeLayout.handleInput(input, keyBindings)) {
						return true;
					}
				}
			}
		}

		IUserInputHandler handler = recipeGuiTabs.getInputHandler();
		if (handler.handleUserInput(this, input, keyBindings).isPresent()) {
			return true;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (input.is(keyBindings.getCloseRecipeGui()) || input.is(minecraft.options.keyInventory)) {
			onClose();
			return true;
		} else if (input.is(keyBindings.getRecipeBack())) {
			back();
			return true;
		} else if (input.is(keyBindings.getNextCategory())) {
			logic.nextRecipeCategory();
			return true;
		} else if (input.is(keyBindings.getPreviousCategory())) {
			logic.previousRecipeCategory();
			return true;
		} else if (input.is(keyBindings.getNextRecipePage())) {
			logic.nextPage();
			return true;
		} else if (input.is(keyBindings.getPreviousRecipePage())) {
			logic.previousPage();
			return true;
		}
		return false;
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
	public <V> void show(IFocus<V> focus) {
		IFocusGroup checkedFocuses = FocusGroup.create(focus, registeredIngredients);
		if (logic.setFocus(checkedFocuses)) {
			open();
		}
	}

	@Override
	public void show(List<IFocus<?>> focuses) {
		IFocusGroup checkedFocuses = FocusGroup.create(focuses, registeredIngredients);
		if (logic.setFocus(checkedFocuses)) {
			open();
		}
	}

	@Override
	public void showTypes(List<RecipeType<?>> recipeTypes) {
		ErrorUtil.checkNotEmpty(recipeTypes, "recipeTypes");

		if (logic.setCategoryFocus(recipeTypes)) {
			open();
		}
	}

	@SuppressWarnings({"deprecation", "removal"})
	@Override
	public void showCategories(List<ResourceLocation> recipeCategoryUids) {
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");
		List<RecipeType<?>> recipeTypes = recipeManager.getRecipeTypes(recipeCategoryUids);
		if (logic.setCategoryFocus(recipeTypes)) {
			open();
		}
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		double x = MouseUtil.getX();
		double y = MouseUtil.getY();

		return getIngredientUnderMouse(x, y)
			.map(IClickedIngredient::getTypedIngredient)
			.flatMap(i -> i.getIngredient(ingredientType).stream())
			.findFirst()
			.orElse(null);
	}

	public void back() {
		logic.back();
	}

	private void updateLayout() {
		if (!init) {
			return;
		}
		IRecipeCategory<?> recipeCategory = logic.getSelectedRecipeCategory();
		IDrawable recipeBackground = recipeCategory.getBackground();

		final int x = area.getX();
		final int y = area.getY();
		final int width = area.getWidth();
		final int height = area.getHeight();

		int availableHeight = height - headerHeight;
		final int heightPerRecipe = recipeBackground.getHeight() + innerPadding;
		int recipesPerPage = availableHeight / heightPerRecipe;

		if (recipesPerPage == 0) {
			availableHeight = heightPerRecipe;
			recipesPerPage = 1;
		}

		final int recipeXOffset = x + (width - recipeBackground.getWidth()) / 2;
		final int recipeSpacing = (availableHeight - (recipesPerPage * recipeBackground.getHeight())) / (recipesPerPage + 1);

		logic.setRecipesPerPage(recipesPerPage);

		title = StringUtil.stripStyling(recipeCategory.getTitle());
		final int availableTitleWidth = titleArea.getWidth();
		if (font.width(title) > availableTitleWidth) {
			title = StringUtil.truncateStringToWidth(title, availableTitleWidth, font);
		}
		ImmutableRect2i titleStringArea = MathUtil.centerTextArea(this.titleArea, font, title);
		titleHoverChecker.updateBounds(titleStringArea);

		int spacingY = recipeBackground.getHeight() + recipeSpacing;

		recipeLayouts.clear();
		recipeLayouts.addAll(logic.getRecipeLayouts(recipeXOffset, y + headerHeight + recipeSpacing, spacingY));
		addRecipeTransferButtons(recipeLayouts);

		nextPage.active = previousPage.active = logic.hasMultiplePages();
		nextRecipeCategory.active = previousRecipeCategory.active = logic.hasMultipleCategories();

		pageString = logic.getPageString();

		List<ITypedIngredient<?>> recipeCatalysts = logic.getRecipeCatalysts().toList();
		this.recipeCatalysts.updateLayout(recipeCatalysts, this);
		recipeGuiTabs.initLayout(this);
	}

	private void addRecipeTransferButtons(List<RecipeLayout<?>> recipeLayouts) {
		if (minecraft == null) {
			return;
		}
		Player player = minecraft.player;
		if (player == null) {
			return;
		}

		List<? extends GuiEventListener> oldTransferButtons = children().stream()
			.filter(widget -> widget instanceof RecipeTransferButton)
			.toList();

		for (GuiEventListener button : oldTransferButtons) {
			removeWidget(button);
		}

		AbstractContainerMenu container = getParentContainer();

		for (RecipeLayout<?> recipeLayout : recipeLayouts) {
			RecipeTransferButton button = recipeLayout.getRecipeTransferButton();
			if (button != null) {
				button.init(recipeTransferManager, container, player);
				button.setOnClickHandler((mouseX, mouseY) -> {
					boolean maxTransfer = Screen.hasShiftDown();
					if (container != null && RecipeTransferUtil.transferRecipe(recipeTransferManager, container, recipeLayout, player, maxTransfer)) {
						onClose();
					}
				});
				addRenderableWidget(button);
			}
		}
	}

	@Nullable
	private AbstractContainerMenu getParentContainer() {
		if (parentScreen instanceof AbstractContainerScreen<?> screen) {
			return screen.getMenu();
		}
		return null;
	}

	@Override
	public void onStateChange() {
		updateLayout();
	}

	@Nullable
	public IGuiProperties getProperties() {
		if (width <= 0 || height <= 0) {
			return null;
		}
		int extraWidth = getRecipeCatalystExtraWidth();
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
}
