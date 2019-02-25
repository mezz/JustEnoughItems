package mezz.jei.plugins.vanilla.crafting;

import javax.annotation.Nullable;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.category.extensions.ICraftingCategoryExtension;

public class ShapelessCraftingCategoryExtension<T extends IRecipe> implements ICraftingCategoryExtension {
	protected final T recipe;

	public ShapelessCraftingCategoryExtension(T recipe) {
		this.recipe = recipe;
	}

	@Override
	public void setIngredients(IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return recipe.getId();
	}
}
