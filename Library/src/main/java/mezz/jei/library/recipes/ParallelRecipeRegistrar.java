package mezz.jei.library.recipes;

import com.google.common.base.Stopwatch;
import mezz.jei.api.IAsyncCompatiblePlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.util.JeiThreadFactory;
import mezz.jei.library.load.registration.RecipeRegistration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Optimized recipe registror that parallelizes recipe registration across multiple threads.
 * Groups recipes by RecipeType and registers them in parallel for maximum throughput.
 */
public class ParallelRecipeRegistrar {
	private static final Logger LOGGER = LogManager.getLogger();

	// Timeout for recipe registration (seconds)
	private static final int REGISTRATION_TIMEOUT_SECONDS = 60;

	// Minimum recipes to enable parallel registration
	private static final int MIN_RECIPES_FOR_PARALLEL = 50;

	// Minimum recipe types to enable parallel registration
	private static final int MIN_RECIPE_TYPES_FOR_PARALLEL = 5;

	/**
	 * Register recipes from all plugins with parallel execution.
	 * Groups recipes by RecipeType and processes them in parallel.
	 */
	public static void registerRecipesParallel(List<IModPlugin> plugins, RecipeRegistration recipeRegistration) {
		if (!DebugConfig.isAsyncLoadingEnabled()) {
			// Fall back to sequential registration
			registerRecipesSequential(plugins, recipeRegistration);
			return;
		}

		LOGGER.info("Registering recipes with parallel execution...");
		Stopwatch stopwatch = Stopwatch.createStarted();

		// Separate plugins into async-safe and sync-only
		List<IModPlugin> syncPlugins = new ArrayList<>();
		List<IModPlugin> asyncPlugins = new ArrayList<>();

		for (IModPlugin plugin : plugins) {
			if (plugin instanceof IAsyncCompatiblePlugin asyncPlugin && asyncPlugin.canExecuteAsync()) {
				asyncPlugins.add(plugin);
			} else {
				syncPlugins.add(plugin);
			}
		}

		// Execute sync plugins first (sequential)
		for (IModPlugin plugin : syncPlugins) {
			try {
				plugin.registerRecipes(recipeRegistration);
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error("Caught an error from mod plugin during recipe registration: {} {}",
					plugin.getClass(), plugin.getPluginUid(), e);
			}
		}

		// Execute async plugins in parallel
		if (!asyncPlugins.isEmpty()) {
			registerRecipesAsyncParallel(asyncPlugins, recipeRegistration);
		}

		stopwatch.stop();
		LOGGER.info("Parallel recipe registration took {}", stopwatch);
	}

