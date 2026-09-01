package mezz.jei.gui.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer.IDeserializeResult;
import mezz.jei.common.config.file.JsonArrayFileHelper;
import mezz.jei.common.config.file.serializers.TypedIngredientSerializer;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.DeduplicatingRunner;
import mezz.jei.common.util.PathUtil;
import mezz.jei.common.util.ServerConfigPathUtil;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.IngredientBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.config.file.serializers.RecipeBookmarkSerializer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BookmarkConfig implements IBookmarkConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Duration SAVE_DELAY_TIME = Duration.ofSeconds(5);
	private static final int JSON_VERSION = 1;

	private static final String JSON_KEY_TYPE = "type";
	private static final String JSON_KEY_VALUE = "value";
	private static final String JSON_TYPE_STACK = "item_stack";
	private static final String JSON_TYPE_INGREDIENT = "ingredient";
	private static final String JSON_TYPE_RECIPE = "recipe";

	static final String MARKER_STACK = "T:";
	static final String MARKER_INGREDIENT = "I:";
	static final String LEGACY_MARKER_OTHER = "O:";

	static final String MARKER_RECIPE = "R:";

	private final Path jeiConfigurationDir;
	private final DeduplicatingRunner delayedSave = new DeduplicatingRunner(SAVE_DELAY_TIME);

	private static Optional<Path> getPath(Path jeiConfigurationDir, String fileName) {
		return ServerConfigPathUtil.getWorldPath(jeiConfigurationDir)
			.flatMap(configPath -> {
				try {
					Files.createDirectories(configPath);
				} catch (IOException e) {
					LOGGER.error("Unable to create bookmark config folder: {}", configPath, e);
					return Optional.empty();
				}
				Path path = configPath.resolve(fileName);
				return Optional.of(path);
			});
	}

	private static Optional<Path> getJsonPath(Path jeiConfigurationDir) {
		return getPath(jeiConfigurationDir, "bookmarks.json");
	}

	private static Optional<Path> getLegacyPath(Path jeiConfigurationDir) {
		return getPath(jeiConfigurationDir, "bookmarks.ini");
	}

	public BookmarkConfig(Path jeiConfigurationDir) {
		this.jeiConfigurationDir = jeiConfigurationDir;
	}

	@Override
	public void saveBookmarks(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IGuiHelper guiHelper,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess,
		Collection<IBookmark> bookmarks
	) {
		List<IBookmark> bookmarksSnapshot = List.copyOf(bookmarks);
		getJsonPath(jeiConfigurationDir)
			.ifPresent(path -> {
				delayedSave.run(() -> {
					save(path, recipeManager, focusFactory, ingredientManager, bookmarksSnapshot);
				});
			});
	}

	private static boolean save(
		Path path,
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IIngredientManager ingredientManager,
		Collection<IBookmark> bookmarks
	) {
		TypedIngredientSerializer ingredientSerializer = new TypedIngredientSerializer(ingredientManager);
		RecipeBookmarkSerializer recipeBookmarkSerializer = new RecipeBookmarkSerializer(recipeManager, focusFactory, ingredientSerializer, ingredientManager);

		List<JsonElement> jsonElements = new ArrayList<>();
		for (IBookmark bookmark : bookmarks) {
			JsonObject jsonObject = new JsonObject();
			if (bookmark instanceof IngredientBookmark<?> ingredientBookmark) {
				ITypedIngredient<?> typedIngredient = ingredientBookmark.getIngredient();
				if (typedIngredient.getIngredient() instanceof ItemStack stack) {
					jsonObject.addProperty(JSON_KEY_TYPE, JSON_TYPE_STACK);
					jsonObject.addProperty(JSON_KEY_VALUE, stack.save(new CompoundTag()).toString());
				} else {
					jsonObject.addProperty(JSON_KEY_TYPE, JSON_TYPE_INGREDIENT);
					jsonObject.addProperty(JSON_KEY_VALUE, ingredientSerializer.serialize(typedIngredient));
				}
			} else if (bookmark instanceof RecipeBookmark<?,?> recipeBookmark) {
				jsonObject.addProperty(JSON_KEY_TYPE, JSON_TYPE_RECIPE);
				jsonObject.addProperty(JSON_KEY_VALUE, recipeBookmarkSerializer.serialize(recipeBookmark));
			} else {
				LOGGER.error("Unknown IBookmark type, unable to save it: {}", bookmark.getClass());
				continue;
			}
			jsonElements.add(jsonObject);
		}

		try {
			JsonArrayFileHelper.write(path, JSON_VERSION, jsonElements);
			LOGGER.debug("Saved bookmarks list to file {}", path);
			return true;
		} catch (RuntimeException | IOException e) {
			LOGGER.error("Failed to save bookmarks list to file {}", path, e);
			return false;
		}
	}

	@Override
	public void loadBookmarks(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IGuiHelper guiHelper,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess,
		BookmarkList bookmarkList,
		RecipeTransferService recipeTransferService
	) {
		List<IBookmark> bookmarks = new ArrayList<>();

		getJsonPath(jeiConfigurationDir)
			.ifPresent(path -> bookmarks.addAll(loadJsonBookmarks(recipeManager, focusFactory, ingredientManager, recipeTransferService, path)));

		List<IBookmark> legacyBookmarks = loadLegacyBookmarks(recipeManager, focusFactory, ingredientManager, recipeTransferService);
		if (!legacyBookmarks.isEmpty()) {
			bookmarks.addAll(legacyBookmarks);
			getJsonPath(jeiConfigurationDir)
				.ifPresent(path -> {
					if (save(path, recipeManager, focusFactory, ingredientManager, bookmarks)) {
						backupLegacyBookmarkConfig();
					}
				});
		}

		for (IBookmark bookmark : bookmarks) {
			bookmarkList.addToListWithoutNotifying(bookmark, false);
		}
		if (!bookmarks.isEmpty()) {
			bookmarkList.notifyListenersOfChange();
		}
	}

	private List<IBookmark> loadJsonBookmarks(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IIngredientManager ingredientManager,
		RecipeTransferService recipeTransferService,
		Path path
	) {
		if (!Files.exists(path)) {
			return List.of();
		}

		TypedIngredientSerializer ingredientSerializer = new TypedIngredientSerializer(ingredientManager);
		RecipeBookmarkSerializer recipeBookmarkSerializer = new RecipeBookmarkSerializer(recipeManager, focusFactory, ingredientSerializer, ingredientManager, recipeTransferService);
		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

		List<IBookmark> bookmarks = new ArrayList<>();
		try {
			List<JsonElement> jsonElements = JsonArrayFileHelper.read(path, JSON_VERSION, (element, error) -> {
				LOGGER.error("Encountered error when loading the bookmark config from file {}\n{}\n{}", path, element, error);
			});
			for (JsonElement jsonElement : jsonElements) {
				IBookmark bookmark = loadJsonBookmark(recipeBookmarkSerializer, ingredientSerializer, itemStackHelper, ingredientManager, path, jsonElement);
				if (bookmark != null) {
					bookmarks.add(bookmark);
				}
			}
		} catch (RuntimeException | IOException e) {
			LOGGER.error("Failed to load bookmarks from file {}", path, e);
		}
		return bookmarks;
	}

	private static @Nullable IBookmark loadJsonBookmark(
		RecipeBookmarkSerializer recipeBookmarkSerializer,
		TypedIngredientSerializer ingredientSerializer,
		IIngredientHelper<ItemStack> itemStackHelper,
		IIngredientManager ingredientManager,
		Path path,
		JsonElement jsonElement
	) {
		if (!jsonElement.isJsonObject()) {
			LOGGER.error("Failed to load bookmark from file {}, expected an object:\n{}", path, jsonElement);
			return null;
		}

		JsonObject jsonObject = jsonElement.getAsJsonObject();
		String type = getString(jsonObject, JSON_KEY_TYPE);
		String value = getString(jsonObject, JSON_KEY_VALUE);
		if (type == null || value == null) {
			LOGGER.error("Failed to load bookmark from file {}, expected string '{}' and '{}' fields:\n{}", path, JSON_KEY_TYPE, JSON_KEY_VALUE, jsonElement);
			return null;
		}

		return switch (type) {
			case JSON_TYPE_STACK -> loadItemStackBookmark(itemStackHelper, ingredientManager, value);
			case JSON_TYPE_INGREDIENT -> loadIngredientBookmark(ingredientSerializer, ingredientManager, value);
			case JSON_TYPE_RECIPE -> loadRecipeBookmark(recipeBookmarkSerializer, value);
			default -> {
				LOGGER.error("Failed to load unknown bookmark type from file {}:\n{}", path, jsonElement);
				yield null;
			}
		};
	}

	private static @Nullable String getString(JsonObject jsonObject, String key) {
		JsonElement element = jsonObject.get(key);
		if (element == null || !element.isJsonPrimitive()) {
			return null;
		}
		try {
			return element.getAsString();
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}

	private List<IBookmark> loadLegacyBookmarks(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IIngredientManager ingredientManager,
		RecipeTransferService recipeTransferService
	) {
		return getLegacyPath(jeiConfigurationDir)
			.<List<IBookmark>>map(path -> loadLegacyBookmarks(path, recipeManager, focusFactory, ingredientManager, recipeTransferService))
			.orElseGet(List::of);
	}

	private static List<IBookmark> loadLegacyBookmarks(
		Path path,
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IIngredientManager ingredientManager,
		RecipeTransferService recipeTransferService
	) {
		if (!Files.exists(path)) {
			return List.of();
		}

		List<String> lines;
		try {
			lines = Files.readAllLines(path);
		} catch (IOException e) {
			LOGGER.error("Failed to load legacy bookmarks from file {}", path, e);
			return List.of();
		}

		TypedIngredientSerializer ingredientSerializer = new TypedIngredientSerializer(ingredientManager);
		RecipeBookmarkSerializer recipeBookmarkSerializer = new RecipeBookmarkSerializer(recipeManager, focusFactory, ingredientSerializer, ingredientManager, recipeTransferService);

		Collection<IIngredientType<?>> otherIngredientTypes = ingredientManager.getRegisteredIngredientTypes()
			.stream()
			.filter(i -> !i.equals(VanillaTypes.ITEM_STACK))
			.toList();

		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

		List<IBookmark> bookmarks = new ArrayList<>();
		for (String line : lines) {
			IBookmark bookmark = loadLegacyBookmarkLine(ingredientSerializer, recipeBookmarkSerializer, itemStackHelper, ingredientManager, otherIngredientTypes, line);
			if (bookmark != null) {
				bookmarks.add(bookmark);
			}
		}
		return bookmarks;
	}

	private static @Nullable IBookmark loadLegacyBookmarkLine(
		TypedIngredientSerializer ingredientSerializer,
		RecipeBookmarkSerializer recipeBookmarkSerializer,
		IIngredientHelper<ItemStack> itemStackHelper,
		IIngredientManager ingredientManager,
		Collection<IIngredientType<?>> otherIngredientTypes,
		String line
	) {
		if (line.startsWith(MARKER_STACK)) {
			String itemStackAsJson = line.substring(MARKER_STACK.length());
			return loadItemStackBookmark(itemStackHelper, ingredientManager, itemStackAsJson);
		}
		if (line.startsWith(MARKER_INGREDIENT)) {
			String serializedIngredient = line.substring(MARKER_INGREDIENT.length());
			return loadIngredientBookmark(ingredientSerializer, ingredientManager, serializedIngredient);
		}
		if (line.startsWith(LEGACY_MARKER_OTHER)) {
			String uid = line.substring(LEGACY_MARKER_OTHER.length());
			return loadLegacyIngredientBookmark(otherIngredientTypes, ingredientManager, uid);
		}
		if (line.startsWith(MARKER_RECIPE)) {
			String serializedRecipe = line.substring(MARKER_RECIPE.length());
			return loadRecipeBookmark(recipeBookmarkSerializer, serializedRecipe);
		}
		LOGGER.error("Failed to load unknown legacy bookmark type:\n{}", line);
		return null;
	}

	private void backupLegacyBookmarkConfig() {
		getLegacyPath(jeiConfigurationDir)
			.ifPresent(path -> {
				if (!Files.exists(path)) {
					return;
				}
				try {
					Path backupPath = path.resolveSibling(path.getFileName() + ".bak");
					PathUtil.moveAtomicReplace(path, backupPath);
					LOGGER.info("Backed up legacy bookmarks config file to '{}'", backupPath);
				} catch (IOException e) {
					LOGGER.error("Failed to back up legacy bookmarks config file '{}'", path, e);
				}
			});
	}

	static @Nullable IBookmark loadItemStackBookmark(
		IIngredientHelper<ItemStack> itemStackHelper,
		IIngredientManager ingredientManager,
		String itemStackAsJson
	) {
		try {
			CompoundTag itemStackAsNbt = TagParser.parseTag(itemStackAsJson);
			ItemStack itemStack = ItemStack.of(itemStackAsNbt);
			if (!itemStack.isEmpty()) {
				ItemStack normalized = itemStackHelper.normalizeIngredient(itemStack);
				Optional<ITypedIngredient<ItemStack>> typedIngredient = ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, normalized);
				if (typedIngredient.isEmpty()) {
					LOGGER.warn("Failed to load bookmarked ItemStack from json string, the item no longer exists:\n{}", itemStackAsJson);
				} else {
					return IngredientBookmark.create(typedIngredient.get(), ingredientManager);
				}
			} else {
				LOGGER.warn("Failed to load bookmarked ItemStack from json string, the item is empty:\n{}", itemStackAsJson);
			}
		} catch (CommandSyntaxException e) {
			LOGGER.error("Failed to load bookmarked ItemStack from json string:\n{}", itemStackAsJson, e);
		}
		return null;
	}

	static @Nullable IBookmark loadIngredientBookmark(
		TypedIngredientSerializer ingredientSerializer,
		IIngredientManager ingredientManager,
		String serializedIngredient
	) {
		IDeserializeResult<ITypedIngredient<?>> deserialized = ingredientSerializer.deserialize(serializedIngredient);
		Optional<ITypedIngredient<?>> result = deserialized.getResult();
		if (result.isEmpty()) {
			List<String> errors = deserialized.getErrors();
			LOGGER.warn("Failed to load bookmarked ingredients from string: \n{}\n{}", serializedIngredient, String.join(", ", errors));
		} else {
			return IngredientBookmark.create(result.get(), ingredientManager);
		}
		return null;
	}

	private static @Nullable IBookmark loadLegacyIngredientBookmark(
		Collection<IIngredientType<?>> otherIngredientTypes,
		IIngredientManager ingredientManager,
		String uid
	) {
		Optional<ITypedIngredient<?>> typedIngredient = getLegacyNormalizedIngredientByUid(ingredientManager, otherIngredientTypes, uid);
		if (typedIngredient.isEmpty()) {
			LOGGER.error("Failed to load unknown bookmarked ingredient with uid:\n{}", uid);
		} else {
			return IngredientBookmark.create(typedIngredient.get(), ingredientManager);
		}
		return null;
	}

	static @Nullable IBookmark loadRecipeBookmark(
		RecipeBookmarkSerializer recipeBookmarkSerializer,
		String serializedRecipe
	) {
		IDeserializeResult<RecipeBookmark<?, ?>> deserialized = recipeBookmarkSerializer.deserialize(serializedRecipe);
		Optional<RecipeBookmark<?, ?>> result = deserialized.getResult();
		if (result.isEmpty()) {
			List<String> errors = deserialized.getErrors();
			LOGGER.warn("Failed to load bookmarked recipe from string: \n{}\n{}", serializedRecipe, String.join(", ", errors));
		} else {
			return result.get();
		}
		return null;
	}

	private static Optional<ITypedIngredient<?>> getLegacyNormalizedIngredientByUid(IIngredientManager ingredientManager, Collection<IIngredientType<?>> ingredientTypes, String uid) {
		return ingredientTypes.stream()
			.map(t -> ingredientManager.getTypedIngredientByUid(t, uid))
			.<ITypedIngredient<?>>flatMap(Optional::stream)
			.findFirst();
	}
}
