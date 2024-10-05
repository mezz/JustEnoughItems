package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import java.util.List;

public class SmithingTrimCategoryExtension extends SmithingCategoryExtension<SmithingTrimRecipe> {
	public SmithingTrimCategoryExtension(IPlatformRecipeHelper recipeHelper) {
		super(recipeHelper);
	}

	@Override
	public void onDisplayedIngredientsUpdate(
		SmithingTrimRecipe recipe,
		IRecipeSlotDrawable templateSlot,
		IRecipeSlotDrawable baseSlot,
		IRecipeSlotDrawable additionSlot,
		IRecipeSlotDrawable outputSlot,
		IFocusGroup focuses
	) {
		List<IFocus<?>> outputFocuses = focuses.getFocuses(RecipeIngredientRole.OUTPUT).toList();
		if (outputFocuses.isEmpty()) {
			ItemStack template = templateSlot.getDisplayedItemStack().orElse(ItemStack.EMPTY);
			ItemStack base = baseSlot.getDisplayedItemStack().orElse(ItemStack.EMPTY);
			ItemStack addition = additionSlot.getDisplayedItemStack().orElse(ItemStack.EMPTY);

			SmithingRecipeInput recipeInput = new SmithingRecipeInput(template, base, addition);
			ItemStack output = RecipeUtil.assembleResultItem(recipeInput, recipe);
			outputSlot.createDisplayOverrides()
				.addItemStack(output);
		} else {
			ItemStack output = outputSlot.getDisplayedItemStack().orElse(ItemStack.EMPTY);
			ItemStack base = new ItemStack(output.getItem());
			ItemStack template = templateSlot.getDisplayedItemStack().orElse(ItemStack.EMPTY);
			ItemStack addition = additionSlot.getDisplayedItemStack().orElse(ItemStack.EMPTY);

			baseSlot.createDisplayOverrides()
				.addItemStack(base);

			SmithingRecipeInput recipeInput = new SmithingRecipeInput(template, base, addition);
			output = RecipeUtil.assembleResultItem(recipeInput, recipe);
			outputSlot.createDisplayOverrides()
				.addItemStack(output);
		}
	}
}
