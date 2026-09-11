package mezz.jei.api.recipe;

import com.google.common.base.Suppliers;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.function.Supplier;

/**
 * Identifies a type of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Each {@link IRecipeCategory} can be uniquely identified by its {@link IRecipeType}.
 *
 * Unfortunately, the vanilla RecipeType only works for recipes that extend the vanilla {@link Recipe} class,
 * so this more general version is needed for modded recipes in JEI.
 *
 * @see RecipeTypes for all the built-in recipe types that are added by JEI.
 *
 * @since 9.5.0
 *
 * @deprecated Use {@link IRecipeType} to avoid naming collision with the vanilla {@link net.minecraft.world.item.crafting.RecipeType}.
 */
@Deprecated(since = "20.0.0", forRemoval = true)
public final class RecipeType<T> implements IRecipeType<T> {
	/**
	 * @deprecated use {@link IRecipeType#create}
	 */
	@Deprecated(since = "20.0.0", forRemoval = true)
	public static <T> RecipeType<T> create(String nameSpace, String path, Class<? extends T> recipeClass) {
		Identifier uid = Identifier.fromNamespaceAndPath(nameSpace, path);
		return new RecipeType<>(uid, recipeClass);
	}

	/**
	 * Create a JEI RecipeType from a Vanilla RecipeType.
	 * Returns a RecipeType that uses {@link RecipeHolder} to hold recipes.
	 * @since 16.0.0
	 * @deprecated use {@link IRecipeHolderType#create}
	 */
	@Deprecated(since = "20.0.0", forRemoval = true)
	public static <R extends Recipe<?>> RecipeType<RecipeHolder<R>> createFromVanilla(net.minecraft.world.item.crafting.RecipeType<R> vanillaRecipeType) {
		Identifier uid = BuiltInRegistries.RECIPE_TYPE.getKey(vanillaRecipeType);
		if (uid == null) {
			throw new IllegalArgumentException("Vanilla Recipe Type must be registered before using it here. %s".formatted(vanillaRecipeType));
		}
		return createRecipeHolderType(uid);
	}

	/**
	 * Create a JEI RecipeType from a Vanilla RecipeType's registered uid.
	 * Returns a RecipeType that uses {@link RecipeHolder} to hold recipes.
	 * @since 19.16.5
	 * @deprecated use {@link IRecipeHolderType#create} or {@link #create}.
	 */
	@Deprecated(since = "20.0.0", forRemoval = true)
	public static <R extends Recipe<?>> RecipeType<RecipeHolder<R>> createRecipeHolderType(Identifier uid) {
		@SuppressWarnings({"unchecked", "RedundantCast"})
		Class<? extends RecipeHolder<R>> holderClass = (Class<? extends RecipeHolder<R>>) (Object) RecipeHolder.class;
		return new RecipeType<>(uid, holderClass);
	}

	/**
	 * Create a Deferred Supplier for a JEI RecipeType from a Deferred Vanilla RecipeType.
	 *
	 * This is useful when you register vanilla recipe types using a deferred registration system.
	 *
	 * @since 19.16.5
	 * @deprecated use {@link IRecipeHolderType#createDeferred}
	 */
	@Deprecated(since = "20.0.0", forRemoval = true)
	public static <R extends Recipe<?>> Supplier<RecipeType<RecipeHolder<R>>> createFromDeferredVanilla(
		Supplier<net.minecraft.world.item.crafting.RecipeType<R>> deferredVanillaRecipeType
	) {
		return Suppliers.memoize(() -> createFromVanilla(deferredVanillaRecipeType.get()));
	}

	final Identifier uid;
	final Class<? extends T> recipeClass;

	@SuppressWarnings("ConstantValue")
	public RecipeType(Identifier uid, Class<? extends T> recipeClass) {
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
	@Override
	public Identifier getUid() {
		return uid;
	}

	/**
	 * The class of recipes represented by this recipe type.
	 */
	@Override
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
