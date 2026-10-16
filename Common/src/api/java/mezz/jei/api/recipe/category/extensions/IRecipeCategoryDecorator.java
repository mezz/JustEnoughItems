package mezz.jei.api.recipe.category.extensions;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IAdvancedRegistration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * The {@link IRecipeCategoryDecorator} allows further customization of recipe categories.
 * It can customize source recipe IDs, drawing, and tooltips, even for other mods' recipes.
 * <p>
 * Register it with {@link IAdvancedRegistration#addRecipeCategoryDecorator(IRecipeType, IRecipeCategoryDecorator)}.
 *
 * @since 15.1.0
 */
public interface IRecipeCategoryDecorator<T> {
	/**
	 * Modify the underlying recipe IDs shown in tooltips and copied from a recipe layout.
	 * See {@link IRecipeCategory#getSourceRecipeIds(Object)} for the identifier contract.
	 * <p>
	 * Decorators receive the same mutable list in registration order when a layout is created,
	 * starting with the category's source IDs. IDs may be added, removed, or cleared.
	 * JEI copies the completed list for the layout after all decorators have run.
	 * This does not change the display identifier or bookmarks.
	 *
	 * @param sourceRecipeIds the mutable source IDs, including changes from previous decorators
	 * @since 31.14.0
	 */
	default void decorateSourceRecipeIds(List<Identifier> sourceRecipeIds, T recipe, IRecipeCategory<T> recipeCategory) {

	}

	/**
	 * Draw extras or additional info about the recipe after the {@link IRecipeCategory} that
	 * this decorator is registered to has drawn.
	 * Tooltips are handled by {@link #decorateTooltips(ITooltipBuilder, Object, IRecipeCategory, IRecipeSlotsView, double, double)}.
	 *
	 * @param recipe          the current recipe being drawn.
	 * @param recipeCategory  the recipe category of the recipe.
	 * @param recipeSlotsView a view of the current recipe slots being drawn.
	 * @param guiGraphics     the current {@link GuiGraphicsExtractor} for rendering.
	 * @param mouseX          the X position of the mouse, relative to the recipe.
	 * @param mouseY          the Y position of the mouse, relative to the recipe.
	 *
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @see IRecipeSlotsView for information about the ingredients that are currently being drawn.
	 *
	 * @since 15.1.0
	 */
	default void draw(T recipe, IRecipeCategory<T> recipeCategory, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {

	}

	/**
	 * Allows adding to existing tooltips added by the {@link IRecipeCategory} that
	 * this decorator is registered to.
	 *
	 * @param tooltip         the existing tooltip strings.
	 * @param recipe          the current recipe being drawn.
	 * @param recipeCategory  the recipe category of the recipe.
	 * @param recipeSlotsView a view of the current recipe slots being drawn.
	 * @param mouseX          the X position of the mouse, relative to the recipe.
	 * @param mouseY          the Y position of the mouse, relative to the recipe.
	 *
	 * @since 19.5.4
	 */
	default void decorateTooltips(ITooltipBuilder tooltip, T recipe, IRecipeCategory<T> recipeCategory, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

	}
}
