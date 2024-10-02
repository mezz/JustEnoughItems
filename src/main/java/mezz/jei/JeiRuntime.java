package mezz.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientBookmarks;
import mezz.jei.gui.BookmarksOverlay;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

public class JeiRuntime implements IJeiRuntime {

  private final RecipeRegistry recipeRegistry;
  private final ItemListOverlay itemListOverlay;
  private final BookmarksOverlay bookmarksOverlay;
  private final RecipesGui recipesGui;
  private final IngredientRegistry ingredientRegistry;
  private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers;
  private final IngredientBookmarks ingredientBookmarks;

  public JeiRuntime(RecipeRegistry recipeRegistry, ItemListOverlay itemListOverlay, RecipesGui recipesGui,
      IngredientRegistry ingredientRegistry, List<IAdvancedGuiHandler<?>> advancedGuiHandlers,
      IngredientBookmarks ingredientBookmarks, BookmarksOverlay bookmarksOverlay) {
    this.recipeRegistry = recipeRegistry;
    this.itemListOverlay = itemListOverlay;
    this.recipesGui = recipesGui;
    this.ingredientRegistry = ingredientRegistry;
    this.advancedGuiHandlers = advancedGuiHandlers;
    this.ingredientBookmarks = ingredientBookmarks;
    this.bookmarksOverlay = bookmarksOverlay;
  }

  public void close() {
    if (itemListOverlay.isOpen()) {
      itemListOverlay.close();
    }
    if (bookmarksOverlay.isOpen()) {
      bookmarksOverlay.close();
    }
    if (recipesGui.isOpen()) {
      recipesGui.close();
    }
  }

  @Override
  public RecipeRegistry getRecipeRegistry() {
    return recipeRegistry;
  }

  @Override
  public ItemListOverlay getItemListOverlay() {
    return itemListOverlay;
  }

  @Override
  public BookmarksOverlay getBookmarksOverlay() {
    return bookmarksOverlay;
  }

  @Override
  public RecipesGui getRecipesGui() {
    return recipesGui;
  }

  public IngredientRegistry getIngredientRegistry() {
    return ingredientRegistry;
  }

  public IIngredientBookmarks getIngredientBookmarks() {
    return ingredientBookmarks;
  }

  public List<IAdvancedGuiHandler<?>> getActiveAdvancedGuiHandlers(GuiScreen guiScreen) {
    List<IAdvancedGuiHandler<?>> activeAdvancedGuiHandler = new ArrayList<IAdvancedGuiHandler<?>>();
    if (guiScreen instanceof GuiContainer) {
      for (IAdvancedGuiHandler<?> advancedGuiHandler : advancedGuiHandlers) {
        Class<?> guiContainerClass = advancedGuiHandler.getGuiContainerClass();
        if (guiContainerClass.isInstance(guiScreen)) {
          activeAdvancedGuiHandler.add(advancedGuiHandler);
        }
      }
    }
    return activeAdvancedGuiHandler;
  }
}
