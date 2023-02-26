package mezz.jei.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IClickableIngredientInternal;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.gui.PageNavigation;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.gui.input.IPaged;
import mezz.jei.gui.input.IRecipeFocusSource;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.CombinedInputHandler;
import mezz.jei.gui.input.handlers.LimitedAreaInputHandler;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.util.CheatUtil;
import mezz.jei.gui.util.CommandUtil;
import mezz.jei.gui.util.MaximalRectangle;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Displays a list of ingredients with navigation at the top.
 */
public class IngredientGridWithNavigation implements IRecipeFocusSource {
	private static final int NAVIGATION_HEIGHT = 20;
	private static final int BORDER_MARGIN = 6;
	private static final int BORDER_PADDING = 5;
	private static final int INNER_PADDING = 2;

	private int firstItemIndex = 0;
	private final IngredientGridPaged pageDelegate;
	private final PageNavigation navigation;
	private final IIngredientGridConfig gridConfig;
	private final CheatUtil cheatUtil;
	private final IClientToggleState toggleState;
	private final IClientConfig clientConfig;
	private final IngredientGrid ingredientGrid;
	private final IIngredientGridSource ingredientSource;
	private final DrawableNineSliceTexture background;
	private final DrawableNineSliceTexture slotBackground;
	private final CommandUtil commandUtil;

	private ImmutableRect2i backgroundArea = ImmutableRect2i.EMPTY;
	private ImmutableRect2i slotBackgroundArea = ImmutableRect2i.EMPTY;
	private Set<ImmutableRect2i> guiExclusionAreas = Set.of();

	public IngredientGridWithNavigation(
		IIngredientGridSource ingredientSource,
		IngredientGrid ingredientGrid,
		IClientToggleState toggleState,
		IClientConfig clientConfig,
		IConnectionToServer serverConnection,
		IIngredientGridConfig gridConfig,
		DrawableNineSliceTexture background,
		DrawableNineSliceTexture slotBackground,
		Textures textures,
		CheatUtil cheatUtil
	) {
		this.toggleState = toggleState;
		this.clientConfig = clientConfig;
		this.ingredientGrid = ingredientGrid;
		this.ingredientSource = ingredientSource;
		this.gridConfig = gridConfig;
		this.cheatUtil = cheatUtil;
		this.pageDelegate = new IngredientGridPaged();
		this.navigation = new PageNavigation(this.pageDelegate, false, textures);
		this.background = background;
		this.slotBackground = slotBackground;
		this.commandUtil = new CommandUtil(clientConfig, serverConnection);

		this.ingredientSource.addSourceListChangedListener(() -> updateLayout(true));
	}

	public boolean hasRoom() {
		return this.ingredientGrid.hasRoom();
	}

	public void updateLayout(boolean resetToFirstPage) {
		if (resetToFirstPage) {
			firstItemIndex = 0;
		}
		List<ITypedIngredient<?>> ingredientList = ingredientSource.getIngredientList();
		if (firstItemIndex >= ingredientList.size()) {
			firstItemIndex = 0;
		}
		this.ingredientGrid.set(firstItemIndex, ingredientList);
		this.navigation.updatePageNumber();
	}

	private static ImmutableRect2i avoidExclusionAreas(
		ImmutableRect2i availableArea,
		ImmutableRect2i estimatedNavigationArea,
		Set<ImmutableRect2i> guiExclusionAreas,
		IIngredientGridConfig gridConfig
	) {
		final int maxDimension = Math.max(availableArea.getWidth(), availableArea.getHeight());
		final int samplingScale = Math.max(IngredientGrid.INGREDIENT_HEIGHT / 2, maxDimension / 25);

		ImmutableRect2i largestSafeArea = MaximalRectangle.getLargestRectangles(
			availableArea,
			guiExclusionAreas,
			samplingScale
		)
			.max(Comparator.comparingInt((ImmutableRect2i rect) -> IngredientGrid.calculateSize(gridConfig, rect).getArea())
				.thenComparing(r -> r.getWidth() * r.getHeight()))
			.orElse(ImmutableRect2i.EMPTY);

		final boolean intersectsNavigationArea = guiExclusionAreas.stream()
			.anyMatch(estimatedNavigationArea::intersects);
		if (intersectsNavigationArea) {
			return largestSafeArea;
		}

		IngredientGrid.SlotInfo slotInfo = IngredientGrid.calculateBlockedSlotPercentage(gridConfig, availableArea, guiExclusionAreas);
		IngredientGrid.SlotInfo safeSlotInfo = IngredientGrid.calculateBlockedSlotPercentage(gridConfig, largestSafeArea, guiExclusionAreas);
		if (slotInfo.percentBlocked() > 0.25 || safeSlotInfo.total() > slotInfo.total()) {
			return largestSafeArea;
		} else {
			return availableArea;
		}
	}

