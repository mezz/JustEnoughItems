package mezz.jei.api.recipe.category.extensions.vanilla.crafting;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Implement this interface instead of just {@link IRecipeCategoryExtension}
 * to have your recipe extension work as part of {@link RecipeTypes#CRAFTING} recipe.
 *
 * For shaped recipes, override {@link #getWidth(RecipeHolder)} and {@link #getHeight(RecipeHolder)}.
 *
 * Register this extension by getting the extendable crafting category from:
 * {@link IVanillaCategoryExtensionRegistration#getCraftingCategory()}
 * and then registering it with {@link IExtendableCraftingRecipeCategory#addExtension(Class, ICraftingCategoryExtension)}.
 *
 * @apiNote Since 16.0.0, extensions have the recipe passed to them in each method,
 * so they can be singleton instances instead of creating many of them to wrap recipes.
 */
public interface ICraftingCategoryExtension<R extends CraftingRecipe> extends IRecipeCategoryExtension<RecipeHolder<R>> {
	/**
	 * Override the default {@link IRecipeCategory} behavior.
	 *
	 * @see IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)
	 *
	 * @since 16.0.0
	 */
	default void setRecipe(RecipeHolder<R> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		setRecipe(builder, craftingGridHelper, focuses);
	}

	/**
	 * Return the registry name of the recipe here.
	 * With advanced tooltips on, this will show on the output item's tooltip.
	 *
	 * This will also show the modId when the recipe modId and output item modId do not match.
	 * This lets the player know where the recipe came from.
	 *
	 * @return the registry name of the recipe
	 * @since 16.0.0
	 */
	default Optional<ResourceLocation> getRegistryName(RecipeHolder<R> recipeHolder) {
		return Optional.ofNullable(getRegistryName())
			.or(() -> Optional.of(recipeHolder.id()));
	}

	/**
	 * @return the width of a shaped recipe, or 0 for a shapeless recipe
	 * @since 16.0.0
	 */
	default int getWidth(RecipeHolder<R> recipeHolder) {
		return getWidth();
	}

	/**
	 * @return the height of a shaped recipe, or 0 for a shapeless recipe
	 * @since 16.0.0
	 */
	default int getHeight(RecipeHolder<R> recipeHolder) {
		return getHeight();
	}

	/**
	 * Override the default {@link IRecipeCategory} behavior.
	 *
	 * @see IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)
	 * @deprecated use {@link #setRecipe(RecipeHolder, IRecipeLayoutBuilder, ICraftingGridHelper, IFocusGroup)}
	 */
	@Deprecated(since = "16.0.0", forRemoval = true)
	default void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {

	}

	/**
	 * Return the registry name of the recipe here.
	 * With advanced tooltips on, this will show on the output item's tooltip.
	 *
	 * This will also show the modId when the recipe modId and output item modId do not match.
	 * This lets the player know where the recipe came from.
	 *
	 * @return the registry name of the recipe, or null if there is none
	 * @deprecated use {@link #getRegistryName(RecipeHolder)}
	 */
	@Deprecated(since = "16.0.0", forRemoval = true)
	@Nullable
	default ResourceLocation getRegistryName() {
		return null;
	}

	/**
	 * @return the width of a shaped recipe, or 0 for a shapeless recipe
	 * @since 9.3.0
	 * @deprecated use {@link #getWidth(RecipeHolder)}
	 */
	@Deprecated(since = "16.0.0", forRemoval = true)
	default int getWidth() {
		return 0;
	}

	/**
	 * @return the height of a shaped recipe, or 0 for a shapeless recipe
	 * @since 9.3.0
	 * @deprecated use {@link #getHeight(RecipeHolder)}
	 */
	@Deprecated(since = "16.0.0", forRemoval = true)
	default int getHeight() {
		return 0;
	}
}
