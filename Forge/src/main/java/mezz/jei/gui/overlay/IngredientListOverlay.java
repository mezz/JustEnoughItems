package mezz.jei.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.common.network.IServerConnection;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.events.RuntimeEventSubscriptions;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.ghost.GhostIngredientDragManager;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.MouseUtil;
import mezz.jei.input.mouse.ICharTypedHandler;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.CheatInputHandler;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import mezz.jei.input.mouse.handlers.NullInputHandler;
import mezz.jei.input.mouse.handlers.ProxyInputHandler;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class IngredientListOverlay implements IIngredientListOverlay, IRecipeFocusSource, ICharTypedHandler {
	private static final int BORDER_MARGIN = 6;
	private static final int INNER_PADDING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int SEARCH_HEIGHT = BUTTON_SIZE;

	private final GuiIconToggleButton configButton;
	private final IngredientGridWithNavigation contents;
	private final IClientConfig clientConfig;
	private final IWorldConfig worldConfig;
	private final IServerConnection serverConnection;
	private final GuiScreenHelper guiScreenHelper;
	private final GuiTextFieldFilter searchField;
	private final GhostIngredientDragManager ghostIngredientDragManager;
	private ImmutableRect2i displayArea = ImmutableRect2i.EMPTY;
	private boolean hasRoom;

	// properties of the gui we're next to
	@Nullable
	private IGuiProperties guiProperties;

	public IngredientListOverlay(
		IIngredientGridSource ingredientGridSource,
		IFilterTextSource filterTextSource,
		RegisteredIngredients registeredIngredients,
		GuiScreenHelper guiScreenHelper,
		IngredientGridWithNavigation contents,
		IClientConfig clientConfig,
		IWorldConfig worldConfig,
		IServerConnection serverConnection
	) {
		this.guiScreenHelper = guiScreenHelper;
		this.contents = contents;
		this.clientConfig = clientConfig;
		this.worldConfig = worldConfig;
		this.serverConnection = serverConnection;

		this.searchField = new GuiTextFieldFilter();
		this.searchField.setValue(filterTextSource.getFilterText());
		this.searchField.setFocused(false);
		this.searchField.setResponder(filterTextSource::setFilterText);

		ingredientGridSource.addSourceListChangedListener(() -> updateBounds(true));

		this.configButton = ConfigButton.create(this, worldConfig);
		this.ghostIngredientDragManager = new GhostIngredientDragManager(this.contents, guiScreenHelper, registeredIngredients, worldConfig);
	}

	public boolean isListDisplayed() {
		return worldConfig.isOverlayEnabled() && this.guiProperties != null && this.hasRoom;
	}

	private static ImmutableRect2i createDisplayArea(IGuiProperties guiProperties) {
		ImmutableRect2i screenRectangle = GuiProperties.getScreenRectangle(guiProperties);
		int guiRight = GuiProperties.getGuiRight(guiProperties);
		return screenRectangle
			.cropLeft(guiRight)
			.insetBy(BORDER_MARGIN);
	}

	public void updateScreen(@Nullable Screen guiScreen, boolean exclusionAreasChanged) {
		IGuiProperties guiProperties = guiScreenHelper.getGuiProperties(guiScreen);
		if (guiProperties == null) {
			if (this.guiProperties != null) {
				this.guiProperties = null;
				this.searchField.setFocused(false);
				this.ghostIngredientDragManager.stopDrag();
			}
		} else {
			final boolean guiPropertiesChanged = this.guiProperties == null || !GuiProperties.areEqual(this.guiProperties, guiProperties);
			if (exclusionAreasChanged || guiPropertiesChanged) {
				updateNewScreen(guiProperties, guiPropertiesChanged);
			}
		}
	}

	private void updateNewScreen(IGuiProperties guiProperties, boolean guiPropertiesChanged) {
		this.guiProperties = guiProperties;
		this.displayArea = createDisplayArea(guiProperties);
		if (guiPropertiesChanged) {
			this.ghostIngredientDragManager.stopDrag();
		}
		updateBounds(false);
	}

	private void updateBounds(boolean resetToFirstPage) {
		if (this.guiProperties == null) {
			return;
		}
		final boolean searchBarCentered = isSearchBarCentered(this.clientConfig, this.guiProperties);

		final ImmutableRect2i availableContentsArea = getAvailableContentsArea(searchBarCentered);
		final Set<ImmutableRect2i> guiExclusionAreas = guiScreenHelper.getGuiExclusionAreas();
		this.hasRoom = this.contents.updateBounds(availableContentsArea, guiExclusionAreas);

		final ImmutableRect2i searchAndConfigArea = getSearchAndConfigArea(searchBarCentered, guiProperties);
		final ImmutableRect2i searchArea = searchAndConfigArea.cropRight(BUTTON_SIZE);
		final ImmutableRect2i configButtonArea = searchAndConfigArea.keepRight(BUTTON_SIZE);

		int searchTextColor = this.contents.isEmpty() ? 0xFFFF0000 : 0xFFFFFFFF;
		this.searchField.setTextColor(searchTextColor);
		this.searchField.updateBounds(searchArea);

		this.configButton.updateBounds(configButtonArea);

		if (this.hasRoom) {
			this.contents.updateLayout(resetToFirstPage);
		}
	}

	private static boolean isSearchBarCentered(IClientConfig clientConfig, IGuiProperties guiProperties) {
		return clientConfig.isCenterSearchBarEnabled() &&
			GuiProperties.getGuiBottom(guiProperties) + SEARCH_HEIGHT < guiProperties.getScreenHeight();
	}

	private ImmutableRect2i getAvailableContentsArea(boolean searchBarCentered) {
		if (searchBarCentered) {
			return this.displayArea;
		}
		return this.displayArea.cropBottom(SEARCH_HEIGHT + INNER_PADDING);
	}

	private ImmutableRect2i getSearchAndConfigArea(boolean searchBarCentered, IGuiProperties guiProperties) {
		if (searchBarCentered) {
			ImmutableRect2i guiRectangle = GuiProperties.getGuiRectangle(guiProperties);
			return this.displayArea
				.keepBottom(SEARCH_HEIGHT)
				.matchWidthAndX(guiRectangle);
		} else if (this.hasRoom) {
			final ImmutableRect2i contentsArea = this.contents.getBackgroundArea();
			return this.displayArea
				.keepBottom(SEARCH_HEIGHT)
				.matchWidthAndX(contentsArea);
		} else {
			return this.displayArea.keepBottom(SEARCH_HEIGHT);
		}
	}

	public void drawScreen(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (isListDisplayed()) {
			this.searchField.renderButton(poseStack, mouseX, mouseY, partialTicks);
			this.contents.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
		}
		if (this.guiProperties != null) {
			this.configButton.draw(poseStack, mouseX, mouseY, partialTicks);
		}
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			this.ghostIngredientDragManager.drawTooltips(minecraft, poseStack, mouseX, mouseY);
			this.contents.drawTooltips(minecraft, poseStack, mouseX, mouseY);
		}
		if (this.guiProperties != null) {
			this.configButton.drawTooltips(poseStack, mouseX, mouseY);
		}
	}

	public void drawOnForeground(Minecraft minecraft, PoseStack poseStack, AbstractContainerScreen<?> gui, int mouseX, int mouseY) {
		if (isListDisplayed()) {
			poseStack.pushPose();
			{
				poseStack.translate(-gui.getGuiLeft(), -gui.getGuiTop(), 0);
				this.ghostIngredientDragManager.drawOnForeground(minecraft, poseStack, mouseX, mouseY);
			}
			poseStack.popPose();
		}
	}

	public void handleTick() {
		if (this.isListDisplayed()) {
			this.searchField.tick();
		}
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			if (this.clientConfig.isCenterSearchBarEnabled() && searchField.isMouseOver(mouseX, mouseY)) {
				return true;
			}
			return displayArea.contains(mouseX, mouseY) &&
				!guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY);
		}
		if (this.guiProperties != null) {
			return this.configButton.isMouseOver(mouseX, mouseY);
		}
		return false;
	}

	@Override
	public Stream<IClickedIngredient<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		if (isListDisplayed()) {
			return this.contents.getIngredientUnderMouse(mouseX, mouseY);
		}
		return Stream.empty();
	}

	public IUserInputHandler createInputHandler() {
		final IUserInputHandler displayedInputHandler = new CombinedInputHandler(
			this.ghostIngredientDragManager.createInputHandler(),
			this.searchField.createInputHandler(),
			this.configButton.createInputHandler(),
			this.contents.createInputHandler(),
			new CheatInputHandler(this.contents, worldConfig, clientConfig, serverConnection)
		);

		final IUserInputHandler hiddenInputHandler = this.configButton.createInputHandler();

		return new ProxyInputHandler(() -> {
			if (this.guiProperties == null) {
				return NullInputHandler.INSTANCE;
			}
			if (isListDisplayed()) {
				return displayedInputHandler;
			}
			return hiddenInputHandler;
		});
	}

	@Override
	public boolean hasKeyboardFocus() {
		return isListDisplayed() && this.searchField.isFocused();
	}

	@Override
	public boolean onCharTyped(char codePoint, int modifiers) {
		return searchField.charTyped(codePoint, modifiers);
	}

	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		if (isListDisplayed()) {
			double mouseX = MouseUtil.getX();
			double mouseY = MouseUtil.getY();
			return this.contents.getIngredientUnderMouse(mouseX, mouseY)
				.<ITypedIngredient<?>>map(IClickedIngredient::getTypedIngredient)
				.findFirst();
		}
		return Optional.empty();
	}

	@Nullable
	@Override
	public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		if (isListDisplayed()) {
			double mouseX = MouseUtil.getX();
			double mouseY = MouseUtil.getY();
			return this.contents.getIngredientUnderMouse(mouseX, mouseY)
				.map(IClickedIngredient::getTypedIngredient)
				.map(i -> i.getIngredient(ingredientType))
				.flatMap(Optional::stream)
				.findFirst()
				.orElse(null);
		}
		return null;
	}

	@Override
	public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		if (isListDisplayed()) {
			return this.contents.getVisibleIngredients(ingredientType)
				.toList();
		}
		return Collections.emptyList();
	}

	public void register(RuntimeEventSubscriptions subscriptions) {
		subscriptions.register(ScreenEvent.PotionSizeEvent.class, event -> {
			if (isListDisplayed()) {
				// Forcibly renders the potion indicators in compact mode.
				// This gives the ingredient list overlay more room to display ingredients.
				event.setResult(Event.Result.ALLOW);
			}
		});
	}
}
