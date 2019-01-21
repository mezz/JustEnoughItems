package mezz.jei.bookmarks;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.config.ClientConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientRegistry;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BookmarkList implements IIngredientGridSource {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String MARKER_OTHER = "O:";
	private static final String MARKER_STACK = "T:";

	private final List<Object> list = new LinkedList<>();
	private final List<IIngredientListElement> ingredientListElements = new LinkedList<>();
	private final IngredientRegistry ingredientRegistry;
	private final List<IIngredientGridSource.Listener> listeners = new ArrayList<>();

	public BookmarkList(IngredientRegistry ingredientRegistry) {
		this.ingredientRegistry = ingredientRegistry;
	}

	public <T> boolean add(T ingredient) {
		Object normalized = normalize(ingredient);
		if (!contains(normalized)) {
			if (addToLists(normalized, true)) {
				notifyListenersOfChange();
				saveBookmarks();
				return true;
			}
		}
		return false;
	}

	protected <T> T normalize(T ingredient) {
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		return ingredientHelper.normalizeIngredient(ingredient);
	}

	private boolean contains(Object ingredient) {
		// We cannot assume that ingredients have a working equals() implementation. Even ItemStack doesn't have one...
		IIngredientHelper<Object> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		for (Object existing : list) {
			if (ingredient == existing) {
				return true;
			}
			if (existing != null && existing.getClass() == ingredient.getClass()) {
				if (ingredientHelper.getUniqueId(existing).equals(ingredientHelper.getUniqueId(ingredient))) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean remove(Object ingredient) {
		int index = 0;
		for (Object existing : list) {
			if (ingredient == existing) {
				list.remove(index);
				ingredientListElements.remove(index);
				notifyListenersOfChange();
				saveBookmarks();
				return true;
			}
			index++;
		}
		return false;
	}

	public void saveBookmarks() {
		List<String> strings = new ArrayList<>();
		for (IIngredientListElement<?> element : ingredientListElements) {
			Object object = element.getIngredient();
			if (object instanceof ItemStack) {
				strings.add(MARKER_STACK + ((ItemStack) object).write(new NBTTagCompound()).toString());
			} else {
				strings.add(MARKER_OTHER + getUid(element));
			}
		}
		File file = ClientConfig.getInstance().getBookmarkFile();
		try (FileWriter writer = new FileWriter(file)) {
			IOUtils.writeLines(strings, "\n", writer);
		} catch (IOException e) {
			LOGGER.error("Failed to save bookmarks list to file {}", file, e);
		}
	}

	private <T> String getUid(IIngredientListElement<T> element) {
		T ingredient = element.getIngredient();
		IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
		return ingredientHelper.getUniqueId(ingredient);
	}

	public void loadBookmarks() {
		File file = ClientConfig.getInstance().getBookmarkFile();
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

		Collection<IIngredientType> otherIngredientTypes = new ArrayList<>(ingredientRegistry.getRegisteredIngredientTypes());
		otherIngredientTypes.remove(VanillaTypes.ITEM);

		list.clear();
		ingredientListElements.clear();
		for (String ingredientJsonString : ingredientJsonStrings) {
			if (ingredientJsonString.startsWith(MARKER_STACK)) {
				String itemStackAsJson = ingredientJsonString.substring(MARKER_STACK.length());
				try {
					NBTTagCompound itemStackAsNbt = JsonToNBT.getTagFromJson(itemStackAsJson);
					ItemStack itemStack = ItemStack.read(itemStackAsNbt);
					if (!itemStack.isEmpty()) {
						ItemStack normalized = normalize(itemStack);
						addToLists(normalized, false);
					} else {
						LOGGER.warn("Failed to load bookmarked ItemStack from json string, the item no longer exists:\n{}", itemStackAsJson);
					}
				} catch (CommandSyntaxException e) {
					LOGGER.error("Failed to load bookmarked ItemStack from json string:\n{}", itemStackAsJson, e);
				}
			} else if (ingredientJsonString.startsWith(MARKER_OTHER)) {
				String uid = ingredientJsonString.substring(MARKER_OTHER.length());
				Object ingredient = getUnknownIngredientByUid(otherIngredientTypes, uid);
				if (ingredient != null) {
					Object normalized = normalize(ingredient);
					addToLists(normalized, false);
				}
			} else {
				LOGGER.error("Failed to load unknown bookmarked ingredient:\n{}", ingredientJsonString);
			}
		}
		notifyListenersOfChange();
	}

	@Nullable
	private Object getUnknownIngredientByUid(Collection<IIngredientType> ingredientTypes, String uid) {
		for (IIngredientType<?> ingredientType : ingredientTypes) {
			Object ingredient = ingredientRegistry.getIngredientByUid(ingredientType, uid);
			if (ingredient != null) {
				return ingredient;
			}
		}
		return null;
	}

	private <T> boolean addToLists(T ingredient, boolean addToFront) {
		IIngredientListElement<T> element = IngredientListElementFactory.createUnorderedElement(ingredient);
		if (addToFront) {
			list.add(0, ingredient);
			ingredientListElements.add(0, element);
		} else {
			list.add(ingredient);
			ingredientListElements.add(element);
		}
		return true;
	}

	@Override
	public List<IIngredientListElement> getIngredientList(String filterText) {
		return ingredientListElements;
	}

	public boolean isEmpty() {
		return ingredientListElements.isEmpty();
	}

	@Override
	public void addListener(IIngredientGridSource.Listener listener) {
		listeners.add(listener);
	}

	private void notifyListenersOfChange() {
		for (IIngredientGridSource.Listener listener : listeners) {
			listener.onChange();
		}
	}
}
