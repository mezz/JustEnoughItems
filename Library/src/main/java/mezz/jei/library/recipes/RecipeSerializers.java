package mezz.jei.library.recipes;

import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class RecipeSerializers {
	private static @Nullable RecipeSerializers INSTANCE;

	private final Supplier<RecipeSerializer<?>> jeiShapedRecipeSerializer;
	private final Supplier<RecipeSerializer<?>> jeiSmeltingRecipeSerializer;

	public static void register(
		Supplier<RecipeSerializer<?>> jeiShapedRecipeSerializer,
		Supplier<RecipeSerializer<?>> jeiSmeltingRecipeSerializer
	) {
		INSTANCE = new RecipeSerializers(jeiShapedRecipeSerializer, jeiSmeltingRecipeSerializer);
	}

	private RecipeSerializers(
		Supplier<RecipeSerializer<?>> jeiShapedRecipeSerializer,
		Supplier<RecipeSerializer<?>> jeiSmeltingRecipeSerializer
	) {
		this.jeiShapedRecipeSerializer = jeiShapedRecipeSerializer;
		this.jeiSmeltingRecipeSerializer = jeiSmeltingRecipeSerializer;
	}

	public static RecipeSerializer<?> getJeiShapedRecipeSerializer() {
		if (INSTANCE == null) {
			throw new IllegalStateException("Recipe serializer not yet initialized");
		}
		return INSTANCE.jeiShapedRecipeSerializer.get();
	}

	public static RecipeSerializer<?> getJeiSmeltingRecipeSerializer() {
		if (INSTANCE == null) {
			throw new IllegalStateException("Recipe serializer not yet initialized");
		}
		return INSTANCE.jeiSmeltingRecipeSerializer.get();
	}
}
