package mezz.jei;

import javax.annotation.Nullable;

import mezz.jei.api.INbtIgnoreList;
import mezz.jei.util.Log;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@Deprecated
public class NbtIgnoreList implements INbtIgnoreList {

	@Override
	public void ignoreNbtTagNames(String... nbtTagNames) {
	}

	@Override
	public void ignoreNbtTagNames(@Nullable Item item, String... nbtTagNames) {

	}

	@Nullable
	@Override
	public NBTTagCompound getNbt(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return null;
		}

		return itemStack.getTagCompound();
	}
}
