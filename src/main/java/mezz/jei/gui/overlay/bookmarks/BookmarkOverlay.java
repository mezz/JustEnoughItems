package mezz.jei.gui.overlay.bookmarks;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Set;

import mezz.jei.api.IBookmarkOverlay;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.Config;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.gui.overlay.GridAlignment;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.util.CommandUtil;

public class BookmarkOverlay implements IShowsRecipeFocuses, ILeftAreaContent, IBookmarkOverlay {
	private static final int BUTTON_SIZE = 20;

	// areas
	private Rectangle parentArea = new Rectangle();
	private Rectangle displayArea = new Rectangle();

	// display elements
	private final IngredientGridWithNavigation contents;
	private final GuiIconToggleButton bookmarkButton;
	private final GhostIngredientDragManager ghostIngredientDragManager;

	// visibility
	private boolean hasRoom = false;

	// data
	private final BookmarkList bookmarkList;

	public BookmarkOverlay(BookmarkList bookmarkList, GuiHelper guiHelper, GuiScreenHelper guiScreenHelper, IngredientRegistry ingredientRegistry) {
		this.bookmarkList = bookmarkList;
		this.bookmarkButton = BookmarkButton.create(this, bookmarkList, guiHelper);
		this.contents = new IngredientGridWithNavigation(bookmarkList, guiScreenHelper, GridAlignment.RIGHT);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.contents, guiScreenHelper, ingredientRegistry);
		bookmarkList.addListener(() -> contents.updateLayoutKeepingPageAnchorVisible());
	}

	public boolean isListDisplayed() {
		return Config.isBookmarkOverlayEnabled() && hasRoom && !bookmarkList.isEmpty();
	}

	public boolean hasRoom() {
		return hasRoom;
	}

	@Override
	public void updateBounds(Rectangle area, Set<Rectangle> guiExclusionAreas) {
		this.parentArea = area;
		hasRoom = updateBounds(guiExclusionAreas);
	}

	@Override
	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		if (this.isListDisplayed()) {
			this.contents.draw(minecraft, mouseX, mouseY, partialTicks);
		} else {
			this.ghostIngredientDragManager.stopDrag();
		}
		this.bookmarkButton.draw(minecraft, mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawOnForeground(GuiContainer gui, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(-gui.getGuiLeft(), -gui.getGuiTop(), 0);
			this.ghostIngredientDragManager.drawOnForeground(gui.mc, mouseX, mouseY);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.ghostIngredientDragManager.drawTooltips(minecraft, mouseX, mouseY);
			this.contents.drawTooltips(minecraft, mouseX, mouseY);
		}
		bookmarkButton.drawTooltips(minecraft, mouseX, mouseY);
	}

	private static int getMinWidth() {
		return Math.max(4 * BUTTON_SIZE, Config.smallestNumColumns * IngredientGrid.INGREDIENT_WIDTH);
	}

	public boolean updateBounds(Set<Rectangle> guiExclusionAreas) {
		displayArea = new Rectangle(parentArea);

		final int minWidth = getMinWidth();
		if (displayArea.width < minWidth) {
			return false;
		}

		Rectangle availableContentsArea = new Rectangle(
			displayArea.x,
			displayArea.y,
			displayArea.width,
			displayArea.height - (BUTTON_SIZE + 4)
		);
		boolean contentsHasRoom = this.contents.updateBounds(availableContentsArea, guiExclusionAreas, minWidth);

		// update area to match contents size
		Rectangle contentsArea = this.contents.getArea();
		displayArea.x = contentsArea.x;
		displayArea.width = contentsArea.width;

		this.bookmarkButton.updateBounds(new Rectangle(
			displayArea.x,
			(int) Math.floor(displayArea.getMaxY()) - BUTTON_SIZE - 2,
			BUTTON_SIZE,
			BUTTON_SIZE
		));

		this.contents.updateLayoutKeepingPageAnchorVisible();

		return contentsHasRoom;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		if (isListDisplayed()) {
			IClickedIngredient<?> clicked = this.contents.getIngredientUnderMouse(mouseX, mouseY);
			if (clicked != null) {
				IIngredientListElement pageAnchorElement = this.contents.getElementUnderMouse(mouseX, mouseY);
				if (pageAnchorElement != null) {
					clicked.setOnClickHandler(() -> this.contents.setPageAnchorElement(pageAnchorElement));
				}
				return clicked;
			}
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return this.isListDisplayed() && this.contents.canSetFocusWithMouse();
	}

	@Override
	public boolean handleMouseScrolled(int mouseX, int mouseY, int scrollDelta) {
		return isListDisplayed() &&
			displayArea.contains(mouseX, mouseY) &&
			this.contents.handleMouseScrolled(mouseX, mouseY, scrollDelta);
	}

	@Override
	public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (isListDisplayed()) {
			if (this.ghostIngredientDragManager.handleMouseClicked(mouseX, mouseY)) {
				return true;
			}
			if (displayArea.contains(mouseX, mouseY)) {
				Minecraft minecraft = Minecraft.getMinecraft();
				GuiScreen currentScreen = minecraft.currentScreen;
				if (currentScreen != null && !(currentScreen instanceof RecipesGui)
					&& (mouseButton == 0 || mouseButton == 1 || minecraft.gameSettings.keyBindPickBlock.isActiveAndMatches(mouseButton - 100))) {
					IClickedIngredient<?> clicked = getIngredientUnderMouse(mouseX, mouseY);
					if (clicked != null) {
						if (Config.isCheatItemsEnabled()) {
							ItemStack itemStack = clicked.getCheatItemStack();
							if (!itemStack.isEmpty()) {
								CommandUtil.giveStack(itemStack, mouseButton);
							}
							clicked.onClickHandled();
							return true;
						}
						EntityPlayerSP player = minecraft.player;
						if (player != null && player.inventory.getItemStack().isEmpty()) {
							if (mouseButton == 0 && GuiScreen.isShiftKeyDown() && this.ghostIngredientDragManager.handleQuickMoveGhostIngredient(currentScreen, clicked)) {
								return true;
							}
							if (this.ghostIngredientDragManager.handleClickGhostIngredient(currentScreen, clicked)) {
								return true;
							}
						}
					}
				}
			}
			if (contents.isMouseOver(mouseX, mouseY)) {
				this.contents.handleMouseClicked(mouseX, mouseY, mouseButton);
			}
		}
		if (bookmarkButton.isMouseOver(mouseX, mouseY)) {
			return bookmarkButton.handleMouseClick(mouseX, mouseY);
		}
		return false;
	}

	@Override
	public void onHidden() {
		this.ghostIngredientDragManager.stopDrag();
	}

	@Nullable
	@Override
	public Object getIngredientUnderMouse() {
		if (isListDisplayed()) {
			IIngredientListElement elementUnderMouse = this.contents.getElementUnderMouse();
			if (elementUnderMouse != null) {
				return elementUnderMouse.getIngredient();
			}
		}
		return null;
	}
}
