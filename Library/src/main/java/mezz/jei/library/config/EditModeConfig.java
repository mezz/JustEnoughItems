package mezz.jei.library.config;


import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.IngredientVisibility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditModeConfig implements IEditModeConfig {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Set<String> blacklist = new HashSet<>();
	private final ISerializer serializer;
	private final IIngredientManager ingredientManager;
	private WeakReference<IngredientVisibility> ingredientVisibilityRef = new WeakReference<>(null);

	public EditModeConfig(ISerializer serializer, IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
		this.serializer = serializer;
		this.serializer.initialize(this);
		this.serializer.load(this);
	}

	public <V> void addIngredientToConfigBlacklist(ITypedIngredient<V> typedIngredient, HideMode blacklistType, IIngredientHelper<V> ingredientHelper) {
		if (addIngredientToConfigBlacklistInternal(typedIngredient, blacklistType, ingredientHelper)) {
			serializer.save(this);
			notifyListenersOfVisibilityChange(typedIngredient, false);
		}
	}

	private <V> boolean addIngredientToConfigBlacklistInternal(
		ITypedIngredient<V> typedIngredient,
		HideMode blacklistType,
		IIngredientHelper<V> ingredientHelper
	) {
		String uid = getIngredientUid(typedIngredient, HideMode.SINGLE, ingredientHelper);
		String wildcardUid = getIngredientUid(typedIngredient, HideMode.WILDCARD, ingredientHelper);

		if (wildcardUid.equals(uid)) {
			// there's only one type of this ingredient, adding it as SINGLE is the same as adding it as WILDCARD.
			blacklistType = HideMode.WILDCARD;
		}

		if (blacklistType == HideMode.SINGLE) {
			return blacklist.add(uid);
		} else if (blacklistType == HideMode.WILDCARD) {
			return blacklist.add(wildcardUid);
		}

		return false;
	}

	public <V> void removeIngredientFromConfigBlacklist(
		ITypedIngredient<V> typedIngredient,
		HideMode blacklistType,
		IIngredientHelper<V> ingredientHelper
	) {
		final String uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);
		if (blacklist.remove(uid)) {
			serializer.save(this);
			notifyListenersOfVisibilityChange(typedIngredient, true);
		}
	}

	public <V> boolean isIngredientOnConfigBlacklist(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		for (HideMode hideMode : HideMode.values()) {
			if (isIngredientOnConfigBlacklist(typedIngredient, hideMode, ingredientHelper)) {
				return true;
			}
		}
		return false;
	}

	private <V> Set<HideMode> getIngredientOnConfigBlacklist(ITypedIngredient<V> ingredient, IIngredientHelper<V> ingredientHelper) {
		final String singleUid = getIngredientUid(ingredient, HideMode.SINGLE, ingredientHelper);
		final String wildcardUid = getIngredientUid(ingredient, HideMode.WILDCARD, ingredientHelper);
		if (singleUid.equals(wildcardUid)) {
			if (blacklist.contains(singleUid)) {
				// there's only one type of this ingredient, adding it as SINGLE is the same as adding it as WILDCARD.
				return Set.of(HideMode.SINGLE, HideMode.WILDCARD);
			}
			return Set.of();
		}

		Set<HideMode> set = new HashSet<>();
		if (blacklist.contains(singleUid)) {
			set.add(HideMode.SINGLE);
		}
		if (blacklist.contains(wildcardUid)) {
			set.add(HideMode.WILDCARD);
		}
		return Collections.unmodifiableSet(set);
	}

	public <V> boolean isIngredientOnConfigBlacklist(ITypedIngredient<V> typedIngredient, HideMode blacklistType, IIngredientHelper<V> ingredientHelper) {
		final String uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);
		return blacklist.contains(uid);
	}

	private static <V> String getIngredientUid(ITypedIngredient<V> typedIngredient, HideMode blacklistType, IIngredientHelper<V> ingredientHelper) {
		final V ingredient = typedIngredient.getIngredient();
		return switch (blacklistType) {
			case SINGLE -> ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
			case WILDCARD -> ingredientHelper.getWildcardId(ingredient);
		};
	}

	@Override
	public <V> boolean isIngredientHiddenUsingConfigFile(ITypedIngredient<V> ingredient) {
		IIngredientType<V> type = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(type);
		return isIngredientOnConfigBlacklist(ingredient, ingredientHelper);
	}

	@Override
	public <V> Set<HideMode> getIngredientHiddenUsingConfigFile(ITypedIngredient<V> ingredient) {
		IIngredientType<V> type = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(type);
		return getIngredientOnConfigBlacklist(ingredient, ingredientHelper);
	}

	@Override
	public <V> void hideIngredientUsingConfigFile(ITypedIngredient<V> ingredient, HideMode hideMode) {
		IIngredientType<V> type = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(type);
		addIngredientToConfigBlacklist(ingredient, hideMode, ingredientHelper);
	}

	@Override
	public <V> void showIngredientUsingConfigFile(ITypedIngredient<V> ingredient, HideMode hideMode) {
		IIngredientType<V> type = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(type);
		removeIngredientFromConfigBlacklist(ingredient, hideMode, ingredientHelper);
	}

	public void registerListener(IngredientVisibility ingredientVisibility) {
		this.ingredientVisibilityRef = new WeakReference<>(ingredientVisibility);
	}

	public interface ISerializer {
		void initialize(EditModeConfig config);
		void save(EditModeConfig config);
		void load(EditModeConfig config);
	}

	public static class FileSerializer implements ISerializer {
		private final Path path;

		public FileSerializer(Path path) {
			this.path = path;
		}

		@Override
		public void initialize(EditModeConfig config) {
			if (!Files.exists(path)) {
				save(config);
			}
		}

		@Override
		public void save(EditModeConfig config) {
			try {
				Files.write(path, config.blacklist);
				LOGGER.debug("Saved blacklist config to file: {}", path);
			} catch (IOException e) {
				LOGGER.error("Failed to save blacklist config to file {}", path, e);
			}
		}

		@Override
		public void load(EditModeConfig config) {
			if (!Files.exists(path)) {
				return;
			}
			try {
				List<String> strings = Files.readAllLines(path);
				config.blacklist.clear();
				config.blacklist.addAll(strings);
			} catch (IOException e) {
				LOGGER.error("Failed to load blacklist from file {}", path, e);
			}
		}
	}

	private <T> void notifyListenersOfVisibilityChange(ITypedIngredient<T> ingredient, boolean visible) {
		IngredientVisibility ingredientVisibility = this.ingredientVisibilityRef.get();
		if (ingredientVisibility != null) {
			ingredientVisibility.notifyListeners(ingredient, visible);
		}
	}
}
