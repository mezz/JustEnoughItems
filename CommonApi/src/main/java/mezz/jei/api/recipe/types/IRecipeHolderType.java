package mezz.jei.api.recipe.types;

import com.google.common.base.Suppliers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

/**
 * Convenience type that makes it easier to work with {@link IRecipeType} created with vanilla's {@link RecipeHolder}.
 *
 * Create instances with {@link IRecipeHolderType#create}
 *
 * @since 20.0.0
 */
public interface IRecipeHolderType<T extends Recipe<?>> extends IRecipeType<RecipeHolder<T>> {
	/**
	 * Create a JEI RecipeType from a Vanilla RecipeType.
	 * Returns a RecipeType that uses {@link RecipeHolder} to hold recipes.
	 * @since 20.0.0
	 */
	static <R extends Recipe<?>> IRecipeHolderType<R> create(RecipeType<R> vanillaRecipeType) {
		return new JeiRecipeHolderType<>(vanillaRecipeType);
	}

	/**
	 * Create a JEI RecipeType from a Identifier.
	 * Returns a RecipeType that uses {@link RecipeHolder} to hold recipes.
	 * @since 20.0.0
	 */
	static <R extends Recipe<?>> IRecipeHolderType<R> create(Identifier recipeId) {
		return new JeiRecipeHolderType<>(recipeId);
	}

	/**
	 * Create a JEI RecipeType from a deferred Vanilla RecipeType.
	 * Returns a Supplier for a RecipeType that uses {@link RecipeHolder} to hold recipes.
	 * @since 20.0.0
	 */
	static <R extends Recipe<?>> Supplier<IRecipeHolderType<R>> createDeferred(Supplier<RecipeType<R>> vanillaRecipeType) {
		return Suppliers.memoize(() -> create(vanillaRecipeType.get()));
	}

	record JeiRecipeHolderType<T extends Recipe<?>>(Identifier uid) implements IRecipeHolderType<T> {
		private static Identifier getUid(RecipeType<?> recipeType) {
			Identifier uid = BuiltInRegistries.RECIPE_TYPE.getKey(recipeType);
			if (uid == null) {
				throw new IllegalArgumentException("Vanilla Recipe Type must be registered before using it here. %s".formatted(recipeType));
			}
			return uid;
		}

		JeiRecipeHolderType(RecipeType<T> vanillaRecipeType) {
			this(getUid(vanillaRecipeType));
		}

		@Override
		public Identifier getUid() {
			return uid;
		}

		@Override
		public Class<? extends RecipeHolder<T>> getRecipeClass() {
			@SuppressWarnings({"unchecked", "RedundantCast"})
			Class<? extends RecipeHolder<T>> castClass = (Class<? extends RecipeHolder<T>>) (Object) RecipeHolder.class;
			return castClass;
		}
	}
}
