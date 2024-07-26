package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public class OminousBottleSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final OminousBottleSubtypeInterpreter INSTANCE = new OminousBottleSubtypeInterpreter();

	private OminousBottleSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		Integer amplifier = itemStack.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
		if (amplifier == null) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		return amplifier.toString();
	}
}
