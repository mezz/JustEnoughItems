package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.gui.ChatFormatting;
import mezz.jei.Internal;
import mezz.jei.ItemFilter;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.JEIModConfigGui;
import mezz.jei.gui.ingredients.GuiItemStackFast;
import mezz.jei.gui.ingredients.GuiItemStackFastList;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.ICloseable;
import mezz.jei.input.IKeyable;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.util.ItemStackElement;
import mezz.jei.util.Log;
import mezz.jei.util.MathUtil;
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
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.HoverChecker;
import org.lwjgl.input.Keyboard;

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

	@Nonnull
	private final ItemFilter itemFilter;
	@Nonnull
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers;

	private final GuiItemStackFastList guiItemStacks = new GuiItemStackFastList();
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

	private GuiItemStackFast hovered = null;

	// properties of the gui we're beside
	@Nullable
	private GuiProperties guiProperties;
	@Nullable
	private List<Rectangle> guiAreas;
	@Nonnull
	private List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandlers = Collections.emptyList();

	private boolean open = false;

	public ItemListOverlay(@Nonnull ItemFilter itemFilter, @Nonnull List<IAdvancedGuiHandler<?>> advancedGuiHandlers) {
		this.itemFilter = itemFilter;
		this.advancedGuiHandlers = advancedGuiHandlers;
	}

	public void initGui(@Nonnull GuiScreen guiScreen) {
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

		final int columns = getColumns();
		if (columns < 4) {
			close();
			return;
		}

		final int rows = getRows();
		final int xSize = columns * itemStackWidth;
		final int xEmptySpace = guiProperties.getScreenWidth() - guiProperties.getGuiLeft() - guiProperties.getGuiXSize() - xSize;

		final int leftEdge = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + (xEmptySpace / 2);
		final int rightEdge = leftEdge + xSize;

		final int yItemButtonSpace = getItemButtonYSpace();
		final int itemButtonsHeight = rows * itemStackHeight;

		final int buttonStartY = buttonSize + (2 * borderPadding) + (yItemButtonSpace - itemButtonsHeight) / 2;
		createItemButtons(guiItemStacks, guiAreas, leftEdge, buttonStartY, columns, rows);

		nextButton = new GuiButtonExt(0, rightEdge - buttonSize, borderPadding, buttonSize, buttonSize, nextLabel);
		backButton = new GuiButtonExt(1, leftEdge, borderPadding, buttonSize, buttonSize, backLabel);

		int configButtonX = rightEdge - buttonSize + 1;
		int configButtonY = guiProperties.getScreenHeight() - buttonSize - borderPadding;
		configButton = new GuiButtonExt(2, configButtonX, configButtonY, buttonSize, buttonSize, null);
		ResourceLocation configButtonIconLocation = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_GUI_PATH + "recipeBackground.png");
		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		configButtonIcon = guiHelper.createDrawable(configButtonIconLocation, 0, 166, 16, 16);
		configButtonCheatIcon = guiHelper.createDrawable(configButtonIconLocation, 16, 166, 16, 16);
		configButtonHoverChecker = new HoverChecker(configButton, 0);

		int searchFieldY = guiProperties.getScreenHeight() - searchHeight - borderPadding - 2;
		int searchFieldWidth = rightEdge - leftEdge - buttonSize - 1;
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		searchField = new GuiTextFieldFilter(0, fontRenderer, leftEdge, searchFieldY, searchFieldWidth, searchHeight);
		setKeyboardFocus(false);
		searchField.setItemFilter(itemFilter);

		updateLayout();

		open();
	}

	@Nonnull
	private List<IAdvancedGuiHandler<?>> getActiveAdvancedGuiHandlers(@Nonnull GuiScreen guiScreen) {
		List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandler = new ArrayList<>();
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
		List<Rectangle> guiAreas = new ArrayList<>();
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

	public void updateGui(@Nonnull GuiScreen guiScreen) {
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
				if (!Objects.equals(this.guiAreas, guiAreas)) {
					initGui(guiContainer);
				}
			}
		}
	}

	private static void createItemButtons(@Nonnull GuiItemStackFastList guiItemStacks, @Nullable List<Rectangle> guiAreas, final int xStart, final int yStart, final int columnCount, final int rowCount) {
		guiItemStacks.clear();

		for (int row = 0; row < rowCount; row++) {
			int y = yStart + (row * itemStackHeight);
			for (int column = 0; column < columnCount; column++) {
				int x = xStart + (column * itemStackWidth);
				GuiItemStackFast guiItemStackFast = new GuiItemStackFast(x, y, itemStackPadding);
				if (guiAreas != null) {
					Rectangle stackArea = guiItemStackFast.getArea();
					if (intersects(guiAreas, stackArea)) {
						continue;
					}
				}
				guiItemStacks.add(guiItemStackFast);
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
		ImmutableList<ItemStackElement> itemList = itemFilter.getItemList();
		guiItemStacks.set(firstItemIndex, itemList);

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

		firstItemIndex += guiItemStacks.size();
		if (firstItemIndex >= itemsCount) {
			firstItemIndex = 0;
		}
		updateLayout();
	}

	private void previousPage() {
		final int itemsPerPage = guiItemStacks.size();
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
		updateLayout();
	}

	public void drawScreen(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
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
			hovered = guiItemStacks.render(minecraft, false, mouseX, mouseY);
		} else {
			boolean mouseOver = isMouseOver(mouseX, mouseY);
			hovered = guiItemStacks.render(minecraft, mouseOver, mouseX, mouseY);
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

	public void drawTooltips(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
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
						ChatFormatting.RED + Translator.translateToLocal("jei.tooltip.cheat.mode")
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
		if (guiProperties == null || !isOpen() || (mouseX < guiProperties.getGuiLeft() + guiProperties.getGuiXSize())) {
			return false;
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
	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return null;
		}

		Focus focus = guiItemStacks.getFocusUnderMouse(mouseX, mouseY);
		if (focus != null) {
			setKeyboardFocus(false);
			focus.setAllowsCheating();
		}
		return focus;
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
			GuiScreen configScreen = new JEIModConfigGui(minecraft.currentScreen);
			minecraft.displayGuiScreen(configScreen);
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
	public boolean onKeyPressed(int keyCode) {
		if (hasKeyboardFocus()) {
			char character = Keyboard.getEventCharacter();
			boolean changed = searchField.textboxKeyTyped(character, Keyboard.getEventKey());
			if (changed) {
				while (firstItemIndex >= itemFilter.size() && firstItemIndex > 0) {
					previousPage();
				}
				updateLayout();
			}
			return changed || ChatAllowedCharacters.isAllowedCharacter(character);
		}
		return false;
	}

	private int getItemButtonXSpace() {
		if (guiProperties == null) {
			return 0;
		}
		return guiProperties.getScreenWidth() - (guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + (2 * borderPadding));
	}

	private int getItemButtonYSpace() {
		if (guiProperties == null) {
			return 0;
		}
		return guiProperties.getScreenHeight() - (buttonSize + searchHeight + 2 + (4 * borderPadding));
	}

	private int getColumns() {
		return getItemButtonXSpace() / itemStackWidth;
	}

	private int getRows() {
		return getItemButtonYSpace() / itemStackHeight;
	}

	private int getPageCount() {
		final int itemCount = itemFilter.size();
		final int stacksPerPage = guiItemStacks.size();
		if (stacksPerPage == 0) {
			return 1;
		}
		int pageCount = MathUtil.divideCeil(itemCount, stacksPerPage);
		pageCount = Math.max(1, pageCount);
		return pageCount;
	}

	private int getPageNum() {
		final int stacksPerPage = guiItemStacks.size();
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
		if (hovered == null) {
			return null;
		} else {
			return hovered.getItemStack();
		}
	}

	@Override
	public void setFilterText(@Nullable String filterText) {
		if (filterText == null) {
			Log.error("null filterText", new NullPointerException());
			return;
		}
		searchField.setText(filterText);
		Config.setFilterText(filterText);
		updateLayout();
	}

	@Nonnull
	@Override
	public String getFilterText() {
		return itemFilter.getFilterText();
	}
}
