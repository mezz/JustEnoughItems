package mezz.jei.fabric.mixin;

import mezz.jei.library.plugins.vanilla.grindstone.GrindstoneResultSlotInvoker;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public interface MixinGrindstoneResultSlotInvoker extends GrindstoneResultSlotInvoker {
    @Invoker("getExperienceFromItem")
    int invokeGetExperienceFromItem(ItemStack stack);
}
