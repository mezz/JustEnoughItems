package mezz.jei.library.plugins.vanilla.cooking;

import com.google.gson.JsonObject;
import mezz.jei.library.recipes.RecipeSerializers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.Nullable;

public class JeiSmeltingRecipe extends SmeltingRecipe {
	public static final RecipeSerializer<JeiSmeltingRecipe> SERIALIZER = new Serializer();

	private final Ingredient input;
	private final Ingredient fuel;
	private final ItemStack fuelOutput;
	private final ItemStack result;

	public JeiSmeltingRecipe(
		ResourceLocation id,
		Ingredient input,
		Ingredient fuel,
		ItemStack fuelOutput,
		ItemStack result,
		float experience,
		int cookingTime
	) {
		super(id, "", CookingBookCategory.MISC, input, result, experience, cookingTime);
		this.input = input;
		this.fuel = fuel;
		this.fuelOutput = fuelOutput;
		this.result = result;
	}

	public Ingredient getInput() {
		return input;
	}

	public Ingredient getFuel() {
		return fuel;
	}

	public ItemStack getFuelOutput() {
		return fuelOutput;
	}

	public ItemStack getResult() {
		return result;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.getJeiSmeltingRecipeSerializer();
	}

	private static class Serializer implements RecipeSerializer<JeiSmeltingRecipe> {
		@Override
		public JeiSmeltingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			Ingredient input = Ingredient.fromJson(json.get("ingredient"));
			Ingredient fuel = json.has("fuel") ?
				Ingredient.fromJson(json.get("fuel")) :
				Ingredient.EMPTY;
			ItemStack fuelOutput = json.has("fuel_output") ?
				ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "fuel_output")) :
				ItemStack.EMPTY;
			ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
			float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
			int cookingTime = GsonHelper.getAsInt(json, "cookingtime", 200);
			return new JeiSmeltingRecipe(recipeId, input, fuel, fuelOutput, result, experience, cookingTime);
		}

		@Nullable
		@Override
		public JeiSmeltingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient input = Ingredient.fromNetwork(buffer);
			Ingredient fuel = Ingredient.fromNetwork(buffer);
			ItemStack fuelOutput = buffer.readItem();
			ItemStack result = buffer.readItem();
			float experience = buffer.readFloat();
			int cookingTime = buffer.readVarInt();
			return new JeiSmeltingRecipe(recipeId, input, fuel, fuelOutput, result, experience, cookingTime);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, JeiSmeltingRecipe recipe) {
			recipe.input.toNetwork(buffer);
			recipe.fuel.toNetwork(buffer);
			buffer.writeItem(recipe.fuelOutput);
			buffer.writeItem(recipe.result);
			buffer.writeFloat(recipe.getExperience());
			buffer.writeVarInt(recipe.getCookingTime());
		}
	}
}
