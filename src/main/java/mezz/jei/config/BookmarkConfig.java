package mezz.jei.config;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.TypedIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BookmarkConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String MARKER_OTHER = "O:";
	private static final String MARKER_STACK = "T:";
	private final File jeiConfigurationDir;

	@Nullable
	private static File getFile(File jeiConfigurationDir) {
		Path configPath = ServerInfo.getWorldPath(jeiConfigurationDir.toPath());
		if (configPath == null) {
			return null;
		}
		return configPath.resolve("bookmarks.ini").toFile();
	}

	private static File getOldFile(File jeiConfigurationDir) {
		return Path.of(jeiConfigurationDir.getAbsolutePath(), "bookmarks.ini").toFile();
	}

	public BookmarkConfig(File jeiConfigurationDir) {
		this.jeiConfigurationDir = jeiConfigurationDir;
	}

	public void saveBookmarks(IIngredientManager ingredientManager, List<ITypedIngredient<?>> ingredientList) {
		File file = getFile(jeiConfigurationDir);
		if (file == null) {
			return;
		}

		List<String> strings = new ArrayList<>();
		for (ITypedIngredient<?> typedIngredient : ingredientList) {
			if (typedIngredient.getIngredient() instanceof ItemStack stack) {
				strings.add(MARKER_STACK + stack.save(new CompoundTag()));
			} else {
				strings.add(MARKER_OTHER + getUid(ingredientManager, typedIngredient));
			}
		}

		try (FileWriter writer = new FileWriter(file)) {
			IOUtils.writeLines(strings, "\n", writer);
		} catch (IOException e) {
			LOGGER.error("Failed to save bookmarks list to file {}", file, e);
		}
	}

	public void loadBookmarks(IngredientManager ingredientManager, BookmarkList bookmarkList) {
		File file = getFile(jeiConfigurationDir);
		if (file == null) {
			return;
		} else if (!file.exists()) {
			File oldFile = getOldFile(jeiConfigurationDir);
			if (!oldFile.exists()) {
				return;
			}
			try {
				Files.copy(oldFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("Failed to copy old bookmarks {} to new location {}", oldFile, file, e);
				return;
			}
		}
		List<String> ingredientJsonStrings;
		try (FileReader reader = new FileReader(file)) {
			ingredientJsonStrings = IOUtils.readLines(reader);
		} catch (IOException e) {
			LOGGER.error("Failed to load bookmarks from file {}", file, e);
			return;
		}

		Collection<IIngredientType<?>> otherIngredientTypes = new ArrayList<>(ingredientManager.getRegisteredIngredientTypes());
		otherIngredientTypes.remove(VanillaTypes.ITEM);

		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM);

		for (String ingredientJsonString : ingredientJsonStrings) {
			if (ingredientJsonString.startsWith(MARKER_STACK)) {
				String itemStackAsJson = ingredientJsonString.substring(MARKER_STACK.length());
				try {
					CompoundTag itemStackAsNbt = TagParser.parseTag(itemStackAsJson);
					ItemStack itemStack = ItemStack.of(itemStackAsNbt);
					if (!itemStack.isEmpty()) {
						ItemStack normalized = itemStackHelper.normalizeIngredient(itemStack);
						Optional<ITypedIngredient<ItemStack>> typedIngredient = TypedIngredient.createTyped(ingredientManager, VanillaTypes.ITEM, normalized);
						if (typedIngredient.isEmpty()) {
							LOGGER.warn("Failed to load bookmarked ItemStack from json string, the item no longer exists:\n{}", itemStackAsJson);
						} else {
							bookmarkList.addToList(typedIngredient.get(), false);
						}
					} else {
						LOGGER.warn("Failed to load bookmarked ItemStack from json string, the item no longer exists:\n{}", itemStackAsJson);
					}
				} catch (CommandSyntaxException e) {
					LOGGER.error("Failed to load bookmarked ItemStack from json string:\n{}", itemStackAsJson, e);
				}
			} else if (ingredientJsonString.startsWith(MARKER_OTHER)) {
				String uid = ingredientJsonString.substring(MARKER_OTHER.length());
				Optional<ITypedIngredient<?>> typedIngredient = getNormalizedIngredientByUid(ingredientManager, otherIngredientTypes, uid);
				if (typedIngredient.isEmpty()) {
					LOGGER.error("Failed to load unknown bookmarked ingredient:\n{}", ingredientJsonString);
				} else {
					bookmarkList.addToList(typedIngredient.get(), false);
				}
			} else {
				LOGGER.error("Failed to load unknown bookmarked ingredient:\n{}", ingredientJsonString);
			}
		}
		bookmarkList.notifyListenersOfChange();
	}

	private static <T> String getUid(IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return ingredientHelper.getUniqueId(typedIngredient.getIngredient(), UidContext.Ingredient);
	}

	private static Optional<ITypedIngredient<?>> getNormalizedIngredientByUid(IngredientManager ingredientManager, Collection<IIngredientType<?>> ingredientTypes, String uid) {
		return ingredientTypes.stream()
			.map(t -> getNormalizedIngredientByUid(ingredientManager, t, uid))
			.flatMap(Optional::stream)
			.findFirst();
	}

	private static <T> Optional<ITypedIngredient<?>> getNormalizedIngredientByUid(IngredientManager ingredientManager, IIngredientType<T> ingredientType, String uid) {
		T ingredient = ingredientManager.getIngredientByUid(ingredientType, uid);
		return Optional.ofNullable(ingredient)
			.map(i -> {
				IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
				return ingredientHelper.normalizeIngredient(i);
			})
			.flatMap(i -> TypedIngredient.createTyped(ingredientManager, ingredientType, i));
	}
}
