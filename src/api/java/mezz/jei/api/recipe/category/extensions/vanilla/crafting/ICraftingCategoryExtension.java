package mezz.jei.api.recipe.category.extensions.vanilla.crafting;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Size2i;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * Implement this interface instead of just {@link IRecipeCategoryExtension} to have your recipe extension work as part of the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category as a shapeless recipe.
 *
 * For shaped recipes, override {@link #getWidth()} and {@link #getHeight()}.
 *
 * Register this extension by getting the extendable crafting category from:
 * {@link IVanillaCategoryExtensionRegistration#getCraftingCategory()}
 * and then registering it with {@link IExtendableRecipeCategory#addCategoryExtension}.
 */
public interface ICraftingCategoryExtension extends IRecipeCategoryExtension {
	/**
	 * Override the default {@link IRecipeCategory#setRecipe} behavior.
	 *
	 * @see IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, List)
	 *
	 * @since 9.3.0
	 */
	default void setRecipe(
		IRecipeLayoutBuilder recipeLayoutBuilder,
		ICraftingGridHelper craftingGridHelper,
		List<? extends IFocus<?>> focuses
	) {

	}

	/**
	 * Return the registry name of the recipe here.
	 * With advanced tooltips on, this will show on the output item's tooltip.
	 *
	 * This will also show the modId when the recipe modId and output item modId do not match.
	 * This lets the player know where the recipe came from.
	 *
	 * @return the registry name of the recipe, or null if there is none
	 */
	@Nullable
	default ResourceLocation getRegistryName() {
		return null;
	}

	/**
	 * @return the width of a shaped recipe, or 0 for a shapeless recipe
	 * @since 9.3.0
	 */
	default int getWidth() {
		// if not implemented, this calls the old getSize function for backward compatibility
		Size2i size = getSize();
		if (size == null) {
			return 0;
		}
		return size.width;
	}

	/**
	 * @return the height of a shaped recipe, or 0 for a shapeless recipe
	 * @since 9.3.0
	 */
	default int getHeight() {
		// if not implemented, this calls the old getSize function for backward compatibility
		Size2i size = getSize();
		if (size == null) {
			return 0;
		}
		return size.height;
	}

	/**
	 * @return the size of a shaped recipe, or null for a shapeless recipe
	 * @deprecated Use {@link #getWidth()} and {@link #getHeight()} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	@Nullable
	default Size2i getSize() {
		return null;
	}
}
