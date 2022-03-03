package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.textures.Textures;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.MouseUtil;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.CheatInputHandler;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import mezz.jei.input.mouse.handlers.ProxyInputHandler;
import mezz.jei.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class BookmarkOverlay implements IRecipeFocusSource, ILeftAreaContent, IBookmarkOverlay {
	private static final int INNER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;

	// areas
	private ImmutableRect2i parentArea = ImmutableRect2i.EMPTY;

	// display elements
	private final IngredientGridWithNavigation contents;
	private final GuiIconToggleButton bookmarkButton;

	// visibility
	private boolean hasRoom = false;

	// data
	private final BookmarkList bookmarkList;
	private final IClientConfig clientConfig;
	private final IWorldConfig worldConfig;

	public BookmarkOverlay(
		BookmarkList bookmarkList,
		Textures textures,
		IngredientGridWithNavigation contents,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		GuiScreenHelper guiScreenHelper
	) {
		this.bookmarkList = bookmarkList;
		this.clientConfig = clientConfig;
		this.worldConfig = worldConfig;
		this.bookmarkButton = BookmarkButton.create(this, bookmarkList, textures, worldConfig);
		this.contents = contents;
		bookmarkList.addListener(() -> {
			worldConfig.setBookmarkEnabled(!bookmarkList.isEmpty());
			Set<ImmutableRect2i> guiExclusionAreas = guiScreenHelper.getGuiExclusionAreas();
			this.hasRoom = updateBounds(guiExclusionAreas);
		});
	}

	public boolean isListDisplayed() {
		return worldConfig.isBookmarkOverlayEnabled() && hasRoom && !bookmarkList.isEmpty();
	}

	public boolean hasRoom() {
		return hasRoom;
	}

	@Override
	public boolean updateBounds(ImmutableRect2i area, Set<ImmutableRect2i> guiExclusionAreas) {
		this.parentArea = area;
		hasRoom = updateBounds(guiExclusionAreas);
		return hasRoom;
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

	public boolean updateBounds(Set<ImmutableRect2i> guiExclusionAreas) {
		ImmutableRect2i availableContentsArea = parentArea.cropBottom(BUTTON_SIZE + INNER_PADDING);
		boolean contentsHasRoom = this.contents.updateBounds(availableContentsArea, guiExclusionAreas);

		ImmutableRect2i contentsArea = this.contents.getBackgroundArea();

		ImmutableRect2i bookmarkButtonArea = parentArea.toMutable()
			.matchWidthAndX(contentsArea)
			.keepBottom(BUTTON_SIZE)
			.keepLeft(BUTTON_SIZE)
			.toImmutable();
		this.bookmarkButton.updateBounds(bookmarkButtonArea);

		if (contentsHasRoom) {
			this.contents.updateLayout(false);
		}
		return contentsHasRoom;
	}

	@Override
	public Optional<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Optional.empty();
	}

	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		return getIngredientUnderMouse(MouseUtil.getX(), MouseUtil.getY())
			.map(IClickedIngredient::getValue);
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(ingredientType)
				.map(ITypedIngredient::getIngredient)
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
