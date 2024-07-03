package mezz.jei.gui.overlay.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.gui.TooltipRenderer;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ElementRenderer<T> implements IElementRenderer<T> {
	private static final int BLACKLIST_COLOR = 0xFFFF0000;

	private final IClientToggleState toggleState;
	private final IEditModeConfig editModeConfig;
	private final IIngredientManager ingredientManager;
	private final IIngredientRenderer<T> ingredientRenderer;

	public ElementRenderer(IIngredientType<T> ingredientType, IClientToggleState toggleState, IEditModeConfig editModeConfig, IIngredientManager ingredientManager) {
		this.toggleState = toggleState;
		this.editModeConfig = editModeConfig;
		this.ingredientManager = ingredientManager;
		this.ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
	}

	@Override
	public void render(GuiGraphics guiGraphics, IElement<T> element, ImmutableRect2i area, int padding) {
		ITypedIngredient<T> typedIngredient = element.getTypedIngredient();
		if (toggleState.isEditModeEnabled()) {
			renderEditMode(guiGraphics, area, padding, editModeConfig, typedIngredient);
			RenderSystem.enableBlend();
		}

		int xPosition = area.getX() + padding;
		int yPosition = area.getY() + padding;
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(xPosition, yPosition, 0);
			SafeIngredientUtil.render(ingredientManager, ingredientRenderer, guiGraphics, typedIngredient);
			element.renderExtras(guiGraphics);
		}
		poseStack.popPose();
	}

	private static <T> void renderEditMode(GuiGraphics guiGraphics, ImmutableRect2i area, int padding, IEditModeConfig editModeConfig, ITypedIngredient<T> typedIngredient) {
		if (editModeConfig.isIngredientHiddenUsingConfigFile(typedIngredient)) {
			guiGraphics.fill(
				RenderType.guiOverlay(),
				area.getX() + padding,
				area.getY() + padding,
				area.getX() + 16 + padding,
				area.getY() + 16 + padding,
				BLACKLIST_COLOR
			);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	}

	@Override
	public void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, IngredientGridTooltipHelper tooltipHelper, IElement<T> element) {
		ITypedIngredient<T> typedIngredient = element.getTypedIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		List<Component> tooltip = element.getTooltip(tooltipHelper, ingredientRenderer, ingredientHelper);

		TooltipRenderer.drawHoveringText(guiGraphics, tooltip, mouseX, mouseY, typedIngredient, ingredientRenderer, ingredientManager);
	}
}
