package mezz.jei.gui.overlay.bookmarks;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IngredientsTooltipComponent implements ClientTooltipComponent, IBookmarkTooltip {
    private final List<Pair<IIngredientRenderer<?>, ITypedIngredient<?>>> ingredients = new ArrayList<>();

    public IngredientsTooltipComponent(IRecipeLayoutDrawable<?> layout, IIngredientManager ingredientManager) {
        IRecipeSlotsView recipeSlotsView = layout.getRecipeSlotsView();
        List<IRecipeSlotView> slots = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
        Map<String, ITypedIngredient<?>> summary = new HashMap<>();
        List<ITypedIngredient<?>> displayed = slots.stream()
            .map(IRecipeSlotView::getDisplayedIngredient)
            .filter(Optional::isPresent)
            .<ITypedIngredient<?>>map(Optional::get)
            .toList();

        for (ITypedIngredient<?> ingredient : displayed) {
            addToSummary(ingredient, ingredientManager, summary);
        }
        for (ITypedIngredient<?> value : summary.values()) {
            IIngredientRenderer<?> renderer = ingredientManager.getIngredientRenderer(value.getType());
            ingredients.add(Pair.of(renderer, value));
        }
        ingredients.sort(Comparator.comparingLong(i -> getAmount(i.getRight(), ingredientManager)));
    }

    private static <T> void addToSummary(ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager, Map<String, ITypedIngredient<?>> summary) {
        IIngredientType<T> type = typedIngredient.getType();
        IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(type);
        if (!helper.countable()) {
            return;
        }
        String uid = getUid(typedIngredient, ingredientManager);
        summary.compute(uid, (k, v) -> {
            if (v == null) {
                return typedIngredient;
            } else {
                return type.castIngredient(v)
                    .flatMap(i -> {
                        T merged = helper.merge(i, typedIngredient.getIngredient());
                        if (merged != null) {
                            return ingredientManager.createTypedIngredient(type, merged);
                        }
                        return Optional.empty();
                    })
                    .orElse(null);
            }
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
            Pair<IIngredientRenderer<?>, ITypedIngredient<?>> pair = ingredients.get(i);
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
