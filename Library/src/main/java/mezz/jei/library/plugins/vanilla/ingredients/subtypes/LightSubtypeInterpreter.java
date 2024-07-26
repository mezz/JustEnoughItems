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
		float level = getLevel(itemStack);
		int round = Math.round(level);
		return Integer.toString(round);
	}

	private float getLevel(ItemStack itemStack) {
		CompoundTag compoundtag = itemStack.getTagElement("BlockStateTag");

		try {
			if (compoundtag != null) {
				Tag tag = compoundtag.get(LightBlock.LEVEL.getName());
				if (tag != null) {
					return (float)Integer.parseInt(tag.getAsString()) / 16.0F;
				}
			}
		} catch (NumberFormatException ignored) {
			return 1.0F;
		}

		return 1.0F;
	}
}
