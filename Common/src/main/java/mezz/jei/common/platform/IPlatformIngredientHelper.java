package mezz.jei.common.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.stream.Stream;

public interface IPlatformIngredientHelper {
	List<Ingredient> getPotionContainers(PotionBrewing potionBrewing);

	Stream<Ingredient> getPotionIngredients(PotionBrewing potionBrewing);

	float getCompostValue(ItemStack itemStack);

	HolderSet<Item> getSupportedItems(Holder<Enchantment> enchantment);
}
