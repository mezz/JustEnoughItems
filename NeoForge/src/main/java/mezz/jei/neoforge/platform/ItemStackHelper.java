package mezz.jei.neoforge.platform;

import com.mojang.datafixers.util.Either;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.loot.NeoForgeLootContextParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemStackHelper implements IPlatformItemStackHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType) {
		try {
			LootContext context = createFuelContext(itemStack, recipeType);
			return ResolvableInt.getFromItem(itemStack, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			LOGGER.error("Failed to check if item is fuel {}.", itemStackInfo, e);
			return 0;
		}
	}

	private static LootContext createFuelContext(ItemStack itemStack, RecipeType<?> recipeType) {
		BlockState blockState = getFurnaceBlockState(recipeType);
		ContextMap contextMap = ContextMap.builder()
			.set(LootContextParams.BLOCK_STATE, blockState)
			.set(NeoForgeLootContextParams.QUERIED_STACK, itemStack)
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
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		assert level != null;
		RegistryAccess registryAccess = level.registryAccess();
		Item item = stack.getItem();
		String creatorModId = item.getCreatorModId(registryAccess, stack);
		return Optional.ofNullable(creatorModId);
	}

	@Override
	public List<Component> gatherTooltipLines(
		ItemStack itemStack,
		Item.TooltipContext tooltipContext,
		@Nullable Player player,
		TooltipFlag tooltipFlag
	) {
		List<Component> tooltip = itemStack.getTooltipLines(tooltipContext, player, tooltipFlag);
		List<Either<FormattedText, TooltipComponent>> elements = tooltip.stream()
			.<Either<FormattedText, TooltipComponent>>map(Either::left)
			.collect(Collectors.toCollection(ArrayList::new));

		Minecraft minecraft = Minecraft.getInstance();
		List<ClientTooltipComponent> gathered = ClientHooks.gatherTooltipComponentsFromElements(
			itemStack,
			elements,
			0,
			minecraft.getWindow().getGuiScaledWidth(),
			minecraft.getWindow().getGuiScaledHeight(),
			minecraft.font
		);
		if (gathered.isEmpty()) {
			return List.of();
		}

		return elements.stream()
			.map(element -> element.left())
			.flatMap(Optional::stream)
			.filter(Component.class::isInstance)
			.map(Component.class::cast)
			.toList();
	}

	@Override
	public ItemAttributeModifiers getItemAttributeModifiers(ItemStack stack) {
		return stack.getAttributeModifiers();
	}

	@Override
	public boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient) {
		return ingredient.supportsEnchantment(enchantment);
	}
}
