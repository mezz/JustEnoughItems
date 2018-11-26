package mezz.jei.bookmarks;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.config.Config;
import mezz.jei.ingredients.IngredientRegistry;
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
import java.util.Collections;
import java.util.List;

public class BookmarkList {

	private static final String MARKER_OTHER = "O:";
	private static final String MARKER_STACK = "T:";

	private final List<Object> list = new ArrayList<>();

	public List<Object> get() {
		return Collections.unmodifiableList(list);
	}

	public boolean add(Object ingredient) {
		if (!contains(ingredient)) {
			list.add(normalize(ingredient));
			saveBookmarks();
			return true;
		}
		return false;
	}

	protected Object normalize(Object ingredient) {
		IIngredientHelper<Object> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredient);
		Object copy = LegacyUtil.getIngredientCopy(ingredient, ingredientHelper);
		if (copy instanceof ItemStack) {
			((ItemStack) copy).setCount(1);
		} else if (copy instanceof FluidStack) {
			((FluidStack) copy).amount = 1000;
		}
		return copy;
	}

	private static final int NOT_FOUND = -3;
	private static final int FOUND_NORMALIZED = -2;
	private static final int FOUND_EQUAL = -1;

	public boolean contains(Object ingredient) {
		return index(ingredient) != NOT_FOUND;
	}

	private int index(Object ingredient) {
		if (list.isEmpty()) {
			return NOT_FOUND;
		}
		if (list.contains(ingredient)) {
			return FOUND_EQUAL;
		}
		Object normalized = normalize(ingredient);
		if (list.contains(normalized)) {
			return FOUND_NORMALIZED;
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
			if (index == FOUND_EQUAL) {
				list.remove(ingredient);
			} else if (index == FOUND_NORMALIZED) {
				list.remove(normalize(ingredient));
			} else {
				list.remove(index);
			}
			saveBookmarks();
			return true;
		}
		return false;
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public void saveBookmarks() {
		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		List<String> strings = new ArrayList<>();
		for (Object object : list) {
			if (object instanceof ItemStack) {
				strings.add(MARKER_STACK + ((ItemStack) object).writeToNBT(new NBTTagCompound()).toString());
			} else if (object != null) {
				IIngredientHelper<Object> ingredientHelper = ingredientRegistry.getIngredientHelper(object);
				strings.add(MARKER_OTHER + ingredientHelper.getUniqueId(object));
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
		for (String ingredientJsonString : ingredientJsonStrings) {
			if (ingredientJsonString.startsWith(MARKER_STACK)) {
				String itemStackAsJson = ingredientJsonString.substring(MARKER_STACK.length());
				try {
					NBTTagCompound itemStackAsNbt = JsonToNBT.getTagFromJson(itemStackAsJson);
					ItemStack itemStack = new ItemStack(itemStackAsNbt);
					if (!itemStack.isEmpty()) {
						list.add(itemStack);
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
					list.add(ingredient);
				}
			} else {
				Log.get().error("Failed to load unknown bookmarked ingredient:\n{}", ingredientJsonString);
			}
		}

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

}
