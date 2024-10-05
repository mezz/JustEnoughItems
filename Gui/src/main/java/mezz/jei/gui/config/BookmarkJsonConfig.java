package mezz.jei.gui.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.codecs.EnumCodec;
import mezz.jei.common.config.file.JsonArrayFileHelper;
import mezz.jei.common.util.ServerConfigPathUtil;
import mezz.jei.core.util.PathUtil;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.BookmarkType;
import mezz.jei.gui.bookmarks.IBookmark;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookmarkJsonConfig implements IBookmarkConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int VERSION = 2;

	private static final Codec<BookmarkType> TYPE_CODEC = EnumCodec.create(BookmarkType.class, BookmarkType::valueOf);
	private static @Nullable MapCodec<IBookmark> BOOKMARK_CODEC;

	@SuppressWarnings("deprecation")
	private final LegacyBookmarkConfig legacyBookmarkConfig;
	private final Path jeiConfigurationDir;

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
					LOGGER.error("Unable to create bookmark config folder: {}", configPath);
					return Optional.empty();
				}
				Path path = configPath.resolve("bookmarks.json");
				return Optional.of(path);
			});
	}

	public BookmarkJsonConfig(Path jeiConfigurationDir) {
		this.jeiConfigurationDir = jeiConfigurationDir;
		//noinspection deprecation
		this.legacyBookmarkConfig = new LegacyBookmarkConfig(jeiConfigurationDir);
	}

	private RegistryOps<JsonElement> getRegistryOps(RegistryAccess registryAccess) {
		return registryAccess.createSerializationContext(JsonOps.INSTANCE);
	}

	@Override
	public boolean saveBookmarks(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IGuiHelper guiHelper,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess,
		ICodecHelper codecHelper,
		List<IBookmark> bookmarks
	) {
		return getPath(jeiConfigurationDir)
			.map(path -> {
				Codec<IBookmark> bookmarkCodec = getBookmarkCodec(codecHelper, ingredientManager, recipeManager).codec();
				RegistryOps<JsonElement> registryOps = getRegistryOps(registryAccess);

				try (BufferedWriter out = Files.newBufferedWriter(path)) {
					JsonArrayFileHelper.write(
						out,
						VERSION,
						bookmarks,
						bookmarkCodec,
						registryOps,
						error -> {
							LOGGER.error("Encountered an error when saving the bookmarks config to file {}\n{}", path, error);
						},
						(element, exception) -> {
							LOGGER.error("Encountered an exception when saving the bookmarks config to file {}\n{}", path, element, exception);
						}
					);
					LOGGER.debug("Saved bookmarks config to file: {}", path);
					return true;
				} catch (IOException e) {
					LOGGER.error("Failed to save bookmarks config to file {}", path, e);
					return false;
				}
			})
			.orElse(false);
	}

	@Override
	public void loadBookmarks(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IGuiHelper guiHelper,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess,
		BookmarkList bookmarkList,
		ICodecHelper codecHelper
	) {
		RegistryOps<JsonElement> registryOps = getRegistryOps(registryAccess);
		List<IBookmark> bookmarks = loadJsonBookmarks(ingredientManager, recipeManager, registryOps, codecHelper);

		List<IBookmark> legacyIniBookmarks = legacyBookmarkConfig.loadBookmarks(recipeManager, focusFactory, ingredientManager, registryAccess);
		List<IBookmark> legacyCompressedBookmarks = loadLegacyCompressedJsonBookmarks(ingredientManager, recipeManager, registryAccess, codecHelper);

		List<IBookmark> legacyBookmarks = new ArrayList<>();
		legacyBookmarks.addAll(legacyIniBookmarks);
		legacyBookmarks.addAll(legacyCompressedBookmarks);

		if (!legacyBookmarks.isEmpty()) {
			bookmarks = new ArrayList<>(bookmarks);
			bookmarks.addAll(legacyBookmarks);

			getPath(jeiConfigurationDir)
				.ifPresent(legacyJsonPath -> {
					if (Files.exists(legacyJsonPath)) {
						try {
							Path backupPath = legacyJsonPath.resolveSibling(legacyJsonPath.getFileName() + ".bak");
							PathUtil.moveAtomicReplace(legacyJsonPath, backupPath);
							LOGGER.info("Backed up legacy json compressed bookmarks config file to '{}'", backupPath);
						} catch (IOException e) {
							LOGGER.error("Failed to back up legacy json compressed bookmarks config file '{}'", legacyJsonPath, e);
						}
					}
				});

			if (saveBookmarks(recipeManager, focusFactory, guiHelper, ingredientManager, registryAccess, codecHelper, bookmarks)) {
				//noinspection deprecation
				LegacyBookmarkConfig.getPath(jeiConfigurationDir)
					.ifPresent(legacyPath -> {
						if (Files.exists(legacyPath)) {
							try {
								Path backupPath = legacyPath.resolveSibling(legacyPath.getFileName() + ".bak");
								PathUtil.moveAtomicReplace(legacyPath, backupPath);
								LOGGER.info("Backed up legacy bookmarks config file to '{}'", backupPath);
							} catch (IOException e) {
								LOGGER.error("Failed to back up legacy bookmarks config file '{}'", legacyPath, e);
							}
						}
					});
			}
		}

		bookmarkList.setFromConfigFile(bookmarks);
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
							LOGGER.error("Encountered an error when loading the bookmark config from file {}\n{}\n{}", path, element, error);
						},
						(element, exception) -> {
							LOGGER.error("Encountered an exception when loading the bookmark config from file {}\n{}", path, element, exception);
						}
					);
					LOGGER.debug("Loaded bookmarks config from file: {}", path);
				} catch (RuntimeException | IOException e) {
					LOGGER.error("Failed to load bookmarks from file {}", path, e);
					bookmarks = new ArrayList<>();
				}

				return bookmarks;
			})
			.orElseGet(List::of);
	}

	@Unmodifiable
	private List<IBookmark> loadLegacyCompressedJsonBookmarks(
		IIngredientManager ingredientManager,
		IRecipeManager recipeManager,
		RegistryAccess registryAccess,
		ICodecHelper codecHelper
	) {
		return getPath(jeiConfigurationDir)
			.<List<IBookmark>>map(path -> {
				if (!Files.exists(path)) {
					return List.of();
				}

				List<IBookmark> bookmarks;
				Codec<IBookmark> bookmarkCodec = getBookmarkCodec(codecHelper, ingredientManager, recipeManager).codec();

				RegistryOps<JsonElement> compressedOps = registryAccess.createSerializationContext(JsonOps.COMPRESSED);

				try (BufferedReader reader = Files.newBufferedReader(path)) {
					bookmarks = JsonArrayFileHelper.read(
						reader,
						null,
						bookmarkCodec,
						compressedOps,
						(element, error) -> {
							// ignore errors
						},
						(element, exception) -> {
							// ignore errors
						}
					);
					LOGGER.debug("Loaded legacy compressed json bookmarks config from file: {}", path);
				} catch (RuntimeException | IOException e) {
					LOGGER.error("Failed to load legacy compressed json bookmarks from file {}", path, e);
					bookmarks = new ArrayList<>();
				}

				return bookmarks;
			})
			.orElseGet(List::of);
	}
}
