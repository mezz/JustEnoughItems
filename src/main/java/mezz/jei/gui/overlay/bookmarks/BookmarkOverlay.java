package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.input.mouse.handlers.CheatInputHandler;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.textures.Textures;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import mezz.jei.input.mouse.handlers.ProxyInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.Set;

public class BookmarkOverlay implements IRecipeFocusSource, ILeftAreaContent, IBookmarkOverlay {
	private static final int BUTTON_SIZE = 20;

	// areas
	private Rect2i parentArea = new Rect2i(0, 0, 0, 0);

	// display elements
	private final IngredientGridWithNavigation contents;
	private final GuiIconToggleButton bookmarkButton;

	// visibility
	private boolean hasRoom = false;

	// data
	private final BookmarkList bookmarkList;
	private final IClientConfig clientConfig;
	private final IWorldConfig worldConfig;

	public BookmarkOverlay(BookmarkList bookmarkList, Textures textures, IngredientGridWithNavigation contents, IClientConfig clientConfig, IWorldConfig worldConfig) {
		this.bookmarkList = bookmarkList;
		this.clientConfig = clientConfig;
		this.worldConfig = worldConfig;
		this.bookmarkButton = BookmarkButton.create(this, bookmarkList, textures, worldConfig);
		this.contents = contents;
		bookmarkList.addListener(() -> {
			worldConfig.setBookmarkEnabled(!bookmarkList.isEmpty());
			contents.updateLayout(false);
		});
	}

	public boolean isListDisplayed() {
		return worldConfig.isBookmarkOverlayEnabled() && hasRoom && !bookmarkList.isEmpty();
	}

	public boolean hasRoom() {
		return hasRoom;
	}

	@Override
	public void updateBounds(Rect2i area, Set<Rect2i> guiExclusionAreas) {
		this.parentArea = area;
		hasRoom = updateBounds(guiExclusionAreas);
	}

	@Override
	public void drawScreen(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (this.isListDisplayed()) {
			this.contents.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
		}
		this.bookmarkButton.draw(poseStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.contents.drawTooltips(minecraft, poseStack, mouseX, mouseY);
		}
		bookmarkButton.drawTooltips(poseStack, mouseX, mouseY);
	}

	private static int getMinWidth(IClientConfig clientConfig) {
		return Math.max(4 * BUTTON_SIZE, clientConfig.getMinColumns() * IngredientGrid.INGREDIENT_WIDTH);
	}

	public boolean updateBounds(Set<Rect2i> guiExclusionAreas) {
		Rect2i displayArea = parentArea;

		final int minWidth = getMinWidth(this.clientConfig);
		if (displayArea.getWidth() < minWidth) {
			return false;
		}

		Rect2i availableContentsArea = new Rect2i(
			displayArea.getX(),
			displayArea.getY(),
			displayArea.getWidth(),
			displayArea.getHeight() - (BUTTON_SIZE + 4)
		);
		boolean contentsHasRoom = this.contents.updateBounds(availableContentsArea, guiExclusionAreas);

		// update area to match contents size
		Rect2i contentsArea = this.contents.getArea();
		displayArea = new Rect2i(
			contentsArea.getX(),
			displayArea.getY(),
			contentsArea.getWidth(),
			displayArea.getHeight()
		);

		this.bookmarkButton.updateBounds(new Rect2i(
			displayArea.getX(),
			displayArea.getY() + displayArea.getHeight() - BUTTON_SIZE - 2,
			BUTTON_SIZE,
			BUTTON_SIZE
		));

		this.contents.updateLayout(false);

		return contentsHasRoom;
	}

	@Override
	public Optional<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Optional.empty();
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(ingredientType)
				.orElse(null);
		}
		return null;
	}

	@Override
	public IUserInputHandler createInputHandler() {
		final IUserInputHandler bookmarkButtonInputHandler = this.bookmarkButton.createInputHandler();

		final IUserInputHandler displayedInputHandler = new CombinedInputHandler(
			new CheatInputHandler(this, worldConfig, clientConfig),
			this.contents.createInputHandler(),
			bookmarkButtonInputHandler
		);

		return new ProxyInputHandler(() -> {
			if (isListDisplayed()) {
				return displayedInputHandler;
			}
			return bookmarkButtonInputHandler;
		});
	}
}
