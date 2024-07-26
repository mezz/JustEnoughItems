package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class PaintingSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final PaintingSubtypeInterpreter INSTANCE = new PaintingSubtypeInterpreter();

	private PaintingSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		CustomData properties = itemStack.get(DataComponents.ENTITY_DATA);
		if (properties == null) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		CompoundTag compoundTag = properties.copyTag();
		Tag variant = compoundTag.get("variant");
		if (variant == null) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		return variant.getAsString();
	}
}
