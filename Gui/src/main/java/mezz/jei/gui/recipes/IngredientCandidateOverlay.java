package mezz.jei.gui.recipes;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class IngredientCandidateOverlay {
	private static final int CELL_SIZE = 18;
	private static final int PADDING = 6;
	private static final int SCROLLBAR_WIDTH = 14;
	private static final int SCROLLBAR_GAP = 2;
	private static final int MIN_SCROLL_MARKER_HEIGHT = 14;
	private static final int LINE_SPACING = 2;
	private static final int MAX_COLUMNS = 7;
	private static final int MAX_ROWS = 4;

	private final ScalableDrawable background;
	private final ScalableDrawable scrollbarBackground;
	private final ScalableDrawable scrollbarMarker;
	private final IDrawableStatic slotBackground;

	private boolean visible = false;
	private List<IRecipeSlotDrawable> slots = List.of();
	private Component header = Component.empty();
	private Component tagName = Component.empty();
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private int gridTop;
	private int columns;
	private int totalRows;
	private int maxRowOffset;
	private int rowOffset;

	public IngredientCandidateOverlay() {
		Textures textures = Internal.getTextures();
		this.background = textures.getRecipeGuiBackground();
		this.scrollbarBackground = textures.getScrollbarBackground();
		this.scrollbarMarker = textures.getScrollbarMarker();
		this.slotBackground = textures.getSlot();
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void hide() {
		this.visible = false;
	}

	public boolean show(
		RecipeSlotUnderMouse slotUnderMouse,
		int screenWidth,
		int screenHeight,
		IIngredientVisibility ingredientVisibility
	) {
		List<ITypedIngredient<?>> allIngredients = slotUnderMouse.slot().getAllIngredients().toList();
		List<ITypedIngredient<?>> visibleIngredients = allIngredients.stream()
			.filter(ingredientVisibility::isIngredientVisible)
			.toList();
		if (visibleIngredients.size() <= 1) {
			return false;
		}

		int count = visibleIngredients.size();
		int columns = Math.min(count, MAX_COLUMNS);
		int totalRows = MathUtil.divideCeil(count, columns);
		int visibleRows = Math.min(totalRows, MAX_ROWS);
		int maxRowOffset = Math.max(0, totalRows - MAX_ROWS);

		TagKey<?> tagKey = getTagKey(visibleIngredients.getFirst(), allIngredients).orElse(null);
		Component header;
		Component tagName = Component.empty();
		if (tagKey != null) {
			tagName = Services.PLATFORM.getRenderHelper().getName(tagKey);
			header = Component.literal("#" + tagKey.location()).withStyle(ChatFormatting.GRAY);
		} else {
			header = Component.translatable("jei.tooltip.recipe.slot.candidates.title", count);
		}

		Rect2i slotArea = slotUnderMouse.slot().getAreaIncludingBackground();
		int absoluteSlotX = slotUnderMouse.offset().x() + slotArea.getX();
		int absoluteSlotY = slotUnderMouse.offset().y() + slotArea.getY();
		int absoluteSlotBottom = absoluteSlotY + slotArea.getHeight();

		Font font = Minecraft.getInstance().font;
		int headerHeight = font.lineHeight + LINE_SPACING;
		if (!tagName.getString().isEmpty()) {
			headerHeight += font.lineHeight + LINE_SPACING;
		}
		int scrollbarWidth;
		if (maxRowOffset > 0) {
			scrollbarWidth = SCROLLBAR_WIDTH + SCROLLBAR_GAP;
		} else {
			scrollbarWidth = 0;
		}

		int gridWidth = (columns * CELL_SIZE) + scrollbarWidth;
		int headerWidth = font.width(header);
		if (!tagName.getString().isEmpty()) {
			headerWidth = Math.max(headerWidth, font.width(tagName));
		}
		int width = (2 * PADDING) + Math.max(gridWidth, headerWidth);
		int height = (2 * PADDING) + headerHeight + (visibleRows * CELL_SIZE);

		int x = Math.clamp(absoluteSlotX, 4, Math.max(4, screenWidth - width - 4));
		int y = absoluteSlotBottom + 4;
		if (y + height > screenHeight - 4) {
			y = Math.max(4, absoluteSlotY - height - 4);
		}

		ImmutableRect2i area = new ImmutableRect2i(x, y, width, height);
		int gridTop = y + PADDING + headerHeight;

		IRecipeManager recipeManager = Internal.getJeiRuntime().getRecipeManager();
		List<IRecipeSlotDrawable> slots = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			ITypedIngredient<?> ingredient = visibleIngredients.get(i);
			IRecipeSlotDrawable slot = recipeManager.createRecipeSlotDrawable(
				RecipeIngredientRole.OUTPUT,
				List.of(Optional.of(ingredient)),
				Set.of(0),
				0
			);
			slots.add(slot);
		}

		this.slots = slots;
		this.header = header;
		this.tagName = tagName;
		this.area = area;
		this.gridTop = gridTop;
		this.columns = columns;
		this.totalRows = totalRows;
		this.maxRowOffset = maxRowOffset;
		this.rowOffset = 0;
		this.visible = true;
		return true;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.visible && this.area.contains(mouseX, mouseY);
	}

	public boolean mouseScrolled(double scrollDeltaY) {
		if (!this.visible) {
			return false;
		}
		int delta;
		if (scrollDeltaY > 0) {
			delta = -1;
		} else {
			delta = 1;
		}
		int newRowOffset = Math.clamp(this.rowOffset + delta, 0, this.maxRowOffset);
		if (newRowOffset == this.rowOffset) {
			return false;
		}
		this.rowOffset = newRowOffset;
		return true;
	}

	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (!this.visible) {
			return;
		}
		background.draw(guiGraphics, area);

		Font font = Minecraft.getInstance().font;
		int textX = this.area.getX() + PADDING;
		int textY = this.area.getY() + PADDING;
		if (!this.tagName.getString().isEmpty()) {
			guiGraphics.text(font, this.tagName, textX, textY, 0xFF505050, false);
			guiGraphics.text(font, this.header, textX, textY + font.lineHeight + LINE_SPACING, 0xFF505050, false);
		} else {
			guiGraphics.text(font, this.header, textX, textY, 0xFF505050, false);
		}

		int firstIndex = this.rowOffset * this.columns;
		int lastIndex = Math.min(firstIndex + (MAX_ROWS * this.columns), this.slots.size());
		IRecipeSlotDrawable hoveredSlot = getSlotUnderMouse(mouseX, mouseY);
		for (int i = firstIndex; i < lastIndex; i++) {
			IRecipeSlotDrawable slot = this.slots.get(i);
			int col = i % this.columns;
			int displayRow = (i / this.columns) - this.rowOffset;
			int slotX = this.area.getX() + PADDING + (col * CELL_SIZE);
			int slotY = this.gridTop + (displayRow * CELL_SIZE);
			slot.setPosition(slotX, slotY);
			slotBackground.draw(guiGraphics, slotX - 1, slotY - 1);
			slot.draw(guiGraphics, slot == hoveredSlot);
		}

		if (this.maxRowOffset > 0) {
			int visibleRows = Math.min(this.totalRows, MAX_ROWS);
			drawScrollbar(guiGraphics, visibleRows);
		}
	}

	public void drawTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (!this.visible) {
			return;
		}
		IRecipeSlotDrawable slot = getSlotUnderMouse(mouseX, mouseY);
		if (slot != null) {
			slot.drawTooltip(guiGraphics, mouseX, mouseY);
		}
	}

	private static <T> Optional<TagKey<?>> getTagKey(ITypedIngredient<T> first, List<ITypedIngredient<?>> allIngredients) {
		IIngredientType<T> type = first.getType();
		List<T> ingredients = allIngredients.stream()
			.map(i -> i.getIngredient(type))
			.flatMap(Optional::stream)
			.toList();
		if (ingredients.isEmpty()) {
			return Optional.empty();
		}
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);
		return ingredientHelper.getTagKeyEquivalent(ingredients);
	}

	@Nullable
	private IRecipeSlotDrawable getSlotUnderMouse(double mouseX, double mouseY) {
		int localX = (int) mouseX - (this.area.getX() + PADDING);
		int localY = (int) mouseY - this.gridTop;
		if (localX < 0 || localY < 0) {
			return null;
		}
		int col = localX / CELL_SIZE;
		int row = localY / CELL_SIZE;
		if (col >= this.columns || row >= MAX_ROWS) {
			return null;
		}
		int index = ((this.rowOffset + row) * this.columns) + col;
		if (index >= this.slots.size()) {
			return null;
		}
		return this.slots.get(index);
	}

	private void drawScrollbar(GuiGraphicsExtractor guiGraphics, int visibleRows) {
		int scrollAreaX = this.area.getX() + this.area.getWidth() - PADDING - SCROLLBAR_WIDTH;
		ImmutableRect2i scrollArea = new ImmutableRect2i(scrollAreaX, this.gridTop, SCROLLBAR_WIDTH, visibleRows * CELL_SIZE);

		scrollbarBackground.draw(guiGraphics, scrollArea);

		int totalSpace = scrollArea.getHeight() - 2;
		int scrollMarkerWidth = scrollArea.getWidth() - 2;
		int minMarkerHeight = Math.min(MIN_SCROLL_MARKER_HEIGHT, totalSpace);
		int markerHeight = Math.max(Math.round(totalSpace * (visibleRows / (float) this.totalRows)), minMarkerHeight);
		float scrollFraction = this.rowOffset / (float) this.maxRowOffset;
		int markerY = Math.round((totalSpace - markerHeight) * scrollFraction);
		ImmutableRect2i markerArea = new ImmutableRect2i(
			scrollArea.getX() + 1,
			scrollArea.getY() + 1 + markerY,
			scrollMarkerWidth,
			markerHeight
		);
		scrollbarMarker.draw(guiGraphics, markerArea);
	}
}
