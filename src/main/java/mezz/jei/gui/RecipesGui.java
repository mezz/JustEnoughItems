package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.HoverChecker;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.config.Constants;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.transfer.RecipeTransferUtil;
import mezz.jei.util.Log;
import mezz.jei.util.StringUtil;
import mezz.jei.util.Translator;

public class RecipesGui extends GuiScreen implements IShowsRecipeFocuses, IMouseHandler {
	private static final int borderPadding = 8;
	private static final int textPadding = 5;

	private int titleHeight;
	private int headerHeight;
	private int buttonWidth;

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
	private RecipeLayout hovered;

	private boolean isOpen = false;

	private int guiLeft;
	private int guiTop;
	private int xSize;
	private int ySize;

	public void initGui(@Nonnull Minecraft minecraft) {
		setWorldAndResolution(minecraft, minecraft.currentScreen.width, minecraft.currentScreen.height);

		this.xSize = 176;

		if (this.height > 300) {
			this.ySize = 256;
			this.backgroundTexture = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackgroundTall.png");
		} else {
			this.ySize = 166;
			this.backgroundTexture = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");
		}

		this.guiLeft = (minecraft.currentScreen.width - this.xSize) / 2;
		this.guiTop = (minecraft.currentScreen.height - this.ySize) / 2;

		this.titleHeight = fontRendererObj.FONT_HEIGHT + borderPadding;
		this.headerHeight = titleHeight + fontRendererObj.FONT_HEIGHT + textPadding;

		buttonWidth = 13;
		
		int buttonHeight = fontRendererObj.FONT_HEIGHT + 3;

		int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		int leftButtonX = guiLeft + borderPadding;

		int recipeClassButtonTop = guiTop + borderPadding - 2;
		nextRecipeCategory = new GuiButtonExt(2, rightButtonX, recipeClassButtonTop, buttonWidth, buttonHeight, ">");
		previousRecipeCategory = new GuiButtonExt(3, leftButtonX, recipeClassButtonTop, buttonWidth, buttonHeight, "<");

		int pageButtonTop = guiTop + titleHeight + 3;
		nextPage = new GuiButtonExt(4, rightButtonX, pageButtonTop, buttonWidth, buttonHeight, ">");
		previousPage = new GuiButtonExt(5, leftButtonX, pageButtonTop, buttonWidth, buttonHeight, "<");

		addButtons();

		updateLayout();
	}

	/**
	 * Fire our own RecipesGuiInitEvent instead of Forge's GuiInitEvent to avoid confusion.
	 * When a gui is init, so is this recipes gui, so it would be fired twice.
	 */
	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		this.mc = mc;
		this.itemRender = mc.getRenderItem();
		this.fontRendererObj = mc.fontRendererObj;
		this.width = width;
		this.height = height;

		MinecraftForge.EVENT_BUS.post(new RecipesGuiInitEvent());

