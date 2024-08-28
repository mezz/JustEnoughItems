package mezz.jei.library.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.codecs.EnumCodec;
import mezz.jei.library.ingredients.IngredientVisibility;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EditModeConfig implements IEditModeConfig {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<Object, Pair<HideMode, ITypedIngredient<?>>> blacklist = new LinkedHashMap<>();
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
		HideMode blacklistType
	) {
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return addIngredientToConfigBlacklistInternal(typedIngredient, blacklistType, ingredientHelper);
	}

	private <V> boolean addIngredientToConfigBlacklistInternal(
		ITypedIngredient<V> typedIngredient,
		HideMode blacklistType,
		IIngredientHelper<V> ingredientHelper
	) {
		Object wildcardUid = getIngredientUid(typedIngredient, HideMode.WILDCARD, ingredientHelper);

		if (blacklistType == HideMode.SINGLE) {
			Object uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);

			if (wildcardUid.equals(uid)) {
				// there's only one type of this ingredient, adding it as ITEM the same as adding it as WILDCARD.
				return blacklist.put(wildcardUid, new Pair<>(blacklistType, typedIngredient)) == null;
			}

			return blacklist.put(uid, new Pair<>(blacklistType, typedIngredient)) == null;
		} else if (blacklistType == HideMode.WILDCARD) {
			return blacklist.put(wildcardUid, new Pair<>(blacklistType, typedIngredient)) == null;
		}

		return false;
	}

	public <V> void removeIngredientFromConfigBlacklist(
		ITypedIngredient<V> typedIngredient,
		HideMode blacklistType,
		IIngredientHelper<V> ingredientHelper
	) {
		final Object uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);
		if (blacklist.remove(uid) != null) {
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
		final Object singleUid = getIngredientUid(ingredient, HideMode.SINGLE, ingredientHelper);
		final Object wildcardUid = getIngredientUid(ingredient, HideMode.WILDCARD, ingredientHelper);
		if (singleUid.equals(wildcardUid)) {
			if (blacklist.containsKey(singleUid)) {
				return Set.of(HideMode.SINGLE);
			}
			return Set.of();
		}

		Set<HideMode> set = new HashSet<>();
		if (blacklist.containsKey(singleUid)) {
			set.add(HideMode.SINGLE);
		}
		if (blacklist.containsKey(wildcardUid)) {
			set.add(HideMode.WILDCARD);
		}
		return Collections.unmodifiableSet(set);
	}

	public <V> boolean isIngredientOnConfigBlacklist(ITypedIngredient<V> typedIngredient, HideMode blacklistType, IIngredientHelper<V> ingredientHelper) {
		final Object uid = getIngredientUid(typedIngredient, blacklistType, ingredientHelper);
		return blacklist.containsKey(uid);
	}

	private static <V> Object getIngredientUid(ITypedIngredient<V> typedIngredient, HideMode blacklistType, IIngredientHelper<V> ingredientHelper) {
		final V ingredient = typedIngredient.getIngredient();
		return switch (blacklistType) {
			case SINGLE -> ingredientHelper.getUid(ingredient, UidContext.Ingredient);
			case WILDCARD -> ingredientHelper.getGroupingUid(ingredient);
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
		private final Codec<List<Pair<HideMode, ITypedIngredient<?>>>> listCodec;
		private final RegistryOps<JsonElement> registryOps;

		public FileSerializer(Path path, RegistryAccess registryAccess, ICodecHelper codecHelper) {
			this.path = path;
			Codec<Pair<HideMode, ITypedIngredient<?>>> elementCodec = RecordCodecBuilder.create(builder -> {
				return builder.group(
					EnumCodec.create(HideMode.class, HideMode::valueOf)
						.fieldOf("hide_mode")
						.forGetter(Pair::getFirst),
					codecHelper.getTypedIngredientCodec().codec()
						.fieldOf("ingredient")
						.forGetter(Pair::getSecond)
				).apply(builder, Pair::new);
			});
			this.listCodec = Codec.list(elementCodec);
			this.registryOps = registryAccess.createSerializationContext(JsonOps.COMPRESSED);
		}

		@Override
		public void initialize(EditModeConfig config) {
			if (!Files.exists(path)) {
				save(config);
			}
		}

		@Override
		public void save(EditModeConfig config) {
			List<Pair<HideMode, ITypedIngredient<?>>> values = List.copyOf(config.blacklist.values());

			DataResult<JsonElement> results = listCodec.encodeStart(registryOps, values);
			results.ifError(error -> {
				LOGGER.error("Encountered errors when saving the blacklist config to file {}\n{}", path, error);
			});

			if (results.hasResultOrPartial()) {
				try (JsonWriter jsonWriter = new JsonWriter(Files.newBufferedWriter(path))) {
					Gson gson = new Gson();
					JsonElement jsonElement = results.getPartialOrThrow();
					gson.toJson(jsonElement, jsonWriter);
					jsonWriter.flush();
					LOGGER.debug("Saved blacklist config to file: {}", path);
				} catch (IOException e) {
					LOGGER.error("Failed to save blacklist config to file {}", path, e);
				}
			}
		}

		@Override
		public void load(EditModeConfig config) {
			if (!Files.exists(path)) {
				return;
			}
			try {
				JsonElement jsonElement = JsonParser.parseReader(Files.newBufferedReader(path));
				DataResult<Pair<List<Pair<HideMode, ITypedIngredient<?>>>, JsonElement>> results = listCodec.decode(registryOps, jsonElement);
				results.ifError(error -> {
					LOGGER.error("Encountered errors when loading the blacklist config from file {}\n{}", path, error);
				});

				if (results.hasResultOrPartial()) {
					config.blacklist.clear();
					List<Pair<HideMode, ITypedIngredient<?>>> list = results.getPartialOrThrow().getFirst();
					for (Pair<HideMode, ITypedIngredient<?>> pair : list) {
						config.addIngredientToConfigBlacklistInternal(pair.getSecond(), pair.getFirst());
					}
				}
			} catch (IOException | IllegalArgumentException e) {
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
