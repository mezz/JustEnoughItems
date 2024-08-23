package mezz.jei.gui.bookmarks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public enum BookmarkType {
	INGREDIENT {
		@Override
		public MapCodec<? extends IngredientBookmark<?>> getCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager, IRecipeManager recipeManager) {
			return codecHelper.getTypedIngredientCodec()
				.xmap(
					i -> IngredientBookmark.create(i, ingredientManager),
					IngredientBookmark::getIngredient
				);
		}
	},
	RECIPE {
		private static <R> Codec<? extends RecipeBookmark<R, ?>> getCodec(
			IRecipeCategory<R> recipeCategory,
			ICodecHelper codecHelper,
			IRecipeManager recipeManager
		) {
			return recipeCategory.getCodec(codecHelper, recipeManager)
				.flatXmap(
					recipe -> {
						ResourceLocation recipeUid = recipeCategory.getRegistryName(recipe);
						if (recipeUid == null) {
							return DataResult.error(() -> "Recipe has no registry name");
						}
						IIngredientSupplier ingredients = recipeManager.getRecipeIngredients(recipeCategory, recipe);
						List<ITypedIngredient<?>> outputs = ingredients.getIngredients(RecipeIngredientRole.OUTPUT);
						if (outputs.isEmpty()) {
							return DataResult.error(() -> "Recipe has no outputs");
						}
						ITypedIngredient<?> output = outputs.getFirst();
						RecipeBookmark<R, ?> bookmark = new RecipeBookmark<>(recipeCategory, recipe, recipeUid, output);
						return DataResult.success(bookmark);
					},
					bookmark -> {
						R recipe = bookmark.getRecipe();
						return DataResult.success(recipe);
					}
				);
		}

		@Override
		public MapCodec<? extends RecipeBookmark<?, ?>> getCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager, IRecipeManager recipeManager) {
			return codecHelper.getRecipeTypeCodec(recipeManager)
				.dispatchMap(
					"recipeType",
					bookmark -> bookmark.getRecipeCategory().getRecipeType(),
					recipeType -> {
						IRecipeCategory<?> recipeCategory = recipeManager.getRecipeCategory(recipeType);
						return getCodec(recipeCategory, codecHelper, recipeManager)
							.fieldOf("recipe");
					}
				);
		}
	};

	abstract public MapCodec<? extends IBookmark> getCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager, IRecipeManager recipeManager);
}
