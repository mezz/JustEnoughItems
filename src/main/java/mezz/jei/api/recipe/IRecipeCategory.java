package mezz.jei.api.recipe;

import javax.annotation.Nonnull;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe)
 * and handles setting up the GUI for its recipe category.
 */
public interface IRecipeCategory {

	/**
	 * Returns a unique ID for this recipe category.
	 * Referenced from recipes to identify which recipe category they belong to.
	 */
	@Nonnull
	IRecipeCategoryUid getUid();

	/**
	 * Returns the localized name for this recipe type.
	 * Drawn at the top of the recipe GUI pages for this category.
	 * Called every frame, so make sure to store it in a field.
	 */
	@Nonnull
	String getTitle();

	/**
	 * Returns the drawable background for a single recipe in this category.
	 * Called multiple times per frame, so make sure to store it in a field.
	 */
	@Nonnull
	IDrawable getBackground();

	/**
	 * Initialize the IGuiItemStackGroup and IGuiFluidStackGroup with this recipe's layout.
	 */
	void init(@Nonnull IGuiItemStackGroup guiItemStacks, @Nonnull IGuiFluidStackGroup guiFluidTanks);

	/**
	 * Set the IGuiItemStackGroup and IGuiFluidStackGroup properties from the RecipeWrapper.
	 */
	void setRecipe(@Nonnull IGuiItemStackGroup guiItemStacks, @Nonnull IGuiFluidStackGroup guiFluidTanks, @Nonnull IRecipeWrapper recipeWrapper);

}
