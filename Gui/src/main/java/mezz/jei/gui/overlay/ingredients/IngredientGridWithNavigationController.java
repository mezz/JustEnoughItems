package mezz.jei.gui.overlay.ingredients;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.ghost.GhostIngredientQuickMoveManager;
import mezz.jei.gui.input.DelegatingClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IMouseOverable;
import mezz.jei.gui.input.IPaged;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.SameElementInputHandler;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.util.CommandUtil;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class IngredientGridWithNavigationController implements IPaged, IUserInputHandler {
	private final IngredientGridPageState pageState = new IngredientGridPageState();
	private final IngredientGridScrollController scrollController;
	private final IIngredientGridSource ingredientSource;
	private final IIngredientGrid ingredientGrid;
	private final IIngredientGridConfig gridConfig;
	private final IClientToggleState toggleState;
	private final IClientConfig clientConfig;
	private final IMouseOverable mouseOverable;
	private final CommandUtil commandUtil;
	private final IIngredientManager ingredientManager;
	private final GhostIngredientQuickMoveManager ghostIngredientQuickMoveManager;
	private Runnable onLayoutChanged = () -> {};

	public IngredientGridWithNavigationController(
		IIngredientGridSource ingredientSource,
		IIngredientGrid ingredientGrid,
		IIngredientGridConfig gridConfig,
		IClientToggleState toggleState,
		IClientConfig clientConfig,
		CommandUtil commandUtil,
		IIngredientManager ingredientManager,
		IMouseOverable mouseOverable,
		GhostIngredientQuickMoveManager ghostIngredientQuickMoveManager
	) {
		this.ingredientSource = ingredientSource;
		this.ingredientGrid = ingredientGrid;
		this.gridConfig = gridConfig;
		this.toggleState = toggleState;
		this.clientConfig = clientConfig;
		this.mouseOverable = mouseOverable;
		this.commandUtil = commandUtil;
		this.ingredientManager = ingredientManager;
		this.ghostIngredientQuickMoveManager = ghostIngredientQuickMoveManager;
		this.scrollController = new IngredientGridScrollController(
			ingredientSource,
			ingredientGrid,
			gridConfig,
			clientConfig
		);
	}

	public void setOnLayoutChanged(Runnable onLayoutChanged) {
		this.onLayoutChanged = onLayoutChanged;
	}

	public void updateLayoutToFirstPage() {
		updateLayoutStartingAt(0);
	}

	public void updateLayoutKeepingPageAnchorVisible(@Nullable IElement<?> pageAnchorElement) {
		if (usesScrollbar()) {
			this.scrollController.updateLayoutKeepingScrollAnchorVisible(pageAnchorElement);
		} else {
			List<IElement<?>> ingredientList = ingredientSource.getElements();
			int firstItemIndex = this.pageState.updateKeepingPageAnchorVisible(pageAnchorElement, ingredientList, ingredientGrid.size());
			this.ingredientGrid.set(firstItemIndex, ingredientList);
		}
		this.onLayoutChanged.run();
	}

	@Nullable
	public IElement<?> getPageAnchorElement() {
		if (usesScrollbar()) {
			return this.scrollController.getScrollAnchorElement();
		}
		IElement<?> pageAnchorElement = this.pageState.getPageAnchorElement(ingredientSource.getElements());
		if (pageAnchorElement != null) {
			return pageAnchorElement;
		}
		return this.ingredientGrid.getVisibleElements()
			.findFirst()
			.orElse(null);
	}

	public <T> IClickableIngredientInternal<T> createPageAnchorIngredient(IClickableIngredientInternal<T> delegate) {
		return new PageAnchorClickableIngredient<>(delegate);
	}

	private void updateLayoutStartingAt(int firstItemIndex) {
		if (usesScrollbar()) {
			this.scrollController.updateLayoutStartingAt(firstItemIndex);
		} else {
			List<IElement<?>> ingredientList = ingredientSource.getElements();
			int renderFirstItemIndex = this.pageState.updateForPageNavigation(firstItemIndex, ingredientList.size(), ingredientGrid.size());
			this.ingredientGrid.set(renderFirstItemIndex, ingredientList);
			rememberFirstVisibleElementAsPageAnchor();
		}
		this.onLayoutChanged.run();
	}

	private void rememberFirstVisibleElementAsPageAnchor() {
		this.ingredientGrid.getVisibleElements()
			.findFirst()
			.ifPresent(this.pageState::setPageAnchorElement);
	}

	@Override
	public boolean nextPage() {
		if (usesScrollbar()) {
			return updateLayoutWhenChanged(this.scrollController.scrollByRows(this.scrollController.getVisibleScrollRows()));
		}

		if (getPageCount() <= 1) {
			return false;
		}
		final int itemsCount = ingredientSource.getElements().size();
		if (itemsCount > 0) {
			int nextFirstItemIndex = pageState.getFirstItemIndex() + ingredientGrid.size();
			if (nextFirstItemIndex >= itemsCount) {
				nextFirstItemIndex = 0;
			}
			updateLayoutStartingAt(nextFirstItemIndex);
			return true;
		} else {
			updateLayoutStartingAt(0);
			return false;
		}
	}

	@Override
	public boolean previousPage() {
		if (usesScrollbar()) {
			return updateLayoutWhenChanged(this.scrollController.scrollByRows(-this.scrollController.getVisibleScrollRows()));
		}

		if (getPageCount() <= 1) {
			return false;
		}

		final int itemsPerPage = ingredientGrid.size();
		if (itemsPerPage == 0) {
			updateLayoutStartingAt(0);
			return false;
		}
		final int itemsCount = ingredientSource.getElements().size();

		int pageNum = pageState.getFirstItemIndex() / itemsPerPage;
		if (pageNum == 0) {
			pageNum = itemsCount / itemsPerPage;
		} else {
			pageNum--;
		}

		int previousFirstItemIndex = itemsPerPage * pageNum;
		if (previousFirstItemIndex > 0 && previousFirstItemIndex == itemsCount) {
			pageNum--;
			previousFirstItemIndex = itemsPerPage * pageNum;
		}
		updateLayoutStartingAt(previousFirstItemIndex);
		return true;
	}

	@Override
	public boolean hasNext() {
		if (usesScrollbar()) {
			return this.scrollController.canScroll();
		}
		return getPageCount() > 1;
	}

	@Override
	public boolean hasPrevious() {
		if (usesScrollbar()) {
			return this.scrollController.canScroll();
		}
		return getPageCount() > 1;
	}

	@Override
	public int getPageCount() {
		if (usesScrollbar()) {
			return this.scrollController.getHiddenScrollRows() + 1;
		}
		return IngredientGridPageState.getPageCount(ingredientSource.getElements().size(), ingredientGrid.size());
	}

	@Override
	public int getPageNumber() {
		if (usesScrollbar()) {
			return this.scrollController.getFirstVisibleScrollRow();
		}
		return IngredientGridPageState.getPageNumberForFirstItemIndex(pageState.getFirstItemIndex(), ingredientGrid.size(), ingredientSource.getElements().size());
	}

	@Override
	public Optional<IUserInputHandler> handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		if (!mouseOverable.isMouseOver(mouseX, mouseY)) {
			return Optional.empty();
		}
		if (usesScrollbar()) {
			IngredientGridScrollController.ScrollResult scrollResult = this.scrollController.scrollByMouse(scrollDeltaY);
			updateLayoutWhenChanged(scrollResult.changed());
			if (scrollResult.consumed()) {
				return Optional.of(this);
			}
			return Optional.empty();
		}
		if (scrollDeltaY < 0) {
			if (nextPage()) {
				return Optional.of(this);
			}
		} else if (scrollDeltaY > 0) {
			if (previousPage()) {
				return Optional.of(this);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (input.is(keyBindings.getNextPage())) {
			nextPage();
			return Optional.of(this);
		}

		if (input.is(keyBindings.getPreviousPage())) {
			previousPage();
			return Optional.of(this);
		}

		if (input.is(keyBindings.getQuickMove())) {
			if (this.ghostIngredientQuickMoveManager.quickMove(screen, input)) {
				return Optional.of(this);
			}
		}

		return checkHotbarKeys(screen, input);
	}

	private boolean usesScrollbar() {
		return this.gridConfig.navigationMode().getValue()
			.usesScrollbar();
	}

	public boolean canScroll() {
		return this.scrollController.canScroll();
	}

	public int getVisibleScrollAmount() {
		return this.scrollController.getVisibleScrollAmount();
	}

	public int getHiddenScrollAmount() {
		return this.scrollController.getHiddenScrollAmount();
	}

	public float getScrollOffsetY() {
		return this.scrollController.getScrollOffsetY();
	}

	public void setScrollOffsetY(float scrollOffsetY) {
		updateLayoutWhenChanged(this.scrollController.setScrollOffsetY(scrollOffsetY));
	}

	private boolean updateLayoutWhenChanged(boolean layoutChanged) {
		if (layoutChanged) {
			this.onLayoutChanged.run();
		}
		return layoutChanged;
	}

	/**
	 * Modeled after ContainerScreen#checkHotbarKeys(int)
	 * Sets the stack in a hotbar slot to the one that's hovered over.
	 */
	private Optional<IUserInputHandler> checkHotbarKeys(Screen screen, UserInput input) {
		if (!clientConfig.cheatToHotbarUsingHotkeysEnabled().getValue() ||
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

		return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY)
			.<IUserInputHandler>flatMap(clickedIngredient -> {
				ItemStack cheatItemStack = clickedIngredient.getCheatItemStack(ingredientManager);
				if (!cheatItemStack.isEmpty()) {
					commandUtil.setHotbarStack(cheatItemStack, hotbarSlot);
					return Stream.of(new SameElementInputHandler(this, clickedIngredient::isMouseOver));
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

	private class PageAnchorClickableIngredient<T> extends DelegatingClickableIngredientInternal<T> {
		PageAnchorClickableIngredient(IClickableIngredientInternal<T> delegate) {
			super(delegate);
		}

		@Override
		public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
			IElement<T> element = getElement();
			if (element.isVisible()) {
				pageState.setPageAnchorElement(element);
				scrollController.setScrollAnchorElement(element);
			}
			super.show(recipesGui, focusUtil, roles);
		}
	}
}
