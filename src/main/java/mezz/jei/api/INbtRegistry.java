package mezz.jei.api;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Tell JEI how to interpret NBT tags are used when comparing and looking up items.
 * <p>
 * Some items have subtypes, most of them use meta values for this.
 * If your item has subtypes that depend on NBT, use this interface so JEI can tell those subtypes apart.
 * <p>
 * Most items do not use nbt to differentiate subtypes, so this interface is being used instead of a blacklist.
 * Replaces {@link INbtIgnoreList}.
 *
 * @since 3.6.0
 * @deprecated since 3.6.4. Use {@link ISubtypeRegistry}
 */
@Deprecated
public interface INbtRegistry {
	/**
	 * Tells JEI to treat all NBT as relevant to these items' subtypes.
	 *
	 * @deprecated since 3.6.4. Use {@link ISubtypeRegistry}
	 */
	@Deprecated
	void useNbtForSubtypes(Item... items);

	/**
	 * Add an nbt interpreter to turn nbt into data that can be used to compare item subtypes.
	 *
	 * @param item           the item that uses nbt to tell subtypes apart.
	 * @param nbtInterpreter the nbt interpreter for the item.
	 * @deprecated since 3.6.4. Use {@link ISubtypeRegistry}
	 */
	@Deprecated
	void registerNbtInterpreter(Item item, INbtInterpreter nbtInterpreter);

	/**
	 * Get the data from an itemStack that is relevant to comparing and telling subtypes apart.
	 * Returns null if the itemStack has no NBT or the NBT is not used for subtypes.
	 *
	 * @deprecated since 3.6.4. Use {@link ISubtypeRegistry}
	 */
	@Deprecated
	@Nullable
	String getSubtypeInfoFromNbt(ItemStack itemStack);

	interface INbtInterpreter {
		/**
		 * Get the data from an itemStack that is relevant to telling subtypes apart.
		 * Returns null if the NBT has no data used for subtypes.
		 *
		 * @deprecated since 3.6.4. Use {@link ISubtypeRegistry}
		 */
		@Deprecated
		@Nullable
		String getSubtypeInfoFromNbt(NBTTagCompound nbtTagCompound);
	}
}
