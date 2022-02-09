package mezz.jei.config;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.ingredients.IngredientManager;
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

	public void saveBookmarks(IIngredientManager ingredientManager, List<?> ingredientList) {
		File file = getFile(jeiConfigurationDir);
		if (file == null) {
			return;
		}

		List<String> strings = new ArrayList<>();
		for (Object ingredient : ingredientList) {
			if (ingredient instanceof ItemStack stack) {
				strings.add(MARKER_STACK + stack.save(new CompoundTag()));
			} else {
				strings.add(MARKER_OTHER + getUid(ingredientManager, ingredient));
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
						bookmarkList.addToList(normalized, false);
					} else {
						LOGGER.warn("Failed to load bookmarked ItemStack from json string, the item no longer exists:\n{}", itemStackAsJson);
					}
				} catch (CommandSyntaxException e) {
					LOGGER.error("Failed to load bookmarked ItemStack from json string:\n{}", itemStackAsJson, e);
				}
			} else if (ingredientJsonString.startsWith(MARKER_OTHER)) {
				String uid = ingredientJsonString.substring(MARKER_OTHER.length());
				Object ingredient = getUnknownIngredientByUid(ingredientManager, otherIngredientTypes, uid);
				if (ingredient != null) {
					IIngredientHelper<Object> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
					Object normalized = ingredientHelper.normalizeIngredient(ingredient);
					bookmarkList.addToList(normalized, false);
				}
			} else {
				LOGGER.error("Failed to load unknown bookmarked ingredient:\n{}", ingredientJsonString);
			}
		}
		bookmarkList.notifyListenersOfChange();
	}

	private static <T> String getUid(IIngredientManager ingredientManager, T ingredient) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		return ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
	}

	@Nullable
	private static Object getUnknownIngredientByUid(IngredientManager ingredientManager, Collection<IIngredientType<?>> ingredientTypes, String uid) {
		for (IIngredientType<?> ingredientType : ingredientTypes) {
			Object ingredient = ingredientManager.getIngredientByUid(ingredientType, uid);
			if (ingredient != null) {
				return ingredient;
			}
		}
		return null;
	}
}
