package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TippedArrowRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper {
	@Nonnull
	private final List<ItemStack> inputs;
	@Nonnull
	private final List<ItemStack> outputs;

	public TippedArrowRecipeWrapper(@Nonnull PotionType type) {
		ItemStack arrowStack = new ItemStack(Items.ARROW);
		ItemStack lingeringPotion = PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), type);
		this.inputs = Arrays.asList(
				arrowStack, arrowStack, arrowStack,
				arrowStack, lingeringPotion, arrowStack,
				arrowStack, arrowStack, arrowStack
		);
		ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
		PotionUtils.addPotionToItemStack(outputStack, type);
		this.outputs = Collections.singletonList(outputStack);
	}

	@Override
	@Nonnull
	public List<ItemStack> getInputs() {
		return inputs;
	}

	@Override
	@Nonnull
	public List<ItemStack> getOutputs() {
		return outputs;
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
