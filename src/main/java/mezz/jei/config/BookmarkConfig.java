package mezz.jei.config;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.util.PathUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		File configFolder = configPath.toFile();
		if (!configFolder.exists() && !configFolder.mkdirs()) {
			LOGGER.error("Unable to create bookmark config folder: {}", configFolder);
			return null;
		}
		return configPath.resolve("bookmarks.ini").toFile();
	}

	private static File getOldFile(File jeiConfigurationDir) {
		return Paths.get(jeiConfigurationDir.getAbsolutePath(), "bookmarks.ini").toFile();
	}

	public BookmarkConfig(File jeiConfigurationDir) {
		this.jeiConfigurationDir = jeiConfigurationDir;
	}

	public void saveBookmarks(IIngredientManager ingredientManager, List<IIngredientListElement<?>> ingredientListElements) {
		File file = getFile(jeiConfigurationDir);
		if (file == null) {
			return;
		}

		List<IIngredientListElement<?>> ingredientListElementsSnapshot = new ArrayList<>(ingredientListElements);
		List<String> strings = new ArrayList<>();
		for (IIngredientListElement<?> element : ingredientListElementsSnapshot) {
			Object object = element.getIngredient();
			if (object instanceof ItemStack) {
				strings.add(MARKER_STACK + ((ItemStack) object).save(new CompoundNBT()).toString());
			} else {
				strings.add(MARKER_OTHER + getUid(ingredientManager, element));
			}
		}

		try {
			PathUtil.writeUsingTempFile(file.toPath(), strings);
			LOGGER.debug("Saved bookmarks list to file {}", file);
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
					CompoundNBT itemStackAsNbt = JsonToNBT.parseTag(itemStackAsJson);
					ItemStack itemStack = ItemStack.of(itemStackAsNbt);
					if (!itemStack.isEmpty()) {
						ItemStack normalized = itemStackHelper.normalizeIngredient(itemStack);
						bookmarkList.addToLists(normalized, false);
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
					bookmarkList.addToLists(normalized, false);
				}
			} else {
				LOGGER.error("Failed to load unknown bookmarked ingredient:\n{}", ingredientJsonString);
			}
		}
		bookmarkList.notifyListenersOfChange();
	}

	private <T> String getUid(IIngredientManager ingredientManager, IIngredientListElement<T> element) {
		T ingredient = element.getIngredient();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		return ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
	}

	@Nullable
	private Object getUnknownIngredientByUid(IngredientManager ingredientManager, Collection<IIngredientType<?>> ingredientTypes, String uid) {
		for (IIngredientType<?> ingredientType : ingredientTypes) {
			Optional<?> ingredient = ingredientManager.getIngredientByUid(ingredientType, uid);
			if (ingredient.isPresent()) {
				return ingredient.get();
			}
		}
		return null;
	}
}
