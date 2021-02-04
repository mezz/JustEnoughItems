package mezz.jei.gui.recipes;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.MouseUtil;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.transfer.RecipeTransferUtil;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Rectangle2dBuilder;
import mezz.jei.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RecipesGui extends Screen implements IRecipesGui, IShowsRecipeFocuses, IRecipeLogicStateListener {
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
	private String title = "";
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
	private Rectangle2d area = new Rectangle2d(0, 0, 0, 0);
	private Rectangle2d titleArea = new Rectangle2d(0, 0, 0, 0);

	private boolean init = false;

	public RecipesGui(
		IRecipeManager recipeManager,
		RecipeTransferManager recipeTransferManager,
		IngredientManager ingredientManager,
		IModIdHelper modIdHelper,
		IClientConfig clientConfig
	) {
		super(new StringTextComponent("Recipes"));
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

	private static void drawCenteredStringWithShadow(MatrixStack matrixStack, FontRenderer font, String string, Rectangle2d area) {
		Rectangle2d textArea = MathUtil.centerTextArea(area, font, string);
		font.drawStringWithShadow(matrixStack, string, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}

	public Rectangle2d getArea() {
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
	public void init(Minecraft minecraft, int width, int height) {
		super.init(minecraft, width, height);

		final int xSize = 198;
		int ySize = this.height - 68;
		int extraSpace = 0;
		final int maxHeight = this.clientConfig.getMaxRecipeGuiHeight();
		if (ySize > maxHeight) {
			extraSpace = ySize - maxHeight;
			ySize = maxHeight;
		}

		final int guiLeft = (width - xSize) / 2;
		final int guiTop = RecipeGuiTab.TAB_HEIGHT + 21 + (extraSpace / 2);

		this.area = new Rectangle2d(guiLeft, guiTop, xSize, ySize);

		final int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		final int leftButtonX = guiLeft + borderPadding;

		int titleHeight = font.FONT_HEIGHT + borderPadding;
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

		addButtons();

		this.init = true;
		updateLayout();
	}

	private void addButtons() {
		this.addButton(nextRecipeCategory);
		this.addButton(nextRecipeCategory);
		this.addButton(previousRecipeCategory);
		this.addButton(nextPage);
		this.addButton(previousPage);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (minecraft == null) {
			return;
		}
		renderBackground(matrixStack);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		final int x = area.getX();
		final int y = area.getY();
		final int width = area.getWidth();
		final int height = area.getHeight();
		this.background.draw(matrixStack, x, y, width, height);

		RenderSystem.disableBlend();

		fill(matrixStack,
			x + borderPadding + buttonWidth,
			nextRecipeCategory.y,
			x + width - borderPadding - buttonWidth,
			nextRecipeCategory.y + buttonHeight,
			0x30000000);
		fill(matrixStack,
			x + borderPadding + buttonWidth,
			nextPage.y,
			x + width - borderPadding - buttonWidth,
			nextPage.y + buttonHeight,
			0x30000000);

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		drawCenteredStringWithShadow(matrixStack, font, title, titleArea);

		Rectangle2d pageArea = MathUtil.union(previousPage.getArea(), nextPage.getArea());
		drawCenteredStringWithShadow(matrixStack, font, pageString, pageArea);

		nextRecipeCategory.render(matrixStack, mouseX, mouseY, partialTicks);
		previousRecipeCategory.render(matrixStack, mouseX, mouseY, partialTicks);
		nextPage.render(matrixStack, mouseX, mouseY, partialTicks);
		previousPage.render(matrixStack, mouseX, mouseY, partialTicks);

		RecipeLayout<?> hoveredLayout = null;
		for (RecipeLayout<?> recipeLayout : recipeLayouts) {
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hoveredLayout = recipeLayout;
			}
			recipeLayout.drawRecipe(matrixStack, mouseX, mouseY);
		}

		GuiIngredient<?> hoveredRecipeCatalyst = recipeCatalysts.draw(matrixStack, mouseX, mouseY);

		recipeGuiTabs.draw(minecraft, matrixStack, mouseX, mouseY);

		if (hoveredLayout != null) {
			hoveredLayout.drawOverlays(matrixStack, mouseX, mouseY);
		}
		if (hoveredRecipeCatalyst != null) {
			hoveredRecipeCatalyst.drawOverlays(matrixStack, 0, 0, mouseX, mouseY);
		}

		if (titleHoverChecker.checkHover(mouseX, mouseY) && !logic.hasAllCategories()) {
			TranslationTextComponent showAllRecipesString = new TranslationTextComponent("jei.tooltip.show.all.recipes");
			TooltipRenderer.drawHoveringText(showAllRecipesString, mouseX, mouseY, matrixStack);
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		if (minecraft != null && minecraft.currentScreen == this) {
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

	@Nullable
	@Override
	public IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isOpen()) {
			{
				IClickedIngredient<?> clicked = recipeCatalysts.getIngredientUnderMouse(mouseX, mouseY);
				if (clicked != null) {
					return clicked;
				}
			}

			if (isMouseOver(mouseX, mouseY)) {
				for (RecipeLayout<?> recipeLayouts : this.recipeLayouts) {
					GuiIngredient<?> clicked = recipeLayouts.getGuiIngredientUnderMouse(mouseX, mouseY);
					if (clicked != null) {
						Object displayedIngredient = clicked.getDisplayedIngredient();
						if (displayedIngredient != null) {
							return ClickedIngredient.create(displayedIngredient, clicked.getRect());
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return true;
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
		if (isMouseOver(mouseX, mouseY)) {
			if (titleHoverChecker.checkHover(mouseX, mouseY)) {
				if (logic.setCategoryFocus()) {
					return true;
				}
			} else {
				for (RecipeLayout<?> recipeLayout : recipeLayouts) {
					if (recipeLayout.handleClick(mouseX, mouseY, mouseButton)) {
						return true;
					}
				}
			}
		}

		if (recipeGuiTabs.isMouseOver(mouseX, mouseY)) {
			if (recipeGuiTabs.handleMouseClicked(mouseX, mouseY, mouseButton)) {
				return true;
			}
		}

		InputMappings.Input input = InputMappings.Type.MOUSE.getOrMakeInput(mouseButton);
		if (handleKeybindings(input)) {
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		InputMappings.Input input = InputMappings.getInputByCode(keyCode, scanCode);
		return handleKeybindings(input);
	}

	private boolean handleKeybindings(InputMappings.Input input) {
		if (KeyBindings.isInventoryCloseKey(input) || KeyBindings.isInventoryToggleKey(input)) {
			closeScreen();
			return true;
		} else if (KeyBindings.recipeBack.isActiveAndMatches(input)) {
			back();
			return true;
		} else {
			JeiRuntime runtime = Internal.getRuntime();
			if (runtime != null) {
				IngredientListOverlay itemListOverlay = runtime.getIngredientListOverlay();
				if (!itemListOverlay.isMouseOver(MouseUtil.getX(), MouseUtil.getY())) {
					if (KeyBindings.nextPage.isActiveAndMatches(input)) {
						logic.nextPage();
						return true;
					} else if (KeyBindings.previousPage.isActiveAndMatches(input)) {
						logic.previousPage();
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isOpen() {
		return minecraft != null && minecraft.currentScreen == this;
	}

	private void open() {
		if (minecraft != null) {
			if (!isOpen()) {
				parentScreen = minecraft.currentScreen;
			}
			minecraft.displayGuiScreen(this);
		}
	}

	@Override
	public void closeScreen() {
		if (isOpen() && minecraft != null) {
			if (parentScreen != null) {
				minecraft.displayGuiScreen(parentScreen);
				parentScreen = null;
			}
			logic.clearHistory();
			return;
		}
		super.closeScreen();
	}

	@Override
	public <V> void show(IFocus<V> focus) {
		Focus<V> checkedFocus = Focus.check(focus);
		if (logic.setFocus(checkedFocus)) {
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
	public Object getIngredientUnderMouse() {
		IClickedIngredient<?> ingredient = getIngredientUnderMouse(MouseUtil.getX(), MouseUtil.getY());
		if (ingredient != null) {
			return ingredient.getValue();
		}
		return null;
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

		title = recipeCategory.getTitle();
		final int availableTitleWidth = titleArea.getWidth();
		if (font.getStringWidth(title) > availableTitleWidth) {
			title = StringUtil.truncateStringToWidth(title, availableTitleWidth, font);
		}
		Rectangle2d titleStringArea = MathUtil.centerTextArea(this.titleArea, font, title);
		titleHoverChecker.updateBounds(titleStringArea);

		int spacingY = recipeBackground.getHeight() + recipeSpacing;

		recipeLayouts.clear();
		recipeLayouts.addAll(logic.getRecipeLayouts(recipeXOffset, y + headerHeight + recipeSpacing, spacingY));
		addRecipeTransferButtons(recipeLayouts);

		nextPage.active = previousPage.active = logic.hasMultiplePages();
		nextRecipeCategory.active = previousRecipeCategory.active = logic.hasMultipleCategories();

		pageString = logic.getPageString();

		List<Object> recipeCatalysts = logic.getRecipeCatalysts();
		this.recipeCatalysts.updateLayout(recipeCatalysts, this);
		recipeGuiTabs.initLayout(this);
	}

	private void addRecipeTransferButtons(List<RecipeLayout<?>> recipeLayouts) {
		children.removeAll(buttons);
		buttons.clear();
		addButtons();

		if (minecraft == null) {
			return;
		}
		PlayerEntity player = minecraft.player;
		if (player == null) {
			return;
		}
		Container container = getParentContainer();

		for (RecipeLayout<?> recipeLayout : recipeLayouts) {
			RecipeTransferButton button = recipeLayout.getRecipeTransferButton();
			if (button != null) {
				button.init(recipeTransferManager, container, player);
				button.setOnClickHandler((mouseX, mouseY) -> {
					boolean maxTransfer = Screen.hasShiftDown();
					if (container != null && RecipeTransferUtil.transferRecipe(recipeTransferManager, container, recipeLayout, player, maxTransfer)) {
						closeScreen();
					}
				});
				addButton(button);
			}
		}
	}

	@Nullable
	private Container getParentContainer() {
		if (parentScreen instanceof ContainerScreen) {
			return ((ContainerScreen<?>) parentScreen).getContainer();
		}
		return null;
	}

	@Override
	public void onStateChange() {
		updateLayout();
	}
}
