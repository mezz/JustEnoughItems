package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class PaintingSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final PaintingSubtypeInterpreter INSTANCE = new PaintingSubtypeInterpreter();

	private PaintingSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		CompoundTag compoundtag = itemStack.getOrCreateTagElement("EntityTag");
		return compoundtag.getString("variant");
	}
}
