package mezz.jei.gui;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.ItemFilter;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.JEIModConfigGui;
import mezz.jei.gui.ingredients.GuiIngredientFast;
import mezz.jei.gui.ingredients.GuiIngredientFastList;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.ICloseable;
import mezz.jei.input.IKeyable;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.util.Java6Helper;
import mezz.jei.util.Log;
import mezz.jei.util.MathUtil;
import mezz.jei.util.StackHelper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.HoverChecker;

public class ItemListOverlay implements IItemListOverlay, IShowsRecipeFocuses, IMouseHandler, IKeyable, ICloseable {

	private static final int borderPadding = 2;
	private static final int searchHeight = 16;
	private static final int buttonSize = 20;
	private static final String nextLabel = ">";
	private static final String backLabel = "<";

	private static final int itemStackPadding = 1;
	private static final int itemStackWidth = GuiItemStackGroup.getWidth(itemStackPadding);
	private static final int itemStackHeight = GuiItemStackGroup.getHeight(itemStackPadding);
	private static int firstItemIndex = 0;

	private final ItemFilter itemFilter;
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers;
	private final Set<ItemStack> highlightedStacks = new HashSet<ItemStack>();

	private final GuiIngredientFastList guiIngredientList;
	private GuiButton nextButton;
	private GuiButton backButton;
	private GuiButton configButton;
	private IDrawable configButtonIcon;
	private IDrawable configButtonCheatIcon;
	private HoverChecker configButtonHoverChecker;
	private GuiTextFieldFilter searchField;

	private String pageNumDisplayString;
	private int pageNumDisplayX;
	private int pageNumDisplayY;

	private GuiIngredientFast hovered = null;

	// properties of the gui we're beside
	@Nullable
	private GuiProperties guiProperties;
	@Nullable
	private List<Rectangle> guiAreas;
	private List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandlers = Collections.emptyList();

	private boolean open = false;

	public ItemListOverlay(ItemFilter itemFilter, List<IAdvancedGuiHandler<?>> advancedGuiHandlers, IIngredientRegistry ingredientRegistry) {
		this.itemFilter = itemFilter;
		this.advancedGuiHandlers = advancedGuiHandlers;
		this.guiIngredientList = new GuiIngredientFastList(ingredientRegistry);
	}

	public void initGui(GuiScreen guiScreen) {
		GuiProperties guiProperties = GuiProperties.create(guiScreen);
		if (guiProperties == null) {
			return;
		}

		this.guiProperties = guiProperties;
		this.activeAdvancedGuiHandlers = getActiveAdvancedGuiHandlers(guiScreen);
		if (!activeAdvancedGuiHandlers.isEmpty() && guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			guiAreas = getGuiAreas(guiContainer);
		} else {
			guiAreas = null;
		}

		final int columns = getColumns(guiProperties);
		if (columns < 4) {
			close();
			return;
		}

		final int rows = getRows(guiProperties);
		final int xSize = columns * itemStackWidth;
		final int xEmptySpace = guiProperties.getScreenWidth() - guiProperties.getGuiLeft() - guiProperties.getGuiXSize() - xSize;

		final int leftEdge = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + (xEmptySpace / 2);
		final int rightEdge = leftEdge + xSize;

		final int yItemButtonSpace = getItemButtonYSpace(guiProperties);
		final int itemButtonsHeight = rows * itemStackHeight;

		final int buttonStartY = buttonSize + (2 * borderPadding) + (yItemButtonSpace - itemButtonsHeight) / 2;
		createItemButtons(guiIngredientList, guiAreas, leftEdge, buttonStartY, columns, rows);

		nextButton = new GuiButtonExt(0, rightEdge - buttonSize, borderPadding, buttonSize, buttonSize, nextLabel);
		backButton = new GuiButtonExt(1, leftEdge, borderPadding, buttonSize, buttonSize, backLabel);

		final int searchFieldX;
		final int searchFieldY = guiProperties.getScreenHeight() - searchHeight - borderPadding - 2;
		final int searchFieldWidth;

		if (isSearchBarCentered(guiProperties)) {
			searchFieldX = guiProperties.getGuiLeft();
			searchFieldWidth = guiProperties.getGuiXSize() - buttonSize - 1;
		} else {
			searchFieldX = leftEdge;
			searchFieldWidth = rightEdge - leftEdge - buttonSize - 1;
		}

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		searchField = new GuiTextFieldFilter(0, fontRenderer, searchFieldX, searchFieldY, searchFieldWidth, searchHeight);
		setKeyboardFocus(false);
		searchField.setItemFilter(itemFilter);

		int configButtonX = searchFieldX + searchFieldWidth + 1;
		int configButtonY = guiProperties.getScreenHeight() - buttonSize - borderPadding;
		configButton = new GuiButtonExt(2, configButtonX, configButtonY, buttonSize, buttonSize, null);
		ResourceLocation configButtonIconLocation = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_RECIPE_BACKGROUND_PATH);
		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		configButtonIcon = guiHelper.createDrawable(configButtonIconLocation, 0, 166, 16, 16);
		configButtonCheatIcon = guiHelper.createDrawable(configButtonIconLocation, 16, 166, 16, 16);
		configButtonHoverChecker = new HoverChecker(configButton, 0);

