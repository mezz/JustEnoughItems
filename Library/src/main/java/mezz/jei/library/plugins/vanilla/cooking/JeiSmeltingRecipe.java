package mezz.jei.library.plugins.vanilla.cooking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mezz.jei.library.recipes.RecipeSerializers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public class JeiSmeltingRecipe extends SmeltingRecipe {
	public static final MapCodec<JeiSmeltingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Ingredient.CODEC.fieldOf("ingredient").forGetter(JeiSmeltingRecipe::input),
			SlotDisplay.CODEC.optionalFieldOf("fuel", SlotDisplay.AnyFuel.INSTANCE).forGetter(JeiSmeltingRecipe::fuel),
			ItemStack.STRICT_SINGLE_ITEM_CODEC.fieldOf("result").forGetter(JeiSmeltingRecipe::result),
			Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(JeiSmeltingRecipe::experience),
			Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(JeiSmeltingRecipe::cookingTime)
		)
		.apply(instance, JeiSmeltingRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, JeiSmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC,
		JeiSmeltingRecipe::input,
		SlotDisplay.STREAM_CODEC,
		JeiSmeltingRecipe::fuel,
		ItemStack.STREAM_CODEC,
		JeiSmeltingRecipe::result,
		ByteBufCodecs.FLOAT,
		JeiSmeltingRecipe::experience,
		ByteBufCodecs.VAR_INT,
		JeiSmeltingRecipe::cookingTime,
		JeiSmeltingRecipe::new
	);
	public static final RecipeSerializer<JeiSmeltingRecipe> SERIALIZER = new Serializer();

	private final SlotDisplay fuel;

	public JeiSmeltingRecipe(Ingredient input, SlotDisplay fuel, ItemStack result, float experience, int cookingTime) {
		super("", CookingBookCategory.MISC, input, result, experience, cookingTime);
		this.fuel = fuel;
	}

	public SlotDisplay fuel() {
		return fuel;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(
			new FurnaceRecipeDisplay(
				input().display(),
				fuel,
				new SlotDisplay.ItemStackSlotDisplay(result()),
				new SlotDisplay.ItemSlotDisplay(Items.FURNACE),
				cookingTime(),
				experience()
			)
		);
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public RecipeSerializer<SmeltingRecipe> getSerializer() {
		return (RecipeSerializer) RecipeSerializers.getJeiSmeltingRecipeSerializer();
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
