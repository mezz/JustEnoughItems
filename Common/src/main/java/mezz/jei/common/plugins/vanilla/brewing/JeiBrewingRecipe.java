package mezz.jei.common.plugins.vanilla.brewing;

import com.google.common.base.Objects;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.List;

public class JeiBrewingRecipe implements IJeiBrewingRecipe {
	private final List<ItemStack> ingredients;
	private final List<ItemStack> potionInputs;
	private final ItemStack potionOutput;
	private final BrewingRecipeUtil brewingRecipeUtil;
	private final int hashCode;

	public JeiBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput, BrewingRecipeUtil brewingRecipeUtil) {
		this.ingredients = List.copyOf(ingredients);
		this.potionInputs = List.copyOf(potionInputs);
		this.potionOutput = potionOutput;
		this.brewingRecipeUtil = brewingRecipeUtil;

		brewingRecipeUtil.addRecipe(potionInputs, potionOutput);

		this.hashCode = Objects.hashCode(
			ingredients.stream().map(ItemStack::getItem).toList(),
			potionInputs.stream().map(ItemStack::getItem).toList(),
			potionOutput.getItem()
		);
	}

	@Override
	public List<ItemStack> getPotionInputs() {
		return potionInputs;
	}

	@Override
	public List<ItemStack> getIngredients() {
		return ingredients;
	}

	@Override
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
		IPlatformRegistry<Potion> potionRegistry = Services.PLATFORM.getRegistry(Registry.POTION_REGISTRY);
		ResourceLocation key1 = potionRegistry.getRegistryName(type1);
		ResourceLocation key2 = potionRegistry.getRegistryName(type2);
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
