package mezz.jei.api.ingredients;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import java.util.Collection;
import java.util.List;

/**
 * Renders a type of ingredient in JEI's item list and recipes.
 *
 * If you have a new type of ingredient to add to JEI,
 * you will have to implement this to create a default renderer for
 * {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 */
public interface IIngredientRenderer<T> {
	/**
	 * Renders an ingredient at a specific location.
	 *
	 * @param stack  The current {@link PoseStack} for rendering the ingredient.
	 * @param ingredient the ingredient to render.
	 *
	 * @since 9.3.0
	 */
	void render(PoseStack stack, T ingredient);

	/**
	 * Renders an ingredient at a specific location.
	 *
	 * @param poseStack  The current {@link PoseStack} for rendering the ingredient.
	 * @param ingredient the ingredient to render.
	 * @param posX       the x offset for rendering this ingredient
	 * @param posY       the y offset for rendering this ingredient
	 *
	 * @since 11.7.0
	 */
	default void render(PoseStack poseStack, T ingredient, int posX, int posY) {
		poseStack.pushPose();
		{
			poseStack.translate(posX, posY, 0);
			render(poseStack, ingredient);
		}
		poseStack.popPose();
	}

	/**
	 * Render a batch of ingredients.
	 * Implementing this is not necessary, but can be used to optimize rendering many ingredients at once.
	 *
	 * @since 11.7.0
	 */
	default void renderBatch(PoseStack poseStack, List<BatchRenderElement<T>> elements) {
		for (BatchRenderElement<T> element : elements) {
			render(poseStack, element.ingredient(), element.x(), element.y());
		}
	}

	/**
	 * Get the tooltip text for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param ingredient  The ingredient to get the tooltip for.
	 * @param tooltipFlag Whether to show advanced information on item tooltips, toggled by F3+H
	 * @return The tooltip text for the ingredient.
	 */
	List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag);

	/**
	 * Get the tooltip font renderer for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param minecraft  The minecraft instance.
	 * @param ingredient The ingredient to get the tooltip for.
	 * @return The font renderer for the ingredient.
	 */
	default Font getFontRenderer(Minecraft minecraft, T ingredient) {
		return minecraft.font;
	}

	/**
	 * Get the width of the ingredient drawn on screen by this renderer.
	 *
	 * @since 9.3.0
	 */
	default int getWidth() {
		return 16;
	}

	/**
	 * Get the height of the ingredient drawn on screen by this renderer.
	 *
	 * @since 9.3.0
	 */
	default int getHeight() {
		return 16;
	}
}
