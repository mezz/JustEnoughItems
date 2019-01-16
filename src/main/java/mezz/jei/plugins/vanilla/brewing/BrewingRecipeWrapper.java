package mezz.jei.plugins.vanilla.brewing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Objects;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Translator;

public class BrewingRecipeWrapper implements IRecipeWrapper {
	private static final BrewingRecipeUtil UTIL = new BrewingRecipeUtil();

	private final List<ItemStack> ingredients;
	private final ItemStack potionInput;
	private final ItemStack potionOutput;
	private final List<List<ItemStack>> inputs;
	private final int hashCode;

	public BrewingRecipeWrapper(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput) {
		this.ingredients = ingredients;
		this.potionInput = potionInput;
		this.potionOutput = potionOutput;

		UTIL.addRecipe(potionInput, potionOutput);

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
			firstIngredient.getItem(), firstIngredient.getMetadata());
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
		ingredients.setOutput(VanillaTypes.ITEM, potionOutput);
	}

	public List getInputs() {
		return inputs;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		int brewingSteps = getBrewingSteps();
		if (brewingSteps < Integer.MAX_VALUE) {
			String steps = Translator.translateToLocalFormatted("gui.jei.category.brewing.steps", brewingSteps);
			minecraft.fontRenderer.drawString(steps, 70, 28, Color.gray.getRGB());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BrewingRecipeWrapper)) {
			return false;
		}
		BrewingRecipeWrapper other = (BrewingRecipeWrapper) obj;

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
		ResourceLocation key1 = ForgeRegistries.POTION_TYPES.getKey(type1);
		ResourceLocation key2 = ForgeRegistries.POTION_TYPES.getKey(type2);
		return java.util.Objects.equals(key1, key2);
	}

	public int getBrewingSteps() {
		return UTIL.getBrewingSteps(potionOutput);
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
