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
	public IRecipeHandler getRecipeHandler(@Nullable Class recipeClass) {
		return null;
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategories() {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(@Nullable ItemStack input) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithInput(@Nullable Fluid input) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable ItemStack output) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<IRecipeCategory> getRecipeCategoriesWithOutput(@Nullable Fluid output) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithInput(@Nullable IRecipeCategory recipeCategory, @Nullable ItemStack input) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithInput(@Nullable IRecipeCategory recipeCategory, @Nullable Fluid input) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithOutput(@Nullable IRecipeCategory recipeCategory, @Nullable ItemStack output) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public ImmutableList<Object> getRecipesWithOutput(@Nullable IRecipeCategory recipeCategory, @Nullable Fluid output) {
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public List<Object> getRecipes(@Nullable IRecipeCategory recipeCategory) {
		return Collections.unmodifiableList(Collections.emptyList());
	}

	@Override
	public void addRecipe(@Nullable Object recipe) {

	}

	@Nullable
	@Override
	public IRecipeTransferHelper getRecipeTransferHelper(@Nullable Container container, @Nullable IRecipeCategory recipeCategory) {
		return null;
	}
}
