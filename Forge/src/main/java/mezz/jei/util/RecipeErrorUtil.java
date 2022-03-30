package mezz.jei.util;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.util.IRecipeRegistryHelper;
import mezz.jei.common.util.RecipeRegistryHelper;
import mezz.jei.ingredients.IIngredientSupplier;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.recipes.IngredientSupplierHelper;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public final class RecipeErrorUtil {
	private static final Logger LOGGER = LogManager.getLogger();
	@Nullable
	private static IModIdHelper modIdHelper;
	private static IRecipeRegistryHelper recipeRegistryHelper = new RecipeRegistryHelper();
	@Nullable
	private static RegisteredIngredients registeredIngredients;

	private RecipeErrorUtil() {
	}

	public static void setModIdHelper(IModIdHelper modIdHelper) {
		RecipeErrorUtil.modIdHelper = modIdHelper;
	}

	public static void setRecipeRegistryHelper(IRecipeRegistryHelper recipeRegistryHelper) {
		RecipeErrorUtil.recipeRegistryHelper = recipeRegistryHelper;
	}

	public static void setRegisteredIngredients(RegisteredIngredients registeredIngredients) {
		RecipeErrorUtil.registeredIngredients = registeredIngredients;
	}

	public static <T> String getInfoFromRecipe(T recipe, IRecipeCategory<T> recipeCategory) {
		StringBuilder recipeInfoBuilder = new StringBuilder();
		String recipeName = getNameForRecipe(recipe);
		recipeInfoBuilder.append(recipeName);

		if (registeredIngredients == null) {
			recipeInfoBuilder.append("\nRegistered ingredients have not been set");
			return recipeInfoBuilder.toString();
		}

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

	private static void appendRoleData(IIngredientSupplier ingredientSupplier, RecipeIngredientRole role, StringBuilder recipeInfoBuilder, RegisteredIngredients registeredIngredients) {
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

	private static <T> String getIngredientInfo(IIngredientType<T> ingredientType, RecipeIngredientRole role, IIngredientSupplier ingredients, RegisteredIngredients registeredIngredients) {
		List<T> ingredientList = ingredients.getIngredientStream(ingredientType, role).toList();
		IIngredientHelper<T> ingredientHelper = registeredIngredients.getIngredientHelper(ingredientType);

		Stream<String> stringStream = ingredientList.stream()
			.map(ingredientHelper::getErrorInfo);

		return truncatedStream(stringStream, ingredientList.size(), 10)
			.toList()
			.toString();
	}

	public static String getNameForRecipe(Object recipe) {
		ResourceLocation registryName = recipeRegistryHelper.getRegistryNameForRecipe(recipe);
		if (registryName != null) {
			if (modIdHelper != null) {
				String modId = registryName.getNamespace();
				String modName = modIdHelper.getModNameForModId(modId);
				return modName + " " + registryName + " " + recipe.getClass();
			}
			return registryName + " " + recipe.getClass();
		}
		try {
			return recipe.toString();
		} catch (RuntimeException e) {
			LOGGER.error("Failed recipe.toString", e);
			return recipe.getClass().toString();
		}
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
