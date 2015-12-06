package mezz.jei;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;

import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;

public class RecipeRegistryDummy implements IRecipeRegistry {
	@Nullable
	@Override
	public IRecipeHandler getRecipeHandler(Class recipeClass) {
		return null;
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories() {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(ItemStack input) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(Fluid input) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(ItemStack output) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(Fluid output) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithInput(IRecipeCategory recipeCategory, ItemStack input) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithInput(IRecipeCategory recipeCategory, Fluid input) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithOutput(IRecipeCategory recipeCategory, ItemStack output) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithOutput(IRecipeCategory recipeCategory, Fluid output) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public List<Object> getRecipes(IRecipeCategory recipeCategory) {
		return Collections.unmodifiableList(Collections.emptyList());
	}

	@Override
	public void addRecipe(Object recipe) {

	}

	@Nullable
	@Override
	public IRecipeTransferHelper getRecipeTransferHelper(Container container, IRecipeCategory recipeCategory) {
		return null;
	}
}
