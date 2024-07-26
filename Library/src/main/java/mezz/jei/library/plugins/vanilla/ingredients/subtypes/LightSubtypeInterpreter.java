package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.LightBlock;

public class LightSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final LightSubtypeInterpreter INSTANCE = new LightSubtypeInterpreter();

	private LightSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		BlockItemStateProperties properties = itemStack.get(DataComponents.BLOCK_STATE);
		if (properties == null) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		Integer level = properties.get(LightBlock.LEVEL);
		if (level == null) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		return level.toString();
	}
}
