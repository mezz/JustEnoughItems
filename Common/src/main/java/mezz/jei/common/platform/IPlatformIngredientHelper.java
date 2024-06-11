package mezz.jei.common.platform;

import mezz.jei.api.helpers.IStackHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.stream.Stream;

public interface IPlatformIngredientHelper {
	Ingredient createShulkerDyeIngredient(DyeColor color);

	Ingredient createNbtIngredient(ItemStack stack, IStackHelper stackHelper);

	List<Ingredient> getPotionContainers(PotionBrewing potionBrewing);

	Stream<Ingredient> getPotionIngredients(PotionBrewing potionBrewing);
}
