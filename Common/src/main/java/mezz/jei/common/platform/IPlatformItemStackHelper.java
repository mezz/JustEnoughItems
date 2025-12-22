package mezz.jei.common.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface IPlatformItemStackHelper {
	int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType, FuelValues fuelValues);

	Optional<String> getCreatorModId(ItemStack stack);

	List<Component> getTestTooltip(@Nullable Player player, ItemStack itemStack);

	default ItemAttributeModifiers getItemAttributeModifiers(ItemStack stack) {
		return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
	}

	boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient);
}
