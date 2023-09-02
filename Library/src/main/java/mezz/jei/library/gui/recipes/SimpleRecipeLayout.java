package mezz.jei.library.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.library.gui.ingredients.RecipeSlots;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static mezz.jei.library.gui.recipes.RecipeLayout.RECIPE_BORDER_PADDING;

public class SimpleRecipeLayout<R> implements IRecipeLayoutDrawable<R> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final int ingredientCycleOffset = (int) ((Math.random() * 10000) % Integer.MAX_VALUE);

    private final IRecipeCategory<R> recipeCategory;
    private final IIngredientManager ingredientManager;
    private final IModIdHelper modIdHelper;
    private final Textures textures;
    private final RecipeSlots recipeSlots;
    private final R recipe;
    private final DrawableNineSliceTexture recipeBorder;
    @Nullable
    private ShapelessIcon shapelessIcon;

    private int posX;
    private int posY;

    public static <T> Optional<IRecipeLayoutDrawable<T>> create(IRecipeCategory<T> recipeCategory, T recipe, IFocusGroup focuses, IIngredientManager ingredientManager, IIngredientVisibility ingredientVisibility, IModIdHelper modIdHelper, Textures textures) {
        SimpleRecipeLayout<T> recipeLayout = new SimpleRecipeLayout<>(recipeCategory, recipe, ingredientManager, modIdHelper, textures);
        RecipeLayoutBuilder builder = new RecipeLayoutBuilder(ingredientManager, recipeLayout.ingredientCycleOffset);
        try {
            recipeCategory.setRecipe(builder, recipe, focuses);
            if (builder.isUsed()) {
                builder.setRecipeLayout(recipeLayout, focuses, ingredientVisibility);
                return Optional.of(recipeLayout);
            }
        } catch (RuntimeException | LinkageError e) {
            LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getRecipeType().getUid(), e);
        }
        return Optional.empty();
    }

    public SimpleRecipeLayout(
            IRecipeCategory<R> recipeCategory,
            R recipe,
            IIngredientManager ingredientManager,
            IModIdHelper modIdHelper,
            Textures textures
    ) {
        this.recipeCategory = recipeCategory;
        this.recipe = recipe;
        this.ingredientManager = ingredientManager;
        this.modIdHelper = modIdHelper;
        this.textures = textures;
        this.recipeSlots = new RecipeSlots();
        this.recipeBorder = textures.getRecipePreviewBackground();
    }

    public void setShapeless() {
        this.shapelessIcon = new ShapelessIcon(textures);
        int categoryWidth = this.recipeCategory.getWidth();

        // align to top-right
        int x = categoryWidth - shapelessIcon.getIcon().getWidth();
        int y = 0;
        this.shapelessIcon.setPosition(x, y);
    }

    public void setShapeless(int shapelessX, int shapelessY) {
        this.shapelessIcon = new ShapelessIcon(textures);
        this.shapelessIcon.setPosition(shapelessX, shapelessY);
    }

    @Override
    public void setPosition(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    @Override
    public void drawRecipe(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        IDrawable background = recipeCategory.getBackground();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        final int recipeMouseX = mouseX - posX;
        final int recipeMouseY = mouseY - posY;

        poseStack.pushPose();
        {
            poseStack.translate(posX, posY, 0);

            int width = recipeCategory.getWidth() + (2 * RECIPE_BORDER_PADDING);
            int height = recipeCategory.getHeight() + (2 * RECIPE_BORDER_PADDING);
            recipeBorder.draw(poseStack, -RECIPE_BORDER_PADDING, -RECIPE_BORDER_PADDING, width, height);
            background.draw(poseStack);

            // defensive push/pop to protect against recipe categories changing the last pose
            poseStack.pushPose();
            {
                recipeCategory.draw(recipe, recipeSlots.getView(), poseStack, recipeMouseX, recipeMouseY);

                // drawExtras and drawInfo often render text which messes with the color, this clears it
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            poseStack.popPose();

            if (shapelessIcon != null) {
                shapelessIcon.draw(poseStack);
            }

            recipeSlots.draw(poseStack);
        }
        poseStack.popPose();

        RenderSystem.disableBlend();
    }

    @Override
    public void drawOverlays(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        //:P
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public @NotNull Optional<ItemStack> getItemStackUnderMouse(int mouseX, int mouseY) {
        return Optional.empty();
    }

    @Override
    public <T> @NotNull Optional<T> getIngredientUnderMouse(int mouseX, int mouseY, @NotNull IIngredientType<T> ingredientType) {
        return Optional.empty();
    }

    /**
     * Don't use it!
     */
    @Override
    public @NotNull Optional<IRecipeSlotDrawable> getRecipeSlotUnderMouse(double mouseX, double mouseY) {
        return Optional.empty();
    }

    @Override
    public @NotNull Rect2i getRect() {
        return new Rect2i(posX, posY, recipeCategory.getWidth(), recipeCategory.getHeight());
    }

    @Override
    public @NotNull Rect2i getRecipeTransferButtonArea() {
        return new Rect2i(0, 0, 0, 0);
    }

    @Override
    public @NotNull IRecipeSlotsView getRecipeSlotsView() {
        return recipeSlots.getView();
    }

    @Override
    public @NotNull IRecipeCategory<R> getRecipeCategory() {
        return recipeCategory;
    }

    @Override
    public @NotNull R getRecipe() {
        return recipe;
    }

    public RecipeSlots getRecipeSlots() {
        return recipeSlots;
    }
}
