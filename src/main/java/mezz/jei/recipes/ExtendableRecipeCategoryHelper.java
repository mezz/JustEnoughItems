package mezz.jei.recipes;

import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ExtendableRecipeCategoryHelper<T, W extends IRecipeCategoryExtension> {
	private static final Logger LOGGER = LogManager.getLogger();

	// TODO: To reduce memory usage, IRecipeCategoryExtension should not wrap a recipe,
	//  it should accept a recipe as a parameter on every method.

	private final List<RecipeHandler<? extends T, ? extends W>> recipeHandlers = new ArrayList<>();
	private final Set<Class<? extends T>> handledClasses = new HashSet<>();
	private final Map<T, @Nullable W> cache = new IdentityHashMap<>();
	private final Class<? extends T> expectedRecipeClass;

	public ExtendableRecipeCategoryHelper(Class<? extends T> expectedRecipeClass) {
		this.expectedRecipeClass = expectedRecipeClass;
	}

	public <R extends T> void addRecipeExtensionFactory(Class<? extends R> recipeClass, @Nullable Predicate<R> extensionFilter, Function<R, ? extends W> recipeExtensionFactory) {
		if (!expectedRecipeClass.isAssignableFrom(recipeClass)) {
			throw new IllegalArgumentException("Recipe handlers must handle a specific class. Needed: " + expectedRecipeClass + " Got: " + recipeClass);
		}
		if (this.handledClasses.contains(recipeClass)) {
			throw new IllegalArgumentException("A Recipe Extension Factory has already been registered for '" + recipeClass.getName());
		}
		this.handledClasses.add(recipeClass);
		this.recipeHandlers.add(new RecipeHandler<>(recipeClass, extensionFilter, recipeExtensionFactory));
	}

	public <R extends T> W getRecipeExtension(R recipe) {
		return getOptionalRecipeExtension(recipe)
			.orElseThrow(() -> {
				String recipeName = ErrorUtil.getNameForRecipe(recipe);
				return new RuntimeException("Failed to create recipe extension for recipe: " + recipeName);
			});
	}

	public <R extends T> Optional<W> getOptionalRecipeExtension(R recipe) {
		if (cache.containsKey(recipe)) {
			return Optional.ofNullable(cache.get(recipe));
		}

		Optional<W> result = getBestRecipeHandler(recipe)
			.map(handler -> handler.apply(recipe));

		cache.put(recipe, result.orElse(null));

		return result;
	}

	private <R> Stream<RecipeHandler<R, W>> getRecipeHandlerStream(R recipe) {
		@SuppressWarnings("unchecked")
		Class<? extends R> recipeClass = (Class<? extends R>) recipe.getClass();
		return recipeHandlers.stream()
			.filter(recipeHandler -> recipeHandler.getRecipeClass().isAssignableFrom(recipeClass))
			.map(recipeHandler -> {
				@SuppressWarnings("unchecked")
				RecipeHandler<R, W> cast = (RecipeHandler<R, W>) recipeHandler;
				return cast;
			})
			.filter(recipeHandler -> recipeHandler.test(recipe));
	}

	private <R extends T> Optional<RecipeHandler<R, W>> getBestRecipeHandler(R recipe) {
		Class<?> recipeClass = recipe.getClass();

		List<RecipeHandler<R, W>> assignableHandlers = new ArrayList<>();
		// try to find an exact match
		List<RecipeHandler<R, W>> allHandlers = getRecipeHandlerStream(recipe).toList();
		for (RecipeHandler<R, W> recipeHandler : allHandlers) {
			Class<? extends T> handlerRecipeClass = recipeHandler.getRecipeClass();
			if (handlerRecipeClass.equals(recipeClass)) {
				return Optional.of(recipeHandler);
			}
			// remove any handlers that are super of this one
			assignableHandlers.removeIf(handler -> handler.getRecipeClass().isAssignableFrom(handlerRecipeClass));
			// only add this if it's not a super class of another assignable handler
			if (assignableHandlers.stream().noneMatch(handler -> handlerRecipeClass.isAssignableFrom(handler.getRecipeClass()))) {
				assignableHandlers.add(recipeHandler);
			}
		}
		if (assignableHandlers.isEmpty()) {
			return Optional.empty();
		}
		if (assignableHandlers.size() == 1) {
			return Optional.of(assignableHandlers.get(0));
		}

		// try super classes to get the closest match
		Class<?> superClass = recipeClass;
		while (!Object.class.equals(superClass)) {
			superClass = superClass.getSuperclass();
			for (RecipeHandler<R, W> recipeHandler : assignableHandlers) {
				if (recipeHandler.getRecipeClass().equals(superClass)) {
					return Optional.of(recipeHandler);
				}
			}
		}

		List<Class<? extends T>> assignableClasses = assignableHandlers.stream()
			.<Class<? extends T>>map(RecipeHandler::getRecipeClass)
			.toList();
		LOGGER.warn("Found multiple matching recipe handlers for {}: {}", recipeClass, assignableClasses);
		return Optional.of(assignableHandlers.get(0));
	}

	public static class RecipeHandler<T, W extends IRecipeCategoryExtension> {
		private final Class<? extends T> recipeClass;
		private final Function<T, W> factory;
		@Nullable
		private final Predicate<T> filter;

		public RecipeHandler(Class<? extends T> recipeClass, @Nullable Predicate<T> filter, Function<T, W> factory) {
			this.recipeClass = recipeClass;
			this.factory = factory;
			this.filter = filter;
		}

		public Class<? extends T> getRecipeClass() {
			return recipeClass;
		}

		public boolean test(T t) {
			if (filter == null) {
				return true;
			}
			return filter.test(t);
		}

		public W apply(T t) {
			return factory.apply(t);
		}
	}

}
