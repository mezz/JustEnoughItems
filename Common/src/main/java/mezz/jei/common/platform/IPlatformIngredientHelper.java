package mezz.jei.common.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.stream.Stream;

public interface IPlatformIngredientHelper {
	List<Ingredient> getPotionContainers(PotionBrewing potionBrewing);

	Stream<Ingredient> getPotionIngredients(PotionBrewing potionBrewing);

	float getCompostValue(ItemStack itemStack);
}
