package mezz.jei.common.gui;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.DebugConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Cache for tooltip components to avoid redundant calculations.
 * Tooltips for the same ingredient are cached and reused across frames.
 */
public class TooltipCache {
	private static final Logger LOGGER = LogManager.getLogger();

	// Cache for tooltip lines - uses soft values to allow GC under memory pressure
	private static final Cache<TooltipCacheKey, List<mezz.jei.api.gui.builder.ITooltipBuilder>> TOOLTIP_CACHE =
		CacheBuilder.newBuilder()
			.maximumSize(10000)  // Max 10k cached tooltips
			.expireAfterAccess(5, TimeUnit.MINUTES)  // Expire after 5 minutes of no access
			.concurrencyLevel(16)  // Allow 16 concurrent accesses
			.softValues()  // Use soft references for values (GC-friendly)
			.build();

	// Cache for mod names (expensive lookup via IModIdHelper)
	private static final Cache<String, String> MOD_NAME_CACHE =
		CacheBuilder.newBuilder()
			.maximumSize(5000)  // Max 5k mod names
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.concurrencyLevel(8)
			.build();

	/**
	 * Get or compute tooltip for an ingredient.
	 */
	public static <T> List<mezz.jei.api.gui.builder.ITooltipBuilder> getOrCreateTooltip(
		ITypedIngredient<T> typedIngredient,
		TooltipComputer<T> computer
	) {
		if (!DebugConfig.isTooltipCacheEnabled()) {
			// Cache disabled, compute directly
			return List.of(computer.compute());
		}

		TooltipCacheKey key = new TooltipCacheKey(typedIngredient);
		try {
			return TOOLTIP_CACHE.get(key, () -> {
				mezz.jei.api.gui.builder.ITooltipBuilder builder = computer.compute();
				return List.of(builder);
			});
		} catch (ExecutionException e) {
			LOGGER.warn("Failed to get tooltip from cache", e);
			return List.of(computer.compute());
		}
	}

	/**
	 * Get or compute mod name for an ingredient.
	 */
	public static Optional<String> getOrCreateModName(ITypedIngredient<?> typedIngredient) {
		if (!DebugConfig.isTooltipCacheEnabled()) {
			return computeModName(typedIngredient);
		}

		String cacheKey = getModNameCacheKey(typedIngredient);
		try {
			String modName = MOD_NAME_CACHE.get(cacheKey, () ->
				computeModName(typedIngredient).orElse("")
			);
			return modName.isEmpty() ? Optional.empty() : Optional.of(modName);
		} catch (ExecutionException e) {
			LOGGER.warn("Failed to get mod name from cache", e);
			return computeModName(typedIngredient);
		}
	}

	private static <T> Optional<String> computeModName(ITypedIngredient<T> typedIngredient) {
		try {
			IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
			IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
			String modId = ingredientHelper.getDisplayModId(typedIngredient.getIngredient());
			return Optional.of(modId);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private static <T> String getModNameCacheKey(ITypedIngredient<T> typedIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return ingredientHelper.getUniqueId(typedIngredient.getIngredient(),
			mezz.jei.api.ingredients.subtypes.UidContext.Ingredient);
	}

	/**
	 * Invalidate cache for a specific ingredient.
	 */
	public static <T> void invalidate(ITypedIngredient<T> typedIngredient) {
		TooltipCacheKey key = new TooltipCacheKey(typedIngredient);
		TOOLTIP_CACHE.invalidate(key);

		String modNameKey = getModNameCacheKey(typedIngredient);
		MOD_NAME_CACHE.invalidate(modNameKey);
	}

	/**
	 * Clear all cached tooltips.
	 */
	public static void clear() {
		TOOLTIP_CACHE.invalidateAll();
		MOD_NAME_CACHE.invalidateAll();
	}

	/**
	 * Get cache statistics for debugging.
	 */
	public static String getStats() {
		return String.format(
			"Tooltip Cache - Size: %d, Requests: %d, Hits: %d, Misses: %d",
			TOOLTIP_CACHE.size(),
			TOOLTIP_CACHE.stats().requestCount(),
			TOOLTIP_CACHE.stats().hitRate(),
			TOOLTIP_CACHE.stats().missRate()
		);
	}

	/**
	 * Functional interface for tooltip computation.
	 */
	@FunctionalInterface
	public interface TooltipComputer<T> {
		mezz.jei.api.gui.builder.ITooltipBuilder compute();
	}

	/**
	 * Cache key based on ingredient UID.
	 */
	private record TooltipCacheKey<T>(ITypedIngredient<T> ingredient) {
		@Override
		public int hashCode() {
			try {
				IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
				IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(ingredient.getType());
				return helper.getUniqueId(ingredient.getIngredient(),
					mezz.jei.api.ingredients.subtypes.UidContext.Ingredient).hashCode();
			} catch (Exception e) {
				return System.identityHashCode(ingredient);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof TooltipCacheKey<?> other)) return false;

			try {
				IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
				IIngredientHelper<T> thisHelper = ingredientManager.getIngredientHelper(ingredient.getType());
				@SuppressWarnings("unchecked")
				IIngredientHelper<Object> otherHelper = (IIngredientHelper<Object>) ingredientManager.getIngredientHelper(other.ingredient.getType());

				if (!ingredient.getType().equals(other.ingredient.getType())) {
					return false;
				}

				@SuppressWarnings("unchecked")
				T thisIngredient = (T) ingredient.getIngredient();
				@SuppressWarnings("unchecked")
				Object otherIngredient = other.ingredient.getIngredient();

				String thisUid = thisHelper.getUniqueId(thisIngredient,
					mezz.jei.api.ingredients.subtypes.UidContext.Ingredient);
				String otherUid = otherHelper.getUniqueId(otherIngredient,
					mezz.jei.api.ingredients.subtypes.UidContext.Ingredient);

				return thisUid.equals(otherUid);
			} catch (Exception e) {
				return ingredient.equals(other.ingredient);
			}
		}
	}
}
