package mezz.jei.library.plugins.vanilla.cooking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mezz.jei.library.recipes.RecipeSerializers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class JeiSmeltingRecipe extends SmeltingRecipe {
	public static final MapCodec<JeiSmeltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Ingredient.CODEC.fieldOf("ingredient").forGetter(JeiSmeltingRecipe::getInput),
			Ingredient.CODEC.optionalFieldOf("fuel", Ingredient.EMPTY).forGetter(JeiSmeltingRecipe::getFuel),
			ItemStack.OPTIONAL_CODEC.optionalFieldOf("fuel_output", ItemStack.EMPTY).forGetter(JeiSmeltingRecipe::getFuelOutput),
			ItemStack.STRICT_CODEC.fieldOf("result").forGetter(JeiSmeltingRecipe::getResult),
			Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(JeiSmeltingRecipe::getExperience),
			Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(JeiSmeltingRecipe::getCookingTime)
		)
		.apply(instance, JeiSmeltingRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, JeiSmeltingRecipe> STREAM_CODEC = StreamCodec.of(
		JeiSmeltingRecipe::toNetwork,
		JeiSmeltingRecipe::fromNetwork
	);
	public static final RecipeSerializer<JeiSmeltingRecipe> SERIALIZER = new Serializer();

	private final Ingredient input;
	private final Ingredient fuel;
	private final ItemStack fuelOutput;
	private final ItemStack result;

	public JeiSmeltingRecipe(Ingredient input, Ingredient fuel, ItemStack fuelOutput, ItemStack result, float experience, int cookingTime) {
		super("", CookingBookCategory.MISC, input, result, experience, cookingTime);
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

	private static JeiSmeltingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
		Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
		Ingredient fuel = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
		ItemStack fuelOutput = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
		ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
		float experience = buffer.readFloat();
		int cookingTime = buffer.readVarInt();
		return new JeiSmeltingRecipe(input, fuel, fuelOutput, result, experience, cookingTime);
	}

	private static void toNetwork(RegistryFriendlyByteBuf buffer, JeiSmeltingRecipe recipe) {
		Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input);
		Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.fuel);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.fuelOutput);
		ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
		buffer.writeFloat(recipe.getExperience());
		buffer.writeVarInt(recipe.getCookingTime());
	}

	private static class Serializer implements RecipeSerializer<JeiSmeltingRecipe> {
		@Override
		public MapCodec<JeiSmeltingRecipe> codec() {
			return MAP_CODEC;
		}

		@Override
		@Deprecated
		public StreamCodec<RegistryFriendlyByteBuf, JeiSmeltingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
