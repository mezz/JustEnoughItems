package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

/**
 * For getting properties of ItemStacks efficiently
 */
public class ItemStackElement {

	@Nonnull
	private final ItemStack itemStack;
	@Nonnull
	private final String localizedName;

	@Nullable
	public static ItemStackElement create(@Nonnull ItemStack itemStack) {
		try {
			return new ItemStackElement(itemStack);
		} catch (RuntimeException e) {
			Log.warning("Found broken itemStack: " + e);
			return null;
		}
	}

	public ItemStackElement(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
		this.localizedName = itemStack.getDisplayName().toLowerCase();
	}

	@Nonnull
	public ItemStack getItemStack() {
		return itemStack;
	}

	@Nonnull
	public String getLocalizedName() {
		return localizedName;
	}

}
