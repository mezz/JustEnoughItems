package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IClickedIngredient;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.common.gui.overlay.ScreenPropertiesCache;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IRecipeFocusSource;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.MouseUtil;
import mezz.jei.gui.input.handlers.CheatInputHandler;
import mezz.jei.common.input.handlers.CombinedInputHandler;
import mezz.jei.common.input.handlers.ProxyInputHandler;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BookmarkOverlay implements IRecipeFocusSource, IBookmarkOverlay {
	private static final int BORDER_MARGIN = 6;
	private static final int INNER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;

	// input
	private final CheatInputHandler cheatInputHandler;

	// areas
	private final ScreenPropertiesCache screenPropertiesCache;
	private final IScreenHelper screenHelper;

	// display elements
	private final IngredientGridWithNavigation contents;
	private final GuiIconToggleButton bookmarkButton;

	// data
	private final BookmarkList bookmarkList;
	private final IWorldConfig worldConfig;

	public BookmarkOverlay(
		BookmarkList bookmarkList,
		Textures textures,
		IngredientGridWithNavigation contents,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		IScreenHelper screenHelper,
		IConnectionToServer serverConnection,
		IInternalKeyMappings keyBindings
	) {
		this.bookmarkList = bookmarkList;
		this.worldConfig = worldConfig;
		this.bookmarkButton = BookmarkButton.create(this, bookmarkList, textures, worldConfig, keyBindings);
		this.cheatInputHandler = new CheatInputHandler(this, worldConfig, clientConfig, serverConnection);
		this.contents = contents;
		this.screenHelper = screenHelper;
		this.screenPropertiesCache = new ScreenPropertiesCache(screenHelper);
		bookmarkList.addSourceListChangedListener(() -> {
			worldConfig.setBookmarkEnabled(!bookmarkList.isEmpty());
			Minecraft minecraft = Minecraft.getInstance();
			Screen screen = minecraft.screen;
			this.updateScreen(screen, true);
		});
	}

	public boolean isListDisplayed() {
		return worldConfig.isBookmarkOverlayEnabled() &&
			screenPropertiesCache.hasValidScreen() &&
			contents.hasRoom() &&
			!bookmarkList.isEmpty();
	}

	public boolean hasRoom() {
		return contents.hasRoom();
	}

	public void updateScreen(@Nullable Screen guiScreen, boolean forceUpdate) {
		this.screenPropertiesCache.updateScreen(guiScreen, forceUpdate, optionalGuiProperties -> {
			optionalGuiProperties
				.ifPresent(this::updateBounds);
		});
	}

	private void updateBounds(IGuiProperties guiProperties) {
		ImmutableRect2i displayArea =  new ImmutableRect2i(0, 0, guiProperties.getGuiLeft(), guiProperties.getScreenHeight());

		Set<ImmutableRect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas().stream()
			.map(ImmutableRect2i::convert)
			.collect(Collectors.toUnmodifiableSet());

		ImmutableRect2i availableContentsArea = displayArea.cropBottom(BUTTON_SIZE + INNER_PADDING);
		this.contents.updateBounds(availableContentsArea, guiExclusionAreas);
		this.contents.updateLayout(false);

		if (contents.hasRoom()) {
			ImmutableRect2i contentsArea = this.contents.getBackgroundArea();
			ImmutableRect2i bookmarkButtonArea = displayArea
				.insetBy(BORDER_MARGIN)
				.matchWidthAndX(contentsArea)
				.keepBottom(BUTTON_SIZE)
				.keepLeft(BUTTON_SIZE);
			this.bookmarkButton.updateBounds(bookmarkButtonArea);
		} else {
			ImmutableRect2i bookmarkButtonArea = displayArea
				.insetBy(BORDER_MARGIN)
				.keepBottom(BUTTON_SIZE)
				.keepLeft(BUTTON_SIZE);
			this.bookmarkButton.updateBounds(bookmarkButtonArea);
		}
	}

	public void drawScreen(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (isListDisplayed()) {
			this.contents.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
		}
		if (this.screenPropertiesCache.hasValidScreen()) {
			this.bookmarkButton.draw(poseStack, mouseX, mouseY, partialTicks);
		}
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.contents.drawTooltips(minecraft, poseStack, mouseX, mouseY);
		}
		if (this.screenPropertiesCache.hasValidScreen()) {
			bookmarkButton.drawTooltips(poseStack, mouseX, mouseY);
		}
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
