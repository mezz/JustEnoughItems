package mezz.jei.recipes.crafting;

import mezz.jei.api.recipes.IRecipeGui;
import mezz.jei.api.recipes.IRecipeHelper;
import mezz.jei.api.recipes.IRecipeType;
import mezz.jei.api.recipes.RecipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ShapelessRecipesHelper implements IRecipeHelper {

	@Nonnull
	@Override
	public IRecipeType getRecipeType() {
		return RecipeType.CRAFTING_TABLE;
	}

	@Nonnull
	@Override
	public Class getRecipeClass() {
		return ShapelessRecipes.class;
	}

	@Nonnull
	@Override
	public IRecipeGui createGui() {
		return new ShapelessRecipesGui();
	}

	@Nullable
	@Override
	public List<ItemStack> getInputs(@Nonnull Object recipe) {
		ShapelessRecipes shapelessRecipe = (ShapelessRecipes)recipe;
		return getRecipeItems(shapelessRecipe);
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs(@Nonnull Object recipe) {
		ShapelessRecipes shapelessRecipe = (ShapelessRecipes)recipe;
		return Collections.singletonList(shapelessRecipe.getRecipeOutput());
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static List<ItemStack> getRecipeItems(@Nonnull ShapelessRecipes shapelessRecipes) {
		return shapelessRecipes.recipeItems;
	}
}
