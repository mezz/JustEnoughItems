package mezz.jei.bookmarks;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.startup.ForgeModIdHelper;
import mezz.jei.util.LegacyUtil;
import mezz.jei.util.Log;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BookmarkList implements IIngredientGridSource {

	private static final String MARKER_OTHER = "O:";
	private static final String MARKER_STACK = "T:";

	private final List<Object> list = new ArrayList<>();
	private final List<IIngredientListElement> ingredientListElements = new ArrayList<>();
	private final IIngredientRegistry ingredientRegistry;
	private final List<IIngredientGridSource.Listener> listeners = new ArrayList<>();

	public BookmarkList(IIngredientRegistry ingredientRegistry) {
		this.ingredientRegistry = ingredientRegistry;
	}

	public <T> boolean add(T ingredient) {
		if (!contains(ingredient)) {
			T normalized = normalize(ingredient);
			if (addToLists(normalized)) {
				notifyListenersOfChange();
				saveBookmarks();
				return true;
			}
		}
		return false;
	}

	protected <T> T normalize(T ingredient) {
		IIngredientHelper<T> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredient);
		T copy = LegacyUtil.getIngredientCopy(ingredient, ingredientHelper);
		if (copy instanceof ItemStack) {
			((ItemStack) copy).setCount(1);
		} else if (copy instanceof FluidStack) {
			((FluidStack) copy).amount = 1000;
		}
		return copy;
	}

	private static final int NOT_FOUND = -1;

	public boolean contains(Object ingredient) {
		return index(ingredient) != NOT_FOUND;
	}

	private int index(Object ingredient) {
		if (list.isEmpty()) {
			return NOT_FOUND;
		}
		int index = list.indexOf(ingredient);
		if (index >= 0) {
			return index;
		}
		Object normalized = normalize(ingredient);
		int indexNormalized = list.indexOf(normalized);
		if (indexNormalized >= 0) {
			return indexNormalized;
		}
		// We cannot assume that ingredients have a working equals() implementation. Even ItemStack doesn't have one...
		IIngredientHelper<Object> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(normalized);
		for (int i = 0; i < list.size(); i++) {
			Object existing = list.get(i);
			if (existing != null && existing.getClass() == normalized.getClass()) {
				if (ingredientHelper.getUniqueId(existing).equals(ingredientHelper.getUniqueId(normalized))) {
					return i;
				}
			}
		}
		return NOT_FOUND;
	}

	public boolean remove(Object ingredient) {
		int index = index(ingredient);
		if (index != NOT_FOUND) {
			list.remove(index);
			ingredientListElements.remove(index);
			notifyListenersOfChange();
			saveBookmarks();
			return true;
		}
		return false;
	}

	public void saveBookmarks() {
		List<String> strings = new ArrayList<>();
		for (IIngredientListElement<?> element : ingredientListElements) {
			Object object = element.getIngredient();
			if (object instanceof ItemStack) {
				strings.add(MARKER_STACK + ((ItemStack) object).writeToNBT(new NBTTagCompound()).toString());
			} else {
				strings.add(MARKER_OTHER + getUid(element));
			}
		}
		File file = Config.getBookmarkFile();
		if (file != null) {
			try (FileWriter writer = new FileWriter(file)) {
				IOUtils.writeLines(strings, "\n", writer);
			} catch (IOException e) {
				Log.get().error("Failed to save bookmarks list to file {}", file, e);
			}
		}
	}

	private static <T> String getUid(IIngredientListElement<T> element) {
		IIngredientHelper<T> ingredientHelper = element.getIngredientHelper();
		return ingredientHelper.getUniqueId(element.getIngredient());
	}

	public void loadBookmarks() {
		File file = Config.getBookmarkFile();
		if (file == null || !file.exists()) {
			return;
		}
		List<String> ingredientJsonStrings;
		try (FileReader reader = new FileReader(file)) {
			ingredientJsonStrings = IOUtils.readLines(reader);
		} catch (IOException e) {
			Log.get().error("Failed to load bookmarks from file {}", file, e);
			return;
		}

		IngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		Collection<IIngredientType> otherIngredientTypes = new ArrayList<>(ingredientRegistry.getRegisteredIngredientTypes());
		otherIngredientTypes.remove(VanillaTypes.ITEM);

		list.clear();
		ingredientListElements.clear();
		for (String ingredientJsonString : ingredientJsonStrings) {
			if (ingredientJsonString.startsWith(MARKER_STACK)) {
				String itemStackAsJson = ingredientJsonString.substring(MARKER_STACK.length());
				try {
					NBTTagCompound itemStackAsNbt = JsonToNBT.getTagFromJson(itemStackAsJson);
					ItemStack itemStack = new ItemStack(itemStackAsNbt);
					if (!itemStack.isEmpty()) {
						addToLists(itemStack);
					} else {
						Log.get().warn("Failed to load bookmarked ItemStack from json string, the item no longer exists:\n{}", itemStackAsJson);
					}
				} catch (NBTException e) {
					Log.get().error("Failed to load bookmarked ItemStack from json string:\n{}", itemStackAsJson, e);
				}
			} else if (ingredientJsonString.startsWith(MARKER_OTHER)) {
				String uid = ingredientJsonString.substring(MARKER_OTHER.length());
				Object ingredient = getUnknownIngredientByUid(ingredientRegistry, otherIngredientTypes, uid);
				if (ingredient != null) {
					addToLists(ingredient);
				}
			} else {
				Log.get().error("Failed to load unknown bookmarked ingredient:\n{}", ingredientJsonString);
			}
		}
		notifyListenersOfChange();
	}

	@Nullable
	private static Object getUnknownIngredientByUid(IngredientRegistry ingredientRegistry, Collection<IIngredientType> ingredientTypes, String uid) {
		for (IIngredientType<?> ingredientType : ingredientTypes) {
			Object ingredient = ingredientRegistry.getIngredientByUid(ingredientType, uid);
			if (ingredient != null) {
				return ingredient;
			}
		}
		return null;
	}

	private <T> boolean addToLists(T ingredient) {
		IIngredientType<T> ingredientType = ingredientRegistry.getIngredientType(ingredient);
		IIngredientListElement<T> element = IngredientListElementFactory.createUnorderedElement(ingredientRegistry, ingredientType, ingredient, ForgeModIdHelper.getInstance());
		if (element != null) {
			list.add(ingredient);
			ingredientListElements.add(element);
			return true;
		}
		return false;
	}

	@Override
	public List<IIngredientListElement> getIngredientList() {
		return ingredientListElements;
	}

	@Override
	public int size() {
		return ingredientListElements.size();
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
