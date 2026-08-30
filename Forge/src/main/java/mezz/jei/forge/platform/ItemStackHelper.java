package mezz.jei.forge.platform;

import com.mojang.datafixers.util.Either;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.client.ForgeHooksClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemStackHelper implements IPlatformItemStackHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public int getBurnTime(ItemStack itemStack) {
		try {
			@SuppressWarnings("UnstableApiUsage")
			int burnTime = ForgeHooks.getBurnTime(itemStack, null);
			return burnTime;
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			LOGGER.error("Failed to check if item is fuel {}.", itemStackInfo, e);
			return 0;
		}
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		Item item = stack.getItem();
		return item.isBookEnchantable(stack, book);
	}

	@Override
	public Optional<String> getCreatorModId(ItemStack stack) {
		Item item = stack.getItem();
		String creatorModId = item.getCreatorModId(stack);
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
		List<ClientTooltipComponent> gathered = ForgeHooksClient.gatherTooltipComponentsFromElements(
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
	@SuppressWarnings("deprecation")
	public boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient) {
		return enchantment.value().canEnchant(ingredient);
	}
}