	private void updateGridBounds(final ImmutableRect2i availableArea, boolean navigationEnabled) {
		ImmutableRect2i availableGridArea = availableArea.insetBy(BORDER_MARGIN);
		if (gridConfig.drawBackground()) {
			availableGridArea = availableGridArea
				.insetBy(BORDER_PADDING + INNER_PADDING);
		}

		ImmutableRect2i estimatedGridArea = IngredientGrid.calculateBounds(gridConfig, availableGridArea);

		if (!estimatedGridArea.isEmpty()) {
			ImmutableRect2i slotBackgroundArea = calculateSlotBackgroundArea(estimatedGridArea, this.gridConfig);
			ImmutableRect2i estimatedNavigationArea = calculateNavigationArea(slotBackgroundArea, navigationEnabled);
			if (gridConfig.drawBackground()) {
				estimatedNavigationArea.expandBy(BORDER_PADDING + INNER_PADDING);
			}

			availableGridArea = avoidExclusionAreas(
				availableArea,
				estimatedNavigationArea,
				guiExclusionAreas,
				gridConfig
			)
				.insetBy(BORDER_MARGIN)
				.cropTop(NAVIGATION_HEIGHT + INNER_PADDING);

			if (gridConfig.drawBackground()) {
				availableGridArea = availableGridArea.insetBy(BORDER_PADDING + INNER_PADDING);
			}
		}

		this.ingredientGrid.updateBounds(availableGridArea, guiExclusionAreas);
	}

	public void updateBounds(final ImmutableRect2i availableArea, Set<ImmutableRect2i> guiExclusionAreas) {
		this.guiExclusionAreas = guiExclusionAreas;

		final boolean navigationEnabled =
			switch (this.gridConfig.getButtonNavigationVisibility()) {
				case ENABLED -> true;
				case DISABLED -> false;
				case AUTO_HIDE -> {
					updateGridBounds(availableArea, false);
					yield hasRoom() && this.pageDelegate.getPageCount() > 1;
				}
			};
		if (navigationEnabled) {
			updateGridBounds(availableArea, true);
		}
		if (!hasRoom()) {
			return;
		}

		this.slotBackgroundArea = calculateSlotBackgroundArea(this.ingredientGrid.getArea(), this.gridConfig);

		ImmutableRect2i navigationArea = calculateNavigationArea(this.slotBackgroundArea, navigationEnabled);
		this.navigation.updateBounds(navigationArea);

		this.backgroundArea = MathUtil.union(this.slotBackgroundArea, navigationArea);
		if (gridConfig.drawBackground()) {
			this.backgroundArea = this.backgroundArea.expandBy(BORDER_PADDING);
		}
	}

	private static ImmutableRect2i calculateSlotBackgroundArea(ImmutableRect2i ingredientGridArea, IIngredientGridConfig gridConfig) {
		if (gridConfig.drawBackground()) {
			return ingredientGridArea.expandBy(INNER_PADDING);
		} else {
			return ingredientGridArea;
		}
	}

	private static ImmutableRect2i calculateNavigationArea(ImmutableRect2i slotBackgroundArea, boolean navigationEnabled) {
		if (!navigationEnabled) {
			return ImmutableRect2i.EMPTY;
		}

		return slotBackgroundArea
			.keepTop(NAVIGATION_HEIGHT)
			.moveUp(NAVIGATION_HEIGHT + INNER_PADDING);
	}

	public ImmutableRect2i getBackgroundArea() {
		return this.backgroundArea;
	}

	public void draw(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		if (gridConfig.drawBackground()) {
			background.draw(poseStack, this.backgroundArea);
			slotBackground.draw(poseStack, this.slotBackgroundArea);
		}

		this.ingredientGrid.draw(minecraft, poseStack, mouseX, mouseY);
		this.navigation.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		this.ingredientGrid.drawTooltips(minecraft, poseStack, mouseX, mouseY);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.backgroundArea.contains(mouseX, mouseY) &&
			this.guiExclusionAreas.stream()
				.noneMatch(area -> area.contains(mouseX, mouseY));
	}

	public IUserInputHandler createInputHandler() {
		return new CombinedInputHandler(
			new UserInputHandler(this.pageDelegate, this.ingredientGrid, this.toggleState, this.clientConfig, this.commandUtil, this.cheatUtil, this::isMouseOver),
			this.ingredientGrid.getInputHandler(),
			this.navigation.createInputHandler()
		);
	}

