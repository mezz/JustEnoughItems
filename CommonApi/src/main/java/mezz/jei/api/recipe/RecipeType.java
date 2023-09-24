package mezz.jei.api.recipe;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Identifies a type of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Each {@link IRecipeCategory} can be uniquely identified by its {@link RecipeType}.
 *
 * Unfortunately, the vanilla RecipeType only works for recipes that extend the vanilla {@link Recipe} class,
 * so this more general version is needed for modded recipes in JEI.
 *
 * @see RecipeTypes for all the built-in recipe types that are added by JEI.
 *
 * @since 9.5.0
 */
public final class RecipeType<T> {
	public static <T> RecipeType<T> create(String nameSpace, String path, Class<? extends T> recipeClass) {
		ResourceLocation uid = new ResourceLocation(nameSpace, path);
		return new RecipeType<>(uid, recipeClass);
	}

	/**
	 * Create a JEI RecipeType from a Vanilla RecipeType.
	 * Returns a RecipeType that uses {@link RecipeHolder} to hold recipes.
	 * @since 16.0.0
	 */
	public static <R extends Recipe<?>> RecipeType<RecipeHolder<R>> createFromVanilla(net.minecraft.world.item.crafting.RecipeType<R> vanillaRecipeType) {
		ResourceLocation uid = BuiltInRegistries.RECIPE_TYPE.getKey(vanillaRecipeType);
		if (uid == null) {
			throw new IllegalArgumentException("Vanilla Recipe Type must be registered before using it here. %s".formatted(vanillaRecipeType));
		}
		@SuppressWarnings({"unchecked", "RedundantCast"})
		Class<? extends RecipeHolder<R>> holderClass = (Class<? extends RecipeHolder<R>>) (Object) RecipeHolder.class;
		return new RecipeType<>(uid, holderClass);
	}

	private final ResourceLocation uid;
	private final Class<? extends T> recipeClass;

	@SuppressWarnings("ConstantValue")
	public RecipeType(ResourceLocation uid, Class<? extends T> recipeClass) {
		if (uid == null) {
			throw new NullPointerException("uid must not be null.");
		}
		if (recipeClass == null) {
			throw new NullPointerException("recipeClass must not be null.");
		}
		this.uid = uid;
		this.recipeClass = recipeClass;
	}

	/**
	 * The unique id of this recipe type.
	 */
	public ResourceLocation getUid() {
		return uid;
	}

	/**
	 * The class of recipes represented by this recipe type.
	 */
	public Class<? extends T> getRecipeClass() {
		return recipeClass;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof RecipeType<?> other)) {
			return false;
		}
		return this.recipeClass == other.recipeClass &&
			this.uid.equals(other.uid);
	}

	@Override
	public int hashCode() {
		return 31 * uid.hashCode() + recipeClass.hashCode();
	}

	@Override
	public String toString() {
		return "RecipeType[" +
			"uid=" + uid + ", " +
			"recipeClass=" + recipeClass + ']';
	}

}
