package mezz.jei.bookmarks;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.config.Config;
import mezz.jei.util.LegacyUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public void reset() {
		list.clear();
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
		File f = Config.getBookmarkFile();
		if (f != null) {
			try (FileWriter writer = new FileWriter(f)) {
				IOUtils.writeLines(strings, "\n", writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void loadBookmarks() {
		File f = Config.getBookmarkFile();
		if (f == null || !f.exists()) {
			return;
		}
		List<String> strings;
		try (FileReader reader = new FileReader(f)) {
			strings = IOUtils.readLines(reader);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Map<String, Object> map = new HashMap<>();
		for (String s : strings) {
			if (s.startsWith(MARKER_OTHER)) {
				map.put(s.substring(MARKER_OTHER.length()), null);
			}
		}

		mapNonItemStackIngredient(map);

		list.clear();
		for (String s : strings) {
			if (s.startsWith(MARKER_STACK)) {
				try {
					list.add(new ItemStack(JsonToNBT.getTagFromJson(s.substring(MARKER_STACK.length()))));
				} catch (NBTException e) {
					e.printStackTrace();
				}
			} else if (s.startsWith(MARKER_OTHER)) {
				Object object = map.get(s.substring(MARKER_OTHER.length()));
				if (object != null) {
					list.add(object);
				}
			}
		}

	}

	private void mapNonItemStackIngredient(Map<String, Object> map) {
		IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
		for (IIngredientType<?> ingredientType : ingredientRegistry.getRegisteredIngredientTypes()) {
			mapNonItemStackIngredient(ingredientType, map);
		}
	}

	private <T> void mapNonItemStackIngredient(IIngredientType<T> ingredientType, Map<String, Object> map) {
		if (ingredientType != null && ingredientType != VanillaTypes.ITEM) {
			IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
			IIngredientHelper<T> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
			for (T o : ingredientRegistry.getAllIngredients(ingredientType)) {
				if (o != null) {
					String id = ingredientHelper.getUniqueId(o);
					if (map.containsKey(id)) {
						map.put(id, o);
					}
				}
			}
		}
	}

}
