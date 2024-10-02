package mezz.jei.api.ingredients;

import mezz.jei.api.IJeiHelpers;

/**
 * The Ingredient bookmaks allows mods add and remove ingredients from JEI's
 * bookmark list
 * Get the instance from {@link IJeiHelpers#getIngredientBookmarks()}.
 *
 * @Since JEI 3.14.9
 */
public interface IIngredientBookmarks {
  /**
   * Toggles visibility of ingredient in the bookmark list.
   */
  void toggleIngredientBookmark(Object ingredient);

}
