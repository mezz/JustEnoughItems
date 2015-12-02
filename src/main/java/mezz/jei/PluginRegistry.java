package mezz.jei;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IPluginRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;

public class PluginRegistry implements IPluginRegistry {
	private boolean pluginsCanRegister = true;
	@Nonnull
	private final List<IModPlugin> plugins = new ArrayList<>();

	@Override
	public void registerPlugin(IModPlugin plugin) {
		if (!pluginsCanRegister) {
			throw new IllegalStateException("Plugins must be registered during FMLPreInitializationEvent.");
		}

		if (plugin.isModLoaded()) {
			plugins.add(plugin);
		}
	}

	public void init() {
		pluginsCanRegister = false;
	}

	public RecipeRegistry createRecipeRegistry() {
		ImmutableList.Builder<IRecipeCategory> recipeCategories = ImmutableList.builder();
		ImmutableList.Builder<IRecipeHandler> recipeHandlers = ImmutableList.builder();
		ImmutableList.Builder<IRecipeTransferHelper> recipeTransferHelpers = ImmutableList.builder();
		ImmutableList.Builder<Object> recipes = ImmutableList.builder();

		for (IModPlugin plugin : plugins) {
			recipeCategories.addAll(plugin.getRecipeCategories());
			recipeHandlers.addAll(plugin.getRecipeHandlers());
			recipeTransferHelpers.addAll(plugin.getRecipeTransferHelpers());
			recipes.addAll(plugin.getRecipes());
		}

		return new RecipeRegistry(recipeCategories.build(), recipeHandlers.build(), recipeTransferHelpers.build(), recipes.build());
	}
}
