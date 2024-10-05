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
			IRecipeManager recipeManager,
			IIngredientManager ingredientManager
		) {
			return recipeCategory.getCodec(codecHelper, recipeManager)
				.flatXmap(
					recipe -> {
						ResourceLocation recipeUid = recipeCategory.getRegistryName(recipe);
						if (recipeUid == null) {
							return DataResult.error(() -> "Recipe has no registry name");
						}
						IIngredientSupplier ingredients = recipeManager.getRecipeIngredients(recipeCategory, recipe);

						boolean displayIsOutput;
						ITypedIngredient<?> displayIngredient;

						List<ITypedIngredient<?>> outputs = ingredients.getIngredients(RecipeIngredientRole.OUTPUT);
						if (!outputs.isEmpty()) {
							displayIngredient = outputs.getFirst();
							displayIsOutput = true;
						} else {
							List<ITypedIngredient<?>> inputs = ingredients.getIngredients(RecipeIngredientRole.INPUT);
							if (inputs.isEmpty()) {
								return DataResult.error(() -> "Recipe has no inputs or outputs");
							}
							displayIngredient = inputs.getFirst();
							displayIsOutput = false;
						}

						displayIngredient = ingredientManager.normalizeTypedIngredient(displayIngredient);
						RecipeBookmark<R, ?> bookmark = new RecipeBookmark<>(recipeCategory, recipe, recipeUid, displayIngredient, displayIsOutput);
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
						return getCodec(recipeCategory, codecHelper, recipeManager, ingredientManager)
							.fieldOf("recipe");
					}
				);
		}
	};

	abstract public MapCodec<? extends IBookmark> getCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager, IRecipeManager recipeManager);
}
