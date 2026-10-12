package mezz.jei.api.recipe.category.extensions;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * An extension to a recipe category with methods that allow JEI to make sense of it.
 * Plugins implement these for recipe categories that support it, for each type of recipe they have.
 *
 * @apiNote Since 16.0.0, extensions have the recipe passed to them in each method,
 * so they can be singleton instances instead of creating many of them to wrap recipes.
 */
public interface IRecipeCategoryExtension<T> {
	/**
	 * Draw additional info about the recipe.
	 * Use the mouse position for things like button highlights.
	 * Tooltips are handled by {@link #getTooltip(ITooltipBuilder, Object, double, double)}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @since 16.0.0
	 */
	default void drawInfo(T recipe, int recipeWidth, int recipeHeight, GuiGraphics guiGraphics, double mouseX, double mouseY) {

	}

	/**
	 * Get the tooltip for whatever is under the mouse.
	 * ItemStack and fluid tooltips are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IRecipeSlotBuilder#addRichTooltipCallback(IRecipeSlotRichTooltipCallback)}
	 * To add tooltips for a recipe category, see {@link IRecipeCategory#getTooltip}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @since 19.5.4
	 */
	default void getTooltip(ITooltipBuilder tooltip, T recipe, double mouseX, double mouseY) {

	}

	/**
	 * Sets the extras for the recipe category, like input handlers and recipe widgets.
	 *
	 * Recipe Widgets persist as long as a recipe layout is on screen,
	 * so they can be used for caching and displaying recipe-specific
	 * information more easily than from the recipe category directly.
	 *
	 * @since 19.6.0
	 */
	default void createRecipeExtras(T recipe, IRecipeExtrasBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {

	}

	/**
	 * @return true if the given recipe can be handled by this category extension.
	 * @since 16.0.0
	 */
	default boolean isHandled(T recipe) {
		return true;
	}

	/**
	 * Override the underlying recipe identifiers shown in advanced tooltips and copied as recipe IDs.
	 * See {@link IRecipeCategory#getSourceRecipeIds(Object)} for the identifier contract.
	 *
	 * @return source identifiers, an empty list for no underlying recipe, or null to use the category default
	 * @since 27.48.0
	 */
	default @Nullable List<Identifier> getSourceRecipeIds(T recipe) {
		return null;
	}
}
