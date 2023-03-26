package mezz.jei.gui.bookmarks;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.IngredientLookupState;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.UUID;

/**
 * Used to save recipe information and provide a suitable way to show it to the player.
 */
public class RecipeBookmark<R> {

    public static final IIngredientType<RecipeBookmark> TYPE = () -> RecipeBookmark.class;

    private static final Logger LOGGER = LogManager.getLogger();

    private final IIngredientManager ingredientManager;
    private final IRecipeCategory<R> recipeCategory;
    private final List<ITypedIngredient<?>> targets;
    private final IFocusGroup focuses;
    /**
     * Equivalent to IngredientLookupState's index.
     */
    private final int index;
    private final RecipeType<R> recipeType;
    /**
     * From {@link IRecipeCategory#getUniqueId(Object)}
     */
    private final String uid;

    /**
     * For rendering preview of the recipe.
     */
    private final IRecipeLayoutDrawable<R> innerRecipeLayout;

    public RecipeBookmark(
            R recipe,
            IRecipeCategory<R> recipeCategory,
            List<ITypedIngredient<?>> targets,
            IIngredientManager ingredientManager,
            IFocusFactory focusFactory,
            IRecipeManager recipeManager
    ) {
        this.recipeCategory = recipeCategory;
        this.targets = targets;
        this.ingredientManager = ingredientManager;
        this.focuses = createFocuses(targets, ingredientManager, focusFactory);

        this.innerRecipeLayout = recipeManager.createSimpleRecipeLayoutDrawable(
                recipeCategory,
                recipe,
                focuses
        ).orElseThrow(() -> new NullPointerException("Failed to create recipe layout for Recipe Category: " + recipeCategory.getRecipeType().getUid()));

        this.index = findIndex(recipe, recipeCategory, recipeManager, focuses);
        this.recipeType = recipeCategory.getRecipeType();
        ResourceLocation uniqueId = recipeCategory.getUniqueId(recipe);
        this.uid = uniqueId == null ? UUID.randomUUID().toString() : recipeType + ":" + uniqueId;
    }

    private static IFocusGroup createFocuses(List<ITypedIngredient<?>> targets, IIngredientManager ingredientManager, IFocusFactory focusFactory) {
        return focusFactory.createFocusGroup(
                targets.stream()
                        .map(target -> createFocus(target, ingredientManager, focusFactory))
                        .toList()
        );
    }

    private static <V> IFocus<?> createFocus(ITypedIngredient<V> typedIngredient, IIngredientManager ingredientManager, IFocusFactory focusFactory) {
        V normalizeIngredient = ingredientManager.getIngredientHelper(typedIngredient.getType())
                .normalizeIngredient(typedIngredient.getIngredient());
        return focusFactory.createFocus(RecipeIngredientRole.OUTPUT, typedIngredient.getType(), normalizeIngredient);
    }

    private static <R> int findIndex(R recipe, IRecipeCategory<R> recipeCategory, IRecipeManager recipeManager, IFocusGroup focuses) {
        IngredientLookupState state = IngredientLookupState.createWithFocus(recipeManager, focuses);
        state.setRecipeCategory(recipeCategory);
        @Unmodifiable List<?> recipes = state.getFocusedRecipes().getRecipes();
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i) == recipe) {
                return i;
            }
        }
        throw new IllegalStateException("Cannot find recipe index: " + recipe);
    }

    public IRecipeCategory<R> getRecipeCategory() {
        return recipeCategory;
    }

    public RecipeType<R> getRecipeType() {
        return recipeCategory.getRecipeType();
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        return recipeCategory.getRegistryName(getInnerLayout().getRecipe());
    }

    public List<ITypedIngredient<?>> getTargets() {
        return targets;
    }

    public IFocusGroup getFocuses() {
        return focuses;
    }

    public int getIndex() {
        return index;
    }

    public String getUid() {
        return uid;
    }

    public IIngredientManager getIngredientManager() {
        return ingredientManager;
    }

    public IRecipeLayoutDrawable<R> getInnerLayout() {
        return innerRecipeLayout;
    }

    @Override
    public String toString() {
        return "RecipeBookmark{" +
                "recipeCategory=" + recipeCategory +
                ", targets=" + targets +
                ", focuses=" + focuses +
                ", recipeType=" + recipeType +
                ", uid='" + uid + '\'' +
                '}';
    }
}