		this.buttonList.clear();
		this.initGui();
	}

	private void addButtons() {
		this.buttonList.add(nextRecipeCategory);
		this.buttonList.add(previousRecipeCategory);
		this.buttonList.add(nextPage);
		this.buttonList.add(previousPage);
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		return isOpen() && (mouseX >= guiLeft) && (mouseY >= guiTop) && (mouseX < guiLeft + xSize) && (mouseY < guiTop + ySize);
	}

	@Nullable
	@Override
	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return null;
		}
		for (RecipeLayout recipeLayouts : this.recipeLayouts) {
			Focus focus = recipeLayouts.getFocusUnderMouse(mouseX, mouseY);
			if (focus != null) {
				return focus;
			}
		}
		return null;
	}

	// workaround to see if a button was clicked
	private boolean guiActionPerformed = false;

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}

		guiActionPerformed = false;

		try {
			handleMouseInput();
		} catch (IOException e) {
			Log.error("IOException on mouse click.", e);
		}

		if (!guiActionPerformed) {
			Minecraft minecraft = Minecraft.getMinecraft();
			for (RecipeLayout recipeLayout : recipeLayouts) {
				if (recipeLayout.handleClick(minecraft, mouseX, mouseY, mouseButton)) {
					return true;
				}
			}

			if (titleHoverChecker.checkHover(mouseX, mouseY)) {
				boolean success = logic.setCategoryFocus();
				if (success) {
					updateLayout();
				}
				return success;
			}
		}

		return guiActionPerformed;
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}

		if (scrollDelta < 0) {
			logic.nextPage();
			updateLayout();
			return true;
		} else if (scrollDelta > 0) {
			logic.previousPage();
			updateLayout();
			return true;
		}
		return false;
	}

	@Override
	public void open() {
		this.isOpen = true;
	}

	@Override
	public void close() {
		this.isOpen = false;
	}

	@Override
	public boolean isOpen() {
		return isOpen && logic.getRecipeCategory() != null;
	}

	public void showRecipes(@Nonnull Focus focus) {
		focus.setMode(Focus.Mode.OUTPUT);
		if (logic.setFocus(focus)) {
			updateLayout();
			open();
		}
	}

	public void showUses(@Nonnull Focus focus) {
		focus.setMode(Focus.Mode.INPUT);
		if (logic.setFocus(focus)) {
			updateLayout();
			open();
		}
	}

	public void showCategories(@Nonnull List<String> recipeCategoryUids) {
		if (logic.setCategoryFocus(recipeCategoryUids)) {
			updateLayout();
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
			if (RecipeTransferUtil.transferRecipe(recipeLayout, Minecraft.getMinecraft().thePlayer, maxTransfer)) {
				close();
				guiActionPerformed = true;
				updateLayout = false;
			}
		} else {
			updateLayout = false;
		}

		if (updateLayout) {
			updateLayout();
			guiActionPerformed = true;
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
		int titleWidth = fontRendererObj.getStringWidth(title);
		int titleX = guiLeft + (xSize - titleWidth) / 2;
		int titleY = guiTop + borderPadding;
		titleHoverChecker = new HoverChecker(titleY, titleY + fontRendererObj.FONT_HEIGHT, titleX, titleX + titleWidth, 0);

		int posX = guiLeft + recipeXOffset;
		int posY = guiTop + headerHeight + recipeSpacing;
		int spacingY = recipeBackground.getHeight() + recipeSpacing;

		recipeLayouts.clear();
		recipeLayouts.addAll(logic.getRecipeWidgets(posX, posY, spacingY));
		addRecipeTransferButtons(recipeLayouts);

		nextPage.enabled = previousPage.enabled = logic.hasMultiplePages();
		nextRecipeCategory.enabled = previousRecipeCategory.enabled = logic.hasMultipleCategories();

		pageString = logic.getPageString();
	}

	private void addRecipeTransferButtons(List<RecipeLayout> recipeLayouts) {
		buttonList.clear();
		addButtons();

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		for (RecipeLayout recipeLayout : recipeLayouts) {
			RecipeTransferButton button = recipeLayout.getRecipeTransferButton();
			button.init(recipeLayout, player);
			buttonList.add(button);
		}
	}

	public void draw(int mouseX, int mouseY) {
		if (!isOpen()) {
			return;
		}

		Minecraft minecraft = Minecraft.getMinecraft();

		nextRecipeCategory.drawButton(minecraft, mouseX, mouseY);
		previousRecipeCategory.drawButton(minecraft, mouseX, mouseY);

		nextPage.drawButton(minecraft, mouseX, mouseY);
		previousPage.drawButton(minecraft, mouseX, mouseY);
		GlStateManager.disableBlend();

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(guiLeft, guiTop, 0.0F);

			drawRect(borderPadding + buttonWidth, borderPadding - 2, xSize - borderPadding - buttonWidth, borderPadding + 10, 0x30000000);
			drawRect(borderPadding + buttonWidth, titleHeight + textPadding - 2, xSize - borderPadding - buttonWidth, titleHeight + textPadding + 10, 0x30000000);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			StringUtil.drawCenteredString(fontRendererObj, title, xSize, borderPadding, Color.WHITE.getRGB(), true);
			StringUtil.drawCenteredString(fontRendererObj, pageString, xSize, titleHeight + textPadding, Color.WHITE.getRGB(), true);
		}
		GlStateManager.popMatrix();

		hovered = null;
		for (RecipeLayout recipeWidget : recipeLayouts) {
			if (recipeWidget.getFocusUnderMouse(mouseX, mouseY) != null) {
				hovered = recipeWidget;
			} else {
				recipeWidget.draw(minecraft, mouseX, mouseY);
			}
		}
	}

	public void drawHovered(int mouseX, int mouseY) {
		if (!isOpen()) {
			return;
		}

		Minecraft minecraft = Minecraft.getMinecraft();

		if (hovered != null) {
			hovered.draw(minecraft, mouseX, mouseY);
		}
		if (titleHoverChecker.checkHover(mouseX, mouseY)) {
			Focus focus = logic.getFocus();
			if (focus != null && !focus.isBlank()) {
				String showAllRecipesString = Translator.translateToLocal("jei.tooltip.show.all.recipes");
				TooltipRenderer.drawHoveringText(minecraft, showAllRecipesString, mouseX, mouseY);
			}
		}
	}

	public void drawBackground() {
		this.zLevel = -100;
		this.drawDefaultBackground();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		bindTexture(backgroundTexture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.zLevel = 0;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}

	private void bindTexture(ResourceLocation texturePath) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texturePath);
	}

}
