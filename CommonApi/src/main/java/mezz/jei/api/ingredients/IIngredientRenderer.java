package mezz.jei.api.ingredients;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Renders a type of ingredient in JEI's item list and recipes.
 *
 * If you have a new type of ingredient to add to JEI,
 * you will have to implement this to create a default renderer for
 * {@link IModIngredientRegistration#register}
 */
public interface IIngredientRenderer<T> {
	/**
	 * Renders an ingredient.
	 *
	 * @param guiGraphics The current {@link GuiGraphics} for rendering the ingredient.
	 * @param ingredient the ingredient to render.
	 *
	 * @since 9.3.0
	 */
	void render(GuiGraphics guiGraphics, T ingredient);

	/**
	 * Renders an ingredient at a specific location.
	 *
	 * @param guiGraphics The current {@link GuiGraphics} for rendering the ingredient.
	 * @param ingredient the ingredient to render.
	 * @param posX       the x offset for rendering this ingredient
	 * @param posY       the y offset for rendering this ingredient
	 *
	 * @since 19.5.5
	 */
	default void render(GuiGraphics guiGraphics, T ingredient, int posX, int posY) {
		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(posX, posY);
			render(guiGraphics, ingredient);
		}
		poseStack.popMatrix();
	}

	/**
	 * Render a batch of ingredients.
	 * Implementing this is not necessary, but can be used to optimize rendering many ingredients at once.
	 *
	 * @since 19.14.0
	 */
	default void renderBatch(GuiGraphics guiGraphics, List<BatchRenderElement<T>> elements) {
		for (BatchRenderElement<T> element : elements) {
			render(guiGraphics, element.ingredient(), element.x(), element.y());
		}
	}

	/**
	 * Get the tooltip text for this ingredient. JEI searches tooltips based on this.
	 *
	 * @param ingredient  The ingredient to get the tooltip for.
	 * @param tooltipFlag Whether to show advanced information on item tooltips, toggled by F3+H
	 * @return The tooltip text for the ingredient.
	 * @deprecated use {@link #getTooltip(Object, Item.TooltipContext, Player, TooltipFlag)}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated(since = "27.32.0", forRemoval = true)
	List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag);

	/**
	 * Get the tooltip text for this ingredient. JEI searches tooltips based on this.
	 *
	 * @param ingredient     The ingredient to get the tooltip for.
	 * @param tooltipContext The context for building the tooltip.
	 * @param player         The current player, if available.
	 * @param tooltipFlag    Whether to show advanced information on item tooltips, toggled by F3+H
	 * @return The tooltip text for the ingredient.
	 *
	 * @implNote Implementations should override this method. The default implementation delegates to the deprecated overload
	 * to support renderers compiled against older versions of the API.
	 *
	 * @since 27.32.0
	 */
	default List<Component> getTooltip(T ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		return getTooltip(ingredient, tooltipFlag);
	}

	/**
	 * Get a rich tooltip for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param tooltip     A tooltip builder for building rich tooltips.
	 * @param ingredient  The ingredient to get the tooltip for.
	 * @param tooltipFlag Whether to show advanced information on item tooltips, toggled by F3+H
	 *
	 * @since 19.5.4
	 * @deprecated use {@link #getTooltip(ITooltipBuilder, Object, Item.TooltipContext, Player, TooltipFlag)}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated(since = "27.32.0", forRemoval = true)
	default void getTooltip(ITooltipBuilder tooltip, T ingredient, TooltipFlag tooltipFlag) {
		List<Component> components = getTooltip(ingredient, tooltipFlag);
		tooltip.addAll(components);
	}

	/**
	 * Get a rich tooltip for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param tooltip        A tooltip builder for building rich tooltips.
	 * @param ingredient     The ingredient to get the tooltip for.
	 * @param tooltipContext The context for building the tooltip.
	 * @param player         The current player, if available.
	 * @param tooltipFlag    Whether to show advanced information on item tooltips, toggled by F3+H
	 *
	 * @since 27.32.0
	 */
	default void getTooltip(ITooltipBuilder tooltip, T ingredient, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		List<Component> components = getTooltip(ingredient, tooltipContext, player, tooltipFlag);
		tooltip.addAll(components);
	}

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
