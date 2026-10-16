package mezz.jei.library.recipes;

import mezz.jei.api.recipe.category.extensions.vanilla.cooking.ICookingCategoryExtension;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.plugins.vanilla.cooking.CookingCategoryExtension;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CookingExtensionHelper {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ICookingCategoryExtension<AbstractCookingRecipe> DEFAULT_EXTENSION = new CookingCategoryExtension();
	private final Map<Class<?>, ICookingCategoryExtension<?>> extensions = new HashMap<>();
	private final Map<Class<?>, @Nullable ICookingCategoryExtension<?>> resolvedExtensions = new HashMap<>();

	public <R> void addExtension(Class<? extends R> recipeClass, ICookingCategoryExtension<R> extension) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");
		ErrorUtil.checkNotNull(extension, "extension");
		if (!recipeClass.isInterface() && !AbstractCookingRecipe.class.isAssignableFrom(recipeClass)) {
			throw new IllegalArgumentException("Cooking extensions must handle a cooking recipe class or interface: " + recipeClass);
		}
		if (extensions.containsKey(recipeClass)) {
			throw new IllegalArgumentException("An extension has already been registered for: " + recipeClass);
		}
		extensions.put(recipeClass, extension);
		resolvedExtensions.clear();
	}

	@SuppressWarnings("unchecked")
	public <R extends AbstractCookingRecipe> @Nullable ICookingCategoryExtension<? super R> getRecipeExtension(R recipe) {
		Class<?> recipeClass = recipe.getClass();
		if (!resolvedExtensions.containsKey(recipeClass)) {
			resolvedExtensions.put(recipeClass, resolveExtension(recipeClass));
		}
		return (ICookingCategoryExtension<? super R>) resolvedExtensions.get(recipeClass);
	}

	private @Nullable ICookingCategoryExtension<?> resolveExtension(Class<?> recipeClass) {
		ICookingCategoryExtension<?> exact = extensions.get(recipeClass);
		if (exact != null) {
			return exact;
		}
		List<Class<?>> matches = extensions.keySet().stream()
			.filter(type -> type.isAssignableFrom(recipeClass))
			.toList();
		if (matches.isEmpty()) {
			return DEFAULT_EXTENSION;
		}
		List<Class<?>> mostSpecific = matches.stream()
			.filter(type -> matches.stream().noneMatch(other -> other != type && type.isAssignableFrom(other)))
			.toList();
		if (mostSpecific.size() == 1) {
			return extensions.get(mostSpecific.getFirst());
		}
		LOGGER.warn("Found multiple matching cooking recipe extensions for {}: {}", recipeClass,
			mostSpecific.stream().map(Class::getName).sorted().toList());
		return null;
	}
}
