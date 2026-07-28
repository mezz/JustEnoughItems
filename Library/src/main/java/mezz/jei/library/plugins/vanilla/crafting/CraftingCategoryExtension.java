package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class CraftingCategoryExtension implements ICraftingCategoryExtension<CraftingRecipe> {
	@Override
	public int getWidth(RecipeHolder<CraftingRecipe> recipeHolder) {
		RecipeDisplay display = getFirstDisplay(recipeHolder);
		if (display instanceof ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay) {
			return shapedCraftingRecipeDisplay.width();
		}
		return 0;
	}

	@Override
	public int getHeight(RecipeHolder<CraftingRecipe> recipeHolder) {
		RecipeDisplay display = getFirstDisplay(recipeHolder);
		if (display instanceof ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay) {
			return shapedCraftingRecipeDisplay.height();
		}
		return 0;
	}

	@Override
	public boolean isHandled(RecipeHolder<CraftingRecipe> recipeHolder) {
		CraftingRecipe recipe = recipeHolder.value();
		if (recipe.isSpecial()) {
			return false;
		}
		RecipeDisplay display = getFirstDisplay(recipeHolder);
		return display instanceof ShapelessCraftingRecipeDisplay ||
			display instanceof ShapedCraftingRecipeDisplay;
	}

	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<CraftingRecipe> recipeHolder) {
		RecipeDisplay display = getFirstDisplay(recipeHolder);
		if (display == null) {
			return List.of();
		}
		return getIngredients(display);
	}

	private static @Nullable RecipeDisplay getFirstDisplay(RecipeHolder<CraftingRecipe> recipeHolder) {
		List<RecipeDisplay> displays = recipeHolder.value().display();
		if (displays.isEmpty()) {
			return null;
		}
		return displays.getFirst();
	}

	private static List<SlotDisplay> getIngredients(RecipeDisplay display) {
		if (display instanceof ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay) {
			return shapedCraftingRecipeDisplay.ingredients();
		} else if (display instanceof ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay) {
			return shapelessCraftingRecipeDisplay.ingredients();
		} else {
			return List.of();
		}
	}
}
