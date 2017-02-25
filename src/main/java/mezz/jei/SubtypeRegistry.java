package mezz.jei;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.util.Log;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubtypeRegistry implements ISubtypeRegistry {
	private final Map<Item, ISubtypeInterpreter> interpreters = new IdentityHashMap<Item, ISubtypeInterpreter>();

	@Override
	public void useNbtForSubtypes(Item... items) {
		for (Item item : items) {
			registerSubtypeInterpreter(item, AllNbt.INSTANCE);
		}
	}

	@Override
	public void registerNbtInterpreter(Item item, ISubtypeInterpreter interpreter) {
		registerSubtypeInterpreter(item, interpreter);
	}

	@Override
	public void registerSubtypeInterpreter(Item item, ISubtypeInterpreter interpreter) {
		Preconditions.checkNotNull(item, "item cannot be null");
		Preconditions.checkNotNull(interpreter, "interpreter cannot be null");

		if (interpreters.containsKey(item)) {
			Log.error("An interpreter is already registered for this item: {}", item, new IllegalArgumentException());
			return;
		}

		interpreters.put(item, interpreter);
	}

	@Nullable
	@Override
	public String getSubtypeInfo(ItemStack itemStack) {
		Preconditions.checkNotNull(itemStack, "itemStack cannot be null");
		Preconditions.checkArgument(!itemStack.isEmpty(), "itemStack cannot be empty");

		Item item = itemStack.getItem();
		ISubtypeInterpreter subtypeInterpreter = interpreters.get(item);
		if (subtypeInterpreter != null) {
			return subtypeInterpreter.getSubtypeInfo(itemStack);
		}

		return null;
	}

	@Override
	public boolean hasSubtypeInterpreter(ItemStack itemStack) {
		Preconditions.checkNotNull(itemStack, "itemStack cannot be null");
		Preconditions.checkArgument(!itemStack.isEmpty(), "itemStack cannot be empty");

		Item item = itemStack.getItem();
		return interpreters.containsKey(item);
	}

	private static class AllNbt implements ISubtypeInterpreter {
		public static final AllNbt INSTANCE = new AllNbt();

		private AllNbt() {
		}

		@Nullable
		@Override
		public String getSubtypeInfo(ItemStack itemStack) {
			NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
			if (nbtTagCompound == null || nbtTagCompound.hasNoTags()) {
				return null;
			}
			return nbtTagCompound.toString();
		}
	}
}
