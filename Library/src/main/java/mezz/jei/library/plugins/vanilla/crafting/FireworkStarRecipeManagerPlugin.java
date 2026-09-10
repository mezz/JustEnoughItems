package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.library.focus.Focus;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/**
 * Matches only the creation or fade step that can produce the requested star, including unlisted subtypes.
 */
public final class FireworkStarRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<RecipeHolder<CraftingRecipe>> {
	private final List<RecipeHolder<CraftingRecipe>> recipes;
	private final List<RecipeHolder<CraftingRecipe>> sampleRecipes;
	private final FireworkStarRecipeCategoryExtension extension;

	public FireworkStarRecipeManagerPlugin(RecipeMap recipes, FireworkStarRecipeCategoryExtension extension) {
		this.extension = extension;
		this.recipes = recipes.byType(RecipeType.CRAFTING).stream()
			.filter(holder -> holder.value() instanceof FireworkStarRecipe || holder.value() instanceof FireworkStarFadeRecipe)
			.filter(extension::isHandled)
			.toList();
		this.sampleRecipes = this.recipes.stream()
			.flatMap(recipe -> extension.createSampleRecipes(recipe).stream())
			.toList();
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
	public List<RecipeHolder<CraftingRecipe>> getRecipesForInput(ITypedIngredient<?> input) {
		if (input.getItemStack().isEmpty()) {
			return List.of();
		}
		Focus<?> focus = new Focus<>(RecipeIngredientRole.INPUT, input);
		return sampleRecipes.stream()
			.filter(recipe -> !extension.getRepresentativeRecipes(recipe, focus).isEmpty())
			.toList();
	}

	@Override
	public List<RecipeHolder<CraftingRecipe>> getRecipesForOutput(ITypedIngredient<?> output) {
		return getRecipes(output, RecipeIngredientRole.OUTPUT);
	}

	private List<RecipeHolder<CraftingRecipe>> getRecipes(ITypedIngredient<?> ingredient, RecipeIngredientRole role) {
		if (ingredient.getItemStack().isEmpty()) {
			return List.of();
		}
		Focus<?> focus = new Focus<>(role, ingredient);
		return recipes.stream()
			.filter(recipe -> extension.getRecipe(recipe, focus).isPresent())
			.toList();
	}

	@Override
	public List<RecipeHolder<CraftingRecipe>> getAllRecipes() {
		return sampleRecipes;
	}
}
