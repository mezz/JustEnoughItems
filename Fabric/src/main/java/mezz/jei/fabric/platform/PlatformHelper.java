package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.IPlatformHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.core.util.function.CachedSupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;


public class PlatformHelper implements IPlatformHelper {
    private final CachedSupplier<ItemStackHelper> itemStackHelper = new CachedSupplier<>(ItemStackHelper::new);
    private final CachedSupplier<FluidHelper> fluidHelper = new CachedSupplier<>(FluidHelper::new);
    private final CachedSupplier<RenderHelper> renderHelper = new CachedSupplier<>(RenderHelper::new);
    private final CachedSupplier<RecipeHelper> recipeHelper = new CachedSupplier<>(RecipeHelper::new);
    private final CachedSupplier<ConfigHelper> configHelper = new CachedSupplier<>(ConfigHelper::new);
    private final CachedSupplier<InputHelper> inputHelper = new CachedSupplier<>(InputHelper::new);
    private final CachedSupplier<ScreenHelper> screenHelper = new CachedSupplier<>(ScreenHelper::new);
    private final CachedSupplier<IngredientHelper> ingredientHelper = new CachedSupplier<>(IngredientHelper::new);
    private final CachedSupplier<ModHelper> modHelper = new CachedSupplier<>(ModHelper::new);

    @Override
    public <T> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return RegistryWrapper.getRegistry(key);
    }

    @Override
    public ItemStackHelper getItemStackHelper() {
        return itemStackHelper.get();
    }

    @Override
    public IPlatformFluidHelperInternal<?> getFluidHelper() {
        return fluidHelper.get();
    }

    @Override
    public RenderHelper getRenderHelper() {
        return renderHelper.get();
    }

    @Override
    public RecipeHelper getRecipeHelper() {
        return recipeHelper.get();
    }

    @Override
    public ConfigHelper getConfigHelper() {
        return configHelper.get();
    }

    @Override
    public InputHelper getInputHelper() {
        return inputHelper.get();
    }

    @Override
    public ScreenHelper getScreenHelper() {
        return screenHelper.get();
    }

    @Override
    public IngredientHelper getIngredientHelper() {
        return ingredientHelper.get();
    }

    @Override
    public ModHelper getModHelper() {
        return modHelper.get();
    }
}
