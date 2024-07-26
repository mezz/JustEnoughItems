package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;

public class InstrumentSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final InstrumentSubtypeInterpreter INSTANCE = new InstrumentSubtypeInterpreter();

	private InstrumentSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		Holder<Instrument> instrument = itemStack.get(DataComponents.INSTRUMENT);
		if (instrument == null) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		return instrument.getRegisteredName();
	}
}
