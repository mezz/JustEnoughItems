package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.gui.IRecipesGui;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.MouseUtil;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.transfer.RecipeTransferUtil;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.StringUtil;
import mezz.jei.util.Translator;

public class RecipesGui extends GuiScreen implements IRecipesGui, IShowsRecipeFocuses, IRecipeLogicStateListener {
	private static final int borderPadding = 6;
	private static final int innerPadding = 14;
	private static final int buttonWidth = 13;
	private static final int buttonHeight = 13;
	private final RecipeTransferManager recipeTransferManager;

	private int headerHeight;

	/* Internal logic for the gui, handles finding recipes */
	private final IRecipeGuiLogic logic;

	/* List of RecipeLayout to display */
	private final List<RecipeLayout> recipeLayouts = new ArrayList<>();

	private String pageString = "1/1";
	private String title = "";
	private final DrawableNineSliceTexture background;

	private final RecipeCatalysts recipeCatalysts;
	private final RecipeGuiTabs recipeGuiTabs;

	private final HoverChecker titleHoverChecker = new HoverChecker();

	private final GuiButton nextRecipeCategory;
	private final GuiButton previousRecipeCategory;
	private final GuiButton nextPage;
	private final GuiButton previousPage;

	@Nullable
	private GuiScreen parentScreen;
	private int xSize;
	private int ySize;
	private int guiLeft;
	private int guiTop;

	private boolean init = false;

	public RecipesGui(IRecipeManager recipeManager, RecipeTransferManager recipeTransferManager, IngredientManager ingredientManager) {
		this.recipeTransferManager = recipeTransferManager;
		this.logic = new RecipeGuiLogic(recipeManager, recipeTransferManager, this, ingredientManager);
		this.recipeCatalysts = new RecipeCatalysts();
		this.recipeGuiTabs = new RecipeGuiTabs(this.logic);
		this.mc = Minecraft.getInstance();

		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		IDrawableStatic arrowNext = guiHelper.getArrowNext();
		IDrawableStatic arrowPrevious = guiHelper.getArrowPrevious();

		nextRecipeCategory = new GuiIconButtonSmall(2, 0, 0, buttonWidth, buttonHeight, arrowNext) {
			@Override
			public void onClick(double mouseX, double mouseY) {
				logic.nextRecipeCategory();
			}
		};
		previousRecipeCategory = new GuiIconButtonSmall(3, 0, 0, buttonWidth, buttonHeight, arrowPrevious) {
			@Override
			public void onClick(double mouseX, double mouseY) {
				logic.previousRecipeCategory();
			}
		};

		nextPage = new GuiIconButtonSmall(4, 0, 0, buttonWidth, buttonHeight, arrowNext) {
			@Override
			public void onClick(double mouseX, double mouseY) {
				logic.nextPage();
			}
		};

		previousPage = new GuiIconButtonSmall(5, 0, 0, buttonWidth, buttonHeight, arrowPrevious) {
			@Override
			public void onClick(double mouseX, double mouseY) {
				logic.previousPage();
			}
		};

		background = guiHelper.getGuiBackground();
	}

	private static void drawCenteredStringWithShadow(FontRenderer fontRenderer, String string, int guiWidth, int xOffset, int yPos, int color) {
		fontRenderer.drawStringWithShadow(string, (guiWidth - fontRenderer.getStringWidth(string)) / 2.0f + xOffset, yPos, color);
	}

	public int getGuiLeft() {
		return guiLeft;
	}

	public int getGuiTop() {
		return guiTop;
	}

	public int getXSize() {
		return xSize;
	}

	public int getYSize() {
		return ySize;
	}

