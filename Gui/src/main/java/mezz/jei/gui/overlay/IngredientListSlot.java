package mezz.jei.gui.overlay;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.ClickableIngredientInternal;
import mezz.jei.gui.input.DraggableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IDraggableIngredientInternal;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.RenderableElement;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IngredientListSlot {
	private final ImmutableRect2i area;
	private final int padding;
	private boolean blocked = false;
	@Nullable
	private RenderableElement<?> renderableElement;

	public IngredientListSlot(int xPosition, int yPosition, int width, int height, int padding) {
		this.area = new ImmutableRect2i(xPosition, yPosition, width, height);
		this.padding = padding;
	}

	public Optional<IElement<?>> getElement() {
		return Optional.ofNullable(renderableElement)
			.map(RenderableElement::getElement);
	}

	public Optional<IClickableIngredientInternal<?>> getClickableIngredient() {
		return Optional.ofNullable(renderableElement)
			.map(RenderableElement::getElement)
			.map(element -> new ClickableIngredientInternal<>(element, this::isMouseOver, true, true));
	}

	public Optional<IDraggableIngredientInternal<?>> getDraggableIngredient() {
		return Optional.ofNullable(renderableElement)
			.map(RenderableElement::getElement)
			.map(element -> new DraggableIngredientInternal<>(element, area));
	}

	public void render(GuiGraphics guiGraphics) {
		if (!blocked && renderableElement != null) {
			renderableElement.render(guiGraphics, area, padding);
		}
	}

	public void clear() {
		this.renderableElement = null;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return (this.renderableElement != null) && area.contains(mouseX, mouseY);
	}

	public void setElement(RenderableElement<?> renderableElement) {
		this.renderableElement = renderableElement;
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

	public void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, IngredientGridTooltipHelper tooltipHelper) {
		if (renderableElement != null) {
			renderableElement.drawTooltip(guiGraphics, mouseX, mouseY, tooltipHelper);
		}
	}
}
