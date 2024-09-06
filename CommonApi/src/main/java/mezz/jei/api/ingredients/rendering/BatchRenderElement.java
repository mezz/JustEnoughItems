package mezz.jei.api.ingredients.rendering;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/**
 * A single ingredient to render in a batch render operation.
 *
 * @see IIngredientRenderer#renderBatch(GuiGraphics, List)
 *
 * @since 15.16.0
 */
public record BatchRenderElement<T>(T ingredient, int x, int y) {}
