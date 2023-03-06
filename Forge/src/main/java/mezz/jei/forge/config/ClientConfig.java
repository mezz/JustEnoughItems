package mezz.jei.forge.config;

import mezz.jei.core.config.GiveMode;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IngredientSortStage;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.stream.Stream;

public class ClientConfig implements IClientConfig {
    private final ForgeConfigSpec.BooleanValue centerSearch;
    private final ForgeConfigSpec.BooleanValue lowMemorySearch;
    private final ForgeConfigSpec.BooleanValue cheatToHotbar;
    private final ForgeConfigSpec.BooleanValue addBookmarksToFront;
    private final ForgeConfigSpec.EnumValue<GiveMode> giveMode;
    private final ForgeConfigSpec.IntValue maxRecipeGuiHeight;
    private final ForgeConfigSpec.ConfigValue<List<? extends IngredientSortStage>> ingredientSorterStages;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("advanced");
        {
            builder.comment("Display search bar in the center");
            centerSearch = builder.define("center_search", defaultCenterSearchBar);

            builder.comment("LowMemorySlowSearchEnabled");
            lowMemorySearch = builder.define("low_memory_search", false);

            builder.comment("Enable cheating items into the hotbar by using the shift+number keys.");
            cheatToHotbar = builder.define("cheat_to_hotbar", false);

            builder.comment("Enable adding new bookmarks to the front of the bookmark list.");
            addBookmarksToFront = builder.define("add_bookmarks_to_front", true);

            builder.comment("How items should be handed to you");
            giveMode = builder.defineEnum("give_mode", GiveMode.defaultGiveMode);

            builder.comment("Max. recipe gui height");
            maxRecipeGuiHeight = builder.defineInRange(
                    "recipe_gui_height",
                    defaultRecipeGuiHeight,
                    minRecipeGuiHeight,
                    Integer.MAX_VALUE
            );
        }
        builder.pop();

        builder.push("sorting");
        {
            builder.comment("Sorting order for the ingredient list");
            ingredientSorterStages = builder.defineListAllowEmpty(
                    List.of("ingredient_sort_stages"),
                    () -> IngredientSortStage.defaultStages,
                    obj -> obj instanceof IngredientSortStage
            );
        }
        builder.pop();
    }

    @Override
    public boolean isCenterSearchBarEnabled() {
        return centerSearch.get();
    }

    @Override
    public boolean isLowMemorySlowSearchEnabled() {
        return lowMemorySearch.get();
    }

    @Override
    public boolean isCheatToHotbarUsingHotkeysEnabled() {
        return cheatToHotbar.get();
    }

    @Override
    public boolean isAddingBookmarksToFront() {
        return addBookmarksToFront.get();
    }

    @Override
    public GiveMode getGiveMode() {
        return giveMode.get();
    }

    @Override
    public int getMaxRecipeGuiHeight() {
        return maxRecipeGuiHeight.get();
    }

    @Override
    public Stream<IngredientSortStage> getIngredientSorterStages() {
        return ingredientSorterStages.get().stream().map(stage -> (IngredientSortStage) stage);
    }
}
