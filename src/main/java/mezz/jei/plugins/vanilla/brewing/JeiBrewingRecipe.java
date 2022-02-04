package mezz.jei.plugins.vanilla.brewing;

import java.util.List;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Objects;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;

public class JeiBrewingRecipe implements IJeiBrewingRecipe {
	private final List<ItemStack> ingredients;
	private final List<ItemStack> potionInputs;
	private final ItemStack potionOutput;
	private final BrewingRecipeUtil brewingRecipeUtil;
	private final int hashCode;

	public JeiBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput, BrewingRecipeUtil brewingRecipeUtil) {
		this.ingredients = ingredients;
		this.potionInputs = potionInputs;
		this.potionOutput = potionOutput;
		this.brewingRecipeUtil = brewingRecipeUtil;

		brewingRecipeUtil.addRecipe(potionInputs, potionOutput);

		ItemStack firstIngredient = ingredients.get(0);
		ItemStack firstInput = potionInputs.get(0);

		Potion typeIn = PotionUtils.getPotion(firstInput);
		Potion typeOut = PotionUtils.getPotion(potionOutput);
		this.hashCode = Objects.hashCode(firstInput.getItem(), ForgeRegistries.POTIONS.getKey(typeIn),
			potionOutput.getItem(), ForgeRegistries.POTIONS.getKey(typeOut),
			firstIngredient.getItem());
	}

	public List<ItemStack> getPotionInputs() {
		return potionInputs;
	}

	public List<ItemStack> getIngredients() {
		return ingredients;
	}

	public ItemStack getPotionOutput() {
		return potionOutput;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JeiBrewingRecipe other)) {
			return false;
		}

		for (int i = 0; i < potionInputs.size(); i++) {
			ItemStack potionInput = potionInputs.get(i);
			ItemStack otherPotionInput = other.potionInputs.get(i);
			if (!arePotionsEqual(potionInput, otherPotionInput)) {
				return false;
			}
		}

		if (!arePotionsEqual(other.potionOutput, potionOutput)) {
			return false;
		}

		if (ingredients.size() != other.ingredients.size()) {
			return false;
		}

		for (int i = 0; i < ingredients.size(); i++) {
			if (!ItemStack.matches(ingredients.get(i), other.ingredients.get(i))) {
				return false;
			}
		}

		return true;
	}

	private static boolean arePotionsEqual(ItemStack potion1, ItemStack potion2) {
		if (potion1.getItem() != potion2.getItem()) {
			return false;
		}
		Potion type1 = PotionUtils.getPotion(potion1);
		Potion type2 = PotionUtils.getPotion(potion2);
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
		Potion inputType = PotionUtils.getPotion(potionInputs.get(0));
		Potion outputType = PotionUtils.getPotion(potionOutput);
		return ingredients + " + [" + potionInputs.get(0).getItem() + " " + inputType.getName("") + "] = [" + potionOutput + " " + outputType.getName("") + "]";
	}
}
