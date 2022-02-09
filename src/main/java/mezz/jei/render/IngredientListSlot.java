package mezz.jei.render;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.ingredients.IngredientTypeHelper;
import net.minecraft.client.renderer.Rect2i;

import mezz.jei.util.MathUtil;

import java.util.Optional;

public class IngredientListSlot {
	private final Rect2i area;
	private final int padding;
	private boolean blocked = false;
	@Nullable
	private IngredientListElementRenderer<?> ingredientRenderer;

	public IngredientListSlot(int xPosition, int yPosition, int padding) {
		this.padding = padding;
		final int size = 16 + (2 * padding);
		this.area = new Rect2i(xPosition, yPosition, size, size);
	}

	public Optional<IngredientListElementRenderer<?>> getIngredientRenderer() {
		return Optional.ofNullable(ingredientRenderer);
	}

	public <T> Optional<IngredientListElementRenderer<T>> getIngredientRenderer(IIngredientType<T> ingredientType) {
		return IngredientTypeHelper.checkedCast(ingredientRenderer, ingredientType);
	}

	public void clear() {
		this.ingredientRenderer = null;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return (this.ingredientRenderer != null) && MathUtil.contains(area, mouseX, mouseY);
	}

	public void setIngredientRenderer(IngredientListElementRenderer<?> ingredientRenderer) {
		this.ingredientRenderer = ingredientRenderer;
		ingredientRenderer.setArea(area);
		ingredientRenderer.setPadding(padding);
	}

	public Rect2i getArea() {
		return area;
	}

	/**
	 * Set true if this ingredient is blocked by an extra gui area from a mod.
	 */
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public boolean isBlocked() {
		return blocked;
	}
}
