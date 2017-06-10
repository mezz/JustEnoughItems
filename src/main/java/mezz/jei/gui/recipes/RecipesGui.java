package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.IRecipesGui;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.elements.GuiIconButtonSmall;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.overlay.GuiProperties;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.input.InputHandler;
import mezz.jei.input.MouseHelper;
import mezz.jei.transfer.RecipeTransferUtil;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.HoverChecker;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class RecipesGui extends GuiScreen implements IRecipesGui, IShowsRecipeFocuses, IRecipeLogicStateListener {
	private static final int borderPadding = 6;
	private static final int innerPadding = 5;
	private static final int buttonWidth = 13;
	private static final int buttonHeight = 13;

	private int headerHeight;

	/* Internal logic for the gui, handles finding recipes */
	private final IRecipeGuiLogic logic;

	/* List of RecipeLayout to display */
	private final List<RecipeLayout> recipeLayouts = new ArrayList<RecipeLayout>();

	private String pageString = "1/1";
	private String title = "";
	private ResourceLocation backgroundTexture = Internal.getHelpers().getGuiHelper().getRecipeBackgroundResource();

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

	public RecipesGui(IRecipeRegistry recipeRegistry) {
		this.logic = new RecipeGuiLogic(recipeRegistry, this);
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

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.xSize = 196;

		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		if (this.height > 300) {
			this.ySize = 256;
			this.backgroundTexture = guiHelper.getRecipeBackgroundTallResource();
		} else {
			this.ySize = 166;
			this.backgroundTexture = guiHelper.getRecipeBackgroundResource();
		}

		this.guiLeft = (width - this.xSize) / 2;
		this.guiTop = (height - this.ySize) / 2 + 8;

		final int rightButtonX = guiLeft + xSize - borderPadding - buttonWidth;
		final int leftButtonX = guiLeft + borderPadding;

		int titleHeight = fontRenderer.FONT_HEIGHT + borderPadding;
		int recipeClassButtonTop = guiTop + titleHeight - buttonHeight + 2;
		nextRecipeCategory.xPosition = rightButtonX;
		nextRecipeCategory.yPosition = recipeClassButtonTop;
		previousRecipeCategory.xPosition = leftButtonX;
		previousRecipeCategory.yPosition = recipeClassButtonTop;

		int pageButtonTop = recipeClassButtonTop + buttonHeight + 2;
		nextPage.xPosition = rightButtonX;
		nextPage.yPosition = pageButtonTop;
		previousPage.xPosition = leftButtonX;
		previousPage.yPosition = pageButtonTop;

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
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(backgroundTexture);
		this.zLevel = 0;
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		GlStateManager.disableBlend();

		drawRect(guiLeft + borderPadding + buttonWidth,
				nextRecipeCategory.yPosition,
				guiLeft + xSize - borderPadding - buttonWidth,
				nextRecipeCategory.yPosition + buttonHeight,
				0x30000000);
		drawRect(guiLeft + borderPadding + buttonWidth,
				nextPage.yPosition,
				guiLeft + xSize - borderPadding - buttonWidth,
				nextPage.yPosition + buttonHeight,
				0x30000000);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		int textPadding = (buttonHeight - fontRenderer.FONT_HEIGHT) / 2;
		drawCenteredString(fontRenderer, title, xSize, guiLeft, nextRecipeCategory.yPosition + textPadding, Color.WHITE.getRGB(), true);
		drawCenteredString(fontRenderer, pageString, xSize, guiLeft, nextPage.yPosition + textPadding, Color.WHITE.getRGB(), true);

		nextRecipeCategory.func_191745_a(mc, mouseX, mouseY, partialTicks);
		previousRecipeCategory.func_191745_a(mc, mouseX, mouseY, partialTicks);
		nextPage.func_191745_a(mc, mouseX, mouseY, partialTicks);
		previousPage.func_191745_a(mc, mouseX, mouseY, partialTicks);

		RecipeLayout hovered = null;
		for (RecipeLayout recipeLayout : recipeLayouts) {
			if (recipeLayout.isMouseOver(mouseX, mouseY)) {
				hovered = recipeLayout;
			} else {
				recipeLayout.draw(mc, mouseX, mouseY);
			}
		}

		GuiIngredient hoveredItemStack = recipeCatalysts.draw(mc, mouseX, mouseY);

		recipeGuiTabs.draw(mc, mouseX, mouseY);

		if (hovered != null) {
			hovered.draw(mc, mouseX, mouseY);
		}
		if (hoveredItemStack != null) {
			hoveredItemStack.drawHovered(mc, 0, 0, mouseX, mouseY);
		}

		if (titleHoverChecker.checkHover(mouseX, mouseY) && !logic.hasAllCategories()) {
			String showAllRecipesString = Translator.translateToLocal("jei.tooltip.show.all.recipes");
			TooltipRenderer.drawHoveringText(mc, showAllRecipesString, mouseX, mouseY);
		}
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return mc.currentScreen == this && (mouseX >= guiLeft) && (mouseY >= guiTop) && (mouseX < guiLeft + xSize) && (mouseY < guiTop + ySize);
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
					Object clicked = recipeLayouts.getIngredientUnderMouse(mouseX, mouseY);
					if (clicked != null) {
						return new ClickedIngredient<Object>(clicked);
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

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (InputHandler.isInventoryCloseKey(keyCode) || InputHandler.isInventoryToggleKey(keyCode)) {
			close();
			keyHandled = true;
		} else if (KeyBindings.recipeBack.isActiveAndMatches(keyCode)) {
			back();
			keyHandled = true;
		} else if (!Internal.getRuntime().getItemListOverlay().isMouseOver(MouseHelper.getX(), MouseHelper.getY())) {
			if (KeyBindings.nextPage.isActiveAndMatches(keyCode)) {
				logic.nextPage();
				keyHandled = true;
			} else if (KeyBindings.previousPage.isActiveAndMatches(keyCode)) {
				logic.previousPage();
				keyHandled = true;
			}
		}
	}

	public boolean isOpen() {
		return mc.currentScreen == this;
	}

	private void open() {
		if (!isOpen()) {
			parentScreen = mc.currentScreen;
		}
		if (parentScreen instanceof GuiContainer) {
			displayGuiScreenWithoutClose(this);
		} else {
			mc.displayGuiScreen(this);
		}
	}

	public void close() {
		if (isOpen()) {
			if (parentScreen != null) {
				mc.displayGuiScreen(parentScreen);
				parentScreen = null;
			} else {
				mc.player.closeScreen();
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
		} else if (guibutton.id >= RecipeLayout.recipeTransferButtonIndex) {
			int recipeIndex = guibutton.id - RecipeLayout.recipeTransferButtonIndex;
			RecipeLayout recipeLayout = recipeLayouts.get(recipeIndex);
			boolean maxTransfer = GuiScreen.isShiftKeyDown();
			Container container = getParentContainer();
			if (container != null && RecipeTransferUtil.transferRecipe(container, recipeLayout, mc.player, maxTransfer)) {
				close();
			}
		}
	}

	private void updateLayout() {
		if (!init) {
			return;
		}
		IRecipeCategory recipeCategory = logic.getSelectedRecipeCategory();
		IDrawable recipeBackground = recipeCategory.getBackground();

		final int recipesPerPage = Math.max(1, (ySize - headerHeight) / (recipeBackground.getHeight() + innerPadding));
		final int recipeXOffset = guiLeft + (xSize - recipeBackground.getWidth()) / 2;
		final int recipeSpacing = (ySize - headerHeight - (recipesPerPage * recipeBackground.getHeight())) / (recipesPerPage + 1);

		logic.setRecipesPerPage(recipesPerPage);

		title = recipeCategory.getTitle();
		final int titleWidth = fontRenderer.getStringWidth(title);
		final int titleX = guiLeft + (xSize - titleWidth) / 2;
		final int titleY = guiTop + borderPadding;
		titleHoverChecker = new HoverChecker(titleY, titleY + fontRenderer.FONT_HEIGHT, titleX, titleX + titleWidth, 0);

		int spacingY = recipeBackground.getHeight() + recipeSpacing;

		recipeLayouts.clear();
		recipeLayouts.addAll(logic.getRecipeLayouts(recipeXOffset, guiTop + headerHeight + recipeSpacing, spacingY));
		addRecipeTransferButtons(recipeLayouts);

		nextPage.enabled = previousPage.enabled = logic.hasMultiplePages();
		nextRecipeCategory.enabled = previousRecipeCategory.enabled = logic.hasMultipleCategories();

		pageString = logic.getPageString();

		List<Object> recipeCatalysts = logic.getRecipeCatalysts();
		GuiProperties guiProperties = GuiProperties.create(this);
		this.recipeCatalysts.updateLayout(recipeCatalysts, guiProperties);
		recipeGuiTabs.initLayout(guiProperties);
	}

	private void addRecipeTransferButtons(List<RecipeLayout> recipeLayouts) {
		buttonList.clear();
		addButtons();

		EntityPlayer player = mc.player;
		Container container = getParentContainer();

		for (RecipeLayout recipeLayout : recipeLayouts) {
			RecipeTransferButton button = recipeLayout.getRecipeTransferButton();
			if (button != null) {
				button.init(container, player);
				buttonList.add(button);
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

	/**
	 * Displays the provided GuiScreen without invoking {@link GuiScreen#onGuiClosed()}.
	 * <p>
	 * The behavior of this method is derived from {@link Minecraft#displayGuiScreen(GuiScreen)}.
	 *
	 * @param guiScreen the GuiScreen to display.
	 */
	//TODO: remove in 1.12 because gui closing no longer drops items by default
	public static void displayGuiScreenWithoutClose(@Nullable GuiScreen guiScreen) {
		Minecraft mc = Minecraft.getMinecraft();
		if (guiScreen == null && mc.world == null) {
			guiScreen = new GuiMainMenu();
		} else if (guiScreen == null && mc.player.getHealth() <= 0) {
			guiScreen = new GuiGameOver(null);
		}
		GuiScreen prev = mc.currentScreen;
		GuiOpenEvent event = new GuiOpenEvent(guiScreen);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			return;
		}
		GuiScreen gui = event.getGui();
		// Only close if an event listener has rejected us
		if (prev != null && gui != prev && gui != guiScreen) {
			prev.onGuiClosed();
		}
		if (gui instanceof GuiMainMenu || gui instanceof GuiMultiplayer) {
			mc.gameSettings.showDebugInfo = false;
			mc.ingameGUI.getChatGUI().clearChatMessages(true);
		}
		mc.currentScreen = gui;
		if (gui == null) {
			mc.getSoundHandler().resumeSounds();
			mc.setIngameFocus();
		} else {
			mc.setIngameNotInFocus();
			KeyBinding.unPressAllKeys();
			while (Mouse.next()) ;
			while (Keyboard.next()) ;
			ScaledResolution reso = new ScaledResolution(mc);
			int width = reso.getScaledWidth();
			int height = reso.getScaledHeight();
			gui.setWorldAndResolution(mc, width, height);
			mc.skipRenderWorld = false;
		}
	}
}
