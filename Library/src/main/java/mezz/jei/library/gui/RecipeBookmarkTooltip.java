package mezz.jei.library.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.library.ingredients.IIngredientSupplier;
import mezz.jei.library.util.IngredientSupplierHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeBookmarkTooltip implements ClientTooltipComponent, TooltipComponent {

    private final RecipeBookmark<?> recipeBookmark;

    public RecipeBookmarkTooltip(RecipeBookmark<?> recipeBookmark) {
        this.recipeBookmark = recipeBookmark;
    }


    @Override
    public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix4f, MultiBufferSource.@NotNull BufferSource blitOffset) {

    }

    @Override
    public void renderImage(@NotNull Font font, int mouseX, int mouseY, @NotNull PoseStack poseStack, @NotNull ItemRenderer itemRenderer, int blitOffset) {
        poseStack.pushPose();
        {
            recipeBookmark.getInnerLayout().setPosition(mouseX + 2, mouseY + 5);
            recipeBookmark.getInnerLayout().drawRecipe(poseStack, mouseX, mouseY);
        }
        poseStack.popPose();
    }

    @Override
    public int getHeight() {
        return recipeBookmark.getInnerLayout().getRect().getHeight() + 10;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return recipeBookmark.getInnerLayout().getRect().getWidth() + 4;
    }

    private static <R> List<RecipeIngredientData<?>> getAndMerged(RecipeIngredientRole role, IRecipeLayoutDrawable<R> recipeLayout, IIngredientManager ingredientManager) {
        List<RecipeIngredientData<?>> list = new ArrayList<>();
        RecipeIngredientData<?> data = null;
        IIngredientSupplier supplier = IngredientSupplierHelper.getIngredientSupplier(recipeLayout.getRecipe(), recipeLayout.getRecipeCategory(), ingredientManager);
        if (supplier != null) {
            //TODO:
        }

        return list;
    }

    public static class RecipeIngredientData<T> {

        private final IIngredientRenderer<T> ingredientRenderer;
        private final IIngredientHelper<T> ingredientHelper;
        /**
         * A merged Ingredient for rendering ingredient statistics of a recipe.
         */
        @NotNull
        private T mergedIngredient;

        public RecipeIngredientData(IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper, T first) {
            this.ingredientRenderer = ingredientRenderer;
            this.ingredientHelper = ingredientHelper;
            this.mergedIngredient = first;
        }

        public boolean merge(T ingredient) {
            Optional<T> merged = ingredientHelper.merge(mergedIngredient, ingredient);
            this.mergedIngredient = merged.orElse(mergedIngredient);
            return merged.isPresent();
        }

        public void render(PoseStack poseStack) {
            this.ingredientRenderer.render(poseStack, mergedIngredient);
        }

    }

}
