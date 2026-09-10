package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.library.plugins.vanilla.ingredients.FireworkStarIngredientFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public final class FireworkStarRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<CraftingRecipe> {
	private final List<CraftingRecipe> recipes;
	private final IPlatformRecipeHelper recipeHelper;

	public FireworkStarRecipeManagerPlugin(RecipeManager recipeManager, IPlatformRecipeHelper recipeHelper) {
		this.recipes = recipeManager.getAllRecipesFor(RecipeType.CRAFTING).stream()
			.filter(recipe -> recipe instanceof FireworkStarRecipe || recipe instanceof FireworkStarFadeRecipe)
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
			.filter(recipe -> handlesInput(recipe, stack))
			.toList()).orElse(List.of());
	}

	private boolean handlesInput(CraftingRecipe recipe, ItemStack stack) {
		if (recipe instanceof FireworkStarRecipe starRecipe) {
			var data = recipeHelper.getFireworkStarRecipeData(starRecipe);
			return data.fuel().test(stack) || data.dye().test(stack) || data.trail().test(stack) || data.twinkle().test(stack) ||
				data.shapes().values().stream().anyMatch(ingredient -> ingredient.test(stack));
		}
		if (recipe instanceof FireworkStarFadeRecipe fadeRecipe) {
			var data = recipeHelper.getFireworkStarFadeRecipeData(fadeRecipe);
			return data.target().test(stack) || data.dye().test(stack);
		}
		return false;
	}

	@Override
	public List<CraftingRecipe> getRecipesForOutput(ITypedIngredient<?> output) {
		return output.getItemStack().map(stack -> {
			if (!stack.is(Items.FIREWORK_STAR)) {
				return List.<CraftingRecipe>of();
			}
			boolean hasFade = FireworkStarIngredientFactory.getExplosion(stack)
				.map(explosion -> !explosion.fadeColors().isEmpty())
				.orElse(false);
			return recipes.stream()
				.filter(recipe -> hasFade ? recipe instanceof FireworkStarFadeRecipe : recipe instanceof FireworkStarRecipe)
				.toList();
		}).orElse(List.of());
	}

	@Override
	public List<CraftingRecipe> getAllRecipes() {
		return recipes;
	}
}
