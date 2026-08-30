package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ItemStackHelper implements IPlatformItemStackHelper {
	@Override
	public int getBurnTime(ItemStack itemStack) {
		return Objects.requireNonNullElse(FuelRegistry.INSTANCE.get(itemStack.getItem()), 0);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return true;
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
	@SuppressWarnings("deprecation")
	public boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient) {
		return enchantment.value().canEnchant(ingredient);
	}
}
