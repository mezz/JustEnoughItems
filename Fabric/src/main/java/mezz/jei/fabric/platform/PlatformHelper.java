package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.IPlatformHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class PlatformHelper implements IPlatformHelper {
    private final ItemStackHelper itemStackHelper = new ItemStackHelper();
    private final FluidHelper fluidHelper = new FluidHelper();
    private final RenderHelper renderHelper = new RenderHelper();
    private final RecipeHelper recipeHelper = new RecipeHelper();
    private final ConfigHelper configHelper = new ConfigHelper();
    private final InputHelper inputHelper = new InputHelper();
    private final ScreenHelper screenHelper = new ScreenHelper();
    private final IngredientHelper ingredientHelper = new IngredientHelper();
    private final ModHelper modHelper = new ModHelper();

    @Override
    public <T> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return RegistryWrapper.getRegistry(key);
    }

    @Override
    public ItemStackHelper getItemStackHelper() {
        return itemStackHelper;
    }

    @Override
    public IPlatformFluidHelperInternal<?> getFluidHelper() {
        return fluidHelper;
    }

    @Override
    public RenderHelper getRenderHelper() {
        return renderHelper;
    }

    @Override
    public RecipeHelper getRecipeHelper() {
        return recipeHelper;
    }

    @Override
    public ConfigHelper getConfigHelper() {
        return configHelper;
    }

    @Override
    public InputHelper getInputHelper() {
        return inputHelper;
    }

    @Override
    public ScreenHelper getScreenHelper() {
        return screenHelper;
    }

    @Override
    public IngredientHelper getIngredientHelper() {
        return ingredientHelper;
    }

    @Override
    public ModHelper getModHelper() {
        return modHelper;
    }
}
