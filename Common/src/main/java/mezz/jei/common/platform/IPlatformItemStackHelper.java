package mezz.jei.common.platform;

import net.minecraft.world.item.ItemStack;

public interface IPlatformItemStackHelper {
    int getBurnTime(ItemStack itemStack);

    boolean canStack(ItemStack a, ItemStack b);

    boolean isBookEnchantable(ItemStack stack, ItemStack book);
}
