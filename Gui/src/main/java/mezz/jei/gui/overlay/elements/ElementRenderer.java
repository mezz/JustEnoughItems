package mezz.jei.gui.overlay.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

public class ElementRenderer<T> implements IElementRenderer<T> {
	private static final int BLACKLIST_COLOR = 0xFFFF0000;

	private final IIngredientRenderer<T> ingredientRenderer;

	public ElementRenderer(IIngredientType<T> ingredientType) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		this.ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
	}

	@Override
	public void render(GuiGraphics guiGraphics, IElement<T> element, ImmutableRect2i area, int padding) {
		ITypedIngredient<T> typedIngredient = element.getTypedIngredient();
		if (Internal.getClientToggleState().isEditModeEnabled()) {
			renderEditMode(guiGraphics, area, padding, typedIngredient);
			RenderSystem.enableBlend();
		}

		int xPosition = area.getX() + padding;
		int yPosition = area.getY() + padding;
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(xPosition, yPosition, 0);
			SafeIngredientUtil.render(guiGraphics, ingredientRenderer, typedIngredient);
			element.renderExtras(guiGraphics);
		}
		poseStack.popPose();
	}

	private static <T> void renderEditMode(GuiGraphics guiGraphics, ImmutableRect2i area, int padding, ITypedIngredient<T> typedIngredient) {
		IEditModeConfig editModeConfig = Internal.getJeiRuntime().getEditModeConfig();
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
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();

		ITypedIngredient<T> typedIngredient = element.getTypedIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		JeiTooltip tooltip = element.getTooltip(tooltipHelper, ingredientRenderer, ingredientHelper);
		tooltip.draw(guiGraphics, mouseX, mouseY, typedIngredient, ingredientRenderer, ingredientManager);
	}
}
