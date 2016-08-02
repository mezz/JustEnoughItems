package mezz.jei.api.recipe;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Handles setting up the GUI for its recipe category in {@link #setRecipe(IRecipeLayout, IRecipeWrapper)}.
 * Also draws elements that are common to all recipes in the category like the background.
 *
 * @see BlankRecipeCategory
 */
public interface IRecipeCategory<T extends IRecipeWrapper> {

	/**
	 * Returns a unique ID for this recipe category.
	 * Referenced from recipes to identify which recipe category they belong to.
	 *
	 * @see IRecipeHandler#getRecipeCategoryUid(Object)
	 * @see VanillaRecipeCategoryUid
	 */
	@Nonnull
	String getUid();

	/**
	 * Returns the localized name for this recipe type.
	 * Drawn at the top of the recipe GUI pages for this category.
	 */
	@Nonnull
	String getTitle();

	/**
	 * Returns the drawable background for a single recipe in this category.
	 */
	@Nonnull
	IDrawable getBackground();

	/**
	 * Draw any extra elements that might be necessary, icons or extra slots.
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 */
	void drawExtras(@Nonnull Minecraft minecraft);

	/**
	 * Draw any animations like progress bars or flashy effects.
	 * Essentially the same as {@link #drawExtras(Minecraft)} but these can be disabled in the config.
	 * @see IDrawableAnimated for a simple class for drawing animated things.
	 * @see IGuiHelper for useful functions.
	 */
	void drawAnimations(@Nonnull Minecraft minecraft);

	/**
	 * Set the {@link IRecipeLayout} properties from the {@link IRecipeWrapper}.
	 */
	void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull T recipeWrapper);

}
