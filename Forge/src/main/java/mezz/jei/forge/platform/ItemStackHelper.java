package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemStackHelper implements IPlatformItemStackHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public int getBurnTime(ItemStack itemStack) {
        try {
            return ForgeHooks.getBurnTime(itemStack, null);
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
    @Nullable
    public String getCreatorModId(ItemStack stack) {
        Item item = stack.getItem();
        return item.getCreatorModId(stack);
    }

    @Override
    public Collection<CreativeModeTab> getCreativeTabs(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item.getCreativeTabs();
    }

    @Override
    public List<Component> getTestTooltip(@Nullable Player player, ItemStack itemStack) {
        try {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("JEI Tooltip Testing for mod name formatting"));
            ItemTooltipEvent tooltipEvent = ForgeEventFactory.onItemTooltip(itemStack, player, tooltip, TooltipFlag.Default.NORMAL);
            return tooltipEvent.getToolTip();
        } catch (LinkageError | RuntimeException e) {
            LOGGER.error("Error while Testing for mod name formatting", e);
        }
        return List.of();
    }
}
