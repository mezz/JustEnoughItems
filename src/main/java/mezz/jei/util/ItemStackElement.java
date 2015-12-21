package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.GameData;

import mezz.jei.Internal;
import mezz.jei.config.Config;

/**
 * For getting properties of ItemStacks efficiently
 */
public class ItemStackElement {

	@Nonnull
	private final ItemStack itemStack;
	@Nonnull
	private final String localizedName;
	@Nonnull
	private final String modName;

	@Nullable
	public static ItemStackElement create(@Nonnull ItemStack itemStack) {
		try {
			return new ItemStackElement(itemStack);
		} catch (RuntimeException e) {
			Log.warning("Found broken itemStack.", e);
			return null;
		}
	}

	private ItemStackElement(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
		String localizedName = itemStack.getDisplayName().toLowerCase();

		ResourceLocation itemResourceLocation = (ResourceLocation) GameData.getItemRegistry().getNameForObject(itemStack.getItem());
		String modId = itemResourceLocation.getResourceDomain();
		String modName = Internal.getItemRegistry().getModNameForItem(itemStack.getItem());

		this.modName = modId.toLowerCase(Locale.ENGLISH) + ' ' + modName.toLowerCase(Locale.ENGLISH);

		if (Config.isAtPrefixRequiredForModName()) {
			this.localizedName = localizedName;
		} else {
			this.localizedName = localizedName + ' ' + this.modName;
		}
	}

	@Nonnull
	public ItemStack getItemStack() {
		return itemStack;
	}

	@Nonnull
	public String getLocalizedName() {
		return localizedName;
	}

	@Nonnull
	public String getModName() {
		return modName;
	}
}
