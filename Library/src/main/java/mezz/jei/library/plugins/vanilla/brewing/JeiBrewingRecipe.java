package mezz.jei.library.plugins.vanilla.brewing;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.library.plugins.vanilla.ingredients.subtypes.PotionSubtypeInterpreter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class JeiBrewingRecipe implements IJeiBrewingRecipe {
	private final List<ItemStack> ingredients;
	private final List<ItemStack> potionInputs;
	private final ItemStack potionOutput;
	private final @Nullable ResourceLocation uid;
	private final BrewingRecipeUtil brewingRecipeUtil;
	private final int hashCode;

	public JeiBrewingRecipe(
		List<ItemStack> ingredients,
		List<ItemStack> potionInputs,
		ItemStack potionOutput,
		@Nullable ResourceLocation uid,
		BrewingRecipeUtil brewingRecipeUtil
	) {
		this.ingredients = List.copyOf(ingredients);
		this.potionInputs = List.copyOf(potionInputs);
		this.potionOutput = potionOutput;
		this.uid = uid;
		this.brewingRecipeUtil = brewingRecipeUtil;

		brewingRecipeUtil.addRecipe(potionInputs, potionOutput);

		if (uid != null) {
			this.hashCode = uid.hashCode();
		} else {
			this.hashCode = Objects.hash(
				ingredients.stream().map(ItemStack::getItem).toList(),
				potionInputs.stream().map(ItemStack::getItem).toList(),
				potionOutput.getItem()
			);
		}
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

	@Nullable
	@Override
	public ResourceLocation getUid() {
		return uid;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JeiBrewingRecipe other)) {
			return false;
		}

		if (uid != null) {
			return uid.equals(other.uid);
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
		String key1 = PotionSubtypeInterpreter.INSTANCE.apply(potion1, UidContext.Recipe);
		String key2 = PotionSubtypeInterpreter.INSTANCE.apply(potion2, UidContext.Recipe);
		return Objects.equals(key1, key2);
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
		ItemStack input = potionInputs.getFirst();
		String inputName = PotionSubtypeInterpreter.INSTANCE.apply(input, UidContext.Recipe);
		String outputName = PotionSubtypeInterpreter.INSTANCE.apply(potionOutput, UidContext.Recipe);
		return ingredients + " + [" + input.getItem() + " " + inputName + "] = [" + potionOutput + " " + outputName + "]";
	}
}
