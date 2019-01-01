package mezz.jei.gui.overlay.bookmarks;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;

import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.ClientConfig;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.util.CommandUtil;
import org.lwjgl.glfw.GLFW;

public class BookmarkOverlay implements IShowsRecipeFocuses, ILeftAreaContent {
	private static final int BUTTON_SIZE = 20;

	// areas
	private Rectangle parentArea = new Rectangle();
	private Rectangle displayArea = new Rectangle();

	// display elements
	private final IngredientGridWithNavigation contents;
	private final GuiIconToggleButton bookmarkButton;

	// visibility
	private boolean hasRoom = false;

	// data
	private final BookmarkList bookmarkList;

	public BookmarkOverlay(BookmarkList bookmarkList, GuiHelper guiHelper, IngredientGridWithNavigation contents) {
		this.bookmarkList = bookmarkList;
		this.bookmarkButton = BookmarkButton.create(this, bookmarkList, guiHelper);
		this.contents = contents;
		bookmarkList.addListener(() -> contents.updateLayout(false));
	}

	public boolean isListDisplayed() {
		return ClientConfig.getInstance().isBookmarkOverlayEnabled() && hasRoom && !bookmarkList.isEmpty();
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
		if (this.hasRoom && ClientConfig.getInstance().isBookmarkOverlayEnabled()) {
			this.contents.draw(minecraft, mouseX, mouseY, partialTicks);
		}
		this.bookmarkButton.draw(mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawOnForeground(GuiContainer gui, int mouseX, int mouseY) {
	}

	@Override
	public void drawTooltips(Minecraft minecraft, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.contents.drawTooltips(minecraft, mouseX, mouseY);
		}
		bookmarkButton.drawTooltips(mouseX, mouseY);
	}

	private static int getMinWidth() {
		return Math.max(4 * BUTTON_SIZE, ClientConfig.smallestNumColumns * IngredientGrid.INGREDIENT_WIDTH);
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

		this.contents.updateLayout(false);

		return contentsHasRoom;
	}

	@Override
	@Nullable
	public IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		return null;
	}

	@Override
	public boolean canSetFocusWithMouse() {
		return this.isListDisplayed() && this.contents.canSetFocusWithMouse();
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return isListDisplayed() &&
			displayArea.contains(mouseX, mouseY) &&
			this.contents.handleMouseScrolled(mouseX, mouseY, scrollDelta);
	}

	@Override
	public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (displayArea.contains(mouseX, mouseY)) {
			Minecraft minecraft = Minecraft.getInstance();
			GuiScreen currentScreen = minecraft.currentScreen;
			InputMappings.Input input = InputMappings.Type.MOUSE.getOrMakeInput(mouseButton);
			if (currentScreen != null &&
				!(currentScreen instanceof RecipesGui) &&
				(mouseButton == GLFW.GLFW_MOUSE_BUTTON_1 || mouseButton == GLFW.GLFW_MOUSE_BUTTON_2 || minecraft.gameSettings.keyBindPickBlock.isActiveAndMatches(input))) {
				IClickedIngredient<?> clicked = getIngredientUnderMouse(mouseX, mouseY);
				if (clicked != null) {
					if (ClientConfig.getInstance().isCheatItemsEnabled()) {
						ItemStack itemStack = clicked.getCheatItemStack();
						if (!itemStack.isEmpty()) {
							CommandUtil.giveStack(itemStack, input);
						}
						clicked.onClickHandled();
						return true;
					}
				}
			}
		}
		if (bookmarkButton.isMouseOver(mouseX, mouseY)) {
			return bookmarkButton.handleMouseClick(mouseX, mouseY, mouseButton);
		}
		return this.contents.handleMouseClicked(mouseX, mouseY, mouseButton);
	}

}
