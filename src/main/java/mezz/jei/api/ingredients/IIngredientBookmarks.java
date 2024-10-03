package mezz.jei.api.ingredients;

import java.util.List;

import mezz.jei.gui.ingredients.IIngredientListElement;

public interface IIngredientBookmarks {
  /**
   * Toggles visibility of ingredient in the bookmark list.
   */
  <V> void toggleIngredientBookmark(V ingredient);

  List<IIngredientListElement> getIngredientList();

  void clear();
}
