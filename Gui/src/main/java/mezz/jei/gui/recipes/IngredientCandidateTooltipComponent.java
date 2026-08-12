package mezz.jei.gui.recipes;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.IIngredientGridTooltipComponent;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.input.ClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class IngredientCandidateTooltipComponent implements ClientTooltipComponent, TooltipComponent, IIngredientGridTooltipComponent {
	private static final int CELL_SIZE = 18;
	private static final int GRID_PADDING = 1;
	private static final int SCROLLBAR_WIDTH = 14;
	private static final int SCROLLBAR_GAP = 2;
	private static final int MIN_SCROLL_MARKER_HEIGHT = 14;
	private static final int MAX_COLUMNS = 7;
	private static final int MAX_ROWS = 4;

	private final ScalableDrawable scrollbarBackground;
	private final ScalableDrawable scrollbarMarker;
	private final IDrawableStatic slotBackground;
	private final List<IRecipeSlotDrawable> slots;
	private final int columns;
	private final int visibleRows;
	private final int totalRows;
	private final int maxRowOffset;
	private final int width;
	private final int height;

	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private int rowOffset;
	private double mouseX;
	private double mouseY;

	public IngredientCandidateTooltipComponent(IRecipeManager recipeManager, List<ITypedIngredient<?>> ingredients) {
		int count = ingredients.size();
		this.columns = Math.min(count, MAX_COLUMNS);
		this.totalRows = MathUtil.divideCeil(count, this.columns);
		this.visibleRows = Math.min(this.totalRows, MAX_ROWS);
		this.maxRowOffset = Math.max(0, this.totalRows - MAX_ROWS);

		int scrollbarSpace = 0;
		if (this.maxRowOffset > 0) {
			scrollbarSpace = SCROLLBAR_GAP + SCROLLBAR_WIDTH;
		}
		this.width = (2 * GRID_PADDING) + (this.columns * CELL_SIZE) + scrollbarSpace;
		this.height = (2 * GRID_PADDING) + (this.visibleRows * CELL_SIZE);

		Textures textures = Internal.getTextures();
		this.scrollbarBackground = textures.getScrollbarBackground();
		this.scrollbarMarker = textures.getScrollbarMarker();
		this.slotBackground = textures.getSlot();

		this.slots = new ArrayList<>(count);
		for (ITypedIngredient<?> ingredient : ingredients) {
			IRecipeSlotDrawable slot = recipeManager.createRecipeSlotDrawable(
				RecipeIngredientRole.OUTPUT,
				List.of(Optional.of(ingredient)),
				Set.of(0),
				0
			);
			this.slots.add(slot);
		}
	}

	@Override
	public int getHeight(Font font) {
		return this.height;
	}

	@Override
	public int getWidth(Font font) {
		return this.width;
	}

	@Override
	public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor guiGraphics) {
		this.area = new ImmutableRect2i(x, y, this.width, this.height);
		int gridX = x + GRID_PADDING;
		int gridY = y + GRID_PADDING;

		int firstIndex = this.rowOffset * this.columns;
		int lastIndex = Math.min(firstIndex + (this.visibleRows * this.columns), this.slots.size());
		for (int i = firstIndex; i < lastIndex; i++) {
			IRecipeSlotDrawable slot = this.slots.get(i);
			int column = i % this.columns;
			int displayRow = (i / this.columns) - this.rowOffset;
			int cellX = gridX + (column * CELL_SIZE);
			int cellY = gridY + (displayRow * CELL_SIZE);
			slot.setPosition(cellX + 1, cellY + 1);
			this.slotBackground.draw(guiGraphics, cellX, cellY);
			slot.draw(guiGraphics, slot == getSlotUnderMouse(this.mouseX, this.mouseY));
		}

		if (this.maxRowOffset > 0) {
			drawScrollbar(guiGraphics, gridX, gridY);
		}
	}

	void setMousePosition(double mouseX, double mouseY) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		IRecipeSlotDrawable slot = getSlotUnderMouse(mouseX, mouseY);
		if (slot == null) {
			return Stream.empty();
		}
		return slot.getDisplayedIngredient()
			.<IClickableIngredientInternal<?>>map(ingredient -> createCandidateIngredient(ingredient, slot))
			.stream();
	}

	private <T> IClickableIngredientInternal<T> createCandidateIngredient(ITypedIngredient<T> ingredient, IRecipeSlotDrawable slot) {
		IElement<T> element = new IngredientElement<>(ingredient);
		return new ClickableIngredientInternal<>(element, (mouseX, mouseY) -> getSlotUnderMouse(mouseX, mouseY) == slot, false, true);
	}

	public boolean mouseScrolled(double scrollDeltaY) {
		if (scrollDeltaY == 0) {
			return false;
		}
		int delta = 1;
		if (scrollDeltaY > 0) {
			delta = -1;
		}
		int newRowOffset = Math.clamp(this.rowOffset + delta, 0, this.maxRowOffset);
		if (newRowOffset == this.rowOffset) {
			return false;
		}
		this.rowOffset = newRowOffset;
		return true;
	}

	public void drawTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		IRecipeSlotDrawable slot = getSlotUnderMouse(mouseX, mouseY);
		if (slot != null) {
			slot.drawTooltip(guiGraphics, mouseX, mouseY);
		}
	}

	@Nullable
	private IRecipeSlotDrawable getSlotUnderMouse(double mouseX, double mouseY) {
		int localX = (int) mouseX - (this.area.getX() + GRID_PADDING);
		int localY = (int) mouseY - (this.area.getY() + GRID_PADDING);
		if (localX < 0 || localY < 0) {
			return null;
		}
		int column = localX / CELL_SIZE;
		int row = localY / CELL_SIZE;
		if (column >= this.columns || row >= this.visibleRows) {
			return null;
		}
		int index = ((this.rowOffset + row) * this.columns) + column;
		if (index >= this.slots.size()) {
			return null;
		}
		return this.slots.get(index);
	}

	private void drawScrollbar(GuiGraphicsExtractor guiGraphics, int gridX, int gridY) {
		int scrollAreaX = gridX + (this.columns * CELL_SIZE) + SCROLLBAR_GAP;
		ImmutableRect2i scrollArea = new ImmutableRect2i(scrollAreaX, gridY, SCROLLBAR_WIDTH, this.visibleRows * CELL_SIZE);
		this.scrollbarBackground.draw(guiGraphics, scrollArea);

		int totalSpace = scrollArea.getHeight() - 2;
		int scrollMarkerWidth = scrollArea.getWidth() - 2;
		int minMarkerHeight = Math.min(MIN_SCROLL_MARKER_HEIGHT, totalSpace);
		int markerHeight = Math.max(Math.round(totalSpace * (this.visibleRows / (float) this.totalRows)), minMarkerHeight);
		float scrollFraction = this.rowOffset / (float) this.maxRowOffset;
		int markerY = Math.round((totalSpace - markerHeight) * scrollFraction);
		ImmutableRect2i markerArea = new ImmutableRect2i(
			scrollArea.getX() + 1,
			scrollArea.getY() + 1 + markerY,
			scrollMarkerWidth,
			markerHeight
		);
		this.scrollbarMarker.draw(guiGraphics, markerArea);
	}
}
