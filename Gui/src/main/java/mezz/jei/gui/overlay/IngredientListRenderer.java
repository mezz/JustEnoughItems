package mezz.jei.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientInfo;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.util.IImmutableRect2i;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.gui.GuiComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class IngredientListRenderer {
	private static final int BLACKLIST_COLOR = 0xFFFF0000;

	private final List<IngredientListSlot> slots = new ArrayList<>();
	private final ElementRenderersByType renderers = new ElementRenderersByType();
	private final IEditModeConfig editModeConfig;
	private final IWorldConfig worldConfig;
	private final IRegisteredIngredients registeredIngredients;

	private int blocked = 0;

	public IngredientListRenderer(IEditModeConfig editModeConfig, IWorldConfig worldConfig, IRegisteredIngredients registeredIngredients) {
		this.editModeConfig = editModeConfig;
		this.worldConfig = worldConfig;
		this.registeredIngredients = registeredIngredients;
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

	public void render(PoseStack poseStack) {
		for (IIngredientType<?> ingredientType : renderers.getTypes()) {
			renderIngredientType(poseStack, ingredientType);
		}
	}

	private <T> void renderIngredientType(PoseStack poseStack, IIngredientType<T> ingredientType) {
		Collection<ElementRenderer<T>> slots = renderers.get(ingredientType);
		IIngredientInfo<T> ingredientInfo = registeredIngredients.getIngredientInfo(ingredientType);
		IIngredientRenderer<T> ingredientRenderer = ingredientInfo.getIngredientRenderer();
		for (ElementRenderer<T> slot : slots) {
			renderIngredient(poseStack, slot, ingredientRenderer);
		}
	}

	private <T> void renderIngredient(PoseStack poseStack, ElementRenderer<T> slot, IIngredientRenderer<T> ingredientRenderer) {
		ITypedIngredient<T> typedIngredient = slot.getTypedIngredient();
		IImmutableRect2i area = slot.getElementArea();
		int slotPadding = slot.getPadding();
		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(poseStack, area, slotPadding, editModeConfig, typedIngredient);
			RenderSystem.enableBlend();
		}

		T ingredient = typedIngredient.getIngredient();
		try {
			int xPosition = area.getX() + slotPadding;
			int yPosition = area.getY() + slotPadding;
			poseStack.pushPose();
			{
				poseStack.translate(xPosition, yPosition, 0);
				ingredientRenderer.render(poseStack, ingredient);
			}
			poseStack.popPose();
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, ingredient, registeredIngredients);
		}
	}

	private static <T> void renderEditMode(PoseStack poseStack, IImmutableRect2i area, int padding, IEditModeConfig editModeConfig, ITypedIngredient<T> typedIngredient) {
		if (editModeConfig.isIngredientHiddenUsingConfigFile(typedIngredient)) {
			GuiComponent.fill(poseStack, area.getX() + padding, area.getY() + padding, area.getX() + 16 + padding, area.getY() + 16 + padding, BLACKLIST_COLOR);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	}
}
