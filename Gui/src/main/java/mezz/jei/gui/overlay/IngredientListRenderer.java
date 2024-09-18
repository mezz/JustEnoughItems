package mezz.jei.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.GuiComponent;

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
	private final boolean searchable;

	private int blocked = 0;

	public IngredientListRenderer(IIngredientManager ingredientManager, boolean searchable) {
		this.ingredientManager = ingredientManager;
		this.searchable = searchable;
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
				BatchRenderElement<?> batchRenderElement = new BatchRenderElement<>(typedIngredient.getIngredient(), renderArea.getX(), renderArea.getY());
				renderElementsByType.put(ingredientType, batchRenderElement);
				IDrawable renderOverlay = element.createRenderOverlay();
				if (renderOverlay != null) {
					renderOverlays.add(OffsetDrawable.create(renderOverlay, renderArea.getX(), renderArea.getY()));
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

	public void render(PoseStack poseStack) {
		if (searchable && Internal.getWorldConfig().isEditModeEnabled()) {
			renderEditMode(poseStack);
		}

		for (Map.Entry<IIngredientType<?>, List<BatchRenderElement<?>>> entry : renderElementsByType.entrySet()) {
			renderBatch(poseStack, entry);
		}

		for (IDrawable overlay : renderOverlays) {
			overlay.draw(poseStack);
		}
	}

	private <T> void renderBatch(PoseStack poseStack, Map.Entry<IIngredientType<?>, List<BatchRenderElement<?>>> entry) {
		@SuppressWarnings("unchecked")
		IIngredientType<T> type = (IIngredientType<T>) entry.getKey();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(type);
		@SuppressWarnings("unchecked")
		List<BatchRenderElement<T>> elements = (List<BatchRenderElement<T>>) (Object) entry.getValue();
		SafeIngredientUtil.renderBatch(poseStack, type, ingredientRenderer, elements);
	}

	private void renderEditMode(PoseStack poseStack) {
		IEditModeConfig editModeConfig = Internal.getJeiRuntime().getEditModeConfig();

		for (IngredientListSlot slot : slots) {
			slot.getOptionalElement()
				.ifPresent(element -> {
					renderEditMode(poseStack, slot.getArea(), slot.getPadding(), element.getTypedIngredient(), editModeConfig);
				});
		}

		RenderSystem.enableBlend();
	}

	private static <T> void renderEditMode(PoseStack poseStack, ImmutableRect2i area, int padding, ITypedIngredient<T> typedIngredient, IEditModeConfig config) {
		Set<IEditModeConfig.HideMode> hideModes = config.getIngredientHiddenUsingConfigFile(typedIngredient);
		if (!hideModes.isEmpty()) {
			boolean wildcard = hideModes.contains(IEditModeConfig.HideMode.WILDCARD);
			boolean single = hideModes.contains(IEditModeConfig.HideMode.SINGLE);
			if (wildcard && single) {
				GuiComponent.fill(
					poseStack,
					area.getX() + padding,
					area.getY() + padding,
					area.getX() + 16 + padding,
					area.getY() + 8 + padding,
					WILDCARD_BLACKLIST_COLOR
				);
				GuiComponent.fill(
					poseStack,
					area.getX() + padding,
					area.getY() + 8 + padding,
					area.getX() + 16 + padding,
					area.getY() + 16 + padding,
					BLACKLIST_COLOR
				);
			} else if (wildcard) {
				GuiComponent.fill(
					poseStack,
					area.getX() + padding,
					area.getY() + padding,
					area.getX() + 16 + padding,
					area.getY() + 16 + padding,
					WILDCARD_BLACKLIST_COLOR
				);
			} else if (single) {
				GuiComponent.fill(
					poseStack,
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
