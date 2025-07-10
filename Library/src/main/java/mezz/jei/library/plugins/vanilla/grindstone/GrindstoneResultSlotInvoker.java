package mezz.jei.library.plugins.vanilla.grindstone;

import net.minecraft.world.item.ItemStack;

public interface GrindstoneResultSlotInvoker {
    int invokeGetExperienceFromItem(ItemStack stack);
}
