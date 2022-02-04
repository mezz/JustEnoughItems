package mezz.jei.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.gui.ingredients.RecipeSlot;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.TypedIngredient;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.MouseUtil;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.transfer.RecipeTransferUtil;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Rectangle2dBuilder;
import mezz.jei.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
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

public class RecipesGui extends Screen implements IRecipesGui, IRecipeFocusSource, IRecipeLogicStateListener {
	private static final int borderPadding = 6;
	private static final int innerPadding = 14;
	private static final int buttonWidth = 13;
	private static final int buttonHeight = 13;
	private final RecipeTransferManager recipeTransferManager;
	private final IClientConfig clientConfig;

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
	private Rect2i area = new Rect2i(0, 0, 0, 0);
	private Rect2i titleArea = new Rect2i(0, 0, 0, 0);

	private boolean init = false;

	public RecipesGui(
		IRecipeManager recipeManager,
		RecipeTransferManager recipeTransferManager,
		IIngredientManager ingredientManager,
		IModIdHelper modIdHelper,
		IClientConfig clientConfig
	) {
		super(new TextComponent("Recipes"));
		this.recipeTransferManager = recipeTransferManager;
		this.clientConfig = clientConfig;
		this.logic = new RecipeGuiLogic(recipeManager, recipeTransferManager, this, ingredientManager, modIdHelper);
		this.recipeCatalysts = new RecipeCatalysts();
		this.recipeGuiTabs = new RecipeGuiTabs(this.logic);
		this.minecraft = Minecraft.getInstance();

		Textures textures = Internal.getTextures();
		IDrawableStatic arrowNext = textures.getArrowNext();
		IDrawableStatic arrowPrevious = textures.getArrowPrevious();

		nextRecipeCategory = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowNext, b -> logic.nextRecipeCategory());
		previousRecipeCategory = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowPrevious, b -> logic.previousRecipeCategory());
		nextPage = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowNext, b -> logic.nextPage());
		previousPage = new GuiIconButtonSmall(0, 0, buttonWidth, buttonHeight, arrowPrevious, b -> logic.previousPage());

		background = textures.getGuiBackground();
	}

	private static void drawCenteredStringWithShadow(PoseStack poseStack, Font font, String string, Rect2i area) {
		Rect2i textArea = MathUtil.centerTextArea(area, font, string);
		font.drawShadow(poseStack, string, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}

	private static void drawCenteredStringWithShadow(PoseStack poseStack, Font font, Component text, Rect2i area) {
		Rect2i textArea = MathUtil.centerTextArea(area, font, text);
		font.drawShadow(poseStack, text, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}

	public Rect2i getArea() {
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

		this.area = new Rect2i(guiLeft, guiTop, xSize, ySize);

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
		int titleX = previousRecipeCategory.x + previousRecipeCategory.getWidth();
		this.titleArea = new Rectangle2dBuilder(
			titleX,
			recipeClassButtonTop,
			nextRecipeCategory.x - titleX,
			titleHeight
		)
			.insetByPadding(innerPadding)
			.build();

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

		Rect2i pageArea = MathUtil.union(previousPage.getArea(), nextPage.getArea());
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
			hoveredRecipeCatalyst.drawOverlays(poseStack, 0, 0, mouseX, mouseY);
		}

		if (titleHoverChecker.checkHover(mouseX, mouseY) && !logic.hasAllCategories()) {
			TranslatableComponent showAllRecipesString = new TranslatableComponent("jei.tooltip.show.all.recipes");
			TooltipRenderer.drawHoveringText(poseStack, List.of(showAllRecipesString), mouseX, mouseY);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (minecraft != null && minecraft.screen == this) {
			if (MathUtil.contains(this.area, mouseX, mouseY)) {
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
	public Optional<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isOpen()) {
			return recipeCatalysts.getIngredientUnderMouse(mouseX, mouseY)
				.or(() -> getRecipeLayoutsIngredientUnderMouse(mouseX, mouseY));
		}
		return Optional.empty();
	}

	private Optional<IClickedIngredient<?>> getRecipeLayoutsIngredientUnderMouse(double mouseX, double mouseY) {
		return this.recipeLayouts.stream()
			.map(recipeLayout -> getRecipeLayoutIngredientUnderMouse(recipeLayout, mouseX, mouseY))
			.flatMap(Optional::stream)
			.findFirst();
	}

	private static Optional<IClickedIngredient<?>> getRecipeLayoutIngredientUnderMouse(RecipeLayout<?> recipeLayout, double mouseX, double mouseY) {
		return recipeLayout.getRecipeSlotUnderMouse(mouseX, mouseY)
			.flatMap(clicked ->
				clicked.getDisplayedIngredient()
					.map(displayedIngredient -> {
						Rect2i area = absoluteClickedArea(recipeLayout, clicked.getRect());
						return new ClickedIngredient<>(displayedIngredient, area, false, true);
					})
			);
	}

	/**
	 * Converts from relative recipeLayout coordinates to absolute screen coordinates
	 */
	private static Rect2i absoluteClickedArea(RecipeLayout<?> recipeLayout, Rect2i area) {
		return new Rectangle2dBuilder(area)
			.addX(recipeLayout.getPosX())
			.addY(recipeLayout.getPosY())
			.build();
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
		UserInput input = UserInput.fromVanilla(mouseX, mouseY, mouseButton);
		if (input != null) {
			return handleInput(input) ||
				super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		UserInput input = UserInput.fromVanilla(keyCode, scanCode, modifiers);
		return handleInput(input);
	}

	private boolean handleInput(UserInput input) {
		double mouseX = input.getMouseX();
		double mouseY = input.getMouseY();
		if (isMouseOver(mouseX, mouseY)) {
			if (titleHoverChecker.checkHover(mouseX, mouseY)) {
				if (input.is(KeyBindings.leftClick) && logic.setCategoryFocus()) {
					return true;
				}
			} else {
				for (RecipeLayout<?> recipeLayout : recipeLayouts) {
					if (recipeLayout.handleInput(input)) {
						return true;
					}
				}
			}
		}

		IUserInputHandler handler = recipeGuiTabs.getInputHandler();
		if (handler.handleUserInput(this, input).isPresent()) {
			return true;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (input.is(KeyBindings.escapeKey) || input.is(minecraft.options.keyInventory)) {
			onClose();
			return true;
		} else if (input.is(KeyBindings.recipeBack)) {
			back();
			return true;
		} else if (input.is(KeyBindings.nextCategory)) {
			logic.nextRecipeCategory();
			return true;
		} else if (input.is(KeyBindings.previousCategory)) {
			logic.previousRecipeCategory();
			return true;
		} else {
			JeiRuntime runtime = Internal.getRuntime();
			if (runtime != null) {
				IngredientListOverlay itemListOverlay = runtime.getIngredientListOverlay();
				if (!itemListOverlay.isMouseOver(mouseX, mouseY)) {
					if (input.is(KeyBindings.nextRecipePage)) {
						logic.nextPage();
						return true;
					} else if (input.is(KeyBindings.previousRecipePage)) {
						logic.previousPage();
						return true;
					}
				}
			}
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
		List<Focus<?>> checkedFocuses = Focus.check(focus);
		if (logic.setFocus(checkedFocuses)) {
			open();
		}
	}

	@Override
	public void show(List<IFocus<?>> focuses) {
		List<Focus<?>> checkedFocuses = Focus.check(focuses);
		if (logic.setFocus(checkedFocuses)) {
			open();
		}
	}

	@Override
	public void showCategories(List<ResourceLocation> recipeCategoryUids) {
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

		if (logic.setCategoryFocus(recipeCategoryUids)) {
			open();
		}
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		double x = MouseUtil.getX();
		double y = MouseUtil.getY();

		return getIngredientUnderMouse(x, y)
			.map(IClickedIngredient::getValue)
			.flatMap(i -> TypedIngredient.optionalCast(i, ingredientType))
			.map(ITypedIngredient::getIngredient)
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
		Rect2i titleStringArea = MathUtil.centerTextArea(this.titleArea, font, title);
		titleHoverChecker.updateBounds(titleStringArea);

		int spacingY = recipeBackground.getHeight() + recipeSpacing;

		recipeLayouts.clear();
		recipeLayouts.addAll(logic.getRecipeLayouts(recipeXOffset, y + headerHeight + recipeSpacing, spacingY));
		addRecipeTransferButtons(recipeLayouts);

		nextPage.active = previousPage.active = logic.hasMultiplePages();
		nextRecipeCategory.active = previousRecipeCategory.active = logic.hasMultipleCategories();

		pageString = logic.getPageString();

		List<ITypedIngredient<?>> recipeCatalysts = logic.getRecipeCatalysts();
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
}
