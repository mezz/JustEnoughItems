package mezz.jei.recipes;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import net.minecraft.util.ResourceLocation;

import com.google.common.base.Stopwatch;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeManagerPluginSafeWrapper implements IRecipeManagerPlugin {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IRecipeManagerPlugin plugin;
	private final Stopwatch stopWatch = Stopwatch.createUnstarted();

	public RecipeManagerPluginSafeWrapper(IRecipeManagerPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public <V> List<ResourceLocation> getRecipeCategoryUids(IFocus<V> focus) {
		return callPluginMethod(() -> plugin.getRecipeCategoryUids(focus), Collections.emptyList());
	}

	@Override
	public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		return callPluginMethod(() -> plugin.getRecipes(recipeCategory, focus), Collections.emptyList());
	}

	@Override
	public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		return callPluginMethod(() -> plugin.getRecipes(recipeCategory), Collections.emptyList());
	}

	private <T> T callPluginMethod(Supplier<T> supplier, T defaultValue) {
		try {
			stopWatch.reset();
			stopWatch.start();
			T result = supplier.get();
			stopWatch.stop();
			if (stopWatch.elapsed(TimeUnit.MILLISECONDS) > 10) {
				LOGGER.warn("Recipe registry plugin is slow, took {}. {}", stopWatch, plugin.getClass());
			}
			return result;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Recipe registry plugin crashed: {}", plugin.getClass(), e);
			return defaultValue;
		}
	}
}
