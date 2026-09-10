package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/**
 * Finds rocket recipes for all rocket and star decorations, including unlisted subtypes.
 */
public final class FireworkRocketRecipeManagerPlugin implements ISimpleRecipeManagerPlugin<RecipeHolder<CraftingRecipe>> {
	private final List<RecipeHolder<CraftingRecipe>> recipes;
	private final IPlatformRecipeHelper recipeHelper;

	public FireworkRocketRecipeManagerPlugin(RecipeManager recipes, IPlatformRecipeHelper recipeHelper) {
		this.recipes = recipes.getAllRecipesFor(RecipeType.CRAFTING).stream()
			.filter(holder -> holder.value() instanceof FireworkRocketRecipe)
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
	public List<RecipeHolder<CraftingRecipe>> getRecipesForInput(ITypedIngredient<?> input) {
		return input.getItemStack().map(stack -> recipes.stream().filter(holder -> {
				var data = recipeHelper.getFireworkRocketRecipeData((FireworkRocketRecipe) holder.value());
				return data.shell().test(stack) || data.fuel().test(stack) || data.star().test(stack);
			})
			.toList())
			.orElse(List.of());
	}

	@Override
	public List<RecipeHolder<CraftingRecipe>> getRecipesForOutput(ITypedIngredient<?> output) {
		return output.getItemStack().map(stack -> recipes.stream().filter(holder -> {
				var data = recipeHelper.getFireworkRocketRecipeData((FireworkRocketRecipe) holder.value());
				return ItemStack.isSameItem(stack, data.result());
			})
			.toList())
			.orElse(List.of());
	}

	@Override
	public List<RecipeHolder<CraftingRecipe>> getAllRecipes() {
		return recipes;
	}
}
