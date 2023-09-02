package mezz.jei.api.runtime;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;

public interface IBookmarkManager {

    <V> ResourceLocation getIdentifier(V bookmark);

    <V> ResourceLocation getIdentifier(IIngredientType<V> ingredientType);

    /**
     * Returns the appropriate ingredient helper for this ingredient.
     */
    <V> IIngredientHelper<V> getIngredientHelper(V bookmark);

    <V> IIngredientHelper<V> getIngredientHelper(ResourceLocation identifier);

    /**
     * Returns the appropriate ingredient helper for this ingredient type.
     */
    <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType);

    /**
     * Returns the ingredient renderer for this ingredient.
     */
    <V> IIngredientRenderer<V> getIngredientRenderer(V bookmark);

    <V> IIngredientRenderer<V> getIngredientRenderer(ResourceLocation identifier);

    /**
     * Returns the ingredient renderer for this ingredient class.
     */
    <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType);


}
