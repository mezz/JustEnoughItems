package mezz.jei.ingredients;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableMap;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.load.registration.SubtypeRegistration;
import mezz.jei.util.ErrorUtil;

public class SubtypeManager implements ISubtypeManager {
	private final ImmutableMap<Item, ISubtypeInterpreter> interpreters;

	public SubtypeManager(SubtypeRegistration subtypeRegistration) {
		this.interpreters = subtypeRegistration.getInterpreters();
	}

	@Nullable
	@Override
	public String getSubtypeInfo(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);

		Item item = itemStack.getItem();
		ISubtypeInterpreter subtypeInterpreter = interpreters.get(item);
		if (subtypeInterpreter != null) {
			return subtypeInterpreter.apply(itemStack);
		}

		return null;
	}
}
