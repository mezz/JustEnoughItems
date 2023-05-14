package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.IPlatformHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.core.util.function.LazySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public class PlatformHelper implements IPlatformHelper {
    private final Supplier<ItemStackHelper> itemStackHelper = new LazySupplier<>(ItemStackHelper::new);
    private final Supplier<FluidHelper> fluidHelper = new LazySupplier<>(FluidHelper::new);
    private final Supplier<RenderHelper> renderHelper = new LazySupplier<>(RenderHelper::new);
    private final Supplier<RecipeHelper> recipeHelper = new LazySupplier<>(RecipeHelper::new);
    private final Supplier<ConfigHelper> configHelper = new LazySupplier<>(ConfigHelper::new);
    private final Supplier<InputHelper> inputHelper = new LazySupplier<>(InputHelper::new);
    private final Supplier<ScreenHelper> screenHelper = new LazySupplier<>(ScreenHelper::new);
    private final Supplier<IngredientHelper> ingredientHelper = new LazySupplier<>(IngredientHelper::new);
    private final Supplier<ModHelper> modHelper = new LazySupplier<>(ModHelper::new);

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
