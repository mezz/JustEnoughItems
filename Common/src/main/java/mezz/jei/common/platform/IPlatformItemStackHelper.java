package mezz.jei.common.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IPlatformItemStackHelper {
	int getBurnTime(ItemStack itemStack);

	boolean isBookEnchantable(ItemStack stack, ItemStack book);

	Optional<String> getCreatorModId(ItemStack stack);

	/**
	 * Gets the tooltip lines after loader-specific tooltip callbacks used during rendering have run.
	 */
	List<Component> gatherTooltipLines(
		ItemStack itemStack,
		Item.TooltipContext tooltipContext,
		@Nullable Player player,
		TooltipFlag tooltipFlag
	);

	default ItemAttributeModifiers getItemAttributeModifiers(ItemStack stack) {
		return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
	}

	boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient);
}
