package mezz.jei.plugins.vanilla.crafting;

import org.jetbrains.annotations.Nullable;

import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.util.Size2i;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;

public class CraftingCategoryExtension<T extends CraftingRecipe> implements ICraftingCategoryExtension {
	protected final T recipe;

	public CraftingCategoryExtension(T recipe) {
		this.recipe = recipe;
	}

	@Override
	public void setIngredients(IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return recipe.getId();
	}

	@Nullable
	@Override
	public Size2i getSize() {
		if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
			return new Size2i(shapedRecipe.getRecipeWidth(), shapedRecipe.getRecipeHeight());
		}
		return null;
	}
}
