package mezz.jei.library.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.library.gui.RecipeBookmarkTooltip;
import mezz.jei.library.gui.ingredients.CycleTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public class RecipeBookmarkRender implements IIngredientRenderer<RecipeBookmark> {

    private final IDrawableStatic recipeBookmarkBackground;
    private final CycleTimer cycleTimer = new CycleTimer((int) ((Math.random() * 10000) % Integer.MAX_VALUE));

    public RecipeBookmarkRender(IDrawableStatic recipeBookmarkBackground) {
        this.recipeBookmarkBackground = recipeBookmarkBackground;
    }

    private <T> Optional<Pair<ITypedIngredient<T>, IIngredientRenderer<T>>> getCurrentRender(RecipeBookmark<?> recipeBookmark) {
        return cycleTimer.getCycledIngredient(recipeBookmark.getTargets())
            .map(target -> Pair.of((ITypedIngredient<T>) target, (IIngredientRenderer<T>) recipeBookmark.getIngredientManager().getIngredientRenderer(target.getType())));
    }

    @Override
    public void render(PoseStack stack, RecipeBookmark recipeBookmark) {
        recipeBookmarkBackground.draw(stack, 0, 0);
        getCurrentRender(recipeBookmark).ifPresent(pair -> {
            IIngredientRenderer renderer = pair.getRight();
            renderer.render(stack, pair.getLeft().getIngredient());
        });
    }

    @Override
    public List<Component> getTooltip(RecipeBookmark recipeBookmark, TooltipFlag tooltipFlag) {
        return getCurrentRender(recipeBookmark).map(pair -> {
            IIngredientRenderer renderer = pair.getRight();
            return renderer.getTooltip(pair.getLeft().getIngredient(), tooltipFlag);
        }).orElse(Collections.emptyList());
    }

    @Override
    public List<ClientTooltipComponent> addTooltipComponment(List<ClientTooltipComponent> components, RecipeBookmark ingredient, TooltipFlag tooltipFlag) {
        components.add(new RecipeBookmarkTooltip(ingredient));
        return components;
    }

    @Override
    public Font getFontRenderer(Minecraft minecraft, RecipeBookmark recipeBookmark) {
        return getCurrentRender(recipeBookmark).map(pair -> {
            IIngredientRenderer helper = pair.getRight();
            return helper.getFontRenderer(minecraft, pair.getLeft().getIngredient());
        }).orElse(minecraft.font);
    }

    @Override
    public int getWidth() {
        return IIngredientRenderer.super.getWidth();
    }

    @Override
    public int getHeight() {
        return IIngredientRenderer.super.getHeight();
    }

}
