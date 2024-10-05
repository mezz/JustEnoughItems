package mezz.jei.common.platform;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.List;
import java.util.stream.Stream;

public interface IPlatformIngredientHelper {
	Ingredient createShulkerDyeIngredient(DyeColor color);

	List<Ingredient> getPotionContainers(PotionBrewing potionBrewing);

	Stream<Ingredient> getPotionIngredients(PotionBrewing potionBrewing);

	default float getCompostValue(ItemStack itemStack) {
		return ComposterBlock.COMPOSTABLES.getOrDefault(itemStack.getItem(), 0f);
	}
}
