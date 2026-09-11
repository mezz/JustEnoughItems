package mezz.jei.api.ingredients.rendering;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * A single ingredient to render in a batch render operation.
 * The x and y positions are the top-left corner of the ingredient in scaled GUI screen coordinates,
 * where {@code (0, 0)} is the top-left corner of the Minecraft window.
 * JEI does not translate the graphics pose before calling
 * {@link IIngredientRenderer#renderBatch(GuiGraphicsExtractor, List)}.
 *
 * @see IIngredientRenderer#renderBatch(GuiGraphicsExtractor, List)
 *
 * @since 19.14.0
 */
public record BatchRenderElement<T>(T ingredient, int x, int y) {}