	public int getRecipeCatalystExtraWidth() {
		if (recipeCatalysts.isEmpty()) {
			return 0;
		}
		return recipeCatalysts.getWidth();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.xSize = 198;
		this.ySize = this.height - 68;
		int extraSpace = 0;
		final int maxHeight = ClientConfig.getInstance().getMaxRecipeGuiHeight();
		if (this.ySize > maxHeight) {
			extraSpace = this.ySize - maxHeight;
			this.ySize = maxHeight;
		}

		this.guiLeft = (width - this.xSize) / 2;
		this.guiTop = RecipeGuiTab.TAB_HEIGHT + 21 + (extraSpace / 2);

		final int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		final int leftButtonX = guiLeft + borderPadding;

		int titleHeight = fontRenderer.FONT_HEIGHT + borderPadding;
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

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.zLevel = 0;
		this.background.draw(guiLeft, guiTop, xSize, ySize);

		GlStateManager.disableBlend();

		drawRect(guiLeft + borderPadding + buttonWidth,
			nextRecipeCategory.y,
			guiLeft + xSize - borderPadding - buttonWidth,
			nextRecipeCategory.y + buttonHeight,
			0x30000000);
		drawRect(guiLeft + borderPadding + buttonWidth,
			nextPage.y,
			guiLeft + xSize - borderPadding - buttonWidth,
			nextPage.y + buttonHeight,
			0x30000000);

		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		int textPadding = (buttonHeight - fontRenderer.FONT_HEIGHT) / 2;
		drawCenteredStringWithShadow(fontRenderer, title, xSize, guiLeft, nextRecipeCategory.y + textPadding, 0xFFFFFFFF);
		drawCenteredStringWithShadow(fontRenderer, pageString, xSize, guiLeft, nextPage.y + textPadding, 0xFFFFFFFF);

		nextRecipeCategory.render(mouseX, mouseY, partialTicks);
		previousRecipeCategory.render(mouseX, mouseY, partialTicks);
		nextPage.render(mouseX, mouseY, partialTicks);
		previousPage.render(mouseX, mouseY, partialTicks);

		RecipeLayout hoveredLayout = null;
		for (RecipeLayout recipeLayout : recipeLayouts) {
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hoveredLayout = recipeLayout;
			}
			recipeLayout.drawRecipe(mouseX, mouseY);
		}

		GuiIngredient hoveredRecipeCatalyst = recipeCatalysts.draw(mouseX, mouseY);

		recipeGuiTabs.draw(mc, mouseX, mouseY);

		if (hoveredLayout != null) {
			hoveredLayout.drawOverlays(mouseX, mouseY);
		}
		if (hoveredRecipeCatalyst != null) {
			hoveredRecipeCatalyst.drawOverlays(0, 0, mouseX, mouseY);
		}

