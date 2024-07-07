package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IngredientsTooltipComponent implements ClientTooltipComponent, IBookmarkTooltip {

    private final List<Pair<IIngredientRenderer, ITypedIngredient>> ingredients = new ArrayList<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public IngredientsTooltipComponent(IRecipeLayoutDrawable<?> layout, IIngredientManager ingredientManager) {
        List<IRecipeSlotView> slots = layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT);
        Map<String, ITypedIngredient> summary = new HashMap<>();
        var displayed = slots.stream()
                .map(IRecipeSlotView::getDisplayedIngredient)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        for (ITypedIngredient ingredient : displayed) {
            String uid = getUid(ingredient, ingredientManager);
            var storage = summary.get(uid);
            if (storage != null) {
                IIngredientHelper helper = ingredientManager.getIngredientHelper(ingredient.getType());
                Object merged = helper.merge(storage.getIngredient(), ingredient.getIngredient());
                if (merged != null) {
                    ingredientManager.createTypedIngredient(ingredient.getType(), merged).ifPresent(i -> summary.put(uid, (ITypedIngredient) i));
                }
            } else {
                summary.put(uid, ingredient);
            }
        }
        for (ITypedIngredient value : summary.values()) {
            IIngredientRenderer renderer = ingredientManager.getIngredientRenderer(value.getType());
            ingredients.add(Pair.of(renderer, value));
        }
        ingredients.sort((o1, o2) -> {
            long amount1 = getAmount(o1.getRight(), ingredientManager);
            long amount2 = getAmount(o2.getRight(), ingredientManager);
            return Long.compare(amount2, amount1);
        });
    }

    private static <T> long getAmount(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
        IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(typedIngredient.getType());
        return helper.getAmount(typedIngredient.getIngredient());
    }

    private static <T> String getUid(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
        IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
        return ingredientHelper.getUniqueId(typedIngredient.getIngredient(), UidContext.Recipe);
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public int getWidth(Font font) {
        return ingredients.size() * 16;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        for (int i = 0; i < ingredients.size(); i++) {
            Pair<IIngredientRenderer, ITypedIngredient> pair = ingredients.get(i);
            IIngredientRenderer renderer = pair.getLeft();
            Object ingredient = pair.getRight().getIngredient();
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(x + i * 16, y, 0);
            renderer.render(guiGraphics, ingredient);
            pose.popPose();
        }
    }

    @Override
    public boolean longTerm() {
        return false;
    }
}
