package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import mezz.jei.Internal;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.IRecipesGui;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.MouseHelper;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.transfer.RecipeTransferUtil;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.StringUtil;
import mezz.jei.util.Translator;
import org.lwjgl.input.Mouse;

public class RecipesGui extends GuiScreen implements IRecipesGui, IShowsRecipeFocuses, IRecipeLogicStateListener {
	private static final int borderPadding = 6;
	private static final int innerPadding = 14;
	private static final int buttonWidth = 13;
	private static final int buttonHeight = 13;

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

	private HoverChecker titleHoverChecker = new HoverChecker(0, 0, 0, 0, 0);

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

	public RecipesGui(IRecipeRegistry recipeRegistry, IngredientRegistry ingredientRegistry) {
		this.logic = new RecipeGuiLogic(recipeRegistry, this, ingredientRegistry);
		this.recipeCatalysts = new RecipeCatalysts();
		this.recipeGuiTabs = new RecipeGuiTabs(this.logic);
		this.mc = Minecraft.getMinecraft();

		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		IDrawableStatic arrowNext = guiHelper.getArrowNext();
		IDrawableStatic arrowPrevious = guiHelper.getArrowPrevious();

		nextRecipeCategory = new GuiIconButtonSmall(2, 0, 0, buttonWidth, buttonHeight, arrowNext);
		previousRecipeCategory = new GuiIconButtonSmall(3, 0, 0, buttonWidth, buttonHeight, arrowPrevious);

		nextPage = new GuiIconButtonSmall(4, 0, 0, buttonWidth, buttonHeight, arrowNext);
		previousPage = new GuiIconButtonSmall(5, 0, 0, buttonWidth, buttonHeight, arrowPrevious);

		background = guiHelper.getGuiBackground();
	}

	private static void drawCenteredString(FontRenderer fontRenderer, String string, int guiWidth, int xOffset, int yPos, int color, boolean shadow) {
		fontRenderer.drawString(string, (guiWidth - fontRenderer.getStringWidth(string)) / 2 + xOffset, yPos, color, shadow);
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
		final int maxHeight = Config.getMaxRecipeGuiHeight();
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
		this.buttonList.add(nextRecipeCategory);
		this.buttonList.add(previousRecipeCategory);
		this.buttonList.add(nextPage);
		this.buttonList.add(previousPage);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (mc == null) {
			return;
		}
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.zLevel = 0;
		this.background.draw(Minecraft.getMinecraft(), guiLeft, guiTop, xSize, ySize);

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

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		int textPadding = (buttonHeight - fontRenderer.FONT_HEIGHT) / 2;
		drawCenteredString(fontRenderer, title, xSize, guiLeft, nextRecipeCategory.y + textPadding, Color.WHITE.getRGB(), true);
		drawCenteredString(fontRenderer, pageString, xSize, guiLeft, nextPage.y + textPadding, Color.WHITE.getRGB(), true);

		nextRecipeCategory.drawButton(mc, mouseX, mouseY, partialTicks);
		previousRecipeCategory.drawButton(mc, mouseX, mouseY, partialTicks);
		nextPage.drawButton(mc, mouseX, mouseY, partialTicks);
		previousPage.drawButton(mc, mouseX, mouseY, partialTicks);

		RecipeLayout hoveredLayout = null;
		for (RecipeLayout recipeLayout : recipeLayouts) {
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hoveredLayout = recipeLayout;
			}
			recipeLayout.drawRecipe(mc, mouseX, mouseY);
		}

		GuiIngredient hoveredRecipeCatalyst = recipeCatalysts.draw(mc, mouseX, mouseY);

		recipeGuiTabs.draw(mc, mouseX, mouseY);

		if (hoveredLayout != null) {
			hoveredLayout.drawOverlays(mc, mouseX, mouseY);
		}
		if (hoveredRecipeCatalyst != null) {
			hoveredRecipeCatalyst.drawOverlays(mc, 0, 0, mouseX, mouseY);
		}

