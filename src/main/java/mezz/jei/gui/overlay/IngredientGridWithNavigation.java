package mezz.jei.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.config.IFilterTextSource;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.PageNavigation;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.IPaged;
import mezz.jei.input.IRecipeFocusSource;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import mezz.jei.input.mouse.handlers.CombinedUserInputHandler;
import mezz.jei.render.IngredientListElementRenderer;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.MathUtil;
import mezz.jei.util.Rectangle2dBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Size2i;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Displays a list of ingredients with navigation at the top.
 */
public class IngredientGridWithNavigation implements IRecipeFocusSource {
	private static final int NAVIGATION_HEIGHT = 20;

	private int firstItemIndex = 0;
	private final IngredientGridPaged pageDelegate;
	private final PageNavigation navigation;
	private final GuiScreenHelper guiScreenHelper;
	private final IFilterTextSource filterTextSource;
	private final IWorldConfig worldConfig;
	private final IngredientGrid ingredientGrid;
	private final IIngredientGridSource ingredientSource;

	private Rect2i area = new Rect2i(0, 0, 0, 0);

	public IngredientGridWithNavigation(
		IIngredientGridSource ingredientSource,
		IFilterTextSource filterTextSource,
		GuiScreenHelper guiScreenHelper,
		IngredientGrid ingredientGrid,
		IWorldConfig worldConfig
	) {
		this.filterTextSource = filterTextSource;
		this.worldConfig = worldConfig;
		this.ingredientGrid = ingredientGrid;
		this.ingredientSource = ingredientSource;
		this.guiScreenHelper = guiScreenHelper;
		this.pageDelegate = new IngredientGridPaged();
		this.navigation = new PageNavigation(this.pageDelegate, false);
	}

	public void updateLayout(boolean resetToFirstPage) {
		if (resetToFirstPage) {
			firstItemIndex = 0;
		}
		String filterText = filterTextSource.getFilterText();
		List<IIngredientListElement<?>> ingredientList = ingredientSource.getIngredientList(filterText);
		if (firstItemIndex >= ingredientList.size()) {
			firstItemIndex = 0;
		}
		this.ingredientGrid.guiIngredientSlots.set(firstItemIndex, ingredientList);
		this.navigation.updatePageState();
	}

	public boolean updateBounds(Rect2i availableArea, Set<Rect2i> guiExclusionAreas) {
		Tuple<Rect2i, Rect2i> result = MathUtil.splitY(availableArea, NAVIGATION_HEIGHT);
		final Rect2i estimatedNavigationArea = result.getA();

		Collection<Rect2i> intersectsNavigationArea = guiExclusionAreas.stream()
			.filter(rectangle2d -> MathUtil.intersects(rectangle2d, estimatedNavigationArea))
			.toList();

		final int maxWidth = this.ingredientGrid.maxWidth();
		final int maxHeight = availableArea.getHeight();
		if (maxWidth <= 0 || maxHeight <= 0) {
			return false;
		}
		Size2i maxContentSize = new Size2i(maxWidth, maxHeight);
		availableArea = MathUtil.cropToAvoidIntersection(intersectsNavigationArea, availableArea, maxContentSize);
		if (MathUtil.contentArea(availableArea, maxContentSize) == 0) {
			return false;
		}

		result = MathUtil.splitY(availableArea, NAVIGATION_HEIGHT);
		Rect2i navigationArea = result.getA();
		Rect2i boundsWithoutNavigation = result.getB();
		boolean gridHasRoom = this.ingredientGrid.updateBounds(boundsWithoutNavigation, guiExclusionAreas);
		if (!gridHasRoom) {
			return false;
		}
		Rect2i displayArea = this.ingredientGrid.getArea();
		navigationArea = new Rectangle2dBuilder(navigationArea)
			.setX(displayArea)
			.setWidth(displayArea)
			.build();
		this.navigation.updateBounds(navigationArea);
		this.area = MathUtil.union(displayArea, navigationArea);
		return true;
	}

	public Rect2i getArea() {
		return this.area;
	}

	public void draw(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		this.ingredientGrid.draw(minecraft, poseStack, mouseX, mouseY);
		this.navigation.draw(minecraft, poseStack, mouseX, mouseY, partialTicks);
	}

	public void drawTooltips(Minecraft minecraft, PoseStack poseStack, int mouseX, int mouseY) {
		this.ingredientGrid.drawTooltips(minecraft, poseStack, mouseX, mouseY);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return MathUtil.contains(this.area, mouseX, mouseY) &&
			!guiScreenHelper.isInGuiExclusionArea(mouseX, mouseY);
	}

