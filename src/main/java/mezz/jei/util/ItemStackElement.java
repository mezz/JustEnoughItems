package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
			Log.warning("Found broken itemStack: {}", e);
			return null;
		}
	}

	public ItemStackElement(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
		this.localizedName = itemStack.getDisplayName().toLowerCase();

		String modId = GameRegistry.findUniqueIdentifierFor(itemStack.getItem()).modId;
		ModContainer modContainer = FMLCommonHandler.instance().findContainerFor(modId);
		if (modContainer != null) {
			this.modName = modId.toLowerCase() + " " + modContainer.getName().toLowerCase();
		} else {
			this.modName = modId.toLowerCase();
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
