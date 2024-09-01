package mezz.jei.library.recipes;

import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class RecipeSerializers {
	private static @Nullable RecipeSerializers INSTANCE;

	private final Supplier<RecipeSerializer<?>> jeiShapedRecipeSerializer;

	public static void register(Supplier<RecipeSerializer<?>> jeiShapedRecipeSerializer) {
		INSTANCE = new RecipeSerializers(jeiShapedRecipeSerializer);
	}

	private RecipeSerializers(Supplier<RecipeSerializer<?>> jeiShapedRecipeSerializer) {
		this.jeiShapedRecipeSerializer = jeiShapedRecipeSerializer;
	}

	public static RecipeSerializer<?> getJeiShapedRecipeSerializer() {
		if (INSTANCE == null) {
			throw new IllegalStateException("Recipe serializer not yet initialized");
		}
		return INSTANCE.jeiShapedRecipeSerializer.get();
	}
}
