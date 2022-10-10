package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ItemStackHelper implements IPlatformItemStackHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return Objects.requireNonNullElse(FuelRegistry.INSTANCE.get(itemStack.getItem()), 0);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    @Nullable
    public String getCreatorModId(ItemStack stack) {
        return null;
    }

    @Override
    public Collection<CreativeModeTab> getCreativeTabs(ItemStack itemStack) {
        CreativeModeTab creativeTab = itemStack.getItem().getItemCategory();
        if (creativeTab == null) {
            return List.of();
        }
        return List.of(creativeTab);
    }

    @Override
    public List<Component> getTestTooltip(@Nullable Player player, ItemStack itemStack) {
        try {
            return itemStack.getTooltipLines(player, TooltipFlag.Default.NORMAL);
        } catch (LinkageError | RuntimeException e) {
            LOGGER.error("Error while Testing for mod name formatting", e);
        }
        return List.of();
    }
}
