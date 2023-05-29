package mezz.jei.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.config.IClientToggleState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class IngredientListRenderer {
	private static final int BLACKLIST_COLOR = 0xFFFF0000;

	private final List<IngredientListSlot> slots = new ArrayList<>();
	private final ElementRenderersByType renderers = new ElementRenderersByType();
	private final IEditModeConfig editModeConfig;
	private final IClientToggleState toggleState;
	private final IIngredientManager ingredientManager;

	private int blocked = 0;

	public IngredientListRenderer(IEditModeConfig editModeConfig, IClientToggleState toggleState, IIngredientManager ingredientManager) {
		this.editModeConfig = editModeConfig;
		this.toggleState = toggleState;
		this.ingredientManager = ingredientManager;
	}

	public void clear() {
		slots.clear();
		renderers.clear();
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

	public void set(final int startIndex, List<ITypedIngredient<?>> ingredientList) {
		renderers.clear();
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
					ITypedIngredient<?> ingredient = ingredientList.get(i);
					set(ingredientListSlot, ingredient);
				}
				i++;
			}
		}
	}

	private <V> void set(IngredientListSlot ingredientListSlot, ITypedIngredient<V> value) {
		ElementRenderer<V> renderer = new ElementRenderer<>(value);
		ingredientListSlot.setIngredientRenderer(renderer);
		IIngredientType<V> ingredientType = value.getType();
		renderers.put(ingredientType, renderer);
	}

	public void render(GuiGraphics guiGraphics) {
		for (IIngredientType<?> ingredientType : renderers.getTypes()) {
			renderIngredientType(guiGraphics, ingredientType);
		}
	}

	private <T> void renderIngredientType(GuiGraphics guiGraphics, IIngredientType<T> ingredientType) {
		Collection<ElementRenderer<T>> slots = renderers.get(ingredientType);
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		for (ElementRenderer<T> slot : slots) {
			renderIngredient(guiGraphics, slot, ingredientRenderer);
		}
	}

	private <T> void renderIngredient(GuiGraphics guiGraphics, ElementRenderer<T> slot, IIngredientRenderer<T> ingredientRenderer) {
		ITypedIngredient<T> typedIngredient = slot.getTypedIngredient();
		ImmutableRect2i area = slot.getArea();
		int slotPadding = slot.getPadding();
		if (toggleState.isEditModeEnabled()) {
			renderEditMode(guiGraphics, area, slotPadding, editModeConfig, typedIngredient);
			RenderSystem.enableBlend();
		}

		T ingredient = typedIngredient.getIngredient();
		try {
			int xPosition = area.getX() + slotPadding;
			int yPosition = area.getY() + slotPadding;
			var poseStack = guiGraphics.pose();
			poseStack.pushPose();
			{
				poseStack.translate(xPosition, yPosition, 0);
				ingredientRenderer.render(guiGraphics, ingredient);
			}
			poseStack.popPose();
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, ingredient, ingredientManager);
		}
	}

	private static <T> void renderEditMode(GuiGraphics guiGraphics, ImmutableRect2i area, int padding, IEditModeConfig editModeConfig, ITypedIngredient<T> typedIngredient) {
		if (editModeConfig.isIngredientHiddenUsingConfigFile(typedIngredient)) {
			guiGraphics.fill(area.getX() + padding, area.getY() + padding, area.getX() + 16 + padding, area.getY() + 16 + padding, BLACKLIST_COLOR);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	}
}
