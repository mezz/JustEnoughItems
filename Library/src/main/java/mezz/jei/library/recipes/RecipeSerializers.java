package mezz.jei.library.recipes;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class RecipeSerializers {
	private static @Nullable RecipeSerializers INSTANCE;

	private final Supplier<RecipeSerializer<? extends CraftingRecipe>> jeiShapedRecipeSerializer;
	private final Supplier<RecipeSerializer<? extends SmeltingRecipe>> jeiSmeltingRecipeSerializer;

	public static void register(
		Supplier<RecipeSerializer<? extends CraftingRecipe>> jeiShapedRecipeSerializer,
		Supplier<RecipeSerializer<? extends SmeltingRecipe>> jeiSmeltingRecipeSerializer
	) {
		INSTANCE = new RecipeSerializers(jeiShapedRecipeSerializer, jeiSmeltingRecipeSerializer);
	}

	private RecipeSerializers(
		Supplier<RecipeSerializer<? extends CraftingRecipe>> jeiShapedRecipeSerializer,
		Supplier<RecipeSerializer<? extends SmeltingRecipe>> jeiSmeltingRecipeSerializer
	) {
		this.jeiShapedRecipeSerializer = jeiShapedRecipeSerializer;
		this.jeiSmeltingRecipeSerializer = jeiSmeltingRecipeSerializer;
	}

	public static RecipeSerializer<? extends CraftingRecipe> getJeiShapedRecipeSerializer() {
		if (INSTANCE == null) {
			throw new IllegalStateException("Recipe serializer not yet initialized");
		}
		return INSTANCE.jeiShapedRecipeSerializer.get();
	}

	public static RecipeSerializer<? extends SmeltingRecipe> getJeiSmeltingRecipeSerializer() {
		if (INSTANCE == null) {
			throw new IllegalStateException("Recipe serializer not yet initialized");
		}
		return INSTANCE.jeiSmeltingRecipeSerializer.get();
	}
}
