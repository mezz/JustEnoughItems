package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftingCategoryExtension implements ICraftingCategoryExtension<CraftingRecipe> {
	@Override
	public void setRecipe(RecipeHolder<CraftingRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		CraftingRecipe recipe = recipeHolder.value();
		List<List<ItemStack>> inputs = new ArrayList<>();
		for (Ingredient ingredient : recipe.getIngredients()) {
			List<ItemStack> items = List.of(ingredient.getItems());
			inputs.add(items);
		}
		ItemStack resultItem = RecipeUtil.getResultItem(recipe);

		int width = getWidth(recipeHolder);
		int height = getHeight(recipeHolder);
		craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
		craftingGridHelper.createAndSetInputs(builder, inputs, width, height);
	}

	@SuppressWarnings("removal")
	@Override
	public Optional<ResourceLocation> getRegistryName(RecipeHolder<CraftingRecipe> recipeHolder) {
		return Optional.of(recipeHolder.id());
	}

	@Override
	public int getWidth(RecipeHolder<CraftingRecipe> recipeHolder) {
		CraftingRecipe recipe = recipeHolder.value();
		if (recipe instanceof ShapedRecipe shapedRecipe) {
			return shapedRecipe.getWidth();
		}
		return 0;
	}

	@Override
	public int getHeight(RecipeHolder<CraftingRecipe> recipeHolder) {
		CraftingRecipe recipe = recipeHolder.value();
		if (recipe instanceof ShapedRecipe shapedRecipe) {
			return shapedRecipe.getHeight();
		}
		return 0;
	}

	@Override
	public boolean isHandled(RecipeHolder<CraftingRecipe> recipeHolder) {
		CraftingRecipe recipe = recipeHolder.value();
		return !recipe.isSpecial();
	}
}
