package mezz.jei.gui.config;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer.IDeserializeResult;
import mezz.jei.common.config.file.serializers.LegacyTypedIngredientSerializer;
import mezz.jei.common.util.ServerConfigPathUtil;
import mezz.jei.core.util.LoggedTimer;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.IngredientBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.config.file.serializers.LegacyRecipeBookmarkSerializer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class LegacyBookmarkConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LoggedTimer TIMER = new LoggedTimer();

	private static final String MARKER_STACK = "T:";
	private static final String MARKER_INGREDIENT = "I:";
	private static final String LEGACY_MARKER_OTHER = "O:";

	private static final String MARKER_RECIPE = "R:";

	private final Path jeiConfigurationDir;

	public static Optional<Path> getPath(Path jeiConfigurationDir) {
		return ServerConfigPathUtil.getWorldPath(jeiConfigurationDir)
			.flatMap(configPath -> {
				try {
					Files.createDirectories(configPath);
				} catch (IOException e) {
					LOGGER.error("Unable to create bookmark config folder: {}", configPath);
					return Optional.empty();
				}
				Path path = configPath.resolve("bookmarks.ini");
				return Optional.of(path);
			});
	}

	public LegacyBookmarkConfig(Path jeiConfigurationDir) {
		this.jeiConfigurationDir = jeiConfigurationDir;
	}

	@Unmodifiable
	public List<IBookmark> loadBookmarks(
		IRecipeManager recipeManager,
		IFocusFactory focusFactory,
		IIngredientManager ingredientManager,
		RegistryAccess registryAccess
	) {
		return getPath(jeiConfigurationDir)
			.<List<IBookmark>>map(path -> {
				if (!Files.exists(path)) {
					return List.of();
				}
				List<String> lines;
				try {
					lines = Files.readAllLines(path);
				} catch (IOException e) {
					LOGGER.error("Failed to load bookmarks from file {}", path, e);
					return List.of();
				}

				if (lines.isEmpty()) {
					return List.of();
				}

				TIMER.start(String.format("Converting %s legacy bookmarks", lines.size()));

				List<IBookmark> bookmarkList = new ArrayList<>();
				LegacyTypedIngredientSerializer ingredientSerializer = new LegacyTypedIngredientSerializer(ingredientManager);
				LegacyRecipeBookmarkSerializer legacyRecipeBookmarkSerializer = new LegacyRecipeBookmarkSerializer(recipeManager, focusFactory, ingredientSerializer);

				Collection<IIngredientType<?>> otherIngredientTypes = ingredientManager.getRegisteredIngredientTypes()
						.stream()
						.filter(i -> !i.equals(VanillaTypes.ITEM_STACK))
						.toList();

				IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

				for (String line : lines) {
					if (line.startsWith(MARKER_STACK)) {
						String itemStackAsJson = line.substring(MARKER_STACK.length());
						loadItemStackBookmark(registryAccess, itemStackHelper, ingredientManager, itemStackAsJson, bookmarkList);
					} else if (line.startsWith(MARKER_INGREDIENT)) {
						String serializedIngredient = line.substring(MARKER_INGREDIENT.length());
						loadIngredientBookmark(ingredientSerializer, ingredientManager, serializedIngredient, bookmarkList);
					} else if (line.startsWith(LEGACY_MARKER_OTHER)) {
						String uid = line.substring(LEGACY_MARKER_OTHER.length());
						loadLegacyIngredientBookmark(otherIngredientTypes, ingredientManager, uid, bookmarkList);
					} else if (line.startsWith(MARKER_RECIPE)) {
						String serializedRecipe = line.substring(MARKER_RECIPE.length());
						loadRecipeBookmark(legacyRecipeBookmarkSerializer, serializedRecipe, bookmarkList);
					} else {
						LOGGER.error("Failed to load unknown bookmark type:\n{}", line);
					}
				}

				TIMER.stop();

				return bookmarkList;
			})
			.orElseGet(List::of);
	}

	private static void loadItemStackBookmark(
		RegistryAccess registryAccess,
		IIngredientHelper<ItemStack> itemStackHelper,
		IIngredientManager ingredientManager,
		String itemStackAsJson,
		List<IBookmark> bookmarkList
	) {
		try {
			CompoundTag itemStackAsNbt = TagParser.parseTag(itemStackAsJson);
			ItemStack itemStack = ItemStack.parseOptional(registryAccess, itemStackAsNbt);
			if (!itemStack.isEmpty()) {
				ItemStack normalized = itemStackHelper.normalizeIngredient(itemStack);
				Optional<ITypedIngredient<ItemStack>> typedIngredient = ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, normalized);
				if (typedIngredient.isEmpty()) {
					LOGGER.warn("Failed to load bookmarked ItemStack from json string, the item no longer exists:\n{}", itemStackAsJson);
				} else {
					IngredientBookmark<ItemStack> bookmark = IngredientBookmark.create(typedIngredient.get(), ingredientManager);
					bookmarkList.add(bookmark);
				}
			} else {
				LOGGER.warn("Failed to load bookmarked ItemStack from json string, the item is empty:\n{}", itemStackAsJson);
			}
		} catch (CommandSyntaxException e) {
			LOGGER.error("Failed to load bookmarked ItemStack from json string:\n{}", itemStackAsJson, e);
		}
	}

	private static void loadIngredientBookmark(
		LegacyTypedIngredientSerializer ingredientSerializer,
		IIngredientManager ingredientManager,
		String serializedIngredient,
		List<IBookmark> bookmarkList
	) {
		IDeserializeResult<ITypedIngredient<?>> deserialized = ingredientSerializer.deserialize(serializedIngredient);
		Optional<ITypedIngredient<?>> result = deserialized.getResult();
		if (result.isEmpty()) {
			List<String> errors = deserialized.getErrors();
			LOGGER.warn("Failed to load bookmarked ingredient from string: \n{}\n{}", serializedIngredient, String.join(", ", errors));
		} else {
			IngredientBookmark<?> bookmark = IngredientBookmark.create(result.get(), ingredientManager);
			bookmarkList.add(bookmark);
		}
	}

	private static void loadLegacyIngredientBookmark(
		Collection<IIngredientType<?>> otherIngredientTypes,
		IIngredientManager ingredientManager,
		String uid,
		List<IBookmark> bookmarkList
	) {
		Optional<ITypedIngredient<?>> typedIngredient = getLegacyNormalizedIngredientByUid(ingredientManager, otherIngredientTypes, uid);
		if (typedIngredient.isEmpty()) {
			LOGGER.error("Failed to load unknown bookmarked ingredient with uid:\n{}", uid);
		} else {
			IngredientBookmark<?> bookmark = IngredientBookmark.create(typedIngredient.get(), ingredientManager);
			bookmarkList.add(bookmark);
		}
	}

	private static void loadRecipeBookmark(
		LegacyRecipeBookmarkSerializer legacyRecipeBookmarkSerializer,
		String serializedRecipe,
		List<IBookmark> bookmarkList
	) {
		IDeserializeResult<RecipeBookmark<?, ?>> deserialized = legacyRecipeBookmarkSerializer.deserialize(serializedRecipe);
		Optional<RecipeBookmark<?, ?>> result = deserialized.getResult();
		if (result.isEmpty()) {
			List<String> errors = deserialized.getErrors();
			LOGGER.warn("Failed to load bookmarked recipe from string: \n{}\n{}", serializedRecipe, String.join(", ", errors));
		} else {
			bookmarkList.add(result.get());
		}
	}

	@SuppressWarnings("removal")
	private static Optional<ITypedIngredient<?>> getLegacyNormalizedIngredientByUid(IIngredientManager ingredientManager, Collection<IIngredientType<?>> ingredientTypes, String uid) {
		return ingredientTypes.stream()
			.map(t -> ingredientManager.getTypedIngredientByUid(t, uid))
			.<ITypedIngredient<?>>flatMap(Optional::stream)
			.findFirst();
	}
}
