package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

import java.util.List;
import java.util.Optional;

public abstract class SmithingCategoryExtension<R extends SmithingRecipe> implements ISmithingCategoryExtension<R> {
	private final IPlatformRecipeHelper recipeHelper;

	public SmithingCategoryExtension(IPlatformRecipeHelper recipeHelper) {
		this.recipeHelper = recipeHelper;
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setTemplate(R recipe, T ingredientAcceptor) {
		recipeHelper.getTemplate(recipe)
			.ifPresent(ingredientAcceptor::add);
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setBase(R recipe, T ingredientAcceptor) {
		Ingredient base = recipeHelper.getBase(recipe);
		ingredientAcceptor.add(base);
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setAddition(R recipe, T ingredientAcceptor) {
		recipeHelper.getAddition(recipe)
			.ifPresent(ingredientAcceptor::add);
	}

	@Override
	public <T extends IIngredientAcceptor<T>> void setOutput(R recipe, T ingredientAcceptor) {
		ContextMap contextMap = ingredientAcceptor.getContextMap();
		Optional<Ingredient> templateIngredient = recipeHelper.getTemplate(recipe);
		Ingredient baseIngredient = recipeHelper.getBase(recipe);
		Optional<Ingredient> additionIngredient = recipeHelper.getAddition(recipe);

		List<ItemStack> templateStacks = templateIngredient.map(i -> i.display().resolveForStacks(contextMap)).orElse(List.of(ItemStack.EMPTY));
		if (templateStacks.isEmpty()) {
			templateStacks = List.of(ItemStack.EMPTY);
		}

		List<ItemStack> baseStacks = baseIngredient.display().resolveForStacks(contextMap);
		if (baseStacks.isEmpty()) {
			baseStacks = List.of(ItemStack.EMPTY);
		}

		ItemStack addition = additionIngredient.map(i -> i.display().resolveForFirstStack(contextMap)).orElse(ItemStack.EMPTY);

		for (ItemStack template : templateStacks) {
			for (ItemStack base : baseStacks) {
				SmithingRecipeInput recipeInput = new SmithingRecipeInput(template, base, addition);
				ItemStack output = recipe.assemble(recipeInput);
				ingredientAcceptor.add(output);
			}
		}
	}
}
