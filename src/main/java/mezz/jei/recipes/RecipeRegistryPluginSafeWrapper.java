package mezz.jei.recipes;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.base.Stopwatch;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Log;

public class RecipeRegistryPluginSafeWrapper implements IRecipeRegistryPlugin {
	private final IRecipeRegistryPlugin plugin;
	private final Stopwatch stopWatch = Stopwatch.createUnstarted();

	public RecipeRegistryPluginSafeWrapper(IRecipeRegistryPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
		return callPluginMethod(() -> plugin.getRecipeCategoryUids(focus), Collections.emptyList());
	}

	@Override
	public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		return callPluginMethod(() -> plugin.getRecipeWrappers(recipeCategory, focus), Collections.emptyList());
	}

	@Override
	public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
		return callPluginMethod(() -> plugin.getRecipeWrappers(recipeCategory), Collections.emptyList());
	}

	private <T> T callPluginMethod(Supplier<T> supplier, T defaultValue) {
		try {
			stopWatch.reset();
			stopWatch.start();
			T result = supplier.get();
			stopWatch.stop();
			if (stopWatch.elapsed(TimeUnit.MILLISECONDS) > 10) {
				Log.get().warn("Recipe registry plugin is slow, took {}. {}", stopWatch, plugin.getClass());
			}
			return result;
		} catch (RuntimeException | LinkageError e) {
			Log.get().error("Recipe registry plugin crashed: {}", plugin.getClass(), e);
			return defaultValue;
		}
	}
}
