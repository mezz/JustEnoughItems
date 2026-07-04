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
import mezz.jei.common.codecs.EnumCodec;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class BookmarkCodec {

	private BookmarkCodec() {}

	public static MapCodec<IBookmark> create(ICodecHelper codecHelper, IIngredientManager ingredientManager, IRecipeManager recipeManager, BookmarkFactory bookmarkFactory) {
		MapCodec<? extends IngredientBookmark<?>> ingredientBookmarkCodec = createIngredientBookmarkCodec(codecHelper, bookmarkFactory);
		MapCodec<? extends RecipeBookmark<?, ?>> recipeBookmarkCodec = createRecipeBookmarkCodec(codecHelper, ingredientManager, recipeManager);

		return EnumCodec.create(BookmarkType.class).dispatchMap(
			"bookmarkType",
			IBookmark::getType,
			type -> switch (type) {
				case INGREDIENT -> ingredientBookmarkCodec;
				case RECIPE -> recipeBookmarkCodec;
			}
		);
	}

	private static MapCodec<? extends IngredientBookmark<?>> createIngredientBookmarkCodec(ICodecHelper codecHelper, BookmarkFactory bookmarkFactory) {
		return codecHelper.getTypedIngredientCodec()
			.xmap(
				bookmarkFactory::create,
				IngredientBookmark::getIngredient
			);
	}

	private static MapCodec<? extends RecipeBookmark<?, ?>> createRecipeBookmarkCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager, IRecipeManager recipeManager) {
		return codecHelper.getRecipeTypeCodec(recipeManager)
			.dispatchMap(
				"recipeType",
				bookmark -> bookmark.getRecipeCategory().getRecipeType(),
				recipeType -> {
					IRecipeCategory<?> recipeCategory = recipeManager.getRecipeCategory(recipeType);
					return createRecipeBookmarkCodec(recipeCategory, codecHelper, recipeManager, ingredientManager)
						.fieldOf("recipe");
				}
			);
	}

	private static <R> Codec<? extends RecipeBookmark<R, ?>> createRecipeBookmarkCodec(
		IRecipeCategory<R> recipeCategory,
		ICodecHelper codecHelper,
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager
	) {
		return recipeCategory.getCodec(codecHelper, recipeManager)
			.flatXmap(
				recipe -> {
					Identifier recipeUid = recipeCategory.getIdentifier(recipe);
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
}
