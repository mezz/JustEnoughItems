package mezz.jei.common.gui.recipes.layout;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.ingredients.RecipeSlot;
import mezz.jei.common.gui.ingredients.RecipeSlots;

import java.util.Optional;

public interface IRecipeLayoutInternal<R> {
    int getPosY();

    R getRecipe();

    RecipeSlots getRecipeSlots();

    void setPosition(int posX, int posY);

    void drawRecipe(PoseStack poseStack, int mouseX, int mouseY);

    void drawOverlays(PoseStack poseStack, int mouseX, int mouseY);

    boolean isMouseOver(double mouseX, double mouseY);

    Optional<RecipeSlot> getRecipeSlotUnderMouse(double mouseX, double mouseY);

    void moveRecipeTransferButton(int posX, int posY);

    void setShapeless();

    IRecipeCategory<R> getRecipeCategory();

    int getPosX();
}
