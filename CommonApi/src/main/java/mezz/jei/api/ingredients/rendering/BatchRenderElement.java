package mezz.jei.api.ingredients.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;

import java.util.List;

/**
 * A single ingredient to render in a batch render operation.
 *
 * @see IIngredientRenderer#renderBatch(PoseStack, List)
 *
 * @since 11.7.0
 */
public record BatchRenderElement<T>(T ingredient, int x, int y) {}
