package mezz.jei.common.gui;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.util.JeiThreadFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Async tooltip preloader that prepares tooltip data in the background.
 * This reduces tooltip latency by pre-computing tooltip components before they're needed.
 */
public class TooltipPreloader {
	private static final Logger LOGGER = LogManager.getLogger();

	// Preloaded tooltip data cache
	private static final Map<TooltipCacheKey, PreloadedTooltipData> PRELOADED_TOOLTIPS = new ConcurrentHashMap<>();

	// Maximum number of tooltips to preload
	private static final int MAX_PRELOADED_TOOLTIPS = 100;

	/**
	 * Preload tooltip data for an ingredient asynchronously.
	 * The tooltip will be cached and ready for instant display.
	 */
	public static <T> CompletableFuture<PreloadedTooltipData> preloadTooltip(ITypedIngredient<T> typedIngredient) {
		if (!DebugConfig.isTooltipCacheEnabled()) {
			return CompletableFuture.completedFuture(new PreloadedTooltipData(List.of(), Optional.empty()));
		}

		TooltipCacheKey key = new TooltipCacheKey(typedIngredient);

		// Check if already preloaded
		PreloadedTooltipData existing = PRELOADED_TOOLTIPS.get(key);
		if (existing != null) {
			return CompletableFuture.completedFuture(existing);
		}

		// Preload in background
		return JeiThreadFactory.submitTooltipTask(() -> {
			try {
				PreloadedTooltipData data = computeTooltipData(typedIngredient);
				PRELOADED_TOOLTIPS.put(key, data);

				// Limit cache size
				if (PRELOADED_TOOLTIPS.size() > MAX_PRELOADED_TOOLTIPS) {
					// Remove oldest entry (simple eviction strategy)
					PRELOADED_TOOLTIPS.entrySet().stream()
						.findFirst()
						.ifPresent(e -> PRELOADED_TOOLTIPS.remove(e.getKey()));
				}

				return data;
			} catch (Exception e) {
				LOGGER.warn("Failed to preload tooltip for ingredient", e);
				return new PreloadedTooltipData(List.of(), Optional.empty());
			}
		});
	}

	/**
	 * Get preloaded tooltip data if available.
	 * Returns null if not preloaded yet.
	 */
	public static PreloadedTooltipData getPreloadedTooltip(ITypedIngredient<?> typedIngredient) {
		if (!DebugConfig.isTooltipCacheEnabled()) {
			return null;
		}

		TooltipCacheKey key = new TooltipCacheKey(typedIngredient);
		return PRELOADED_TOOLTIPS.get(key);
	}

	/**
	 * Preload tooltips for multiple ingredients in batch.
	 */
	public static <T> CompletableFuture<Void> preloadTooltipsBatch(List<ITypedIngredient<T>> ingredients) {
		if (!DebugConfig.isTooltipCacheEnabled()) {
			return CompletableFuture.completedFuture(null);
		}

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (ITypedIngredient<T> ingredient : ingredients) {
			futures.add(preloadTooltip(ingredient).thenAccept(data -> {}));
		}

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
	}

	/**
	 * Invalidate preloaded tooltip for an ingredient.
	 */
	public static <T> void invalidate(ITypedIngredient<T> typedIngredient) {
		TooltipCacheKey key = new TooltipCacheKey(typedIngredient);
		PRELOADED_TOOLTIPS.remove(key);
	}

	/**
	 * Clear all preloaded tooltips.
	 */
	public static void clear() {
		PRELOADED_TOOLTIPS.clear();
	}

	/**
	 * Compute tooltip data for an ingredient.
	 */
	private static <T> PreloadedTooltipData computeTooltipData(ITypedIngredient<T> typedIngredient) {
		try {
			IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
			IIngredientType<T> ingredientType = typedIngredient.getType();
			IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
			IModIdHelper modIdHelper = Internal.getJeiRuntime().getJeiHelpers().getModIdHelper();

			// Create tooltip builder
			JeiTooltip tooltip = new JeiTooltip();

			// Get tooltip lines from renderer
			ingredientRenderer.getTooltip(tooltip, typedIngredient.getIngredient(), TooltipFlag.Default.NORMAL);

			// Get mod name as string
			Optional<String> modName = modIdHelper.getModNameForTooltip(typedIngredient)
				.map(Component::getString);

			return new PreloadedTooltipData(tooltip.getLines(), modName);
		} catch (Exception e) {
			LOGGER.warn("Error computing tooltip data", e);
			return new PreloadedTooltipData(List.of(), Optional.empty());
		}
	}

	/**
	 * Preloaded tooltip data container.
	 */
	public record PreloadedTooltipData(
		List<com.mojang.datafixers.util.Either<net.minecraft.network.chat.FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent>> lines,
		Optional<String> modName
	) {}

	/**
	 * Cache key for preloaded tooltips.
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
