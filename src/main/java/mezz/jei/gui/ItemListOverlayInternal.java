package mezz.jei.gui;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.JeiRuntime;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.config.SessionData;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IMouseHandler;
import mezz.jei.input.IPaged;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketJei;
import mezz.jei.util.GuiAreaHelper;
import mezz.jei.util.Java6Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

public class ItemListOverlayInternal implements IShowsRecipeFocuses, IMouseHandler, IPaged {
	private static final int borderPadding = 2;
	private static final int searchHeight = 16;
	private static final int buttonSize = 20;

	/**
	 * @return true if this can be displayed next to the gui with the given guiProperties
	 */
	public static boolean isOverlayEnabled(GuiProperties guiProperties) {
		return guiProperties.getScreenWidth() - (guiProperties.getGuiLeft() + guiProperties.getGuiXSize()) >= 72;
	}

	private static int getItemListYSpace(GuiProperties guiProperties) {
		final int headerSize = buttonSize + (2 * borderPadding);
		if (isSearchBarCentered(guiProperties)) {
			return guiProperties.getScreenHeight() - headerSize;
		}
		final int footerSize = searchHeight + (3 * borderPadding);
		return guiProperties.getScreenHeight() - (headerSize + footerSize);
	}

	private static boolean isSearchBarCentered(GuiProperties guiProperties) {
		return Config.isCenterSearchBarEnabled() &&
				guiProperties.getGuiTop() + guiProperties.getGuiYSize() + searchHeight < guiProperties.getScreenHeight();
	}

	private final ItemListOverlay parent;

	private final PageNavigation pageNavigation;
	private final IngredientGridAll ingredientGridAll;
	private final ConfigButton configButton;
	private final GuiTextFieldFilter searchField;

	// properties of the gui we're beside
	private final GuiProperties guiProperties;
	private final List<Rectangle> guiAreas;
	private List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandlers = Collections.emptyList();

	public ItemListOverlayInternal(ItemListOverlay parent, IIngredientRegistry ingredientRegistry, GuiScreen guiScreen, GuiProperties guiProperties) {
		this.parent = parent;

		this.guiProperties = guiProperties;
		this.activeAdvancedGuiHandlers = GuiAreaHelper.getActiveAdvancedGuiHandlers(parent.getAdvancedGuiHandlers(), guiScreen);
		if (!activeAdvancedGuiHandlers.isEmpty() && guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			guiAreas = GuiAreaHelper.getGuiAreas(activeAdvancedGuiHandlers, guiContainer);
		} else {
			guiAreas = Collections.emptyList();
		}

		final int itemListSpace = getItemListYSpace(guiProperties);
		final int itemListY = buttonSize + (2 * borderPadding);

		int x = guiProperties.getGuiLeft() + guiProperties.getGuiXSize() + borderPadding;
		int width = guiProperties.getScreenWidth() - x - borderPadding;

		Rectangle itemListDisplayArea = new Rectangle(x, itemListY, width, itemListSpace);
		this.ingredientGridAll = new IngredientGridAll(ingredientRegistry, itemListDisplayArea, guiAreas, parent.getItemFilter());

		// updated area
		itemListDisplayArea = this.ingredientGridAll.getArea();
		x = itemListDisplayArea.x;
		width = itemListDisplayArea.width;

		final Rectangle pageNavigationArea = new Rectangle(x, borderPadding, width, buttonSize);
		this.pageNavigation = new PageNavigation(this, pageNavigationArea);

		final int searchFieldX;
		final int searchFieldY = guiProperties.getScreenHeight() - searchHeight - borderPadding - 2;
		final int searchFieldWidth;

		if (isSearchBarCentered(guiProperties)) {
			searchFieldX = guiProperties.getGuiLeft();
			searchFieldWidth = guiProperties.getGuiXSize() - buttonSize - 1;
		} else {
			searchFieldX = x;
			searchFieldWidth = width - buttonSize - 1;
		}

		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
		this.searchField = new GuiTextFieldFilter(0, fontRenderer, searchFieldX, searchFieldY, searchFieldWidth, searchHeight, parent.getItemFilter());
		setKeyboardFocus(false);

		int configButtonX = searchFieldX + searchFieldWidth + 1;
		int configButtonY = guiProperties.getScreenHeight() - buttonSize - borderPadding;
		this.configButton = new ConfigButton(parent, configButtonX, configButtonY, buttonSize);

		updateLayout();
	}

