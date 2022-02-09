package mezz.jei.api.recipe.category.extensions.vanilla.crafting;

import org.jetbrains.annotations.Nullable;

import net.minecraftforge.common.util.Size2i;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;

/**
 * Implement this interface instead of just {@link IRecipeCategoryExtension} to have your recipe extension work as part of the
 * {@link VanillaRecipeCategoryUid#CRAFTING} recipe category as a shapeless recipe.
 *
 * For shaped recipes, override {@link #getSize()}.
 * To override the category's behavior and set the recipe layout yourself, use {@link ICustomCraftingCategoryExtension}.
 */
public interface ICraftingCategoryExtension extends IRecipeCategoryExtension {
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
	 * @return the size of a shaped recipe, or null for a shapeless recipe
	 */
	@Nullable
	default Size2i getSize() {
		return null;
	}
}
