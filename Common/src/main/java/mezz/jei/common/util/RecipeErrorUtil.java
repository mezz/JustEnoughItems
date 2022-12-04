package mezz.jei.common.util;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.ingredients.IIngredientSupplier;
import mezz.jei.common.platform.IPlatformModHelper;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.Services;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Stream;

public final class RecipeErrorUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private RecipeErrorUtil() {
	}

	public static <T> String getInfoFromRecipe(T recipe, IRecipeCategory<T> recipeCategory) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		String recipeName = getNameForRecipe(recipe);
		recipeInfoBuilder.append(recipeName);

		var registeredIngredientsOptional = Internal.getRegisteredIngredients();
		if (registeredIngredientsOptional.isEmpty()) {
			recipeInfoBuilder.append("\nRegistered ingredients have not been set");
			return recipeInfoBuilder.toString();
		}
		IRegisteredIngredients registeredIngredients = registeredIngredientsOptional.get();

		IIngredientSupplier ingredientSupplier = IngredientSupplierHelper.getIngredientSupplier(recipe, recipeCategory, registeredIngredients);
		if (ingredientSupplier == null) {
			recipeInfoBuilder.append("\nFailed to get ingredients from recipe wrapper");
			return recipeInfoBuilder.toString();
		}

		recipeInfoBuilder.append(" {");
		recipeInfoBuilder.append("\n  Outputs:");
		appendRoleData(ingredientSupplier, RecipeIngredientRole.OUTPUT, recipeInfoBuilder, registeredIngredients);

		recipeInfoBuilder.append("\n  Inputs:");
		appendRoleData(ingredientSupplier, RecipeIngredientRole.INPUT, recipeInfoBuilder, registeredIngredients);

		recipeInfoBuilder.append("\n  Catalysts:");
		appendRoleData(ingredientSupplier, RecipeIngredientRole.CATALYST, recipeInfoBuilder, registeredIngredients);

		recipeInfoBuilder.append("\n}");

		return recipeInfoBuilder.toString();
	}

	private static void appendRoleData(IIngredientSupplier ingredientSupplier, RecipeIngredientRole role, StringBuilder recipeInfoBuilder, IRegisteredIngredients registeredIngredients) {
		ingredientSupplier.getIngredientTypes(role)
			.forEach(ingredientType -> {
				String ingredientOutputInfo = getIngredientInfo(ingredientType, role, ingredientSupplier, registeredIngredients);
				recipeInfoBuilder
					.append("\n    ")
					.append(ingredientType.getIngredientClass().getName())
					.append(": ")
					.append(ingredientOutputInfo);
			});
	}

	private static <T> String getIngredientInfo(IIngredientType<T> ingredientType, RecipeIngredientRole role, IIngredientSupplier ingredients, IRegisteredIngredients registeredIngredients) {
		List<T> ingredientList = ingredients.getIngredientStream(ingredientType, role).toList();
		IIngredientHelper<T> ingredientHelper = registeredIngredients.getIngredientHelper(ingredientType);

		Stream<String> stringStream = ingredientList.stream()
			.map(ingredientHelper::getErrorInfo);

		return truncatedStream(stringStream, ingredientList.size(), 10)
			.toList()
			.toString();
	}

	public static String getNameForRecipe(Object recipe) {
		IPlatformRecipeHelper recipeHelper = Services.PLATFORM.getRecipeHelper();
		return recipeHelper.getRegistryNameForRecipe(recipe)
			.map(registryName -> {
				IPlatformModHelper modHelper = Services.PLATFORM.getModHelper();
				String modId = registryName.getNamespace();
				String modName = modHelper.getModNameForModId(modId);
				return modName + " " + registryName + " " + recipe.getClass();
			})
			.orElseGet(() -> {
				try {
					return recipe.toString();
				} catch (RuntimeException e) {
					LOGGER.error("Failed recipe.toString", e);
					return recipe.getClass().toString();
				}
			});
	}

	private static Stream<String> truncatedStream(Stream<String> stream, int size, int limit) {
		if (size + 1 > limit) {
			return Stream.concat(
				stream.limit(limit),
				Stream.of(String.format("<truncated to %s elements, skipped %s>", limit, size - limit))
			);
		}
		return stream;
	}
}
