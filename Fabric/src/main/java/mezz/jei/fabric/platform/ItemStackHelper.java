package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jspecify.annotations.Nullable;

import java.util.List;
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
	public List<Component> gatherTooltipLines(
		ItemStack itemStack,
		Item.TooltipContext tooltipContext,
		@Nullable Player player,
		TooltipFlag tooltipFlag
	) {
		// Fabric injects ItemTooltipCallback into ItemStack#getTooltipLines.
		return itemStack.getTooltipLines(tooltipContext, player, tooltipFlag);
	}

	@Override
	public boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient) {
		return ingredient.canBeEnchantedWith(enchantment, EnchantingContext.ACCEPTABLE);
	}
}
