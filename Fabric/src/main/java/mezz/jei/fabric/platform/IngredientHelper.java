package mezz.jei.fabric.platform;

import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.stream.Stream;

public class IngredientHelper implements IPlatformIngredientHelper {
	@Override
	public Ingredient createShulkerDyeIngredient(DyeColor color) {
		DyeItem dye = DyeItem.byColor(color);
		return Ingredient.of(dye);
	}

	@Override
	public Ingredient createNbtIngredient(ItemStack stack, IStackHelper stackHelper) {
		// TODO: Implement Fabric NBT-aware ingredients
		return Ingredient.of(stack);
	}

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
}
