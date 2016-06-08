package mezz.jei.api.recipe;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe)
 * and handles setting up the GUI for its recipe category.
 */
public interface IRecipeCategory<T extends IRecipeWrapper> {

	/**
	 * Returns a unique ID for this recipe category.
	 * Referenced from recipes to identify which recipe category they belong to.
	 */
	@Nonnull
	String getUid();

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
	 * Optionally draw anything else that might be necessary, icons or extra slots.
	 */
	void drawExtras(@Nonnull Minecraft minecraft);

	/**
	 * Optionally draw animations like progress bars. These animations can be disabled in the config.
	 */
	void drawAnimations(@Nonnull Minecraft minecraft);

	/**
	 * Set the IRecipeLayout properties from the IRecipeWrapper.
	 */
	void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull T recipeWrapper);

}
