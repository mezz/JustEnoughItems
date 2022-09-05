package mezz.jei.forge.plugins.forge;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class ForgePlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "forge");
    }
}
