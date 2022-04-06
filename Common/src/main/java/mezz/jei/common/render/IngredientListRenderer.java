package mezz.jei.common.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.config.IEditModeConfig;
import mezz.jei.common.ingredients.IngredientInfo;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableRect2i;
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
	private final RegisteredIngredients registeredIngredients;

	private int blocked = 0;

	public IngredientListRenderer(IEditModeConfig editModeConfig, IWorldConfig worldConfig, RegisteredIngredients registeredIngredients) {
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
		IngredientInfo<T> ingredientInfo = registeredIngredients.getIngredientInfo(ingredientType);
		IIngredientRenderer<T> ingredientRenderer = ingredientInfo.getIngredientRenderer();
		IIngredientHelper<T> ingredientHelper = ingredientInfo.getIngredientHelper();
		for (ElementRenderer<T> slot : slots) {
			renderIngredient(poseStack, slot, ingredientRenderer, ingredientHelper);
		}
	}

	private <T> void renderIngredient(PoseStack poseStack, ElementRenderer<T> slot, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper) {
		ITypedIngredient<T> typedIngredient = slot.getTypedIngredient();
		ImmutableRect2i area = slot.getArea();
		int slotPadding = slot.getPadding();
		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(poseStack, area, slotPadding, editModeConfig, typedIngredient, ingredientHelper);
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

	private static <T> void renderEditMode(PoseStack poseStack, ImmutableRect2i area, int padding, IEditModeConfig editModeConfig, ITypedIngredient<T> typedIngredient, IIngredientHelper<T> ingredientHelper) {
		if (editModeConfig.isIngredientOnConfigBlacklist(typedIngredient, ingredientHelper)) {
			GuiComponent.fill(poseStack, area.getX() + padding, area.getY() + padding, area.getX() + 16 + padding, area.getY() + 16 + padding, BLACKLIST_COLOR);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	}
}
