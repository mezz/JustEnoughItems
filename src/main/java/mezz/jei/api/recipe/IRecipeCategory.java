package mezz.jei.api.recipe;

import javax.annotation.Nonnull;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStacks;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe)
 * and handles setting up the GUI for its recipe category.
 */
public interface IRecipeCategory {

	/**
	 * Returns the localized name for this recipe type.
	 * Drawn at the top of the recipe GUI pages for this category.
	 * Called every frame, so make sure to store it in a field.
	 */
	@Nonnull String getCategoryTitle();

	/**
	 * Returns the drawable background for a single recipe in this category.
	 * Called multiple times per frame, so make sure to store it in a field.
	 */
	@Nonnull IDrawable getBackground();

	/**
	 * Initialize the IGuiItemStacks with this recipe's layout.
	 */
	public void init(@Nonnull IGuiItemStacks guiItemStacks);

	/**
	 * Set the IGuiItemStacks from the RecipeWrapper.
	 */
	public void setRecipe(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull IRecipeWrapper recipeWrapper);

}
