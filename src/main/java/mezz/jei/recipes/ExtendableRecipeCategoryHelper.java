package mezz.jei.recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;

public class ExtendableRecipeCategoryHelper<T, W extends IRecipeCategoryExtension> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<RecipeHandler<? extends T, ? extends W>> recipeHandlers = new ArrayList<>();
	private final Set<Class<? extends T>> handledClasses = new HashSet<>();
	private final Map<T, W> cache = new IdentityHashMap<>();
	private final Class<? extends T> expectedRecipeClass;

	public ExtendableRecipeCategoryHelper(Class<? extends T> expectedRecipeClass) {
		this.expectedRecipeClass = expectedRecipeClass;
	}

	public <R extends T> void addRecipeExtensionFactory(Class<? extends R> recipeClass, @Nullable Predicate<R> extensionFilter, Function<R, ? extends W> recipeExtensionFactory) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");
		if (!expectedRecipeClass.isAssignableFrom(recipeClass)) {
			throw new IllegalArgumentException("Recipe handlers must handle a specific class. Needed: " + expectedRecipeClass + " Got: " + recipeClass);
		}
		ErrorUtil.checkNotNull(recipeExtensionFactory, "recipeExtensionFactory");
		if (this.handledClasses.contains(recipeClass)) {
			throw new IllegalArgumentException("A Recipe Extension Factory has already been registered for '" + recipeClass.getName());
		}
		this.handledClasses.add(recipeClass);
		this.recipeHandlers.add(new RecipeHandler<>(recipeClass, extensionFilter, recipeExtensionFactory));
	}


	public <R extends T> W getRecipeExtension(R recipe) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		W recipeExtension = cache.computeIfAbsent(recipe, this::getRecipeExtensionUncached);
		if (recipeExtension == null) {
			String recipeName = ErrorUtil.getNameForRecipe(recipe);
			throw new RuntimeException("Failed to create recipe extension for recipe: " + recipeName);
		}
		return recipeExtension;
	}

	@Nullable
	public <R extends T> W getRecipeExtensionOrNull(R recipe) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		return cache.computeIfAbsent(recipe, this::getRecipeExtensionUncached);
	}

	@Nullable
	private <R extends T> W getRecipeExtensionUncached(R recipe) {
		Class<?> recipeClass = recipe.getClass();

		List<RecipeHandler<R, W>> assignableHandlers = new ArrayList<>();
		// try to find an exact match
		for (RecipeHandler<? extends T, ? extends W> recipeHandler : recipeHandlers) {
			Class<?> handlerRecipeClass = recipeHandler.getRecipeClass();
			if (handlerRecipeClass.isAssignableFrom(recipeClass)) {
				@SuppressWarnings("unchecked")
				RecipeHandler<R, W> assignableRecipeHandler = (RecipeHandler<R, W>) recipeHandler;
				if (assignableRecipeHandler.test(recipe)) {
					if (handlerRecipeClass.equals(recipeClass)) {
						return assignableRecipeHandler.apply(recipe);
					}
					// remove any handlers that are super of this one
					assignableHandlers.removeIf(handler -> handler.getRecipeClass().isAssignableFrom(handlerRecipeClass));
					// only add this if it's not a super class of an another assignable handler
					if (assignableHandlers.stream().noneMatch(handler -> handlerRecipeClass.isAssignableFrom(handler.getRecipeClass()))) {
						assignableHandlers.add(assignableRecipeHandler);
					}
				}
			}
		}
		if (assignableHandlers.isEmpty()) {
			return null;
		}
		if (assignableHandlers.size() == 1) {
			RecipeHandler<R, W> recipeHandler = assignableHandlers.get(0);
			return recipeHandler.apply(recipe);
		}

		// try super classes to get closest match
		Class<?> superClass = recipeClass;
		while (!Object.class.equals(superClass)) {
			superClass = superClass.getSuperclass();
			for (RecipeHandler<R, W> recipeHandler : assignableHandlers) {
				if (recipeHandler.getRecipeClass().equals(superClass)) {
					return recipeHandler.apply(recipe);
				}
			}
		}

		List<Class<? extends T>> assignableClasses = assignableHandlers.stream()
			.map(RecipeHandler::getRecipeClass)
			.collect(Collectors.toList());
		LOGGER.warn("Found multiple matching recipe handlers for {}: {}", recipeClass, assignableClasses);
		RecipeHandler<R, W> recipeHandler = assignableHandlers.get(0);
		return recipeHandler.apply(recipe);
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
