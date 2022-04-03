package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.EffectRenderer;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.RenderProperties;

public class ForgeRenderHelper implements IPlatformRenderHelper {
    @Override
    public Font getFontRenderer(Minecraft minecraft, ItemStack itemStack) {
        IItemRenderProperties renderProperties = RenderProperties.get(itemStack);
        Font fontRenderer = renderProperties.getFont(itemStack);
        if (fontRenderer != null) {
            return fontRenderer;
        }
        return minecraft.font;
    }

    @Override
    public boolean shouldRender(MobEffectInstance potionEffect) {
        EffectRenderer effectRenderer = RenderProperties.getEffectRenderer(potionEffect);
        return effectRenderer.shouldRender(potionEffect);
    }
}
