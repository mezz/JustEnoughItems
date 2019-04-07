package mezz.jei.plugins.vanilla.brewing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Objects;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;

public class JeiBrewingRecipe implements IJeiBrewingRecipe {
	private final List<ItemStack> ingredients;
	private final ItemStack potionInput;
	private final ItemStack potionOutput;
	private final BrewingRecipeUtil brewingRecipeUtil;
	private final List<List<ItemStack>> inputs;
	private final int hashCode;

	public JeiBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput, BrewingRecipeUtil brewingRecipeUtil) {
		this.ingredients = ingredients;
		this.potionInput = potionInput;
		this.potionOutput = potionOutput;
		this.brewingRecipeUtil = brewingRecipeUtil;

		brewingRecipeUtil.addRecipe(potionInput, potionOutput);

		this.inputs = new ArrayList<>();
		this.inputs.add(Collections.singletonList(potionInput));
		this.inputs.add(Collections.singletonList(potionInput));
		this.inputs.add(Collections.singletonList(potionInput));
		this.inputs.add(ingredients);

		ItemStack firstIngredient = ingredients.get(0);

		PotionType typeIn = PotionUtils.getPotionFromItem(potionInput);
		PotionType typeOut = PotionUtils.getPotionFromItem(potionOutput);
		this.hashCode = Objects.hashCode(potionInput.getItem(), ForgeRegistries.POTION_TYPES.getKey(typeIn),
			potionOutput.getItem(), ForgeRegistries.POTION_TYPES.getKey(typeOut),
			firstIngredient.getItem());
	}

	public List<List<ItemStack>> getInputs() {
		return inputs;
	}

	public ItemStack getPotionOutput() {
		return potionOutput;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JeiBrewingRecipe)) {
			return false;
		}
		JeiBrewingRecipe other = (JeiBrewingRecipe) obj;

		if (!arePotionsEqual(other.potionInput, potionInput)) {
			return false;
		}

		if (!arePotionsEqual(other.potionOutput, potionOutput)) {
			return false;
		}

		if (ingredients.size() != other.ingredients.size()) {
			return false;
		}

		for (int i = 0; i < ingredients.size(); i++) {
			if (!ItemStack.areItemStacksEqual(ingredients.get(i), other.ingredients.get(i))) {
				return false;
			}
		}

		return true;
	}

	private static boolean arePotionsEqual(ItemStack potion1, ItemStack potion2) {
		if (potion1.getItem() != potion2.getItem()) {
			return false;
		}
		PotionType type1 = PotionUtils.getPotionFromItem(potion1);
		PotionType type2 = PotionUtils.getPotionFromItem(potion2);
		ResourceLocation key1 = type1.getRegistryName();
		ResourceLocation key2 = type2.getRegistryName();
		return java.util.Objects.equals(key1, key2);
	}

	@Override
	public int getBrewingSteps() {
		return brewingRecipeUtil.getBrewingSteps(potionOutput);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		PotionType inputType = PotionUtils.getPotionFromItem(potionInput);
		PotionType outputType = PotionUtils.getPotionFromItem(potionOutput);
		return ingredients + " + [" + potionInput.getItem() + " " + inputType.getNamePrefixed("") + "] = [" + potionOutput + " " + outputType.getNamePrefixed("") + "]";
	}
}
