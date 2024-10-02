package mezz.jei.api;

import javax.annotation.Nullable;
import java.util.Collection;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

public interface IBookmarksOverlay {
  /**
   * @return the stack that's currently under the mouse, or null if there is none
   */
  @Nullable
  ItemStack getStackUnderMouse();
}
