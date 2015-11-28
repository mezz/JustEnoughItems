package mezz.jei.api.gui;

import javax.annotation.Nonnull;
import java.util.Collection;

import net.minecraft.item.ItemStack;

/**
 * IGuiItemStackGroup displays ItemStacks in a gui.
 *
 * If multiple ItemStacks are set, they will be displayed in rotation.
 * ItemStacks with subtypes and wildcard metadata will be displayed as multiple ItemStacks.
 */
public interface IGuiItemStackGroup extends IGuiIngredientGroup<ItemStack> {

	/**
	 * ItemStacks must be initialized once, and then can be set many times.
	 */
	void init(int index, int xPosition, int yPosition);

	@Override
	void set(int index, @Nonnull Collection<ItemStack> itemStacks);

	@Override
	void set(int index, @Nonnull ItemStack itemStack);

}
