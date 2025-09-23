package mezz.jei.gui.config;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.file.serializers.TypedIngredientSerializer;
import mezz.jei.common.util.DeduplicatingRunner;
import mezz.jei.common.util.ServerConfigPathUtil;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.IngredientBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.config.file.serializers.RecipeBookmarkSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static mezz.jei.gui.config.BookmarkConfig.*;

public class LookupHistoryJsonConfig implements ILookupHistoryConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Duration SAVE_DELAY_TIME = Duration.ofSeconds(5);

	private final Path jeiConfigurationDir;
	private final DeduplicatingRunner delayedSave = new DeduplicatingRunner(SAVE_DELAY_TIME);

	private static Optional<Path> getPath(Path jeiConfigurationDir) {
		return ServerConfigPathUtil.getWorldPath(jeiConfigurationDir)
			.flatMap(configPath -> {
				try {
					Files.createDirectories(configPath);
				} catch (IOException e) {
					LOGGER.error("Unable to create lookup history config folder: {}", configPath);
					return Optional.empty();
				}
				Path path = configPath.resolve("lookupHistory.ini");
				return Optional.of(path);
			});
	}

	public LookupHistoryJsonConfig(Path jeiConfigurationDir) {
		this.jeiConfigurationDir = jeiConfigurationDir;
	}

	@Override
	public void save(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		IFocusFactory focusFactory,
		List<IBookmark> bookmarks
	) {
		getPath(jeiConfigurationDir)
			.ifPresent(path -> {
				delayedSave.run(() -> {
					save(path, recipeManager, ingredientManager, focusFactory, bookmarks);
				});
			});
	}

	private void save(
		Path path,
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		IFocusFactory focusFactory,
		List<IBookmark> bookmarks) {
		List<String> strings = new ArrayList<>();
		TypedIngredientSerializer ingredientSerializer = new TypedIngredientSerializer(ingredientManager);
		RecipeBookmarkSerializer recipeBookmarkSerializer = new RecipeBookmarkSerializer(recipeManager, focusFactory, ingredientSerializer);
		for (IBookmark bookmark : bookmarks) {
			if (bookmark instanceof IngredientBookmark<?> ingredientBookmark) {
				ITypedIngredient<?> typedIngredient = ingredientBookmark.getIngredient();
				if (typedIngredient.getIngredient() instanceof ItemStack stack) {
					strings.add(MARKER_STACK + stack.save(new CompoundTag()));
				} else {
					strings.add(MARKER_INGREDIENT + ingredientSerializer.serialize(typedIngredient));
				}
			} else if (bookmark instanceof RecipeBookmark<?, ?> recipeBookmark) {
				strings.add(MARKER_RECIPE + recipeBookmarkSerializer.serialize(recipeBookmark));
			} else {
				LOGGER.error("Unknown IBookmark type, unable to save it: {}", bookmark.getClass());
			}
		}

		try {
			Files.write(path, strings);
			LOGGER.debug("Saved lookup history config to file {}", path);
		} catch (IOException e) {
			LOGGER.error("Failed to save lookup history config to file {}", path, e);
		}
	}

	@Override
	public List<IBookmark> load(
		IRecipeManager recipeManager,
		IIngredientManager ingredientManager,
		IFocusFactory focusFactory
	) {
		return loadBookmarks(ingredientManager, recipeManager, focusFactory);
	}

	@Unmodifiable
	private List<IBookmark> loadBookmarks(
		IIngredientManager ingredientManager,
		IRecipeManager recipeManager,
		IFocusFactory focusFactory
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
					LOGGER.error("Encountered an exception when loading the lookup history config from file {}\n{}", path, e);
					return List.of();
				}

				TypedIngredientSerializer ingredientSerializer = new TypedIngredientSerializer(ingredientManager);
				RecipeBookmarkSerializer recipeBookmarkSerializer = new RecipeBookmarkSerializer(recipeManager, focusFactory, ingredientSerializer);

				IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
				List<IBookmark> bookmarks = new ArrayList<>();
				for (String line : lines) {
					IBookmark bookmark = null;
					if (line.startsWith(MARKER_STACK)) {
						String itemStackAsJson = line.substring(MARKER_STACK.length());
						bookmark = loadItemStackBookmark(itemStackHelper, ingredientManager, itemStackAsJson);
					} else if (line.startsWith(MARKER_INGREDIENT)) {
						String serializedIngredient = line.substring(MARKER_INGREDIENT.length());
						bookmark = loadIngredientBookmark(ingredientSerializer, ingredientManager, serializedIngredient);
					} else if (line.startsWith(MARKER_RECIPE)) {
						String serializedRecipe = line.substring(MARKER_RECIPE.length());
						bookmark = loadRecipeBookmark(recipeBookmarkSerializer, serializedRecipe);
					} else {
						LOGGER.error("Failed to load unknown bookmark type:\n{}", line);
					}
					if (bookmark != null) {
						bookmarks.add(bookmark);
					}
				}

				return bookmarks;
			})
			.orElseGet(List::of);
	}


}
