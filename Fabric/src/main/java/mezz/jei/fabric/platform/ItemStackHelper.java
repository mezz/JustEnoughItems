package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.FuelValues;

import java.util.Optional;

public class ItemStackHelper implements IPlatformItemStackHelper {
	@Override
	public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType, FuelValues fuelValues) {
		return fuelValues.burnDuration(itemStack);
	}

	@Override
	public Optional<String> getCreatorModId(ItemStack stack) {
		return Optional.of(stack.getCreatorNamespace());
	}

	@Override
	public boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient) {
		return ingredient.canBeEnchantedWith(enchantment, EnchantingContext.ACCEPTABLE);
	}
}
