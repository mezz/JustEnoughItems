package mezz.jei.forge.platform;

import com.mojang.datafixers.util.Either;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
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
	public Collection<CreativeModeTab> getCreativeTabs(ItemStack itemStack) {
		Item item = itemStack.getItem();
		return item.getCreativeTabs();
	}

	@Override
	public List<Component> getTestTooltip(@Nullable Player player, ItemStack itemStack) {
		try {
			List<Component> tooltip = itemStack.getTooltipLines(player, TooltipFlag.Default.NORMAL);
			List<Either<FormattedText, TooltipComponent>> elements = tooltip.stream()
				.<Either<FormattedText, TooltipComponent>>map(Either::left)
				.collect(Collectors.toCollection(ArrayList::new));

			Minecraft minecraft = Minecraft.getInstance();
			RenderTooltipEvent.GatherComponents event = new RenderTooltipEvent.GatherComponents(
				itemStack,
				minecraft.getWindow().getGuiScaledWidth(),
				minecraft.getWindow().getGuiScaledHeight(),
				elements,
				-1
			);
			if (MinecraftForge.EVENT_BUS.post(event)) {
				return List.of();
			}

			return event.getTooltipElements().stream()
				.map(element -> element.left())
				.flatMap(Optional::stream)
				.filter(Component.class::isInstance)
				.map(Component.class::cast)
				.toList();
		} catch (LinkageError | RuntimeException e) {
			LOGGER.error("Error while testing for mod name formatting", e);
		}
		return List.of();
	}
}
