package mezz.jei.library.recipes;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class RecipeSerializers {
	private static @Nullable RecipeSerializers INSTANCE;

	private final Supplier<RecipeSerializer<? extends CraftingRecipe>> jeiShapedRecipeSerializer;

	public static void register(Supplier<RecipeSerializer<? extends CraftingRecipe>> jeiShapedRecipeSerializer) {
		INSTANCE = new RecipeSerializers(jeiShapedRecipeSerializer);
	}

	private RecipeSerializers(Supplier<RecipeSerializer<? extends CraftingRecipe>> jeiShapedRecipeSerializer) {
		this.jeiShapedRecipeSerializer = jeiShapedRecipeSerializer;
	}

	public static RecipeSerializer<? extends CraftingRecipe> getJeiShapedRecipeSerializer() {
		if (INSTANCE == null) {
			throw new IllegalStateException("Recipe serializer not yet initialized");
		}
		return INSTANCE.jeiShapedRecipeSerializer.get();
	}
}
