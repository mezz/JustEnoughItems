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

import java.util.Set;

public class ElementRenderer<T> implements IElementRenderer<T> {
	private static final int BLACKLIST_COLOR = 0xDDFF0000;
	private static final int WILDCARD_BLACKLIST_COLOR = 0xDDFFA500;

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
		SafeIngredientUtil.render(guiGraphics, ingredientRenderer, typedIngredient, xPosition, yPosition);
		element.renderExtras(guiGraphics, xPosition, yPosition);
	}

	private static <T> void renderEditMode(GuiGraphics guiGraphics, ImmutableRect2i area, int padding, ITypedIngredient<T> typedIngredient) {
		IEditModeConfig editModeConfig = Internal.getJeiRuntime().getEditModeConfig();
		Set<IEditModeConfig.HideMode> hideModes = editModeConfig.getIngredientHiddenUsingConfigFile(typedIngredient);
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

	@Override
	public void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, IngredientGridTooltipHelper tooltipHelper, IElement<T> element) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();

		ITypedIngredient<T> typedIngredient = element.getTypedIngredient();
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);

		JeiTooltip tooltip = new JeiTooltip();
		element.getTooltip(tooltip, tooltipHelper, ingredientRenderer, ingredientHelper);
		tooltip.draw(guiGraphics, mouseX, mouseY, typedIngredient, ingredientRenderer, ingredientManager);
	}
}
