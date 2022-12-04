package mezz.jei.common.config;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EditModeConfig implements IEditModeConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String[] defaultBlacklist = new String[]{};

	private final Set<String> blacklist = new LinkedHashSet<>();
	private final ISerializer serializer;
	private final IIngredientManager ingredientManager;

	public EditModeConfig(ISerializer serializer, IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
		Collections.addAll(blacklist, defaultBlacklist);
		this.serializer = serializer;
		this.serializer.load(this);
	}

	public <V> void addIngredientToConfigBlacklist(ITypedIngredient<V> typedIngredient, IEditModeConfig.Mode blacklistType, IIngredientHelper<V> ingredientHelper) {
		if (addIngredientToConfigBlacklistInternal(typedIngredient, blacklistType, ingredientHelper)) {
			serializer.save(this);
		}
	}

	private <V> boolean addIngredientToConfigBlacklistInternal(
		ITypedIngredient<V> typedIngredient,
		IEditModeConfig.Mode blacklistType,
		IIngredientHelper<V> ingredientHelper
	) {
		String wildcardUid = getIngredientUid(typedIngredient, IEditModeConfig.Mode.WILDCARD, ingredientHelper);

		if (blacklistType == IEditModeConfig.Mode.ITEM) {
			String uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);

			if (wildcardUid.equals(uid)) {
				// there's only one type of this ingredient, adding it as ITEM the same as adding it as WILDCARD.
				return blacklist.add(wildcardUid);
			}

			return blacklist.add(uid);
		} else if (blacklistType == IEditModeConfig.Mode.WILDCARD) {
			return blacklist.add(wildcardUid);
		}

		return false;
	}

	public <V> void removeIngredientFromConfigBlacklist(
		ITypedIngredient<V> typedIngredient,
		IEditModeConfig.Mode blacklistType,
		IIngredientHelper<V> ingredientHelper
	) {
		final String uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);
		if (blacklist.remove(uid)) {
			serializer.save(this);
		}
	}

	public <V> boolean isIngredientOnConfigBlacklist(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		for (IEditModeConfig.Mode ingredientBlacklistType : IEditModeConfig.Mode.values()) {
			if (isIngredientOnConfigBlacklist(typedIngredient, ingredientBlacklistType, ingredientHelper)) {
				return true;
			}
		}
		return false;
	}

	public <V> boolean isIngredientOnConfigBlacklist(ITypedIngredient<V> typedIngredient, IEditModeConfig.Mode blacklistType, IIngredientHelper<V> ingredientHelper) {
		final String uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);
		return blacklist.contains(uid);
	}

	private static <V> String getIngredientUid(ITypedIngredient<V> typedIngredient, IEditModeConfig.Mode blacklistType, IIngredientHelper<V> ingredientHelper) {
		final V ingredient = typedIngredient.getIngredient();
		return switch (blacklistType) {
			case ITEM -> ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
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
	public <V> void hideIngredientUsingConfigFile(ITypedIngredient<V> ingredient, Mode mode) {
		IIngredientType<V> type = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(type);
		addIngredientToConfigBlacklist(ingredient, mode, ingredientHelper);
	}

	@Override
	public <V> void showIngredientUsingConfigFile(ITypedIngredient<V> ingredient, Mode mode) {
		IIngredientType<V> type = ingredient.getType();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(type);
		removeIngredientFromConfigBlacklist(ingredient, mode, ingredientHelper);
	}

	public interface ISerializer {
		void save(EditModeConfig config);
		void load(EditModeConfig config);
	}

	public static class FileSerializer implements ISerializer {
		private final Path path;

		public FileSerializer(Path path) {
			this.path = path;
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
			try {
				List<String> strings = Files.readAllLines(path);
				config.blacklist.clear();
				config.blacklist.addAll(strings);
			} catch (IOException e) {
				LOGGER.error("Failed to load blacklist from file {}", path, e);
			}
		}
	}
}
