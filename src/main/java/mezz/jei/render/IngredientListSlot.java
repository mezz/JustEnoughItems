package mezz.jei.render;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.util.MathUtil;

public class IngredientListSlot {
	private final Rectangle2d area;
	private final int padding;
	private boolean blocked = false;
	@Nullable
	private IngredientListElementRenderer ingredientRenderer;

	public IngredientListSlot(int xPosition, int yPosition, int padding) {
		this.padding = padding;
		final int size = 16 + (2 * padding);
		this.area = new Rectangle2d(xPosition, yPosition, size, size);
	}

	@Nullable
	public IngredientListElementRenderer getIngredientRenderer() {
		return ingredientRenderer;
	}

	public void clear() {
		this.ingredientRenderer = null;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return (this.ingredientRenderer != null) && MathUtil.contains(area, mouseX, mouseY);
	}

	public void setIngredientRenderer(IngredientListElementRenderer ingredientRenderer) {
		this.ingredientRenderer = ingredientRenderer;
		ingredientRenderer.setArea(area);
		ingredientRenderer.setPadding(padding);
	}

	public Rectangle2d getArea() {
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
