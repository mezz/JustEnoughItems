package mezz.jei.forge.config;

import mezz.jei.api.config.IClientConfig;
import mezz.jei.api.config.IIngredientFilterConfig;
import mezz.jei.api.config.IIngredientGridConfig;
import mezz.jei.api.config.IClientConfigs;
import mezz.jei.api.config.gui.HorizontalAlignment;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ClientConfigs implements IClientConfigs {
    private final IClientConfig clientConfig;
    private final IIngredientFilterConfig ingredientFilterConfig;
    private final IIngredientGridConfig ingredientListConfig;
    private final IIngredientGridConfig bookmarkListConfig;


    public static ClientConfigs register(ModLoadingContext modLoadingContext) {
        var configBuilder = new ForgeConfigSpec.Builder();
        var config = new ClientConfigs(configBuilder);
        var forgeConfig = configBuilder.build();

        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, forgeConfig);
        return config;
    }

    private ClientConfigs(ForgeConfigSpec.Builder builder) {
        clientConfig = new ClientConfig(builder);

        builder.push("search");
        {
            ingredientFilterConfig = new IngredientFilterConfig(builder);
        }
        builder.pop();

        builder.push("ingredient_list");
        {
            ingredientListConfig = new IngredientGridConfig(builder, HorizontalAlignment.RIGHT);
        }
        builder.pop();

        builder.push("bookmark_list");
        {
            bookmarkListConfig = new IngredientGridConfig(builder, HorizontalAlignment.LEFT);
        }
        builder.pop();
    }

    @Override
    public IClientConfig getClientConfig() {
        return clientConfig;
    }

    @Override
    public IIngredientFilterConfig getIngredientFilterConfig() {
        return ingredientFilterConfig;
    }

    @Override
    public IIngredientGridConfig getIngredientListConfig() {
        return ingredientListConfig;
    }

    @Override
    public IIngredientGridConfig getBookmarkListConfig() {
        return bookmarkListConfig;
    }
}
