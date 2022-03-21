package mezz.jei.render;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.util.ImmutableRect2i;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IngredientListSlot {
	private final ImmutableRect2i area;
	private final int padding;
	private boolean blocked = false;
	@Nullable
	private ElementRenderer<?> ingredientRenderer;

	public IngredientListSlot(int xPosition, int yPosition, int width, int height, int padding) {
		this.area = new ImmutableRect2i(xPosition, yPosition, width, height);
		this.padding = padding;
	}

	public Optional<ElementRenderer<?>> getIngredientRenderer() {
		return Optional.ofNullable(ingredientRenderer);
	}

	public Optional<ITypedIngredient<?>> getTypedIngredient() {
		return getIngredientRenderer()
			.map(ElementRenderer::getTypedIngredient);
	}

	public void clear() {
		this.ingredientRenderer = null;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return (this.ingredientRenderer != null) && area.contains(mouseX, mouseY);
	}

	public void setIngredientRenderer(ElementRenderer<?> ingredientRenderer) {
		this.ingredientRenderer = ingredientRenderer;
		ingredientRenderer.setArea(area);
		ingredientRenderer.setPadding(padding);
	}

	public ImmutableRect2i getArea() {
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
