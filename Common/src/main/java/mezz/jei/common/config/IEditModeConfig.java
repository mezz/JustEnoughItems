package mezz.jei.common.config;

import mezz.jei.api.ingredients.ITypedIngredient;

public interface IEditModeConfig {
    <V> boolean isIngredientHiddenUsingConfigFile(ITypedIngredient<V> ingredient);

    <V> void hideIngredientUsingConfigFile(ITypedIngredient<V> ingredient, Mode mode);

    <V> void showIngredientUsingConfigFile(ITypedIngredient<V> ingredient, Mode mode);

    enum Mode {
        ITEM, WILDCARD
    }
}
