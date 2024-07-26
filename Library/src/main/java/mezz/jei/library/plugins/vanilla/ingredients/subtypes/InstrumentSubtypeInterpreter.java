package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class InstrumentSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final InstrumentSubtypeInterpreter INSTANCE = new InstrumentSubtypeInterpreter();

	private InstrumentSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		CompoundTag compoundtag = itemStack.getTag();
		if (compoundtag != null && compoundtag.contains("instrument", 8)) {
			ResourceLocation resourcelocation = ResourceLocation.tryParse(compoundtag.getString("instrument"));
			if (resourcelocation != null) {
				return BuiltInRegistries.INSTRUMENT.getHolder(ResourceKey.create(Registries.INSTRUMENT, resourcelocation))
					.map(holder -> holder.key().location().toString())
					.orElse(IIngredientSubtypeInterpreter.NONE);
			}
		}

		return IIngredientSubtypeInterpreter.NONE;
	}
}
