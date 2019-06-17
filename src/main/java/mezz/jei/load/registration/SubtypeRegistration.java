package mezz.jei.load.registration;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import com.google.common.collect.ImmutableMap;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubtypeRegistration implements ISubtypeRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<Item, ISubtypeInterpreter> interpreters = new IdentityHashMap<>();

	@Override
	public void useNbtForSubtypes(Item... items) {
		for (Item item : items) {
			registerSubtypeInterpreter(item, AllNbt.INSTANCE);
		}
	}

	@Override
	public void registerSubtypeInterpreter(Item item, ISubtypeInterpreter interpreter) {
		ErrorUtil.checkNotNull(item, "item ");
		ErrorUtil.checkNotNull(interpreter, "interpreter");

		if (interpreters.containsKey(item)) {
			LOGGER.error("An interpreter is already registered for this item: {}", item, new IllegalArgumentException());
			return;
		}

		interpreters.put(item, interpreter);
	}

	@Override
	public boolean hasSubtypeInterpreter(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);

		Item item = itemStack.getItem();
		return interpreters.containsKey(item);
	}

	public ImmutableMap<Item, ISubtypeInterpreter> getInterpreters() {
		return ImmutableMap.copyOf(interpreters);
	}

	private static class AllNbt implements ISubtypeInterpreter {
		public static final AllNbt INSTANCE = new AllNbt();

		private AllNbt() {
		}

		@Override
		public String apply(ItemStack itemStack) {
			CompoundNBT nbtTagCompound = itemStack.getTag();
			if (nbtTagCompound == null || nbtTagCompound.isEmpty()) {
				return ISubtypeInterpreter.NONE;
			}
			return nbtTagCompound.toString();
		}
	}
}
