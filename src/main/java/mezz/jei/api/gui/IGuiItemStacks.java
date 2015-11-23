package mezz.jei.api.gui;

import javax.annotation.Nonnull;
import java.util.Collection;

import net.minecraft.item.ItemStack;

/**
 * IGuiItemStacks displays ItemStacks in a gui.
 *
 * If multiple ItemStacks are set, they will be displayed in rotation.
 * ItemStacks with subtypes and wildcard metadata will be displayed as multiple ItemStacks.
 */
public interface IGuiItemStacks {

	/**
	 * ItemStacks must be initialized once, and then can be set many times.
	 */
	void init(int index, int xPosition, int yPosition);

	void set(int index, @Nonnull Collection<ItemStack> itemStacks);

	void set(int index, @Nonnull ItemStack itemStack);

}
