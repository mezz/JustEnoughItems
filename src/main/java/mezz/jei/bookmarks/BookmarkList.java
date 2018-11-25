package mezz.jei.bookmarks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;

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

public class BookmarkList {

  private static final @Nonnull String MARKER_OTHER = "O:";
  private static final @Nonnull String MARKER_STACK = "T:";

  private final @Nonnull List<Object> list = new ArrayList<>();

  @SuppressWarnings("null")
  public @Nonnull List<Object> get() {
    return Collections.unmodifiableList(list);
  }

  public boolean add(@Nonnull Object ingredient) {
    if (!contains(ingredient)) {
      list.add(normalize(ingredient));
      saveBookmarks();
      return true;
    }
    return false;
  }

  protected @Nonnull Object normalize(@Nonnull Object ingredient) {
    IIngredientHelper<Object> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(ingredient);
    Object copy = LegacyUtil.getIngredientCopy(ingredient, ingredientHelper);
    if (copy instanceof ItemStack) {
      ((ItemStack) copy).setCount(1);
    } else if (copy instanceof FluidStack) {
      ((FluidStack) copy).amount = 1000;
    }
    return copy;
  }

  public boolean contains(@Nonnull Object ingredient) {
    if (list.isEmpty()) {
      return false;
    }
    if (list.contains(ingredient)) {
      return true;
    }
    Object normalized = normalize(ingredient);
    if (list.contains(normalized)) {
      return true;
    }
    // We cannot assume that ingredients have a working equals() implementation. Even ItemStack doesn't have one...
    IIngredientHelper<Object> ingredientHelper = Internal.getIngredientRegistry().getIngredientHelper(normalized);
    for (Object existing : list) {
      if (existing != null && existing.getClass() == normalized.getClass()) {
        if (ingredientHelper.getUniqueId(existing).equals(ingredientHelper.getUniqueId(normalized))) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean remove(@Nonnull Object ingredient) {
    if (list.remove(ingredient)) {
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

  @SuppressWarnings({ "rawtypes", "unchecked" }) // doesn't compile with generics
  private void mapNonItemStackIngredient(Map<String, Object> map) {
    IIngredientRegistry ingredientRegistry = Internal.getIngredientRegistry();
    for (IIngredientType ingredientType : ingredientRegistry.getRegisteredIngredientTypes()) {
      if (ingredientType != null && ingredientType != VanillaTypes.ITEM) {
        IIngredientHelper ingredientHelper = ingredientRegistry.getIngredientHelper(ingredientType);
        for (Object o : ingredientRegistry.getAllIngredients(ingredientType)) {
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

}
