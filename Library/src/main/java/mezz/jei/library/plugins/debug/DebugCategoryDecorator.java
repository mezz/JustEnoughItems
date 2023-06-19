package mezz.jei.library.plugins.debug;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

class DebugCategoryDecorator<T> implements IRecipeCategoryDecorator<T> {
    private static final DebugCategoryDecorator<?> INSTANCE = new DebugCategoryDecorator<>();

    @SuppressWarnings("unchecked")
    public static <T> DebugCategoryDecorator<T> getInstance() {
        return (DebugCategoryDecorator<T>) INSTANCE;
    }

    @Override
    public void draw(
        T recipe, IRecipeCategory<T> recipeCategory, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
        double mouseX, double mouseY
    ) {
        var id = recipeCategory.getRegistryName(recipe);
        if (id == null) {
            return;
        }

        var posX = recipeCategory.getWidth() / 2;
        var posY = recipeCategory.getHeight();
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.drawCenteredString(minecraft.font, "Decorator: " + id, posX, posY, 0xFF_FFFF);
    }
}
