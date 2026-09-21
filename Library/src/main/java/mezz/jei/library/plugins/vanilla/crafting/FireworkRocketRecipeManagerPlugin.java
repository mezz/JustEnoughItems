package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public final class FireworkRocketRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<CraftingRecipe> {
	private final List<CraftingRecipe> recipes;
	private final IPlatformRecipeHelper recipeHelper;

	public FireworkRocketRecipeManagerPlugin(RecipeManager recipeManager, IPlatformRecipeHelper recipeHelper) {
		this.recipes = recipeManager.getAllRecipesFor(RecipeType.CRAFTING).stream()
			.filter(FireworkRocketRecipe.class::isInstance)
			.toList();
		this.recipeHelper = recipeHelper;
	}

	@Override
	public boolean isHandledInput(ITypedIngredient<?> input) {
		return !getRecipesForInput(input).isEmpty();
	}

	@Override
	public boolean isHandledOutput(ITypedIngredient<?> output) {
		return !getRecipesForOutput(output).isEmpty();
	}

	@Override
	public List<CraftingRecipe> getRecipesForInput(ITypedIngredient<?> input) {
		return input.getItemStack().map(stack -> recipes.stream()
			.filter(recipe -> {
				var data = recipeHelper.getFireworkRocketRecipeData((FireworkRocketRecipe) recipe);
				return data.shell().test(stack) || data.fuel().test(stack) || data.star().test(stack);
			})
			.toList())
			.orElse(List.of());
	}

	@Override
	public List<CraftingRecipe> getRecipesForOutput(ITypedIngredient<?> output) {
		return output.getItemStack().map(stack -> recipes.stream()
			.filter(recipe -> stack.sameItem(recipeHelper.getFireworkRocketRecipeData((FireworkRocketRecipe) recipe).result()))
			.toList())
			.orElse(List.of());
	}

	@Override
	public List<CraftingRecipe> getAllRecipes() {
		return recipes;
	}
}
