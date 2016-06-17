package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.util.Log;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubtypeRegistry implements ISubtypeRegistry {
	private final Map<Item, ISubtypeInterpreter> interpreters = new HashMap<>();

	@Override
	public void useNbtForSubtypes(@Nonnull Item... items) {
		for (Item item : items) {
			registerNbtInterpreter(item, AllNbt.INSTANCE);
		}
	}

	@Override
	public void registerNbtInterpreter(@Nullable Item item, @Nullable ISubtypeInterpreter interpreter) {
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return;
		}
		if (interpreter == null) {
			Log.error("Null interpreter", new NullPointerException());
			return;
		}

		if (interpreters.containsKey(item)) {
			Log.error("An interpreter is already registered for this item: {}", item, new IllegalArgumentException());
			return;
		}

		interpreters.put(item, interpreter);
	}

	@Nullable
	@Override
	public String getSubtypeInfo(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return null;
		}

		Item item = itemStack.getItem();
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return null;
		}

		ISubtypeInterpreter nbtInterpreter = interpreters.get(item);
		if (nbtInterpreter == null) {
			return null;
		}

		return nbtInterpreter.getSubtypeInfo(itemStack);
	}

	private static class AllNbt implements ISubtypeInterpreter {
		public static final AllNbt INSTANCE = new AllNbt();

		private AllNbt() { }

		@Nullable
		@Override
		public String getSubtypeInfo(@Nonnull ItemStack itemStack) {
			NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
			if (nbtTagCompound == null) {
				return null;
			}
			return nbtTagCompound.toString();
		}
	}
}
