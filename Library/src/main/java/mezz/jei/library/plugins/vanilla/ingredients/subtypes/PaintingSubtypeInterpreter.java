package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

public class PaintingSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
	public static final PaintingSubtypeInterpreter INSTANCE = new PaintingSubtypeInterpreter();

	private PaintingSubtypeInterpreter() {

	}

	@Override
	public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
		CustomData properties = ingredient.get(DataComponents.ENTITY_DATA);
		if (properties == null) {
			return null;
		}
		CompoundTag compoundTag = properties.copyTag();
		return compoundTag.get("variant");
	}
}
