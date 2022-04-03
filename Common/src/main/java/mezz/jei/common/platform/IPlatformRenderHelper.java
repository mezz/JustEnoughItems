package mezz.jei.common.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

public interface IPlatformRenderHelper {
    Font getFontRenderer(Minecraft minecraft, ItemStack itemStack);

    boolean shouldRender(MobEffectInstance potionEffect);
}
