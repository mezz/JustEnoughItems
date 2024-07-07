package mezz.jei.gui.overlay;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.overlay.elements.ElementRenderer;
import mezz.jei.gui.overlay.elements.ElementRenderers;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.RenderableElement;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class IngredientListRenderer {
	private final List<IngredientListSlot> slots = new ArrayList<>();
	private final ElementRenderers elementRenderers;

	private int blocked = 0;

	public IngredientListRenderer() {
		this.elementRenderers = new ElementRenderers();
	}

	public void clear() {
		slots.clear();
		blocked = 0;
	}

	public int size() {
		return slots.size() - blocked;
	}

	public void add(IngredientListSlot ingredientListSlot) {
		slots.add(ingredientListSlot);
	}

	public Stream<IngredientListSlot> getSlots() {
		return slots.stream()
			.filter(s -> !s.isBlocked());
	}

	public void set(final int startIndex, List<IElement<?>> ingredientList) {
		blocked = 0;

		int i = startIndex;
		for (IngredientListSlot ingredientListSlot : slots) {
			if (ingredientListSlot.isBlocked()) {
				ingredientListSlot.clear();
				blocked++;
			} else {
				if (i >= ingredientList.size()) {
					ingredientListSlot.clear();
				} else {
					IElement<?> element = ingredientList.get(i);
					RenderableElement<?> renderableElement = createRenderableElement(element);
					ingredientListSlot.setElement(renderableElement);
				}
				i++;
			}
		}
	}

	private <T> RenderableElement<T> createRenderableElement(IElement<T> element) {
		ITypedIngredient<T> typedIngredient = element.getTypedIngredient();
		IIngredientType<T> type = typedIngredient.getType();
		ElementRenderer<T> renderer = elementRenderers.get(type);
		return new RenderableElement<>(element, renderer);
	}

	public void render(GuiGraphics guiGraphics) {
		for (IngredientListSlot slot : slots) {
			slot.render(guiGraphics);
		}
	}
}
