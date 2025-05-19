package mezz.jei.library.load.registration;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.advanced.IRecipeManagerPluginHelper;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.api.recipe.types.IRecipeType;

import java.util.List;
import java.util.stream.Stream;

public class SingleTypeRecipeManagerPluginAdapter<T> implements IRecipeManagerPlugin {
	private final IRecipeManagerPluginHelper helper;
	private final IRecipeType<T> recipeType;
	private final ISimpleRecipeManagerPlugin<T> plugin;

	public SingleTypeRecipeManagerPluginAdapter(
		IRecipeManagerPluginHelper helper,
		IRecipeType<T> recipeType,
		ISimpleRecipeManagerPlugin<T> plugin
	) {
		this.helper = helper;
		this.recipeType = recipeType;
		this.plugin = plugin;
	}

	@Override
	public <V> List<IRecipeType<?>> getRecipeTypes(IFocus<V> focus) {
		if (isHandled(focus)) {
			return List.of(recipeType);
		}
		return List.of();
	}

	private boolean isHandled(IFocus<?> focus) {
		if (helper.isCraftingStation(recipeType, focus)) {
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
			case CRAFTING_STATION -> {
				if (helper.isCraftingStation(recipeType, focus)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public <T2, V> List<T2> getRecipes(IRecipeType<T2> recipeType, IFocus<V> focus) {
		if (recipeType.equals(this.recipeType) &&
			isHandled(focus)
		) {
			List<T> recipes = getRecipes(focus);
			@SuppressWarnings("unchecked")
			List<T2> castRecipes = (List<T2>) recipes;
			return castRecipes;
		}
		return List.of();
	}

	private List<T> getRecipes(IFocus<?> focus) {
		if (!isHandled(focus)) {
			return List.of();
		}
		switch (focus.getRole()) {
			case INPUT -> {
				List<T> recipesForInput = plugin.getRecipesForInput(focus.getTypedValue());
				if (helper.isCraftingStation(recipeType, focus)) {
					return Stream.concat(recipesForInput.stream(), plugin.getAllRecipes().stream())
						.distinct()
						.toList();
				}
				return recipesForInput;
			}
			case OUTPUT -> {
				List<T> recipesForOutput = plugin.getRecipesForOutput(focus.getTypedValue());
				if (helper.isCraftingStation(recipeType, focus)) {
					return Stream.concat(recipesForOutput.stream(), plugin.getAllRecipes().stream())
						.distinct()
						.toList();
				}
				return recipesForOutput;
			}
			case CRAFTING_STATION -> {
				if (helper.isCraftingStation(recipeType, focus)) {
					return plugin.getAllRecipes();
				}
				return List.of();
			}
		}
		return List.of();
	}

	@Override
	public <T2> List<T2> getRecipes(IRecipeType<T2> recipeType) {
		if (recipeType.equals(this.recipeType)) {
			List<T> recipes = plugin.getAllRecipes();
			@SuppressWarnings("unchecked")
			List<T2> castRecipes = (List<T2>) recipes;
			return castRecipes;
		}
		return List.of();
	}
}
