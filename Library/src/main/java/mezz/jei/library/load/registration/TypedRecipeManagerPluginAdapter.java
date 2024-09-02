package mezz.jei.library.load.registration;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.advanced.IRecipeManagerPluginHelper;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.List;
import java.util.stream.Stream;

public class TypedRecipeManagerPluginAdapter<T> implements IRecipeManagerPlugin {
	private final IRecipeManagerPluginHelper helper;
	private final RecipeType<T> recipeType;
	private final ISimpleRecipeManagerPlugin<T> plugin;

	public TypedRecipeManagerPluginAdapter(
		IRecipeManagerPluginHelper helper,
		RecipeType<T> recipeType,
		ISimpleRecipeManagerPlugin<T> plugin
	) {
		this.helper = helper;
		this.recipeType = recipeType;
		this.plugin = plugin;
	}

	@Override
	public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
		if (isHandled(focus)) {
			return List.of(recipeType);
		}
		return List.of();
	}

	private boolean isHandled(IFocus<?> focus) {
		if (helper.isRecipeCatalyst(recipeType, focus)) {
			return true;
		}

		switch (focus.getRole()) {
			case INPUT -> {
				if (plugin.isHandledInput(focus.getTypedValue())) {
					return true;
				}
			}
			case OUTPUT -> {
				if (plugin.isHandledOutput(focus.getTypedValue())) {
					return true;
				}
			}
			case CATALYST -> {
				if (helper.isRecipeCatalyst(recipeType, focus)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public <T2, V> List<T2> getRecipes(IRecipeCategory<T2> recipeCategory, IFocus<V> focus) {
		if (recipeCategory.getRecipeType().equals(recipeType)) {
			List<T> recipes = getRecipes(focus);
			@SuppressWarnings("unchecked")
			List<T2> castRecipes = (List<T2>) recipes;
			return castRecipes;
		}
		return List.of();
	}

	private List<T> getRecipes(IFocus<?> focus) {
		switch (focus.getRole()) {
			case INPUT -> {
				List<T> recipesForInput = plugin.getRecipesForInput(focus.getTypedValue());
				if (helper.isRecipeCatalyst(recipeType, focus)) {
					return Stream.concat(recipesForInput.stream(), plugin.getAllRecipes().stream())
						.distinct()
						.toList();
				}
				return recipesForInput;
			}
			case OUTPUT -> {
				List<T> recipesForOutput = plugin.getRecipesForOutput(focus.getTypedValue());
				if (helper.isRecipeCatalyst(recipeType, focus)) {
					return Stream.concat(recipesForOutput.stream(), plugin.getAllRecipes().stream())
						.distinct()
						.toList();
				}
				return recipesForOutput;
			}
			case CATALYST -> {
				if (helper.isRecipeCatalyst(recipeType, focus)) {
					return plugin.getAllRecipes();
				}
				return List.of();
			}
		}
		return List.of();
	}

	@Override
	public <T2> List<T2> getRecipes(IRecipeCategory<T2> recipeCategory) {
		if (recipeCategory.getRecipeType().equals(recipeType)) {
			List<T> recipes = plugin.getAllRecipes();
			@SuppressWarnings("unchecked")
			List<T2> castRecipes = (List<T2>) recipes;
			return castRecipes;
		}
		return List.of();
	}
}
