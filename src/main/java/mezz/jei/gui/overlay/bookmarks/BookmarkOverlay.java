package mezz.jei.gui.overlay.bookmarks;

import javax.annotation.Nullable;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;

import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.textures.Textures;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IShowsRecipeFocuses;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.MathUtil;
import org.lwjgl.glfw.GLFW;

public class BookmarkOverlay implements IShowsRecipeFocuses, ILeftAreaContent, IBookmarkOverlay {
	private static final int BUTTON_SIZE = 20;

	// areas
	private Rectangle2d parentArea = new Rectangle2d(0, 0, 0, 0);
	private Rectangle2d displayArea = new Rectangle2d(0, 0, 0, 0);

	// display elements
	private final IngredientGridWithNavigation contents;
	private final GuiIconToggleButton bookmarkButton;

	// visibility
	private boolean hasRoom = false;

	// data
	private final BookmarkList bookmarkList;
	private final IWorldConfig worldConfig;

	public BookmarkOverlay(BookmarkList bookmarkList, Textures textures, IngredientGridWithNavigation contents, IWorldConfig worldConfig) {
		this.bookmarkList = bookmarkList;
		this.worldConfig = worldConfig;
		this.bookmarkButton = BookmarkButton.create(this, bookmarkList, textures, worldConfig);
		this.contents = contents;
		bookmarkList.addListener(() -> contents.updateLayout(false));
	}

	public boolean isListDisplayed() {
		return worldConfig.isBookmarkOverlayEnabled() && hasRoom && !bookmarkList.isEmpty();
	}

	public boolean hasRoom() {
		return hasRoom;
	}

	@Override
	public void updateBounds(Rectangle2d area, Set<Rectangle2d> guiExclusionAreas) {
		this.parentArea = area;
		hasRoom = updateBounds(guiExclusionAreas);
	}

	@Override
	public void drawScreen(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
		if (this.isListDisplayed()) {
			this.contents.draw(minecraft, mouseX, mouseY, partialTicks);
		}
		this.bookmarkButton.draw(mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawOnForeground(ContainerScreen gui, int mouseX, int mouseY) {
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

	public boolean updateBounds(Set<Rectangle2d> guiExclusionAreas) {
		displayArea = parentArea;

		final int minWidth = getMinWidth();
		if (displayArea.getWidth() < minWidth) {
			return false;
		}

		Rectangle2d availableContentsArea = new Rectangle2d(
			displayArea.getX(),
			displayArea.getY(),
			displayArea.getWidth(),
			displayArea.getHeight() - (BUTTON_SIZE + 4)
		);
		boolean contentsHasRoom = this.contents.updateBounds(availableContentsArea, guiExclusionAreas, minWidth);

		// update area to match contents size
		Rectangle2d contentsArea = this.contents.getArea();
		displayArea = new Rectangle2d(
			contentsArea.getX(),
			displayArea.getY(),
			contentsArea.getWidth(),
			displayArea.getHeight()
		);

		this.bookmarkButton.updateBounds(new Rectangle2d(
			displayArea.getX(),
			(int) Math.floor(displayArea.getY() + displayArea.getHeight()) - BUTTON_SIZE - 2,
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
			MathUtil.contains(displayArea, mouseX, mouseY) &&
			this.contents.handleMouseScrolled(mouseX, mouseY, scrollDelta);
	}

	@Override
	public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (isListDisplayed()) {
			if (MathUtil.contains(displayArea, mouseX, mouseY)) {
				Minecraft minecraft = Minecraft.getInstance();
				Screen currentScreen = minecraft.currentScreen;
				InputMappings.Input input = InputMappings.Type.MOUSE.getOrMakeInput(mouseButton);
				if (currentScreen != null &&
					!(currentScreen instanceof RecipesGui) &&
					(mouseButton == GLFW.GLFW_MOUSE_BUTTON_1 || mouseButton == GLFW.GLFW_MOUSE_BUTTON_2 || minecraft.gameSettings.keyBindPickBlock.isActiveAndMatches(input))) {
					IClickedIngredient<?> clicked = getIngredientUnderMouse(mouseX, mouseY);
					if (clicked != null) {
						if (worldConfig.isCheatItemsEnabled()) {
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
			if (contents.isMouseOver(mouseX, mouseY)) {
				this.contents.handleMouseClicked(mouseX, mouseY, mouseButton);
			}
		}
		if (bookmarkButton.isMouseOver(mouseX, mouseY)) {
			return bookmarkButton.handleMouseClick(mouseX, mouseY, mouseButton);
		}
		return false;
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
