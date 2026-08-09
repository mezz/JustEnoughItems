package mezz.jei.common.recipes;

import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IBrewingCategoryExtension;
import mezz.jei.common.util.ErrorUtil;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BrewingExtensionHelper {
	private final Map<Class<?>, IBrewingCategoryExtension<?>> extensions = new LinkedHashMap<>();

	public <R> void addRecipeExtension(
		Class<? extends R> recipeClass,
		IBrewingCategoryExtension<R> extension
	) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");
		ErrorUtil.checkNotNull(extension, "extension");
		if (extensions.containsKey(recipeClass)) {
			throw new IllegalArgumentException("An extension has already been registered for: " + recipeClass);
		}
		extensions.put(recipeClass, extension);
	}

	@Nullable
	public <R> IBrewingCategoryExtension<? super R> getRecipeExtension(R recipe) {
		Class<?> recipeClass = recipe.getClass();
		IBrewingCategoryExtension<?> exactExtension = extensions.get(recipeClass);
		if (exactExtension != null) {
			return castExtension(exactExtension);
		}

		for (Map.Entry<Class<?>, IBrewingCategoryExtension<?>> entry : extensions.entrySet()) {
			if (entry.getKey().isInstance(recipe)) {
				return castExtension(entry.getValue());
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <R> IBrewingCategoryExtension<? super R> castExtension(IBrewingCategoryExtension<?> extension) {
		return (IBrewingCategoryExtension<? super R>) extension;
	}
}
