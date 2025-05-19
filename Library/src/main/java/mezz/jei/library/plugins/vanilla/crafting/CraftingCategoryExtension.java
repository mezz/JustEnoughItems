package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public class CraftingCategoryExtension implements ICraftingCategoryExtension<CraftingRecipe> {
	@Override
	public int getWidth(RecipeHolder<CraftingRecipe> recipeHolder) {
		CraftingRecipe recipe = recipeHolder.value();
		if (recipe instanceof ShapedRecipe shapedRecipe) {
			return shapedRecipe.getWidth();
		}
		if (recipe instanceof JeiShapedRecipe shapedRecipe) {
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
		if (recipe instanceof JeiShapedRecipe shapedRecipe) {
			return shapedRecipe.getHeight();
		}
		return 0;
	}

	@Override
	public boolean isHandled(RecipeHolder<CraftingRecipe> recipeHolder) {
		CraftingRecipe recipe = recipeHolder.value();
		if (recipe.isSpecial()) {
			return false;
		}
		List<RecipeDisplay> displays = recipe.display();
		if (displays.isEmpty()) {
			return false;
		}
		RecipeDisplay display = displays.getFirst();
		return display instanceof ShapelessCraftingRecipeDisplay ||
			display instanceof ShapedCraftingRecipeDisplay;
	}

	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<CraftingRecipe> recipeHolder) {
		List<RecipeDisplay> displays = recipeHolder.value().display();
		if (displays.isEmpty()) {
			return List.of();
		}
		RecipeDisplay display = displays.getFirst();
		if (display instanceof ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay) {
			return shapedCraftingRecipeDisplay.ingredients();
		} else if (display instanceof ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay) {
			return shapelessCraftingRecipeDisplay.ingredients();
		} else {
			return List.of();
		}
	}
}
