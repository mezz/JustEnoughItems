package mezz.jei.config;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BookmarkConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String MARKER_OTHER = "O:";
	private static final String MARKER_STACK = "T:";
	private final File bookmarkFile;

	public BookmarkConfig(File jeiConfigurationDir) {
		this.bookmarkFile = new File(jeiConfigurationDir, "bookmarks.ini");
	}

	public void saveBookmarks(IIngredientManager ingredientManager, List<IIngredientListElement<?>> ingredientListElements) {
		List<String> strings = new ArrayList<>();
		for (IIngredientListElement<?> element : ingredientListElements) {
			Object object = element.getIngredient();
			if (object instanceof ItemStack) {
				strings.add(MARKER_STACK + ((ItemStack) object).write(new CompoundNBT()).toString());
			} else {
				strings.add(MARKER_OTHER + getUid(ingredientManager, element));
			}
		}
		File file = bookmarkFile;
		try (FileWriter writer = new FileWriter(file)) {
			IOUtils.writeLines(strings, "\n", writer);
		} catch (IOException e) {
			LOGGER.error("Failed to save bookmarks list to file {}", file, e);
		}
	}

	public void loadBookmarks(IngredientManager ingredientManager, BookmarkList bookmarkList) {
		File file = bookmarkFile;
		if (!file.exists()) {
			return;
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
					CompoundNBT itemStackAsNbt = JsonToNBT.getTagFromJson(itemStackAsJson);
					ItemStack itemStack = ItemStack.read(itemStackAsNbt);
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
		return ingredientHelper.getUniqueId(ingredient);
	}

	@Nullable
	private Object getUnknownIngredientByUid(IngredientManager ingredientManager, Collection<IIngredientType<?>> ingredientTypes, String uid) {
		for (IIngredientType<?> ingredientType : ingredientTypes) {
			Object ingredient = ingredientManager.getIngredientByUid(ingredientType, uid);
			if (ingredient != null) {
				return ingredient;
			}
		}
		return null;
	}
}
