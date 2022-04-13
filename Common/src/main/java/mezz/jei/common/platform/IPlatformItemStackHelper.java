package mezz.jei.common.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface IPlatformItemStackHelper {
    int getBurnTime(ItemStack itemStack);

    boolean canStack(ItemStack a, ItemStack b);

    boolean isBookEnchantable(ItemStack stack, ItemStack book);

    @Nullable
    String getCreatorModId(ItemStack stack);

    Collection<CreativeModeTab> getCreativeTabs(ItemStack itemStack);

    List<Component> getTestTooltip(ItemStack itemStack);
}