	public IUserInputHandler createInputHandler() {
		return new CombinedUserInputHandler(
			new UserInputHandler(this.pageDelegate, this.ingredientGrid, this.worldConfig, this::isMouseOver),
			this.ingredientGrid.createInputHandler(),
			this.navigation.createInputHandler()
		);
	}

	@Nullable
	@Override
	public IClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
		return this.ingredientGrid.getIngredientUnderMouse(mouseX, mouseY);
	}

	public <T> Optional<IIngredientListElement<T>> getElementUnderMouse(IIngredientType<T> ingredientType) {
		return this.ingredientGrid.getElementUnderMouse(ingredientType);
	}

	public <T> List<IIngredientListElement<T>> getVisibleElements(IIngredientType<T> ingredientType) {
		return this.ingredientGrid.guiIngredientSlots.getAllGuiIngredientSlots().stream()
			.map(slot -> slot.getIngredientRenderer(ingredientType))
			.filter(Objects::nonNull)
			.map(IngredientListElementRenderer::getElement)
			.toList();
	}

	private class IngredientGridPaged implements IPaged {
		@Override
		public boolean nextPage() {
			String filterText = filterTextSource.getFilterText();
			final int itemsCount = ingredientSource.getIngredientList(filterText).size();
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
			String filterText = filterTextSource.getFilterText();
			final int itemsCount = ingredientSource.getIngredientList(filterText).size();

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
			String filterText = filterTextSource.getFilterText();
			// true if there is more than one page because this wraps around
			int itemsPerPage = ingredientGrid.size();
			return itemsPerPage > 0 && ingredientSource.getIngredientList(filterText).size() > itemsPerPage;
		}

		@Override
		public boolean hasPrevious() {
			String filterText = filterTextSource.getFilterText();
			// true if there is more than one page because this wraps around
			int itemsPerPage = ingredientGrid.size();
			return itemsPerPage > 0 && ingredientSource.getIngredientList(filterText).size() > itemsPerPage;
		}

		@Override
		public int getPageCount() {
			String filterText = filterTextSource.getFilterText();
			final int itemCount = ingredientSource.getIngredientList(filterText).size();
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
		private final IWorldConfig worldConfig;
		private final UserInput.MouseOverable mouseOverable;

		private UserInputHandler(IngredientGridPaged paged, IRecipeFocusSource focusSource, IWorldConfig worldConfig, UserInput.MouseOverable mouseOverable) {
			this.paged = paged;
			this.focusSource = focusSource;
			this.worldConfig = worldConfig;
			this.mouseOverable = mouseOverable;
		}

		@Override
		public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
			if (!mouseOverable.isMouseOver(mouseX, mouseY)) {
				return false;
			}
			if (scrollDelta < 0) {
				this.paged.nextPage();
				return true;
			} else if (scrollDelta > 0) {
				this.paged.previousPage();
				return true;
			}
			return false;
		}

		@Nullable
		@Override
		public IUserInputHandler handleUserInput(Screen screen, UserInput input) {
			if (input.is(KeyBindings.nextPage)) {
				this.paged.nextPage();
				return this;
			}

			if (input.is(KeyBindings.previousPage)) {
				this.paged.previousPage();
				return this;
			}

			if (checkHotbarKeys(screen, input)) {
				return this;
			}
			return null;
		}

		/**
		 * Modeled after ContainerScreen#checkHotbarKeys(int)
		 * Sets the stack in a hotbar slot to the one that's hovered over.
		 */
		protected boolean checkHotbarKeys(Screen screen, UserInput input) {
			Minecraft minecraft = screen.getMinecraft();
			if (minecraft == null) {
				return false;
			}

			if (!this.worldConfig.isCheatItemsEnabled() || screen instanceof RecipesGui) {
				return false;
			}

			final double mouseX = input.getMouseX();
			final double mouseY = input.getMouseY();
			if (!this.mouseOverable.isMouseOver(mouseX, mouseY)) {
				return false;
			}

			Options gameSettings = minecraft.options;
			int hotbarSlot = getHotbarSlotForInput(input, gameSettings);
			if (hotbarSlot < 0) {
				return false;
			}

			IClickedIngredient<?> ingredientUnderMouse = this.focusSource.getIngredientUnderMouse(mouseX, mouseY);
			if (ingredientUnderMouse == null) {
				return false;
			}

			ItemStack itemStack = ingredientUnderMouse.getCheatItemStack();
			if (itemStack.isEmpty()) {
				return false;
			}

			CommandUtil.setHotbarStack(itemStack, hotbarSlot);
			return true;
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
