package mezz.jei.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @deprecated since 3.6.0. all nbt is now ignored by default. See {@link ISubtypeRegistry}.
 */
@Deprecated
public interface INbtIgnoreList {
	/**
	 * Tell JEI to ignore NBT tags on a specific item when comparing items for recipes.
	 * @since JEI 2.22.0, NBT is automatically ignored on items that don't have subtypes.
	 * @deprecated since 3.6.0. all nbt is now ignored by default. See {@link ISubtypeRegistry}.
	 */
	@Deprecated
	void ignoreNbtTagNames(@Nonnull Item item, String... nbtTagNames);

	/**
	 * Tell JEI to ignore NBT tags when comparing items for recipes.
	 * To avoid nbt conflicts with other mods, use the item-specific version.
	 * @deprecated since 3.6.0. all nbt is now ignored by default. See {@link ISubtypeRegistry}.
	 */
	@Deprecated
	void ignoreNbtTagNames(String... nbtTagNames);

	/**
	 * Get NBT from an itemStack, minus the NBT that is being ignored.
	 * Returns null if the itemStack has no NBT or the resulting NBT would be empty.
	 * @since JEI 2.16.0
	 * @deprecated since 3.6.0. all nbt is now ignored by default. See {@link ISubtypeRegistry}.
	 */
	@Deprecated
	@Nullable
	NBTTagCompound getNbt(@Nonnull ItemStack itemStack);
}
