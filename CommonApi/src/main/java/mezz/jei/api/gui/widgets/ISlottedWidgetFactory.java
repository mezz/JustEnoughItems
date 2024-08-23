package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.List;

/**
 * A widget factory that creates a slotted widget instance that handles specific slots.
 *
 * Assign it slots with {@link IRecipeLayoutBuilder#addSlotToWidget(RecipeIngredientRole, ISlottedWidgetFactory)}
 * and then JEI will call {@link #createWidgetForSlots} after all the slots are built.
 *
 * @since 19.7.0
 */
@FunctionalInterface
public interface ISlottedWidgetFactory<R> {
	/**
	 * Create a widget instance for the given recipe and slots.
	 * This will be called when the slots are built and ready.
	 *
	 * @param recipe the recipe to be used by
	 * @param slots created and assigned to this widget factory with
	 * 			{@link IRecipeLayoutBuilder#addSlotToWidget(RecipeIngredientRole, ISlottedWidgetFactory)}
	 *
	 * @since 19.7.0
	 */
	void createWidgetForSlots(IRecipeExtrasBuilder builder, R recipe, List<IRecipeSlotDrawable> slots);
}