	public boolean hasScreenChanged(GuiScreen guiScreen) {
		if (!Config.isOverlayEnabled()) {
			return true;
		}
		GuiProperties guiProperties = GuiProperties.create(guiScreen);
		if (guiProperties == null) {
			return true;
		}
		if (!this.guiProperties.equals(guiProperties)) {
			return true;
		} else if (!activeAdvancedGuiHandlers.isEmpty() && guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			List<Rectangle> guiAreas = GuiAreaHelper.getGuiAreas(activeAdvancedGuiHandlers, guiContainer);
			if (!Java6Helper.equals(this.guiAreas, guiAreas)) {
				return true;
			}
		}

		return false;
	}

	public void updateLayout() {
		ingredientGridAll.updateLayout();

		int pageNum = ingredientGridAll.getPageNum();
		int pageCount = ingredientGridAll.getPageCount();
		pageNavigation.updateLayout(pageNum, pageCount);

		searchField.update();
	}

	@Override
	public boolean nextPage() {
		if (ingredientGridAll.nextPage()) {
			updateLayout();
			return true;
		}
		return false;
	}

	@Override
	public boolean previousPage() {
		if (ingredientGridAll.previousPage()) {
			updateLayout();
			return true;
		}
		return false;
	}

	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY) {
		GlStateManager.disableLighting();

		pageNavigation.draw(minecraft, mouseX, mouseY);
		searchField.drawTextBox();
		configButton.draw(minecraft, mouseX, mouseY);
		Set<ItemStack> highlightedStacks = parent.getHighlightedStacks();
		ingredientGridAll.draw(minecraft, mouseX, mouseY, highlightedStacks);
	}

	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		ingredientGridAll.drawTooltips(minecraft, mouseX, mouseY);
		configButton.drawTooltips(minecraft, mouseX, mouseY);
	}

	public void handleTick() {
		searchField.updateCursorCounter();
	}

	@Override
	public boolean isMouseOver(int mouseX, int mouseY) {
		if (mouseX >= guiProperties.getGuiLeft() + guiProperties.getGuiXSize()) {
			for (Rectangle guiArea : guiAreas) {
				if (guiArea.contains(mouseX, mouseY)) {
					return false;
				}
			}
			return true;
		} else if (isSearchBarCentered(guiProperties)) {
			return (searchField.isMouseOver(mouseX, mouseY) || configButton.isMouseOver(mouseX, mouseY));
		}
		return false;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		IClickedIngredient<?> clicked = ingredientGridAll.getIngredientUnderMouse(mouseX, mouseY);
		if (clicked != null) {
			setKeyboardFocus(false);
		}
		return clicked;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return ingredientGridAll.canSetFocusWithMouse();
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isMouseOver(mouseX, mouseY)) {
			setKeyboardFocus(false);
			return false;
		}

		JeiRuntime runtime = Internal.getRuntime();
		if (Config.isDeleteItemsInCheatModeActive() && (runtime == null || !runtime.getRecipesGui().isOpen())) {
			Minecraft minecraft = Minecraft.getMinecraft();
			EntityPlayerSP player = minecraft.player;
			ItemStack itemStack = player.inventory.getItemStack();
			if (!itemStack.isEmpty()) {
				player.inventory.setItemStack(ItemStack.EMPTY);
				PacketJei packet = new PacketDeletePlayerItem(itemStack);
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
		if (pageNavigation.handleMouseClickedButtons(minecraft, mouseX, mouseY)) {
			return true;
		} else if (configButton.handleMouseClick(minecraft, mouseX, mouseY)) {
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

	public boolean hasKeyboardFocus() {
		return searchField.isFocused();
	}

	public void setKeyboardFocus(boolean keyboardFocus) {
		searchField.setFocused(keyboardFocus);
	}

	public boolean onKeyPressed(char typedChar, int keyCode) {
		if (hasKeyboardFocus()) {
			boolean handled = searchField.textboxKeyTyped(typedChar, keyCode);
			if (handled) {
				boolean changed = Config.setFilterText(searchField.getText());
				if (changed) {
					SessionData.setFirstItemIndex(0);
					updateLayout();
				}
			}
			return handled;
		}
		return false;
	}

	public void close() {
		setKeyboardFocus(false);
	}

	@Nullable
	public ItemStack getStackUnderMouse() {
		return ingredientGridAll.getStackUnderMouse();
	}

	public void setFilterText(String filterText) {
		searchField.setText(filterText);
		SessionData.setFirstItemIndex(0);
		updateLayout();
	}

	public ImmutableList<ItemStack> getVisibleStacks() {
		return ingredientGridAll.getVisibleStacks();
	}

}
