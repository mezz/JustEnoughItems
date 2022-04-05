package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ForgeItemStackHelper implements IPlatformItemStackHelper {
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
    public boolean canStack(ItemStack a, ItemStack b) {
        if (a.isEmpty() || !a.sameItem(b) || a.hasTag() != b.hasTag()) {
            return false;
        }

        CompoundTag tag = a.getTag();
        if (tag != null && !tag.equals(b.getTag())) {
            return false;
        }
        return a.areCapsCompatible(b);
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
}
