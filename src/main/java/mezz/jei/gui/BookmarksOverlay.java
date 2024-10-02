package mezz.jei.gui;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import mezz.jei.ItemFilter;
import mezz.jei.api.IBookmarksOverlay;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.util.Log;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public class BookmarksOverlay implements IBookmarksOverlay {
  private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers;
  @Nullable
  private BookmarksOverlayInternal internal;
  private final IIngredientRegistry ingredientRegistry;
	
  public BookmarksOverlay(List<IAdvancedGuiHandler<?>> advancedGuiHandlers, IIngredientRegistry ingredientRegistry) {
    this.advancedGuiHandlers = advancedGuiHandlers;
    this.ingredientRegistry = ingredientRegistry;
  }

  @Nullable
  @Override
  public ItemStack getStackUnderMouse() {
    if (internal != null) {
      return internal.getStackUnderMouse();
    }
    return null;
  }

  public List<IAdvancedGuiHandler<?>> getAdvancedGuiHandlers() {
    return advancedGuiHandlers;
  }

  public boolean isOpen() {
    return internal != null;
  }

  public void close() {
    if (internal != null) {
      internal.close();
    }
    internal = null;
  }

  public BookmarksOverlayInternal getInternal() {
    return internal;
  }


	public BookmarksOverlayInternal create(GuiScreen guiScreen) {
		close();

		if (Config.isOverlayEnabled()) {
			GuiProperties guiProperties = GuiProperties.create(guiScreen);
			if (guiProperties != null) {
				final int columns = BookmarksOverlayInternal.getColumns(guiProperties);
				if (columns >= 4) {
					internal = new BookmarksOverlayInternal(this, ingredientRegistry, guiScreen, guiProperties);
					return internal;
				}
			}
		}

		return null;
	}
}
