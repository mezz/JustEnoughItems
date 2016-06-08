package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import mezz.jei.api.INbtRegistry;
import mezz.jei.util.Log;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NbtRegistry implements INbtRegistry {
	private final Map<Item, INbtInterpreter> nbtInterpreters = new HashMap<>();

	@Override
	public void useNbtForSubtypes(@Nonnull Item... items) {
		for (Item item : items) {
			registerNbtInterpreter(item, AllNbt.INSTANCE);
		}
	}

	@Override
	public void registerNbtInterpreter(@Nullable Item item, @Nullable INbtInterpreter nbtInterpreter) {
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return;
		}
		if (nbtInterpreter == null) {
			Log.error("Null nbtInterpreter", new NullPointerException());
			return;
		}

		if (nbtInterpreters.containsKey(item)) {
			Log.error("An nbtInterpreter is already registered for this item: {}", item, new IllegalArgumentException());
			return;
		}

		nbtInterpreters.put(item, nbtInterpreter);
	}

	@Nullable
	@Override
	public String getSubtypeInfoFromNbt(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return null;
		}

		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound == null) {
			return null;
		}

		Item item = itemStack.getItem();
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return null;
		}

		INbtRegistry.INbtInterpreter nbtInterpreter = nbtInterpreters.get(item);
		if (nbtInterpreter == null) {
			return null;
		}

		return nbtInterpreter.getSubtypeInfoFromNbt(tagCompound);
	}

	private static class AllNbt implements INbtInterpreter {
		public static final AllNbt INSTANCE = new AllNbt();

		private AllNbt() { }

		@Nullable
		@Override
		public String getSubtypeInfoFromNbt(@Nonnull NBTTagCompound nbtTagCompound) {
			return nbtTagCompound.toString();
		}
	}
}
