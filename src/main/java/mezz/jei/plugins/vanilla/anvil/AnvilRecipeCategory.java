package mezz.jei.plugins.vanilla.anvil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;

public class AnvilRecipeCategory implements IRecipeCategory<AnvilRecipeWrapper> {

	private final IDrawable background;
	private final IDrawable icon;

	public AnvilRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 168, 125, 18)
			.addPadding(0, 20, 0, 0)
			.build();
		icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.ANVIL));
	}

	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.ANVIL;
	}

	@Override
	public String getTitle() {
		return Blocks.ANVIL.getLocalizedName();
	}

	@Override
	public String getModName() {
		return Constants.MINECRAFT_NAME;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AnvilRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(0, true, 0, 0);
		guiItemStacks.init(1, true, 49, 0);
		guiItemStacks.init(2, false, 107, 0);

		FocusLinkedIngredients<ItemStack> focusLinkedIngredients = getFocusLinkedIngredients(recipeLayout, ingredients);
		if (focusLinkedIngredients == null) {
			guiItemStacks.set(ingredients);
		} else {
			guiItemStacks.setOverrideDisplayFocus(null);
			guiItemStacks.set(0, focusLinkedIngredients.getLeftInputs());
			guiItemStacks.set(1, focusLinkedIngredients.getRightInputs());
			guiItemStacks.set(2, focusLinkedIngredients.getOutputs());
		}

		AnvilRecipeDisplayData displayData = AnvilRecipeDataCache.getDisplayData(recipeWrapper);
		displayData.setCurrentIngredients(guiItemStacks.getGuiIngredients());
	}

	@Nullable
	private static FocusLinkedIngredients<ItemStack> getFocusLinkedIngredients(IRecipeLayout recipeLayout, IIngredients ingredients) {
		IFocus<?> focus = recipeLayout.getFocus();
		if (focus == null || !(focus.getValue() instanceof ItemStack)) {
			return null;
		}

		List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
		List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);
		if (inputs.size() < 2 || outputs.isEmpty()) {
			return null;
		}

		IIngredientHelper<ItemStack> ingredientHelper = Internal.getIngredientRegistry()
			.getIngredientHelper(VanillaTypes.ITEM);
		@SuppressWarnings("unchecked")
		IFocus<ItemStack> itemStackFocus = (IFocus<ItemStack>) focus;
		return getFocusLinkedIngredients(inputs.get(0), inputs.get(1), outputs.get(0), itemStackFocus, ingredientHelper);
	}

	@Nullable
	static <T> FocusLinkedIngredients<T> getFocusLinkedIngredients(
		List<T> leftInputs,
		List<T> rightInputs,
		List<T> outputs,
		@Nullable IFocus<T> focus,
		IIngredientHelper<T> ingredientHelper
	) {
		if (focus == null) {
			return null;
		}

		boolean linkLeft;
		boolean linkRight;
		if (leftInputs.size() == rightInputs.size() && leftInputs.size() == outputs.size()) {
			linkLeft = true;
			linkRight = true;
		} else if (leftInputs.size() == outputs.size() && rightInputs.size() == 1) {
			linkLeft = true;
			linkRight = false;
		} else if (rightInputs.size() == outputs.size() && leftInputs.size() == 1) {
			linkLeft = false;
			linkRight = true;
		} else {
			return null;
		}

		List<Integer> matchingIndexes = new ArrayList<>();
		if (focus.getMode() == IFocus.Mode.INPUT) {
			if (linkLeft) {
				addMatchingIndex(matchingIndexes, leftInputs, focus.getValue(), ingredientHelper);
			}
			if (linkRight) {
				addMatchingIndex(matchingIndexes, rightInputs, focus.getValue(), ingredientHelper);
			}
		} else {
			addMatchingIndex(matchingIndexes, outputs, focus.getValue(), ingredientHelper);
		}

		if (matchingIndexes.isEmpty()) {
			return null;
		}

		return new FocusLinkedIngredients<>(
			linkLeft ? getIngredientsAtIndexes(leftInputs, matchingIndexes) : leftInputs,
			linkRight ? getIngredientsAtIndexes(rightInputs, matchingIndexes) : rightInputs,
			getIngredientsAtIndexes(outputs, matchingIndexes)
		);
	}

	private static <T> void addMatchingIndex(
		List<Integer> matchingIndexes,
		List<T> ingredients,
		T focus,
		IIngredientHelper<T> ingredientHelper
	) {
		for (int i = 0; i < ingredients.size(); i++) {
			T ingredient = ingredients.get(i);
			T match = ingredientHelper.getMatch(Collections.singletonList(ingredient), focus);
			if (match != null) {
				if (!matchingIndexes.contains(i)) {
					matchingIndexes.add(i);
				}
				return;
			}
		}
	}

	private static <T> List<T> getIngredientsAtIndexes(List<T> ingredients, List<Integer> indexes) {
		List<T> result = new ArrayList<>(indexes.size());
		for (Integer index : indexes) {
			result.add(ingredients.get(index));
		}
		return result;
	}

	static final class FocusLinkedIngredients<T> {
		private final List<T> leftInputs;
		private final List<T> rightInputs;
		private final List<T> outputs;

		private FocusLinkedIngredients(List<T> leftInputs, List<T> rightInputs, List<T> outputs) {
			this.leftInputs = leftInputs;
			this.rightInputs = rightInputs;
			this.outputs = outputs;
		}

		List<T> getLeftInputs() {
			return leftInputs;
		}

		List<T> getRightInputs() {
			return rightInputs;
		}

		List<T> getOutputs() {
			return outputs;
		}
	}
}