		if (titleHoverChecker.checkHover(mouseX, mouseY) && !logic.hasAllCategories()) {
			String showAllRecipesString = Translator.translateToLocal("jei.tooltip.show.all.recipes");
			TooltipRenderer.drawHoveringText(showAllRecipesString, mouseX, mouseY);
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		if (mc.currentScreen == this) {
			if ((mouseX >= guiLeft) && (mouseY >= guiTop) && (mouseX < guiLeft + xSize) && (mouseY < guiTop + ySize)) {
				return true;
			}
			for (RecipeLayout recipeLayout : this.recipeLayouts) {
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
				for (RecipeLayout recipeLayouts : this.recipeLayouts) {
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
	public boolean mouseScrolled(double scrollDelta) {
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
		return super.mouseScrolled(scrollDelta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (isMouseOver(mouseX, mouseY)) {
			if (titleHoverChecker.checkHover(mouseX, mouseY)) {
				if (logic.setCategoryFocus()) {
					return true;
				}
			} else {
				for (RecipeLayout recipeLayout : recipeLayouts) {
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
		if (handleKeybinds(input)) {
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		InputMappings.Input input = InputMappings.getInputByCode(keyCode, scanCode);
		return handleKeybinds(input);
	}

	private boolean handleKeybinds(InputMappings.Input input) {
		if (KeyBindings.isInventoryCloseKey(input) || KeyBindings.isInventoryToggleKey(input)) {
			close();
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
		return mc.currentScreen == this;
	}

	private void open() {
		if (!isOpen()) {
			parentScreen = mc.currentScreen;
		}
		mc.displayGuiScreen(this);
	}

	@Override
	public void close() {
		if (isOpen()) {
			if (parentScreen != null) {
				mc.displayGuiScreen(parentScreen);
				parentScreen = null;
			} else {
				EntityPlayerSP player = mc.player;
				if (player != null) {
					player.closeScreen();
				}
			}
			logic.clearHistory();
		}
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
		IRecipeCategory recipeCategory = logic.getSelectedRecipeCategory();
		IDrawable recipeBackground = recipeCategory.getBackground();

		int availableHeight = ySize - headerHeight;
		final int heightPerRecipe = recipeBackground.getHeight() + innerPadding;
		int recipesPerPage = availableHeight / heightPerRecipe;

		if (recipesPerPage == 0) {
			availableHeight = heightPerRecipe;
			recipesPerPage = 1;
		}

		final int recipeXOffset = guiLeft + (xSize - recipeBackground.getWidth()) / 2;
		final int recipeSpacing = (availableHeight - (recipesPerPage * recipeBackground.getHeight())) / (recipesPerPage + 1);

		logic.setRecipesPerPage(recipesPerPage);

		title = recipeCategory.getTitle();
		int titleWidth = fontRenderer.getStringWidth(title);
		final int availableTitleWidth = (nextPage.x - (previousPage.x + previousPage.width)) - (2 * innerPadding);
		if (titleWidth > availableTitleWidth) {
			title = StringUtil.truncateStringToWidth(title, availableTitleWidth, fontRenderer);
			titleWidth = fontRenderer.getStringWidth(title);
		}
		final int titleX = guiLeft + (xSize - titleWidth) / 2;
		final int titleY = guiTop + borderPadding;
		titleHoverChecker.updateBounds(titleY, titleY + fontRenderer.FONT_HEIGHT, titleX, titleX + titleWidth);

		int spacingY = recipeBackground.getHeight() + recipeSpacing;

		recipeLayouts.clear();
		recipeLayouts.addAll(logic.getRecipeLayouts(recipeXOffset, guiTop + headerHeight + recipeSpacing, spacingY));
		addRecipeTransferButtons(recipeLayouts);

		nextPage.enabled = previousPage.enabled = logic.hasMultiplePages();
		nextRecipeCategory.enabled = previousRecipeCategory.enabled = logic.hasMultipleCategories();

		pageString = logic.getPageString();

		List<Object> recipeCatalysts = logic.getRecipeCatalysts();
		this.recipeCatalysts.updateLayout(recipeCatalysts, this);
		recipeGuiTabs.initLayout(this);
	}

	private void addRecipeTransferButtons(List<RecipeLayout> recipeLayouts) {
		buttons.clear();
		addButtons();

		EntityPlayer player = mc.player;
		if (player != null) {
			Container container = getParentContainer();

			for (RecipeLayout recipeLayout : recipeLayouts) {
				RecipeTransferButton button = recipeLayout.getRecipeTransferButton();
				if (button != null) {
					button.init(recipeTransferManager, container, player);
					button.setOnClickHandler((mouseX, mouseY) -> {
						boolean maxTransfer = GuiScreen.isShiftKeyDown();
						if (container != null && RecipeTransferUtil.transferRecipe(recipeTransferManager, container, recipeLayout, player, maxTransfer)) {
							close();
						}
					});
					addButton(button);
				}
			}
		}
	}

	@Nullable
	public GuiScreen getParentScreen() {
		return parentScreen;
	}

	@Nullable
	private Container getParentContainer() {
		if (parentScreen instanceof GuiContainer) {
			return ((GuiContainer) parentScreen).inventorySlots;
		}
		return null;
	}

	@Override
	public void onStateChange() {
		updateLayout();
	}
}
