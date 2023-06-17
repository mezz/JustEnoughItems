package mezz.jei.api.recipe.category.extensions;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IGlobalRecipeCategoryExtension<T> {
    default void draw(T recipe, IRecipeCategory<T> recipeCategory, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {

    }

    default List<Component> decorateRecipeCategoryTooltips(List<Component> tooltips, T recipe, IRecipeCategory<T> recipeCategory, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return tooltips;
    }

    default List<Component> getTooltipStrings(T recipe, IRecipeCategory<T> recipeCategory, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return List.of();
    }
}
