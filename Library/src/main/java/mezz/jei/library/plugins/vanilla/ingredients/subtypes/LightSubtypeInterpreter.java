package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LightBlock;

public class LightSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final LightSubtypeInterpreter INSTANCE = new LightSubtypeInterpreter();

	private LightSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		CompoundTag compoundtag = itemStack.getTagElement("BlockStateTag");

		if (compoundtag != null) {
			Tag tag = compoundtag.get(LightBlock.LEVEL.getName());
			if (tag != null) {
				return tag.getAsString();
			}
		}

		return "15";
	}
}
