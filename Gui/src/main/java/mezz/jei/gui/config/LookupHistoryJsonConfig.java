package mezz.jei.gui.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.codecs.EnumCodec;
import mezz.jei.common.config.file.JsonArrayFileHelper;
import mezz.jei.common.util.DeduplicatingRunner;
import mezz.jei.common.util.ServerConfigPathUtil;
import mezz.jei.gui.bookmarks.BookmarkType;
import mezz.jei.gui.bookmarks.IBookmark;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LookupHistoryJsonConfig implements ILookupHistoryConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Duration SAVE_DELAY_TIME = Duration.ofSeconds(5);
	private static final int VERSION = 1;

	private static final Codec<BookmarkType> TYPE_CODEC = EnumCodec.create(BookmarkType.class);
	private static @Nullable MapCodec<IBookmark> BOOKMARK_CODEC;

	private final Path jeiConfigurationDir;
	private final DeduplicatingRunner delayedSave = new DeduplicatingRunner(SAVE_DELAY_TIME);

	private static MapCodec<IBookmark> getBookmarkCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager, IRecipeManager recipeManager) {
		if (BOOKMARK_CODEC == null) {
			BOOKMARK_CODEC = TYPE_CODEC.dispatchMap(
				"bookmarkType",
				IBookmark::getType,
				type -> type.getCodec(codecHelper, ingredientManager, recipeManager)
			);
		}
		return BOOKMARK_CODEC;
	}

	private static Optional<Path> getPath(Path jeiConfigurationDir) {
		return ServerConfigPathUtil.getWorldPath(jeiConfigurationDir)
			.flatMap(configPath -> {
				try {
					Files.createDirectories(configPath);
				} catch (IOException e) {
					LOGGER.error("Unable to create lookup history config folder: {}", configPath, e);
					return Optional.empty();
				}
				Path path = configPath.resolve("lookupHistory.json");
				return Optional.of(path);
			});
	}

	public LookupHistoryJsonConfig(Path jeiConfigurationDir) {
		this.jeiConfigurationDir = jeiConfigurationDir;
	}

	private RegistryOps<JsonElement> getRegistryOps(RegistryAccess registryAccess) {
		return registryAccess.createSerializationContext(JsonOps.INSTANCE);
	}

	@Override
	public void save(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess,
		ICodecHelper codecHelper,
		List<IBookmark> bookmarks
	) {
		getPath(jeiConfigurationDir)
			.ifPresent(path -> {
				Codec<IBookmark> bookmarkCodec = getBookmarkCodec(codecHelper, ingredientManager, recipeManager).codec();
				delayedSave.run(() -> {
					save(path, bookmarkCodec, registryAccess, bookmarks);
				});
			});
	}

	private void save(Path path, Codec<IBookmark> bookmarkCodec, RegistryAccess registryAccess, List<IBookmark> bookmarks) {
		RegistryOps<JsonElement> registryOps = getRegistryOps(registryAccess);

		try (BufferedWriter out = Files.newBufferedWriter(path)) {
			JsonArrayFileHelper.write(
				out,
				VERSION,
				bookmarks,
				bookmarkCodec,
				registryOps,
				error -> {
					LOGGER.error("Encountered an error when saving the lookup history config to file {}\n{}", path, error);
				},
				(element, exception) -> {
					LOGGER.error("Encountered an exception when saving the lookup history config to file {}\n{}", path, element, exception);
				}
			);
			LOGGER.debug("Saved lookup history config to file: {}", path);
		} catch (IOException e) {
			LOGGER.error("Failed to save lookup history config to file {}", path, e);
		}
	}

	@Override
	public List<IBookmark> load(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess,
		ICodecHelper codecHelper
	) {
		RegistryOps<JsonElement> registryOps = getRegistryOps(registryAccess);
		return loadJsonBookmarks(ingredientManager, recipeManager, registryOps, codecHelper);
	}

	@Unmodifiable
	private List<IBookmark> loadJsonBookmarks(
		IIngredientManager ingredientManager,
		IRecipeManager recipeManager,
		RegistryOps<JsonElement> registryOps,
		ICodecHelper codecHelper
	) {
		return getPath(jeiConfigurationDir)
			.<List<IBookmark>>map(path -> {
				if (!Files.exists(path)) {
					return List.of();
				}

				List<IBookmark> bookmarks;
				Codec<IBookmark> bookmarkCodec = getBookmarkCodec(codecHelper, ingredientManager, recipeManager).codec();

				try (BufferedReader reader = Files.newBufferedReader(path)) {
					bookmarks = JsonArrayFileHelper.read(
						reader,
						VERSION,
						bookmarkCodec,
						registryOps,
						(element, error) -> {
							LOGGER.error("Encountered an error when loading the lookup history config from file {}\n{}\n{}", path, element, error);
						},
						(element, exception) -> {
							LOGGER.error("Encountered an exception when loading the lookup history config from file {}\n{}", path, element, exception);
						}
					);
					LOGGER.debug("Loaded lookup history config from file: {}", path);
				} catch (RuntimeException | IOException e) {
					LOGGER.error("Failed to load lookup history from file {}", path, e);
					bookmarks = new ArrayList<>();
				}

				return bookmarks;
			})
			.orElseGet(List::of);
	}
}