	/**
	 * Register recipes from async-compatible plugins using parallel execution.
	 */
	private static void registerRecipesAsyncParallel(List<IModPlugin> asyncPlugins, RecipeRegistration recipeRegistration) {
		ExecutorService executor = JeiThreadFactory.getPluginLoaderExecutor();

		// Create a thread-safe recipe collector
		RecipeCollector recipeCollector = new RecipeCollector();

		// Phase 1: Collect all recipes from plugins in parallel
		LOGGER.info("Phase 1: Collecting recipes from {} async plugins...", asyncPlugins.size());
		Stopwatch phase1Stopwatch = Stopwatch.createStarted();

		List<CompletableFuture<Void>> collectionFutures = new ArrayList<>();
		for (IModPlugin plugin : asyncPlugins) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					// Create a temporary recipe registration for this plugin
					IRecipeRegistration tempRegistration = new CollectingRecipeRegistration(
						recipeRegistration,
						recipeCollector,
						plugin.getPluginUid().toString()
					);

					plugin.registerRecipes(tempRegistration);
				} catch (Exception e) {
					LOGGER.error("Error collecting recipes from plugin: {}", plugin.getClass(), e);
				}
			}, executor);

			collectionFutures.add(future);
		}

		// Wait for all collections to complete
		try {
			CompletableFuture.allOf(collectionFutures.toArray(new CompletableFuture[0]))
				.get(REGISTRATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			LOGGER.error("Recipe collection timed out after {} seconds", REGISTRATION_TIMEOUT_SECONDS);
			collectionFutures.forEach(f -> f.cancel(true));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Recipe collection was interrupted", e);
		} catch (Exception e) {
			LOGGER.error("Recipe collection failed", e);
		}

		phase1Stopwatch.stop();
		LOGGER.info("Phase 1 complete: Collected {} recipe types in {}",
			recipeCollector.getRecipeTypeCount(), phase1Stopwatch);

		// Phase 2: Register all collected recipes in parallel by RecipeType
		LOGGER.info("Phase 2: Registering recipes by RecipeType in parallel...");
		Stopwatch phase2Stopwatch = Stopwatch.createStarted();

		recipeCollector.registerAllRecipes(recipeRegistration);

		phase2Stopwatch.stop();
		LOGGER.info("Phase 2 complete: Registered all recipes in {}", phase2Stopwatch);
	}

	/**
	 * Sequential recipe registration (fallback).
	 */
	private static void registerRecipesSequential(List<IModPlugin> plugins, RecipeRegistration recipeRegistration) {
		LOGGER.info("Registering recipes sequentially...");
		Stopwatch stopwatch = Stopwatch.createStarted();

		for (IModPlugin plugin : plugins) {
			try {
				plugin.registerRecipes(recipeRegistration);
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error("Caught an error from mod plugin during recipe registration: {} {}",
					plugin.getClass(), plugin.getPluginUid(), e);
			}
		}

		stopwatch.stop();
		LOGGER.info("Sequential recipe registration took {}", stopwatch);
	}

	/**
	 * Thread-safe recipe collector that aggregates recipes from multiple plugins.
	 */
	private static class RecipeCollector {
		// Map of RecipeType -> List of (Plugin UID, Recipes)
		private final Map<RecipeType<?>, List<PluginRecipes<?>>> recipeMap = new ConcurrentHashMap<>();

		/**
		 * Add recipes for a specific RecipeType.
		 */
		@SuppressWarnings("unchecked")
		public <T> void addRecipes(RecipeType<T> recipeType, List<T> recipes, String pluginUid) {
			if (recipes == null || recipes.isEmpty()) {
				return;
			}

			recipeMap.computeIfAbsent(recipeType, k -> new ArrayList<>())
				.add(new PluginRecipes<>(recipes, pluginUid));
		}

		/**
		 * Get the number of unique RecipeTypes collected.
		 */
		public int getRecipeTypeCount() {
			return recipeMap.size();
		}

		/**
		 * Register all collected recipes using parallel execution.
		 */
		@SuppressWarnings("unchecked")
		public void registerAllRecipes(RecipeRegistration recipeRegistration) {
			if (recipeMap.isEmpty()) {
				return;
			}

			// Decide between parallel and sequential based on recipe count
			boolean useParallel = shouldUseParallelRegistration();

			if (useParallel) {
				// Parallel registration by RecipeType
				List<CompletableFuture<Void>> futures = new ArrayList<>();

				for (Map.Entry<RecipeType<?>, List<PluginRecipes<?>>> entry : recipeMap.entrySet()) {
					RecipeType<?> recipeType = entry.getKey();
					List<PluginRecipes<?>> pluginRecipesList = entry.getValue();

					CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
						try {
							// Merge all recipes for this RecipeType
							List<Object> mergedRecipes = new ArrayList<>();
							for (PluginRecipes<?> pluginRecipes : pluginRecipesList) {
								mergedRecipes.addAll(pluginRecipes.recipes());
							}

							// Register merged recipes
							recipeRegistration.addRecipes((RecipeType<Object>) recipeType, mergedRecipes);
						} catch (Exception e) {
							LOGGER.error("Error registering recipes for RecipeType: {}", recipeType, e);
						}
					}, JeiThreadFactory.getSearchForkJoinPool());

					futures.add(future);
				}

				// Wait for all registrations to complete
				try {
					CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
						.get(REGISTRATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					LOGGER.error("Recipe registration timed out after {} seconds", REGISTRATION_TIMEOUT_SECONDS);
					futures.forEach(f -> f.cancel(true));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					LOGGER.error("Recipe registration was interrupted", e);
				} catch (Exception e) {
					LOGGER.error("Recipe registration failed", e);
				}
			} else {
				// Sequential registration
				for (Map.Entry<RecipeType<?>, List<PluginRecipes<?>>> entry : recipeMap.entrySet()) {
					RecipeType<?> recipeType = entry.getKey();
					List<PluginRecipes<?>> pluginRecipesList = entry.getValue();

					try {
						// Merge all recipes for this RecipeType
						List<Object> mergedRecipes = new ArrayList<>();
						for (PluginRecipes<?> pluginRecipes : pluginRecipesList) {
							mergedRecipes.addAll(pluginRecipes.recipes());
						}

						// Register merged recipes
						recipeRegistration.addRecipes((RecipeType<Object>) recipeType, mergedRecipes);
					} catch (Exception e) {
						LOGGER.error("Error registering recipes for RecipeType: {}", recipeType, e);
					}
				}
			}
		}

		/**
		 * Determine if parallel registration should be used.
		 */
		private boolean shouldUseParallelRegistration() {
			int totalRecipeTypes = recipeMap.size();
			int totalRecipes = recipeMap.values().stream()
				.mapToInt(list -> list.stream()
					.mapToInt(PluginRecipes::recipeCount)
					.sum())
				.sum();

			return DebugConfig.isParallelSearchEnabled() &&
				(totalRecipeTypes >= MIN_RECIPE_TYPES_FOR_PARALLEL || totalRecipes >= MIN_RECIPES_FOR_PARALLEL);
		}

		/**
		 * Record of recipes from a single plugin.
		 */
		private record PluginRecipes<T>(List<T> recipes, String pluginUid) {
			public int recipeCount() {
				return recipes.size();
			}
		}
	}

	/**
	 * Wrapper RecipeRegistration that collects recipes instead of immediately registering them.
	 */
	private static class CollectingRecipeRegistration implements IRecipeRegistration {
		private final IRecipeRegistration delegate;
		private final RecipeCollector collector;
		private final String pluginUid;

		public CollectingRecipeRegistration(
			IRecipeRegistration delegate,
			RecipeCollector collector,
			String pluginUid
		) {
			this.delegate = delegate;
			this.collector = collector;
			this.pluginUid = pluginUid;
		}

		@Override
		public <T> void addRecipes(RecipeType<T> recipeType, List<T> recipes) {
			// Collect instead of registering
			collector.addRecipes(recipeType, recipes, pluginUid);
		}

		// Delegate all other methods
		@Override
		public mezz.jei.api.helpers.IJeiHelpers getJeiHelpers() {
			return delegate.getJeiHelpers();
		}

		@Override
		public mezz.jei.api.runtime.IIngredientManager getIngredientManager() {
			return delegate.getIngredientManager();
		}

		@Override
		public mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory getVanillaRecipeFactory() {
			return delegate.getVanillaRecipeFactory();
		}

		@Override
		public <T> void addIngredientInfo(T ingredient, mezz.jei.api.ingredients.IIngredientType<T> ingredientType, net.minecraft.network.chat.Component... descriptionComponents) {
			delegate.addIngredientInfo(ingredient, ingredientType, descriptionComponents);
		}

		@Override
		public <T> void addIngredientInfo(List<T> ingredients, mezz.jei.api.ingredients.IIngredientType<T> ingredientType, net.minecraft.network.chat.Component... descriptionComponents) {
			delegate.addIngredientInfo(ingredients, ingredientType, descriptionComponents);
		}
	}
}
