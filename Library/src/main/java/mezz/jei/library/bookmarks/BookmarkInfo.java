package mezz.jei.library.bookmarks;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;

public class BookmarkInfo<T> {

    private final ResourceLocation identifier;
    private final IIngredientType<T> ingredientType;
    private final IIngredientHelper<T> ingredientHelper;
    private final IIngredientRenderer<T> ingredientRenderer;

    public BookmarkInfo(ResourceLocation identifier, IIngredientType<T> ingredientType, IIngredientHelper<T> ingredientHelper, IIngredientRenderer<T> ingredientRenderer) {
        this.identifier = identifier;
        this.ingredientType = ingredientType;
        this.ingredientHelper = ingredientHelper;
        this.ingredientRenderer = ingredientRenderer;
    }

    public ResourceLocation getIdentifier() {
        return identifier;
    }

    public IIngredientType<T> getIngredientType() {
        return ingredientType;
    }

    public IIngredientHelper<T> getIngredientHelper() {
        return ingredientHelper;
    }

    public IIngredientRenderer<T> getRenderer() {
        return ingredientRenderer;
    }


}
