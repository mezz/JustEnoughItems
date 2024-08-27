package mezz.jei.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.OffsetDrawable;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.core.collect.ListMultiMap;
import mezz.jei.gui.overlay.elements.IElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class IngredientListRenderer {
	private static final int BLACKLIST_COLOR = 0xDDFF0000;
	private static final int WILDCARD_BLACKLIST_COLOR = 0xDDFFA500;

	private final List<IngredientListSlot> slots = new ArrayList<>();
	private final ListMultiMap<IIngredientType<?>, BatchRenderElement<?>> renderElementsByType = new ListMultiMap<>();
	private final List<IDrawable> renderOverlays = new ArrayList<>();
	private final IIngredientManager ingredientManager;
	private final boolean supportsEditMode;

	private int blocked = 0;

	public IngredientListRenderer(IIngredientManager ingredientManager, boolean supportsEditMode) {
		this.ingredientManager = ingredientManager;
		this.supportsEditMode = supportsEditMode;
	}

	public void clear() {
		slots.clear();
		renderElementsByType.clear();
		renderOverlays.clear();
		blocked = 0;
	}

	public int size() {
		return slots.size() - blocked;
	}

	public void add(IngredientListSlot ingredientListSlot) {
		slots.add(ingredientListSlot);
		addRenderElement(ingredientListSlot);
	}

	private void addRenderElement(IngredientListSlot ingredientListSlot) {
		ingredientListSlot.getOptionalElement()
			.ifPresent(element -> {
				ITypedIngredient<?> typedIngredient = element.getTypedIngredient();
				IIngredientType<?> ingredientType = typedIngredient.getType();
				ImmutableRect2i renderArea = ingredientListSlot.getRenderArea();
				BatchRenderElement<?> batchRenderElement = new BatchRenderElement<>(typedIngredient.getIngredient(), renderArea.x(), renderArea.y());
				renderElementsByType.put(ingredientType, batchRenderElement);
				IDrawable renderOverlay = element.createRenderOverlay();
				if (renderOverlay != null) {
					renderOverlays.add(OffsetDrawable.create(renderOverlay, renderArea.x(), renderArea.y()));
				}
			});
	}

	public Stream<IngredientListSlot> getSlots() {
		return slots.stream()
			.filter(s -> !s.isBlocked());
	}

	public void set(final int startIndex, List<IElement<?>> ingredientList) {
		blocked = 0;
		renderElementsByType.clear();
		renderOverlays.clear();

		ListIterator<IElement<?>> elementIterator = ingredientList.listIterator(startIndex);

		for (IngredientListSlot ingredientListSlot : slots) {
			if (ingredientListSlot.isBlocked()) {
				ingredientListSlot.clear();
				blocked++;
			} else if (elementIterator.hasNext()) {
				IElement<?> element = elementIterator.next();
				while (!element.isVisible() && elementIterator.hasNext()) {
					element = elementIterator.next();
				}
				if (element.isVisible()) {
					ingredientListSlot.setElement(element);
					addRenderElement(ingredientListSlot);
				} else {
					ingredientListSlot.clear();
				}
			} else {
				ingredientListSlot.clear();
			}
		}
	}

	public void render(GuiGraphics guiGraphics) {
		if (supportsEditMode && Internal.getClientToggleState().isEditModeEnabled()) {
			renderEditMode(guiGraphics);
		}

		for (Map.Entry<IIngredientType<?>, List<BatchRenderElement<?>>> entry : renderElementsByType.entrySet()) {
			renderBatch(guiGraphics, entry);
		}

		for (IDrawable overlay : renderOverlays) {
			overlay.draw(guiGraphics);
		}
	}

	private <T> void renderBatch(GuiGraphics guiGraphics, Map.Entry<IIngredientType<?>, List<BatchRenderElement<?>>> entry) {
		@SuppressWarnings("unchecked")
		IIngredientType<T> type = (IIngredientType<T>) entry.getKey();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
		@SuppressWarnings("unchecked")
		List<BatchRenderElement<T>> elements = (List<BatchRenderElement<T>>) (Object) entry.getValue();
		SafeIngredientUtil.renderBatch(guiGraphics, type, ingredientRenderer, elements);
	}

	private void renderEditMode(GuiGraphics guiGraphics) {
		IEditModeConfig editModeConfig = Internal.getJeiRuntime().getEditModeConfig();

		for (IngredientListSlot slot : slots) {
			slot.getOptionalElement()
				.ifPresent(element -> {
					renderEditMode(guiGraphics, slot.getArea(), slot.getPadding(), element.getTypedIngredient(), editModeConfig);
				});
		}

		RenderSystem.enableBlend();
	}

	private static <T> void renderEditMode(GuiGraphics guiGraphics, ImmutableRect2i area, int padding, ITypedIngredient<T> typedIngredient, IEditModeConfig config) {
		Set<IEditModeConfig.HideMode> hideModes = config.getIngredientHiddenUsingConfigFile(typedIngredient);
		if (!hideModes.isEmpty()) {
			if (hideModes.contains(IEditModeConfig.HideMode.WILDCARD)) {
				guiGraphics.fill(
					RenderType.guiOverlay(),
					area.getX() + padding,
					area.getY() + padding,
					area.getX() + 16 + padding,
					area.getY() + 16 + padding,
					WILDCARD_BLACKLIST_COLOR
				);
			}
			if (hideModes.contains(IEditModeConfig.HideMode.SINGLE)) {
				guiGraphics.fill(
					RenderType.guiOverlay(),
					area.getX() + padding,
					area.getY() + padding,
					area.getX() + 16 + padding,
					area.getY() + 16 + padding,
					BLACKLIST_COLOR
				);
			}
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	}
}
