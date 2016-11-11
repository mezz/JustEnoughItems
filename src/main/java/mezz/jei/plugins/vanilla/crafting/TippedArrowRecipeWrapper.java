package mezz.jei.plugins.vanilla.crafting;

import java.util.Arrays;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;

public class TippedArrowRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper {
	private final List<ItemStack> inputs;
	private final ItemStack output;

	public TippedArrowRecipeWrapper(PotionType type) {
		ItemStack arrowStack = new ItemStack(Items.ARROW);
		ItemStack lingeringPotion = PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), type);
		this.inputs = Arrays.asList(
				arrowStack, arrowStack, arrowStack,
				arrowStack, lingeringPotion, arrowStack,
				arrowStack, arrowStack, arrowStack
		);
		ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
		this.output = PotionUtils.addPotionToItemStack(outputStack, type);
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(ItemStack.class, this.inputs);
		ingredients.setOutput(ItemStack.class, this.output);
	}

	@Override
	public int getWidth() {
		return 3;
	}

	@Override
	public int getHeight() {
		return 3;
	}
}