		if (titleHoverChecker.checkHover(mouseX, mouseY) && !logic.hasAllCategories()) {
			String showAllRecipesString = Translator.translateToLocal("jei.tooltip.show.all.recipes");
			TooltipRenderer.drawHoveringText(mc, showAllRecipesString, mouseX, mouseY);
		}
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		if (mc != null && mc.currentScreen == this) {
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
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
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
	public void handleMouseInput() throws IOException {
		if (mc == null) {
			return;
		}
		final int x = Mouse.getEventX() * width / mc.displayWidth;
		final int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
		if (isMouseOver(x, y)) {
			int scrollDelta = Mouse.getEventDWheel();
			if (scrollDelta < 0) {
				logic.nextPage();
				return;
			} else if (scrollDelta > 0) {
				logic.previousPage();
				return;
			}
		}
		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mc == null) {
			return;
		}
		if (isMouseOver(mouseX, mouseY)) {
			if (titleHoverChecker.checkHover(mouseX, mouseY)) {
				if (logic.setCategoryFocus()) {
					return;
				}
			} else {
				for (RecipeLayout recipeLayout : recipeLayouts) {
					if (recipeLayout.handleClick(mc, mouseX, mouseY, mouseButton)) {
						return;
					}
				}
			}
		}

		if (recipeGuiTabs.isMouseOver(mouseX, mouseY)) {
			if (recipeGuiTabs.handleMouseClicked(mouseX, mouseY, mouseButton)) {
				return;
			}
		}

		if (handleKeybinds(mouseButton - 100)) {
			return;
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (handleKeybinds(keyCode)) {
			keyHandled = true;
		}
	}

	private boolean handleKeybinds(int eventKey) {
		if (KeyBindings.isInventoryCloseKey(eventKey) || KeyBindings.isInventoryToggleKey(eventKey)) {
			close();
			return true;
		} else if (KeyBindings.recipeBack.isActiveAndMatches(eventKey)) {
			back();
			return true;
		} else {
			JeiRuntime runtime = Internal.getRuntime();
			if (runtime != null) {
				IngredientListOverlay itemListOverlay = runtime.getIngredientListOverlay();
				if (!itemListOverlay.isMouseOver(MouseHelper.getX(), MouseHelper.getY())) {
					if (KeyBindings.nextPage.isActiveAndMatches(eventKey)) {
						logic.nextPage();
						return true;
					} else if (KeyBindings.previousPage.isActiveAndMatches(eventKey)) {
						logic.previousPage();
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isOpen() {
		return mc != null && mc.currentScreen == this;
	}

	private void open() {
		if (mc == null) {
			return;
		}
		if (!isOpen()) {
			parentScreen = mc.currentScreen;
		}
		mc.displayGuiScreen(this);
	}

	public void close() {
		if (mc == null) {
			return;
		}
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
		focus = Focus.check(focus);

		if (logic.setFocus(focus)) {
			open();
		}
	}

	@Override
	public void showCategories(List<String> recipeCategoryUids) {
		ErrorUtil.checkNotEmpty(recipeCategoryUids, "recipeCategoryUids");

		if (logic.setCategoryFocus(recipeCategoryUids)) {
			open();
		}
	}

	@Nullable
	@Override
	public Object getIngredientUnderMouse() {
		IClickedIngredient<?> ingredient = getIngredientUnderMouse(MouseHelper.getX(), MouseHelper.getY());
		if (ingredient != null) {
			return ingredient.getValue();
		}
		return null;
	}

	public void back() {
		logic.back();
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == nextPage.id) {
			logic.nextPage();
		} else if (guibutton.id == previousPage.id) {
			logic.previousPage();
		} else if (guibutton.id == nextRecipeCategory.id) {
			logic.nextRecipeCategory();
		} else if (guibutton.id == previousRecipeCategory.id) {
			logic.previousRecipeCategory();
		} else if (guibutton.id >= RecipeLayout.recipeTransferButtonIndex && mc != null) {
			int recipeIndex = guibutton.id - RecipeLayout.recipeTransferButtonIndex;
			RecipeLayout recipeLayout = recipeLayouts.get(recipeIndex);
			boolean maxTransfer = GuiScreen.isShiftKeyDown();
			Container container = getParentContainer();
			EntityPlayerSP player = mc.player;
			if (container != null && player != null && RecipeTransferUtil.transferRecipe(container, recipeLayout, player, maxTransfer)) {
				close();
			}
		}
	}

	private void updateLayout() {
		if (!init || mc == null) {
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
		titleHoverChecker = new HoverChecker(titleY, titleY + fontRenderer.FONT_HEIGHT, titleX, titleX + titleWidth, 0);

		int spacingY = recipeBackground.getHeight() + recipeSpacing;

		recipeLayouts.clear();
		recipeLayouts.addAll(logic.getRecipeLayouts(recipeXOffset, guiTop + headerHeight + recipeSpacing, spacingY));
		addRecipeTransferButtons(mc, recipeLayouts);

		nextPage.enabled = previousPage.enabled = logic.hasMultiplePages();
		nextRecipeCategory.enabled = previousRecipeCategory.enabled = logic.hasMultipleCategories();

		pageString = logic.getPageString();

		List<Object> recipeCatalysts = logic.getRecipeCatalysts();
		this.recipeCatalysts.updateLayout(recipeCatalysts, this);
		recipeGuiTabs.initLayout(this);
	}

	private void addRecipeTransferButtons(Minecraft minecraft, List<RecipeLayout> recipeLayouts) {
		buttonList.clear();
		addButtons();

		EntityPlayer player = minecraft.player;
		if (player != null) {
			Container container = getParentContainer();

			for (RecipeLayout recipeLayout : recipeLayouts) {
				RecipeTransferButton button = recipeLayout.getRecipeTransferButton();
				if (button != null) {
					button.init(container, player);
					buttonList.add(button);
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
