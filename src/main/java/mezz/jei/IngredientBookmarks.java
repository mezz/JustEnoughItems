package mezz.jei;

import java.util.HashSet;
import java.util.Iterator;

import mezz.jei.api.ingredients.IIngredientBookmarks;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;

public class IngredientBookmarks implements IIngredientBookmarks {
  private final IIngredientRegistry ingredientRegistry;
  private HashSet<String> bookmarkList = new HashSet<String>();

  public IngredientBookmarks(IIngredientRegistry ingredientRegistry) {
    this.ingredientRegistry = ingredientRegistry;
  }

  @Override
  public  void toggleIngredientBookmark(Object ingredient) {
    IIngredientHelper<Object> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
    String uniqueId = ingredientHelper.getUniqueId(ingredient);
    // System.out.println(ingredientHelper.getUniqueId(ingredient));
    if (bookmarkList.contains(uniqueId)) {
      bookmarkList.remove(uniqueId);
    } else {
      bookmarkList.add(uniqueId);
    }

    Iterator<String> iterator = bookmarkList.iterator();
    System.out.println("Dumping bookmarks");
    while(iterator.hasNext()) {
      System.out.println(iterator.next());
    }
  }
}
