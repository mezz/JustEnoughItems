package mezz.jei.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface INbtIgnoreList {
	/**
	 * Tell JEI to ignore NBT tags on a specific item when comparing items for recipes.
	 * @since JEI 2.22.0, NBT is automatically ignored on items that don't have subtypes.
	 */
	void ignoreNbtTagNames(@Nonnull Item item, String... nbtTagNames);

	/**
	 * Tell JEI to ignore NBT tags when comparing items for recipes.
	 * To avoid nbt conflicts with other mods, use the item-specific version.
	 */
	void ignoreNbtTagNames(String... nbtTagNames);

	/**
	 * Get NBT from an itemStack, minus the NBT that is being ignored.
	 * Returns null if the itemStack has no NBT or the resulting NBT would be empty.
	 * @since JEI 2.16.0
	 */
	@Nullable
	NBTTagCompound getNbt(@Nonnull ItemStack itemStack);
}
