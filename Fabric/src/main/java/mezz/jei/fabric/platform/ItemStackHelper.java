package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ItemStackHelper implements IPlatformItemStackHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public int getBurnTime(ItemStack itemStack) {
        Map<Item, Integer> fuels = AbstractFurnaceBlockEntity.getFuel();
        return fuels.getOrDefault(itemStack.getItem(), 0);
    }

    @Override
    public boolean canStack(ItemStack a, ItemStack b) {
        if (a.isEmpty() || !a.sameItem(b) || a.hasTag() != b.hasTag()) {
            return false;
        }

        CompoundTag tag = a.getTag();
        return tag == null || tag.equals(b.getTag());
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
    public List<Component> getTestTooltip(ItemStack itemStack) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            return itemStack.getTooltipLines(player, TooltipFlag.Default.NORMAL);
        } catch (LinkageError | RuntimeException e) {
            LOGGER.error("Error while Testing for mod name formatting", e);
        }
        return List.of();
    }
}