	@Override
	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY);
	}

	public <T> Stream<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		return this.ingredientGrid.getVisibleIngredients(ingredientType);
	}

	public boolean isEmpty() {
		return this.ingredientSource.getIngredientList().isEmpty();
	}

	private class IngredientGridPaged implements IPaged {
		@Override
		public boolean nextPage() {
			final int itemsCount = ingredientSource.getIngredientList().size();
			if (itemsCount > 0) {
				firstItemIndex += ingredientGrid.size();
				if (firstItemIndex >= itemsCount) {
					firstItemIndex = 0;
				}
				updateLayout(false);
				return true;
			} else {
				firstItemIndex = 0;
				updateLayout(false);
				return false;
			}
		}

		@Override
		public boolean previousPage() {
			final int itemsPerPage = ingredientGrid.size();
			if (itemsPerPage == 0) {
				firstItemIndex = 0;
				updateLayout(false);
				return false;
			}
			final int itemsCount = ingredientSource.getIngredientList().size();

			int pageNum = firstItemIndex / itemsPerPage;
			if (pageNum == 0) {
				pageNum = itemsCount / itemsPerPage;
			} else {
				pageNum--;
			}

			firstItemIndex = itemsPerPage * pageNum;
			if (firstItemIndex > 0 && firstItemIndex == itemsCount) {
				pageNum--;
				firstItemIndex = itemsPerPage * pageNum;
			}
			updateLayout(false);
			return true;
		}

		@Override
		public boolean hasNext() {
			// true if there is more than one page because this wraps around
			int itemsPerPage = ingredientGrid.size();
			return itemsPerPage > 0 && ingredientSource.getIngredientList().size() > itemsPerPage;
		}

		@Override
		public boolean hasPrevious() {
			// true if there is more than one page because this wraps around
			int itemsPerPage = ingredientGrid.size();
			return itemsPerPage > 0 && ingredientSource.getIngredientList().size() > itemsPerPage;
		}

		@Override
		public int getPageCount() {
			final int itemCount = ingredientSource.getIngredientList().size();
			final int stacksPerPage = ingredientGrid.size();
			if (stacksPerPage == 0) {
				return 1;
			}
			int pageCount = MathUtil.divideCeil(itemCount, stacksPerPage);
			pageCount = Math.max(1, pageCount);
			return pageCount;
		}

		@Override
		public int getPageNumber() {
			final int stacksPerPage = ingredientGrid.size();
			if (stacksPerPage == 0) {
				return 0;
			}
			return firstItemIndex / stacksPerPage;
		}
	}

	private static class UserInputHandler implements IUserInputHandler {
		private final IngredientGridPaged paged;
		private final IRecipeFocusSource focusSource;
		private final IClientToggleState toggleState;
		private final IClientConfig clientConfig;
		private final UserInput.MouseOverable mouseOverable;
		private final CommandUtil commandUtil;
		private final CheatUtil cheatUtil;

		private UserInputHandler(
			IngredientGridPaged paged,
			IRecipeFocusSource focusSource,
			IClientToggleState toggleState,
			IClientConfig clientConfig,
			CommandUtil commandUtil,
			CheatUtil cheatUtil, UserInput.MouseOverable mouseOverable
		) {
			this.paged = paged;
			this.focusSource = focusSource;
			this.toggleState = toggleState;
			this.clientConfig = clientConfig;
			this.mouseOverable = mouseOverable;
			this.commandUtil = commandUtil;
			this.cheatUtil = cheatUtil;
		}

		@Override
		public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
			if (!mouseOverable.isMouseOver(mouseX, mouseY)) {
				return Optional.empty();
			}
			if (scrollDelta < 0) {
				this.paged.nextPage();
				return Optional.of(this);
			} else if (scrollDelta > 0) {
				this.paged.previousPage();
				return Optional.of(this);
			}
			return Optional.empty();
		}

		@Override
		public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
			if (input.is(keyBindings.getNextPage())) {
				this.paged.nextPage();
				return Optional.of(this);
			}

			if (input.is(keyBindings.getPreviousPage())) {
				this.paged.previousPage();
				return Optional.of(this);
			}

			return checkHotbarKeys(screen, input);
		}

		/**
		 * Modeled after ContainerScreen#checkHotbarKeys(int)
		 * Sets the stack in a hotbar slot to the one that's hovered over.
		 */
		protected Optional<IUserInputHandler> checkHotbarKeys(Screen screen, UserInput input) {
			if (!clientConfig.isCheatToHotbarUsingHotkeysEnabled() ||
				!this.toggleState.isCheatItemsEnabled() ||
				screen instanceof RecipesGui
			) {
				return Optional.empty();
			}

			final double mouseX = input.getMouseX();
			final double mouseY = input.getMouseY();
			if (!this.mouseOverable.isMouseOver(mouseX, mouseY)) {
				return Optional.empty();
			}

			Minecraft minecraft = Minecraft.getInstance();
			Options gameSettings = minecraft.options;
			int hotbarSlot = getHotbarSlotForInput(input, gameSettings);
			if (hotbarSlot < 0) {
				return Optional.empty();
			}

			return this.focusSource.getIngredientUnderMouse(mouseX, mouseY)
				.flatMap(clickedIngredient -> {
					ItemStack cheatItemStack = cheatUtil.getCheatItemStack(clickedIngredient);
					if (!cheatItemStack.isEmpty()) {
						commandUtil.setHotbarStack(cheatItemStack, hotbarSlot);
						ImmutableRect2i area = clickedIngredient.getArea();
						return Stream.of(LimitedAreaInputHandler.create(this, area));
					}
					return Stream.empty();
				})
				.findFirst();
		}

		private static int getHotbarSlotForInput(UserInput input, Options gameSettings) {
			for (int hotbarSlot = 0; hotbarSlot < gameSettings.keyHotbarSlots.length; ++hotbarSlot) {
				KeyMapping keyHotbarSlot = gameSettings.keyHotbarSlots[hotbarSlot];
				if (input.is(keyHotbarSlot)) {
					return hotbarSlot;
				}
			}
			return -1;
		}
	}
}