		updateLayout();

		open();
	}

	private static boolean isSearchBarCentered(GuiProperties guiProperties) {
		return Config.isCenterSearchBarEnabled() &&
				guiProperties.getGuiTop() + guiProperties.getGuiYSize() + searchHeight < guiProperties.getScreenHeight();
	}

	private List<IAdvancedGuiHandler<?>> getActiveAdvancedGuiHandlers(GuiScreen guiScreen) {
		List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandler = new ArrayList<IAdvancedGuiHandler<?>>();
		if (guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			for (IAdvancedGuiHandler<?> advancedGuiHandler : advancedGuiHandlers) {
				if (advancedGuiHandler.getGuiContainerClass().isAssignableFrom(guiContainer.getClass())) {
					activeAdvancedGuiHandler.add(advancedGuiHandler);
				}
			}
		}
		return activeAdvancedGuiHandler;
	}

	private List<Rectangle> getGuiAreas(GuiContainer guiContainer) {
		List<Rectangle> guiAreas = new ArrayList<Rectangle>();
		for (IAdvancedGuiHandler<?> advancedGuiHandler : activeAdvancedGuiHandlers) {
			List<Rectangle> guiExtraAreas = getGuiAreas(guiContainer, advancedGuiHandler);
			if (guiExtraAreas != null) {
				guiAreas.addAll(guiExtraAreas);
			}
		}
		return guiAreas;
	}

	private <T extends GuiContainer> List<Rectangle> getGuiAreas(GuiContainer guiContainer, IAdvancedGuiHandler<T> advancedGuiHandler) {
		if (advancedGuiHandler.getGuiContainerClass().isAssignableFrom(guiContainer.getClass())) {
			T guiT = advancedGuiHandler.getGuiContainerClass().cast(guiContainer);
			return advancedGuiHandler.getGuiExtraAreas(guiT);
		}
		return null;
	}

	public void updateGui(GuiScreen guiScreen) {
		if (this.guiProperties == null) {
			initGui(guiScreen);
		} else {
			GuiProperties guiProperties = GuiProperties.create(guiScreen);
			if (guiProperties == null) {
				return;
			}
			if (!this.guiProperties.equals(guiProperties)) {
				initGui(guiScreen);
			} else if (!activeAdvancedGuiHandlers.isEmpty() && guiScreen instanceof GuiContainer) {
				GuiContainer guiContainer = (GuiContainer) guiScreen;
				List<Rectangle> guiAreas = getGuiAreas(guiContainer);
				if (!Java6Helper.equals(this.guiAreas, guiAreas)) {
					initGui(guiContainer);
				}
			}
		}
	}

	private static void createItemButtons(GuiIngredientFastList guiItemStacks, @Nullable List<Rectangle> guiAreas, final int xStart, final int yStart, final int columnCount, final int rowCount) {
		guiItemStacks.clear();

		for (int row = 0; row < rowCount; row++) {
			int y = yStart + (row * itemStackHeight);
			for (int column = 0; column < columnCount; column++) {
				int x = xStart + (column * itemStackWidth);
				GuiIngredientFast guiIngredientFast = new GuiIngredientFast(x, y, itemStackPadding);
				if (guiAreas != null) {
					Rectangle stackArea = guiIngredientFast.getArea();
					if (intersects(guiAreas, stackArea)) {
						continue;
					}
				}
				guiItemStacks.add(guiIngredientFast);
			}
		}
	}

	private static boolean intersects(List<Rectangle> areas, Rectangle comparisonArea) {
		for (Rectangle area : areas) {
			if (area.intersects(comparisonArea)) {
				return true;
			}
		}
		return false;
	}

	private void updateLayout() {
		ImmutableList<IIngredientListElement> ingredientList = itemFilter.getIngredientList();
		guiIngredientList.set(firstItemIndex, ingredientList);

		FontRenderer fontRendererObj = Minecraft.getMinecraft().fontRendererObj;

		pageNumDisplayString = (getPageNum() + 1) + "/" + getPageCount();
		int pageDisplayWidth = fontRendererObj.getStringWidth(pageNumDisplayString);
		pageNumDisplayX = ((backButton.xPosition + backButton.width) + nextButton.xPosition) / 2 - (pageDisplayWidth / 2);
		pageNumDisplayY = backButton.yPosition + Math.round((backButton.height - fontRendererObj.FONT_HEIGHT) / 2.0f);

		searchField.update();
	}

	private void nextPage() {
		final int itemsCount = itemFilter.size();
		if (itemsCount == 0) {
			firstItemIndex = 0;
			return;
		}

		firstItemIndex += guiIngredientList.size();
		if (firstItemIndex >= itemsCount) {
			firstItemIndex = 0;
		}
		updateLayout();
	}

	private void previousPage() {
		final int itemsPerPage = guiIngredientList.size();
		if (itemsPerPage == 0) {
			firstItemIndex = 0;
			return;
		}
		final int itemsCount = itemFilter.size();

		int pageNum = firstItemIndex / itemsPerPage;
		if (pageNum == 0) {
			pageNum = itemsCount / itemsPerPage;
		} else {
			pageNum--;
		}

		firstItemIndex = itemsPerPage * pageNum;
		if (firstItemIndex > 0 && firstItemIndex == itemsCount) {
			pageNum--;
			firstItemIndex = itemsPerPage * pageNum;
		}
		updateLayout();
	}

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY) {
		if (!isOpen()) {
			return;
		}

		GlStateManager.disableLighting();
		
		minecraft.fontRendererObj.drawString(pageNumDisplayString, pageNumDisplayX, pageNumDisplayY, Color.white.getRGB(), true);
		searchField.drawTextBox();

		nextButton.drawButton(minecraft, mouseX, mouseY);
		backButton.drawButton(minecraft, mouseX, mouseY);
		configButton.drawButton(minecraft, mouseX, mouseY);

		IDrawable icon = Config.isCheatItemsEnabled() ? configButtonCheatIcon : configButtonIcon;
		icon.draw(minecraft, configButton.xPosition + 2, configButton.yPosition + 2);

		GlStateManager.disableBlend();

		if (shouldShowDeleteItemTooltip(minecraft)) {
			hovered = guiIngredientList.render(minecraft, false, mouseX, mouseY);
		} else {
			boolean mouseOver = isMouseOver(mouseX, mouseY);
			hovered = guiIngredientList.render(minecraft, mouseOver, mouseX, mouseY);
		}

		if (!highlightedStacks.isEmpty()) {
			StackHelper helper = Internal.getHelpers().getStackHelper();
			for (GuiIngredientFast guiItemStack : guiIngredientList.getAllGuiIngredients()) {
				Object ingredient = guiItemStack.getIngredient();
				if (ingredient instanceof ItemStack) {
					if (helper.containsStack(highlightedStacks, (ItemStack) ingredient) != null) {
						guiItemStack.drawHighlight();
					}
				}
			}
		}

		if (hovered != null) {
			RenderHelper.enableGUIStandardItemLighting();
			hovered.drawHovered(minecraft);
			RenderHelper.disableStandardItemLighting();
		}

		GlStateManager.enableAlpha();
	}

	private boolean shouldShowDeleteItemTooltip(Minecraft minecraft) {
		if (Config.isDeleteItemsInCheatModeActive()) {
			EntityPlayer player = minecraft.thePlayer;
			if (player.inventory.getItemStack() != null) {
				return true;
			}
		}
		return false;
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (!isOpen()) {
			return;
		}
		
		boolean mouseOver = isMouseOver(mouseX, mouseY);
		if (mouseOver && shouldShowDeleteItemTooltip(minecraft)) {
			String deleteItem = Translator.translateToLocal("jei.tooltip.delete.item");
			TooltipRenderer.drawHoveringText(minecraft, deleteItem, mouseX, mouseY);
		}

		if (hovered != null) {
			hovered.drawTooltip(minecraft, mouseX, mouseY);
		}

		if (configButtonHoverChecker.checkHover(mouseX, mouseY)) {
			String configString = Translator.translateToLocal("jei.tooltip.config");
			if (Config.isCheatItemsEnabled()) {
				List<String> tooltip = Arrays.asList(
						configString,
						TextFormatting.RED + Translator.translateToLocal("jei.tooltip.cheat.mode")
				);
				TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY);
			} else {
				TooltipRenderer.drawHoveringText(minecraft, configString, mouseX, mouseY);
			}
		}
	}

	public void handleTick() {
		if (searchField != null) {
			searchField.updateCursorCounter();
		}
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		if (guiProperties == null || !isOpen()) {
			return false;
		} else if (mouseX < guiProperties.getGuiLeft() + guiProperties.getGuiXSize()) {
			return isSearchBarCentered(guiProperties) &&
					(searchField.isMouseOver(mouseX, mouseY) || configButtonHoverChecker.checkHover(mouseX, mouseY));
		}

		if (guiAreas != null) {
			for (Rectangle guiArea : guiAreas) {
				if (guiArea.contains(mouseX, mouseY)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return null;
		}

		ClickedIngredient<?> clicked = guiIngredientList.getIngredientUnderMouse(mouseX, mouseY);
		if (clicked != null) {
			setKeyboardFocus(false);
			clicked.setAllowsCheating();
		}
		return clicked;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return true;
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isMouseOver(mouseX, mouseY)) {
			setKeyboardFocus(false);
			return false;
		}

		if (Config.isDeleteItemsInCheatModeActive()) {
			Minecraft minecraft = Minecraft.getMinecraft();
			EntityPlayerSP player = minecraft.thePlayer;
			ItemStack itemStack = player.inventory.getItemStack();
			if (itemStack != null) {
				player.inventory.setItemStack(null);
				PacketJEI packet = new PacketDeletePlayerItem(itemStack);
				JustEnoughItems.getProxy().sendPacketToServer(packet);
				return true;
			}
		}

		boolean buttonClicked = handleMouseClickedButtons(mouseX, mouseY);
		if (buttonClicked) {
			setKeyboardFocus(false);
			return true;
		}

		return handleMouseClickedSearch(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (scrollDelta < 0) {
			nextPage();
			return true;
		} else if (scrollDelta > 0) {
			previousPage();
			return true;
		}
		return false;
	}

	private boolean handleMouseClickedButtons(int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (nextButton.mousePressed(minecraft, mouseX, mouseY)) {
			nextPage();
			nextButton.playPressSound(minecraft.getSoundHandler());
			return true;
		} else if (backButton.mousePressed(minecraft, mouseX, mouseY)) {
			previousPage();
			backButton.playPressSound(minecraft.getSoundHandler());
			return true;
		} else if (configButton.mousePressed(minecraft, mouseX, mouseY)) {
			close();
			configButton.playPressSound(minecraft.getSoundHandler());
			if (minecraft.currentScreen != null) {
				GuiScreen configScreen = new JEIModConfigGui(minecraft.currentScreen);
				minecraft.displayGuiScreen(configScreen);
			}
			return true;
		}
		return false;
	}

	private boolean handleMouseClickedSearch(int mouseX, int mouseY, int mouseButton) {
		boolean searchClicked = searchField.isMouseOver(mouseX, mouseY);
		setKeyboardFocus(searchClicked);
		if (searchClicked && searchField.handleMouseClicked(mouseX, mouseY, mouseButton)) {
			updateLayout();
		}
		return searchClicked;
	}

	@Override
	public boolean hasKeyboardFocus() {
		return searchField != null && searchField.isFocused();
	}

	@Override
	public void setKeyboardFocus(boolean keyboardFocus) {
		if (searchField != null) {
			searchField.setFocused(keyboardFocus);
		}
	}

	@Override
	public boolean onKeyPressed(char typedChar, int keyCode) {
		if (hasKeyboardFocus()) {
			boolean changed = searchField.textboxKeyTyped(typedChar, keyCode);
			if (changed) {
				firstItemIndex = 0;
				updateLayout();
			}
			return changed || ChatAllowedCharacters.isAllowedCharacter(typedChar);
		}
		return false;
	}

	private static int getItemButtonXSpace(GuiProperties guiProperties) {
		return guiProperties.getScreenWidth() - (guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + (2 * borderPadding));
	}

	private static int getItemButtonYSpace(GuiProperties guiProperties) {
		if (isSearchBarCentered(guiProperties)) {
			return guiProperties.getScreenHeight() - (buttonSize + (3 * borderPadding));
		}
		return guiProperties.getScreenHeight() - (buttonSize + searchHeight + 2 + (4 * borderPadding));
	}

	private int getColumns(GuiProperties guiProperties) {
		return getItemButtonXSpace(guiProperties) / itemStackWidth;
	}

	private int getRows(GuiProperties guiProperties) {
		return getItemButtonYSpace(guiProperties) / itemStackHeight;
	}

	private int getPageCount() {
		final int itemCount = itemFilter.size();
		final int stacksPerPage = guiIngredientList.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		int pageCount = MathUtil.divideCeil(itemCount, stacksPerPage);
		pageCount = Math.max(1, pageCount);
		return pageCount;
	}

	private int getPageNum() {
		final int stacksPerPage = guiIngredientList.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		return firstItemIndex / stacksPerPage;
	}

	public void open() {
		open = true;
		setKeyboardFocus(false);
	}

	@Override
	public void close() {
		open = false;
		setKeyboardFocus(false);
		Config.saveFilterText();
	}

	@Override
	public boolean isOpen() {
		return open && Config.isOverlayEnabled();
	}

	@Nullable
	@Override
	public ItemStack getStackUnderMouse() {
		if (hovered != null) {
			Object ingredient = hovered.getIngredient();
			if (ingredient instanceof ItemStack) {
				return (ItemStack) ingredient;
			}
		}
		return null;
	}

	@Override
	public void setFilterText(@Nullable String filterText) {
		if (filterText == null) {
			Log.error("null filterText", new NullPointerException());
			return;
		}

		Config.setFilterText(filterText);

		if (searchField != null) {
			firstItemIndex = 0;
			searchField.setText(filterText);
			updateLayout();
		}
	}

	@Override
	public String getFilterText() {
		return Config.getFilterText();
	}

	@Override
	public ImmutableList<ItemStack> getVisibleStacks() {
		ImmutableList.Builder<ItemStack> visibleStacks = ImmutableList.builder();
		for (GuiIngredientFast guiItemStack : guiIngredientList.getAllGuiIngredients()) {
			Object ingredient = guiItemStack.getIngredient();
			if (ingredient instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) ingredient;
				visibleStacks.add(itemStack);
			}
		}
		return visibleStacks.build();
	}

	@Override
	public ImmutableList<ItemStack> getFilteredStacks() {
		return itemFilter.getItemStacks();
	}

	@Override
	public void highlightStacks(Collection<ItemStack> stacks) {
		highlightedStacks.clear();
		highlightedStacks.addAll(stacks);
	}
}
