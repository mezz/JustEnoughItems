package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.util.RegistryUtil;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemStackHelper implements IPlatformItemStackHelper {
	@Override
	public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType) {
		LootContext context = createFuelContext(recipeType);
		return ResolvableInt.getFromItem(itemStack, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
	}

	private static LootContext createFuelContext(RecipeType<?> recipeType) {
		BlockState blockState = getFurnaceBlockState(recipeType);
		ContextMap contextMap = ContextMap.builder()
			.set(LootContextParams.BLOCK_STATE, blockState)
			.build();
		LootParams params = new LootParams(null, contextMap, Map.of(), 0);
		return new LootContext(params, RandomSource.create(), RegistryUtil.getRegistryProvider());
	}

	private static BlockState getFurnaceBlockState(RecipeType<?> recipeType) {
		if (recipeType == RecipeType.BLASTING) {
			return Blocks.BLAST_FURNACE.defaultBlockState();
		}
		if (recipeType == RecipeType.SMOKING) {
			return Blocks.SMOKER.defaultBlockState();
		}
		return Blocks.FURNACE.defaultBlockState();
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
