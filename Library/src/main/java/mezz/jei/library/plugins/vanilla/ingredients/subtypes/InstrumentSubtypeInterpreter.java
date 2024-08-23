package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class InstrumentSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
	public static final InstrumentSubtypeInterpreter INSTANCE = new InstrumentSubtypeInterpreter();

	private InstrumentSubtypeInterpreter() {

	}

	@Override
	public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
		return ingredient.get(DataComponents.INSTRUMENT);
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack itemStack, UidContext context) {
		Holder<Instrument> instrument = itemStack.get(DataComponents.INSTRUMENT);
		if (instrument == null) {
			return "";
		}
		return instrument.getRegisteredName();
	}
}
