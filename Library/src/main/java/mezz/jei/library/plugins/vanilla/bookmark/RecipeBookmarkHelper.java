package mezz.jei.library.plugins.vanilla.bookmark;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.library.InternalLibrary;
import mezz.jei.library.gui.ingredients.CycleTimer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;


@SuppressWarnings({"rawtypes", "unchecked"})
public class RecipeBookmarkHelper implements IIngredientHelper<RecipeBookmark> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final CycleTimer cycleTimer = new CycleTimer((int) ((Math.random() * 10000) % Integer.MAX_VALUE));

    private <T> Optional<Pair<ITypedIngredient<T>, IIngredientHelper<T>>> getCurrentHelper(RecipeBookmark<?> recipeBookmark) {
        return cycleTimer.getCycledIngredient(recipeBookmark.getTargets())
                .map(target -> Pair.of((ITypedIngredient<T>) target, (IIngredientHelper<T>) recipeBookmark.getIngredientManager().getIngredientHelper(target.getType())));
    }

    @Override
    public @NotNull IIngredientType<RecipeBookmark> getIngredientType() {
        return RecipeBookmark.TYPE;
    }

    @Override
    public @NotNull String getDisplayName(@NotNull RecipeBookmark ingredient) {
        return getCurrentHelper(ingredient)
                .map(pair -> pair.getRight().getDisplayName(pair.getLeft().getIngredient()))
                .orElse("Unknown");
    }

    @Override
    public @NotNull String getUniqueId(@NotNull RecipeBookmark ingredient, @NotNull UidContext context) {
        return ingredient.getUid();
    }

    @Override
    public @NotNull String getWildcardId(@NotNull RecipeBookmark ingredient) {
        return getCurrentHelper(ingredient)
                .map(pair -> pair.getRight().getWildcardId(pair.getLeft().getIngredient()))
                .orElse("Unknown");
    }

    @Override
    public @NotNull String getDisplayModId(@NotNull RecipeBookmark ingredient) {
        return getCurrentHelper(ingredient)
                .map(pair -> pair.getRight().getDisplayModId(pair.getLeft().getIngredient()))
                .orElse("Unknown");
    }

    @Override
    public @NotNull Iterable<Integer> getColors(@NotNull RecipeBookmark ingredient) {
        return getCurrentHelper(ingredient)
                .map(pair -> pair.getRight().getColors(pair.getLeft().getIngredient()))
                .orElse(Collections.emptyList());
    }

    @Override
    public @NotNull ResourceLocation getResourceLocation(@NotNull RecipeBookmark ingredient) {
        return getCurrentHelper(ingredient)
                .map(pair -> pair.getRight().getResourceLocation(pair.getLeft().getIngredient()))
                .orElse(new ResourceLocation("unknown"));
    }

    @Override
    public @NotNull ItemStack getCheatItemStack(@NotNull RecipeBookmark ingredient) {
        return getCurrentHelper(ingredient)
                .map(pair -> pair.getRight().getCheatItemStack(pair.getLeft().getIngredient()))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public @NotNull RecipeBookmark copyIngredient(@NotNull RecipeBookmark ingredient) {
        return ingredient;
    }

    @Override
    public @NotNull RecipeBookmark normalizeIngredient(@NotNull RecipeBookmark ingredient) {
        return ingredient;
    }

    @Override
    public boolean isValidIngredient(@NotNull RecipeBookmark ingredient) {
        return true;
    }

    @Override
    public boolean isIngredientOnServer(@NotNull RecipeBookmark ingredient) {
        return true;
    }

    @Override
    public @NotNull Collection<ResourceLocation> getTags(@NotNull RecipeBookmark ingredient) {
        return getCurrentHelper(ingredient)
                .map(pair -> pair.getRight().getTags(pair.getLeft().getIngredient()))
                .orElse(Collections.emptyList());
    }

    @Override
    public @NotNull Stream<ResourceLocation> getTagStream(@NotNull RecipeBookmark ingredient) {
        return getCurrentHelper(ingredient)
                .map(pair -> pair.getRight().getTagStream(pair.getLeft().getIngredient()))
                .orElse(Stream.empty());
    }

    @Override
    public @NotNull Collection<String> getCreativeTabNames(@NotNull RecipeBookmark ingredient) {
        return getCurrentHelper(ingredient)
                .map(pair -> pair.getRight().getCreativeTabNames(pair.getLeft().getIngredient()))
                .orElse(Collections.emptyList());
    }

    @Override
    public @NotNull String getErrorInfo(@Nullable RecipeBookmark ingredient) {
        return ingredient == null ? "null" : getCurrentHelper(ingredient)
                .map(pair -> pair.getRight().getErrorInfo(pair.getLeft().getIngredient()))
                .orElse("Unknown");
    }

    @Override
    public @NotNull Optional<ResourceLocation> getTagEquivalent(@NotNull Collection<RecipeBookmark> ingredients) {
        //No RecipeSlot will hold a RecipeBookmark
        return Optional.empty();
    }

    @Override
    public @NotNull CompoundTag serialize(@NotNull RecipeBookmark ingredient) {
        CompoundTag tag = new CompoundTag();
        writeRecipe(tag, ingredient.getRecipeCategory(), ingredient.getInnerLayout().getRecipe());
        writeTargets(ingredient.getTargets(), tag);
        return tag;
    }


    private static <T> void writeRecipe(CompoundTag tag, IRecipeCategory<T> recipeCategory, T recipe) {
        tag.putString("category", recipeCategory.getRecipeType().getUid().toString());
        ResourceLocation recipeUid = recipeCategory.getUniqueId(recipe);
        if (recipeUid != null) {
            tag.putString("recipeUid", recipeUid.toString());
        }
    }

    private static <T> void writeTargets(List<ITypedIngredient<T>> targets, CompoundTag tag) {
        CompoundTag targetsTag = new CompoundTag();
        IIngredientManager ingredientManager = InternalLibrary.getIngredientManager();
        for (int i = 0; i < targets.size(); i++) {
            ITypedIngredient<T> target = targets.get(i);
            IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(target.getType());
            CompoundTag serialized = ingredientHelper.serialize(target.getIngredient());
            if (serialized.isEmpty()) {
                serialized.putString("targetUid", ingredientHelper.getUniqueId(target.getIngredient(), UidContext.Ingredient));
            }
            targetsTag.put(String.valueOf(i), serialized);
        }
        tag.put("targets", targetsTag);
    }


    @Override
    public @NotNull Optional<RecipeBookmark> deserialize(@NotNull CompoundTag tag) {
        if (tag.isEmpty()) return Optional.empty();

        IIngredientManager ingredientManager = InternalLibrary.getIngredientManager();

        return Optional.ofNullable(deserializeBookmark(tag,
                ingredientManager,
                InternalLibrary.getRecipeManager(),
                InternalLibrary.getFocusFactory(),
                ingredientManager.getRegisteredIngredientTypes()));
    }

    private static <R> RecipeBookmark<R> deserializeBookmark(CompoundTag tag, IIngredientManager ingredientManager, IRecipeManager recipeManager, IFocusFactory focusFactory, Collection<IIngredientType<?>> allTypes) {
        ResourceLocation categoryUid = new ResourceLocation(tag.getString("category"));
        RecipeType<R> recipeType = (RecipeType<R>) recipeManager.getRecipeType(categoryUid).orElse(null);
        ResourceLocation recipeUid;
        if (tag.contains("recipeUid")) {
            recipeUid = new ResourceLocation(tag.getString("recipeUid"));
        } else {
            LOGGER.error("Failed to deserialize bookmark: No recipe uid");
            return null;
        }
        if (recipeType == null) {
            LOGGER.error("Failed to deserialize bookmark: Unknown recipe type {}", categoryUid);
            return null;
        }
        IRecipeCategory<R> recipeCategory = (IRecipeCategory<R>) recipeManager.createRecipeCategoryLookup()
                .limitTypes(Collections.singleton(recipeType))
                .get()
                .findFirst()
                .orElse(null);

        if (recipeCategory == null) {
            LOGGER.error("Failed to deserialize bookmark: Unknown recipe category {}", categoryUid);
            return null;
        }
        if (!tag.contains("targets")) {
            LOGGER.error("Failed to deserialize bookmark: No targets");
            return null;
        }

        CompoundTag targetsTag = tag.getCompound("targets");
        List<ITypedIngredient<?>> targets = new ArrayList<>();

        for (String key : targetsTag.getAllKeys()) {
            CompoundTag targetTag = targetsTag.getCompound(key);
            if (targetTag.contains("targetUid")) {
                String targetUid = targetTag.getString("targetUid");
                Optional<ITypedIngredient<?>> target = getNormalizedIngredientByUid(ingredientManager, allTypes, targetUid);
                target.ifPresent(targets::add);
                break;
            } else {
                Optional<ITypedIngredient<?>> target = deserializeIngredient(targetTag, ingredientManager, allTypes);
                target.ifPresent(targets::add);
                break;
            }
        }

        if (targets.isEmpty()) {
            LOGGER.error("Failed to deserialize bookmark: Cannot deserialize targets");
            return null;
        }

        return recipeManager.getRecipeByUid(recipeType, recipeUid)
                .map(recipe -> new RecipeBookmark<>(recipe, recipeCategory, targets, ingredientManager, focusFactory, recipeManager))
                .orElse(null);

    }

    private static <V> Optional<ITypedIngredient<?>> deserializeIngredient(CompoundTag targetTag, IIngredientManager ingredientManager, Collection<IIngredientType<?>> allTypes) {
        for (IIngredientType<?> ingredientType : allTypes) {
            IIngredientHelper<?> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
            Optional<ITypedIngredient<?>> ingredient = ingredientHelper.deserialize(targetTag)
                    .flatMap(target -> ingredientManager.createTypedIngredient((IIngredientType<V>) ingredientType, (V) target));
            if (ingredient.isPresent()) {
                return ingredient;
            }
        }
        return Optional.empty();
    }

    private static Optional<ITypedIngredient<?>> getNormalizedIngredientByUid(IIngredientManager ingredientManager, Collection<IIngredientType<?>> ingredientTypes, String uid) {
        return ingredientTypes.stream()
                .map(t -> getNormalizedIngredientByUid(ingredientManager, t, uid))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static <T> Optional<ITypedIngredient<?>> getNormalizedIngredientByUid(IIngredientManager ingredientManager, IIngredientType<T> ingredientType, String uid) {
        return ingredientManager.getIngredientByUid(ingredientType, uid)
                .map(i -> {
                    IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
                    return ingredientHelper.normalizeIngredient(i);
                })
                .flatMap(i -> ingredientManager.createTypedIngredient(ingredientType, i));
    }


}
