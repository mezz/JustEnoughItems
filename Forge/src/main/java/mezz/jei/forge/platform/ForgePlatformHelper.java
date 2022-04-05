package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformFluidHelper;
import mezz.jei.common.platform.IPlatformHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class ForgePlatformHelper implements IPlatformHelper {
    private final ForgeItemStackHelper itemStackHelper = new ForgeItemStackHelper();
    private final ForgeFluidHelper fluidHelper = new ForgeFluidHelper();
    private final ForgeRenderHelper renderHelper = new ForgeRenderHelper();
    private final ForgeRecipeHelper recipeHelper = new ForgeRecipeHelper();
    private final ForgeServerHelper serverHelper = new ForgeServerHelper();
    private final ForgeConfigHelper configHelper = new ForgeConfigHelper();
    private final ForgeInputHelper inputHelper = new ForgeInputHelper();
    private final ForgeScreenHelper screenHelper = new ForgeScreenHelper();

    @Override
    public <T> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return ForgeRegistryWrapper.getRegistry(key);
    }

    @Override
    public ForgeItemStackHelper getItemStackHelper() {
        return itemStackHelper;
    }

    @Override
    public IPlatformFluidHelper<?> getFluidHelper() {
        return fluidHelper;
    }

    @Override
    public ForgeRenderHelper getRenderHelper() {
        return renderHelper;
    }

    @Override
    public ForgeRecipeHelper getRecipeHelper() {
        return recipeHelper;
    }

    @Override
    public ForgeServerHelper getServerHelper() {
        return serverHelper;
    }

    @Override
    public ForgeConfigHelper getConfigHelper() {
        return configHelper;
    }

    @Override
    public ForgeInputHelper getInputHelper() {
        return inputHelper;
    }

    @Override
    public ForgeScreenHelper getScreenHelper() {
        return screenHelper;
    }
}
