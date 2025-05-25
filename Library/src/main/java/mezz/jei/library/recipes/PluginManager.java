package mezz.jei.library.recipes;

import com.google.common.base.Stopwatch;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.core.util.TimeUtil;
import mezz.jei.library.recipes.collect.RecipeTypeData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PluginManager {
	private static final Logger LOGGER = LogManager.getLogger();

	private List<IRecipeManagerPlugin> plugins = new ArrayList<>();

	public PluginManager(IRecipeManagerPlugin internalRecipeManagerPlugin) {
		this.plugins.add(internalRecipeManagerPlugin);
	}

	public <T> Stream<T> getRecipes(IRecipeType<T> recipeType, RecipeTypeData<T> recipeTypeData, IFocusGroup focusGroup, boolean includeHidden) {
		Stream<T> recipes = this.plugins.stream()
			.flatMap(p -> getPluginRecipeStream(p, recipeType, focusGroup))
			.distinct();

		if (!includeHidden) {
			Set<T> hiddenRecipes = recipeTypeData.getHiddenRecipes();
			Predicate<T> notHidden = ((Predicate<T>) hiddenRecipes::contains).negate();

			recipes = recipes.filter(notHidden);
		}
		return recipes;
	}

	public Stream<IRecipeType<?>> getRecipeTypes(IFocusGroup focusGroup) {
		return this.plugins.stream()
			.flatMap(p -> getPluginRecipeTypeStream(p, focusGroup))
			.distinct();
	}

	private Stream<IRecipeType<?>> getPluginRecipeTypeStream(IRecipeManagerPlugin plugin, IFocusGroup focuses) {
		List<IFocus<?>> allFocuses = focuses.getAllFocuses();
		return allFocuses.stream()
			.flatMap(focus -> getRecipeTypes(plugin, focus));
	}

	private <T> Stream<T> getPluginRecipeStream(IRecipeManagerPlugin plugin, IRecipeType<T> recipeType, IFocusGroup focuses) {
		if (!focuses.isEmpty()) {
			List<IFocus<?>> allFocuses = focuses.getAllFocuses();
			return allFocuses.stream()
				.flatMap(focus -> getRecipes(plugin, recipeType, focus));
		}
		return getRecipes(plugin, recipeType);
	}

	private Stream<IRecipeType<?>> getRecipeTypes(IRecipeManagerPlugin plugin, IFocus<?> focus) {
		return safeCallPlugin(
			plugin,
			() -> plugin.getRecipeTypes(focus).stream(),
			Stream.of()
		);
	}

	private <T> Stream<T> getRecipes(IRecipeManagerPlugin plugin, IRecipeType<T> recipeType) {
		return safeCallPlugin(
			plugin,
			() -> plugin.getRecipes(recipeType).stream(),
			Stream.of()
		);
	}

	private <T> Stream<T> getRecipes(IRecipeManagerPlugin plugin, IRecipeType<T> recipeType, IFocus<?> focus) {
		return safeCallPlugin(
			plugin,
			() -> plugin.getRecipes(recipeType, focus).stream(),
			Stream.of()
		);
	}

	private <T> T safeCallPlugin(IRecipeManagerPlugin plugin, Supplier<T> supplier, T defaultValue) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		try {
			T result = supplier.get();
			stopwatch.stop();
			if (stopwatch.elapsed(TimeUnit.MILLISECONDS) > 10) {
				LOGGER.warn("Recipe registry plugin is slow, took {}. {}", TimeUtil.toHumanString(stopwatch.elapsed()), plugin.getClass());
			}
			return result;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Recipe registry plugin crashed, it is being disabled: {}", plugin.getClass(), e);
			// make a copy, in order to avoid modifying the current stream
			this.plugins = new ArrayList<>(this.plugins);
			this.plugins.remove(plugin);
			return defaultValue;
		}
	}

	public void addAll(List<IRecipeManagerPlugin> plugins) {
		this.plugins.addAll(plugins);
	}
}
