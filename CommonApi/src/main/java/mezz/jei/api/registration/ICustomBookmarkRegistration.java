package mezz.jei.api.registration;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;

public interface ICustomBookmarkRegistration {

    /**
     * ingredientHelper should provide a implementation for serialising and deserializing bookmarks.
     *
     * @param identifier         A unique identifier that determines the type of the bookmark at the time of deserialization.
     * @param ingredientType     The jei ingredient type of the bookmarked object.
     * @param ingredientHelper   The ingredient helper for the bookmarked object.
     * @param ingredientRenderer The ingredient renderer for the bookmarked object.
     */
    <B> void registerCustomBookmark(
            ResourceLocation identifier,
            IIngredientType<B> ingredientType,
            IIngredientHelper<B> ingredientHelper,
            IIngredientRenderer<B> ingredientRenderer
    );

    /**
     * Register a registered ingredient as a bookmark.
     *
     * @param identifier     A unique identifier that determines the type of the bookmark at the time of deserialization.
     * @param ingredientType The jei ingredient type of the bookmarked object.
     */
    <B> void registerCustomBookmark(
            ResourceLocation identifier,
            IIngredientType<B> ingredientType
    );

}
