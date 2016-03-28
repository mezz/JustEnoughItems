package mezz.jei.gui;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.InputHandler;
import mezz.jei.transfer.RecipeTransferUtil;
import mezz.jei.util.StringUtil;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.HoverChecker;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecipesGui extends GuiScreen implements IShowsRecipeFocuses {
	private static final int borderPadding = 8;
	private static final int textPadding = 5;
	private static final int buttonWidth = 13;
	private static final int buttonHeight = 12;

	private int titleHeight;
	private int headerHeight;

	/* Internal logic for the gui, handles finding recipes */
	private final IRecipeGuiLogic logic = new RecipeGuiLogic();

	/* List of RecipeLayout to display */
	@Nonnull
	private final List<RecipeLayout> recipeLayouts = new ArrayList<>();

	private String pageString;
	private String title;
	private ResourceLocation backgroundTexture;
	private HoverChecker titleHoverChecker;

	private GuiButton nextRecipeCategory;
	private GuiButton previousRecipeCategory;
	private GuiButton nextPage;
	private GuiButton previousPage;

	@Nullable
	private GuiScreen parentScreen;
	private int xSize;
	private int ySize;
	private int guiLeft;
	private int guiTop;

	public RecipesGui() {
		this.mc = Minecraft.getMinecraft();
	}

	public int getGuiLeft() {
		return guiLeft;
	}

	public int getXSize() {
		return xSize;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.xSize = 176;

		if (this.height > 300) {
			this.ySize = 256;
			this.backgroundTexture = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackgroundTall.png");
		} else {
			this.ySize = 166;
			this.backgroundTexture = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");
		}

		this.guiLeft = (width - this.xSize) / 2;
		this.guiTop = (height - this.ySize) / 2;

		this.titleHeight = fontRendererObj.FONT_HEIGHT + borderPadding;
		this.headerHeight = titleHeight + fontRendererObj.FONT_HEIGHT + textPadding;

		final int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		final int leftButtonX = guiLeft + borderPadding;

		int recipeClassButtonTop = guiTop + borderPadding - 2;
		nextRecipeCategory = new GuiButtonExt(2, rightButtonX, recipeClassButtonTop, buttonWidth, buttonHeight, ">");
		previousRecipeCategory = new GuiButtonExt(3, leftButtonX, recipeClassButtonTop, buttonWidth, buttonHeight, "<");

		int pageButtonTop = guiTop + titleHeight + 3;
		nextPage = new GuiButtonExt(4, rightButtonX, pageButtonTop, buttonWidth, buttonHeight, ">");
		previousPage = new GuiButtonExt(5, leftButtonX, pageButtonTop, buttonWidth, buttonHeight, "<");

		addButtons();

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
		drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

		GlStateManager.disableBlend();

		drawRect(guiLeft + borderPadding + buttonWidth,
				guiTop + borderPadding - 2,
				guiLeft + xSize - borderPadding - buttonWidth,
				guiTop + borderPadding + 10,
				0x30000000);
		drawRect(guiLeft + borderPadding + buttonWidth,
				guiTop + titleHeight + textPadding - 2,
				guiLeft + xSize - borderPadding - buttonWidth,
				guiTop + titleHeight + textPadding + 10,
				0x30000000);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		StringUtil.drawCenteredString(fontRendererObj, title, xSize, guiLeft, guiTop + borderPadding, Color.WHITE.getRGB(), true);
		StringUtil.drawCenteredString(fontRendererObj, pageString, xSize, guiLeft, guiTop + titleHeight + textPadding, Color.WHITE.getRGB(), true);

		nextRecipeCategory.drawButton(mc, mouseX, mouseY);
		previousRecipeCategory.drawButton(mc, mouseX, mouseY);
		nextPage.drawButton(mc, mouseX, mouseY);
		previousPage.drawButton(mc, mouseX, mouseY);

		RecipeLayout hovered = null;
		for (RecipeLayout recipeWidget : recipeLayouts) {
			if (recipeWidget.getFocusUnderMouse(guiLeft, guiTop, mouseX, mouseY) != null) {
				hovered = recipeWidget;
			} else {
				recipeWidget.draw(mc, guiLeft, guiTop, mouseX, mouseY);
			}
		}

		if (hovered != null) {
			hovered.draw(mc, guiLeft, guiTop, mouseX, mouseY);
		}

		if (titleHoverChecker.checkHover(mouseX, mouseY)) {
			Focus focus = logic.getFocus();
			if (focus != null && !focus.isBlank()) {
				String showAllRecipesString = Translator.translateToLocal("jei.tooltip.show.all.recipes");
				TooltipRenderer.drawHoveringText(mc, showAllRecipesString, mouseX, mouseY);
			}
		}
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(backgroundTexture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.zLevel = 0;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return mc.currentScreen == this && (mouseX >= guiLeft) && (mouseY >= guiTop) && (mouseX < guiLeft + xSize) && (mouseY < guiTop + ySize);
	}

	@Nullable
	@Override
	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return null;
		}

		for (RecipeLayout recipeLayouts : this.recipeLayouts) {
			Focus focus = recipeLayouts.getFocusUnderMouse(guiLeft, guiTop, mouseX, mouseY);
			if (focus != null) {
				return focus;
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
		final int x = Mouse.getEventX() * width / mc.displayWidth;
		final int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
		if (isMouseOver(x, y)) {
			int scrollDelta = Mouse.getEventDWheel();
			if (scrollDelta < 0) {
				logic.nextPage();
				updateLayout();
				return;
			} else if (scrollDelta > 0) {
				logic.previousPage();
				updateLayout();
				return;
			}
		}
		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (!isMouseOver(mouseX, mouseY)) {
			return;
		}

		if (titleHoverChecker.checkHover(mouseX, mouseY)) {
			if (logic.setCategoryFocus()) {
				updateLayout();
			}
		} else {
			for (RecipeLayout recipeLayout : recipeLayouts) {
				if (recipeLayout.handleClick(mc, mouseX, mouseY, mouseButton)) {
					return;
				}
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (InputHandler.isInventoryCloseKey(keyCode) || InputHandler.isInventoryToggleKey(keyCode)) {
			close();
		} else if (keyCode == KeyBindings.recipeBack.getKeyCode()) {
			back();
		}
	}

	private void open() {
		if (mc.currentScreen != this) {
			parentScreen = mc.currentScreen;
		}
		mc.displayGuiScreen(this);
	}

	private void close() {
		if (parentScreen != null) {
			mc.displayGuiScreen(parentScreen);
			parentScreen = null;
		} else {
			mc.thePlayer.closeScreen();
		}
		logic.clearHistory();
	}

	public void showRecipes(@Nonnull Focus focus) {
		focus.setMode(Focus.Mode.OUTPUT);
		if (logic.setFocus(focus)) {
			open();
		}
	}

	public void showUses(@Nonnull Focus focus) {
		focus.setMode(Focus.Mode.INPUT);
		if (logic.setFocus(focus)) {
			open();
		}
	}

	public void showCategories(@Nonnull List<String> recipeCategoryUids) {
		if (logic.setCategoryFocus(recipeCategoryUids)) {
			open();
		}
	}

	public void back() {
		if (logic.back()) {
			updateLayout();
		}
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		boolean updateLayout = true;

		if (guibutton.id == nextPage.id) {
			logic.nextPage();
		} else if (guibutton.id == previousPage.id) {
			logic.previousPage();
		} else if (guibutton.id == nextRecipeCategory.id) {
			logic.nextRecipeCategory();
		} else if (guibutton.id == previousRecipeCategory.id) {
			logic.previousRecipeCategory();
		} else if (guibutton.id >= RecipeLayout.recipeTransferButtonIndex) {
			int recipeIndex = guibutton.id - RecipeLayout.recipeTransferButtonIndex;
			RecipeLayout recipeLayout = recipeLayouts.get(recipeIndex);
			boolean maxTransfer = GuiScreen.isShiftKeyDown();
			Container container = getParentContainer();
			if (container != null && RecipeTransferUtil.transferRecipe(container, recipeLayout, mc.thePlayer, maxTransfer)) {
				close();
				updateLayout = false;
			}
		} else {
			updateLayout = false;
		}

		if (updateLayout) {
			updateLayout();
		}
	}

	private void updateLayout() {
		IRecipeCategory recipeCategory = logic.getRecipeCategory();
		if (recipeCategory == null) {
			return;
		}

		IDrawable recipeBackground = recipeCategory.getBackground();

		final int recipesPerPage = Math.max(1, (ySize - headerHeight) / (recipeBackground.getHeight() + borderPadding));
		final int recipeXOffset = (xSize - recipeBackground.getWidth()) / 2;
		final int recipeSpacing = (ySize - headerHeight - (recipesPerPage * recipeBackground.getHeight())) / (recipesPerPage + 1);

		logic.setRecipesPerPage(recipesPerPage);

		title = recipeCategory.getTitle();
		final int titleWidth = fontRendererObj.getStringWidth(title);
		final int titleX = guiLeft + (xSize - titleWidth) / 2;
		final int titleY = guiTop + borderPadding;
		titleHoverChecker = new HoverChecker(titleY, titleY + fontRendererObj.FONT_HEIGHT, titleX, titleX + titleWidth, 0);

		int spacingY = recipeBackground.getHeight() + recipeSpacing;

		recipeLayouts.clear();
		recipeLayouts.addAll(logic.getRecipeWidgets(recipeXOffset, headerHeight + recipeSpacing, spacingY));
		addRecipeTransferButtons(recipeLayouts);

		nextPage.enabled = previousPage.enabled = logic.hasMultiplePages();
		nextRecipeCategory.enabled = previousRecipeCategory.enabled = logic.hasMultipleCategories();

		pageString = logic.getPageString();
	}

	private void addRecipeTransferButtons(List<RecipeLayout> recipeLayouts) {
		buttonList.clear();
		addButtons();

		EntityPlayer player = mc.thePlayer;
		Container container = getParentContainer();

		for (RecipeLayout recipeLayout : recipeLayouts) {
			RecipeTransferButton button = recipeLayout.getRecipeTransferButton();
			button.init(container, recipeLayout, player);
			buttonList.add(button);
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
}
