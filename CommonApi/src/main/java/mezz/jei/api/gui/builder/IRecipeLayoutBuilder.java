package mezz.jei.api.gui.builder;

import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;

/**
 * A builder passed to plugins that implement
 * {@link IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
 *
 * Plugins add slot locations and ingredients to JEI using this interface.
 *
 * @since 9.3.0
 */
public interface IRecipeLayoutBuilder {
	/**
	 * Convenience function to add an input slot that will be drawn at the given position relative to the recipe layout.
	 *
	 * @param x relative x position of the slot on the recipe layout.
	 * @param y relative y position of the slot on the recipe layout.
	 * @return a {@link IRecipeSlotBuilder} that has further methods for adding ingredients, etc.
	 *
	 * @since 15.20.0
	 */
	default IRecipeSlotBuilder addInputSlot(int x, int y) {
		return addSlot(RecipeIngredientRole.INPUT)
			.setPosition(x, y);
	}

	/**
	 * Convenience function to add an input slot.
	 * Set its position using {@link IPlaceable} methods on {@link IRecipeSlotBuilder}.
	 *
	 * @return a {@link IRecipeSlotBuilder} that has further methods for adding ingredients, setting position, etc.
	 *
	 * @since 15.20.3
	 */
	default IRecipeSlotBuilder addInputSlot() {
		return addSlot(RecipeIngredientRole.INPUT);
	}

	/**
	 * Convenience function to add an output slot that will be drawn at the given position relative to the recipe layout.
	 *
	 * @param x relative x position of the slot on the recipe layout.
	 * @param y relative y position of the slot on the recipe layout.
	 * @return a {@link IRecipeSlotBuilder} that has further methods for adding ingredients, etc.
	 *
	 * @since 15.20.0
	 */
	default IRecipeSlotBuilder addOutputSlot(int x, int y) {
		return addSlot(RecipeIngredientRole.OUTPUT)
			.setPosition(x, y);
	}

	/**
	 * Convenience function to add an output slot.
	 * Set its position using {@link IPlaceable} methods on {@link IRecipeSlotBuilder}.
	 *
	 * @return a {@link IRecipeSlotBuilder} that has further methods for adding ingredients, setting position, etc.
	 *
	 * @since 15.20.3
	 */
	default IRecipeSlotBuilder addOutputSlot() {
		return addSlot(RecipeIngredientRole.OUTPUT);
	}

	/**
	 * Add a slot that will be drawn at the given position relative to the recipe layout.
	 *
	 * @param role the {@link RecipeIngredientRole} of this slot (for lookups).
	 * @param x relative x position of the slot on the recipe layout.
	 * @param y relative y position of the slot on the recipe layout.
	 * @return a {@link IRecipeSlotBuilder} that has further methods for adding ingredients, etc.
	 *
	 * @since 9.3.0
	 */
	default IRecipeSlotBuilder addSlot(RecipeIngredientRole role, int x, int y) {
		return addSlot(role)
			.setPosition(x, y);
	}

	/**
	 * Add a slot and set its position using {@link IPlaceable} methods on {@link IRecipeSlotBuilder}.
	 *
	 * @param role the {@link RecipeIngredientRole} of this slot (for lookups).
	 * @return a {@link IRecipeSlotBuilder} that has further methods for adding ingredients, etc.
	 *
	 * @since 15.20.1
	 */
	IRecipeSlotBuilder addSlot(RecipeIngredientRole role);

	/**
	 * Assign this slot to a {@link mezz.jei.api.gui.widgets.ISlottedWidgetFactory},
	 * so that the widget can manage this slot instead the recipe category.
	 *
	 * @param widgetFactory the {@link mezz.jei.api.gui.widgets.ISlottedWidgetFactory} to assign this slot to.
	 *
	 * @since 15.10.0
	 * @deprecated there are easier ways to create slotted widgets now. Use {@link IRecipeExtrasBuilder#addSlottedWidget}.
	 */
	@Deprecated(since = "15.20.3", forRemoval = true)
	@SuppressWarnings("removal")
	IRecipeSlotBuilder addSlotToWidget(RecipeIngredientRole role, mezz.jei.api.gui.widgets.ISlottedWidgetFactory<?> widgetFactory);

	/**
	 * Add ingredients that are important for recipe lookup, but are not displayed on the recipe layout.
	 * @param recipeIngredientRole the {@link RecipeIngredientRole} of these ingredients (for lookups).
	 * @return an {@link IIngredientAcceptor} to add ingredients to.
	 *
	 * @since 9.3.0
	 */
	IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole recipeIngredientRole);

	/**
	 * Moves the recipe transfer button's position relative to the recipe layout.
	 * By default, the recipe transfer button is at the bottom, to the right of the recipe.
	 * If it doesn't fit there, you can use this to move it when you init the recipe layout.
	 *
	 * @since 9.3.0
	 */
	void moveRecipeTransferButton(int posX, int posY);

	/**
	 * Adds a shapeless icon to the top right of the recipe,
	 * that shows a tooltip saying "shapeless" when hovered over.
	 *
	 * @since 9.3.0
	 */
	void setShapeless();

	/**
	 * Adds a shapeless icon to the specified position relative to the recipe layout,
	 * that shows a tooltip saying "shapeless" when hovered over.
	 *
	 * @since 9.3.0
	 */
	void setShapeless(int posX, int posY);

	/**
	 * Link slots together so that if one slot matches the current focus, the others will be limited too.
	 * This can only be set on slots that contains the same number of ingredients.
	 *
	 * For example:
	 * Consider a recipe that has an input slot with every plank type
	 * and an output slot with stairs for each plank type.
	 *
	 * The number of inputs and outputs are the same,
	 * and when the full recipe is displayed it rotates through all the different pairs of planks and their stairs.
	 *
	 * If the slots are focus-linked, when the player focuses on acacia planks,
	 * the linked output slot will only display acacia stairs.
	 *
	 * @since 9.5.1
	 */
	void createFocusLink(IIngredientAcceptor<?>... slots);
}
