package mezz.jei.common.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.gui.elements.GuiIconToggleButton;
import mezz.jei.common.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IClickedIngredient;
import mezz.jei.common.input.IRecipeFocusSource;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.handlers.CheatInputHandler;
import mezz.jei.common.input.handlers.CombinedInputHandler;
import mezz.jei.common.input.handlers.ProxyInputHandler;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class BookmarkOverlay implements IRecipeFocusSource, ILeftAreaContent, IBookmarkOverlay {
	private static final int INNER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;

	// input
	private final CheatInputHandler cheatInputHandler;

	// areas
	private ImmutableRect2i parentArea = ImmutableRect2i.EMPTY;

	// display elements
	private final IngredientGridWithNavigation contents;
	private final GuiIconToggleButton bookmarkButton;

	// visibility
	private boolean hasRoom = false;

	// data
	private final BookmarkList bookmarkList;
	private final IWorldConfig worldConfig;

	public BookmarkOverlay(
		BookmarkList bookmarkList,
		Textures textures,
		IngredientGridWithNavigation contents,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		GuiScreenHelper guiScreenHelper,
		IConnectionToServer serverConnection,
		IInternalKeyMappings keyBindings
	) {
		this.bookmarkList = bookmarkList;
		this.worldConfig = worldConfig;
		this.bookmarkButton = BookmarkButton.create(this, bookmarkList, textures, worldConfig, keyBindings);
		this.cheatInputHandler = new CheatInputHandler(this, worldConfig, clientConfig, serverConnection);
		this.contents = contents;
		bookmarkList.addSourceListChangedListener(() -> {
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

		ImmutableRect2i bookmarkButtonArea = parentArea
			.matchWidthAndX(contentsArea)
			.keepBottom(BUTTON_SIZE)
			.keepLeft(BUTTON_SIZE);
		this.bookmarkButton.updateBounds(bookmarkButtonArea);

		if (contentsHasRoom) {
			this.contents.updateLayout(false);
		}
		return contentsHasRoom;
	}

	@Override
	public Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		return getIngredientUnderMouse(mouseX, mouseY)
			.<ITypedIngredient<?>>map(IClickedIngredient::getTypedIngredient)
			.findFirst();
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		return getIngredientUnderMouse(mouseX, mouseY)
			.map(IClickedIngredient::getTypedIngredient)
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream)
			.findFirst()
			.orElse(null);
	}

	@Override
	public IUserInputHandler createInputHandler() {
		final IUserInputHandler bookmarkButtonInputHandler = this.bookmarkButton.createInputHandler();

		final IUserInputHandler displayedInputHandler = new CombinedInputHandler(
			this.cheatInputHandler,
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
