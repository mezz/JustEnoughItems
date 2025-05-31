package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformIngredientHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.List;
import java.util.stream.Stream;

public class IngredientHelper implements IPlatformIngredientHelper {
	@Override
	public List<Ingredient> getPotionContainers(PotionBrewing potionBrewing) {
		return potionBrewing.containers;
	}

	@Override
	public Stream<Ingredient> getPotionIngredients(PotionBrewing potionBrewing) {
		return Stream.concat(
			potionBrewing.potionMixes.stream(),
			potionBrewing.containerMixes.stream()
		)
			.map(PotionBrewing.Mix::ingredient);
	}

	@Override
	public float getCompostValue(ItemStack itemStack) {
		Item item = itemStack.getItem();
		return ComposterBlock.COMPOSTABLES.getOrDefault(item, 0f);
	}
}
