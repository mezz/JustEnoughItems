package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameData;

import mezz.jei.api.JEIManager;

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
		this.localizedName = itemStack.getDisplayName().toLowerCase();

		String modId = GameData.getItemRegistry().getNameForObject(itemStack.getItem()).getResourceDomain();
		String modName = JEIManager.itemRegistry.getModNameForItem(itemStack.getItem());

		this.modName = modId.toLowerCase(Locale.ENGLISH) + ' ' + modName.toLowerCase(Locale.ENGLISH);
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
