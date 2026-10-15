package mezz.jei.library.gui.recipes.supplier.builder;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Minimal version of {@link RecipeSlotBuilder} that can only return the ingredients,
 * but doesn't bother building anything for drawing on screen.
 */
public class IngredientSlotBuilder extends IgnoredIngredientSlotBuilder {
	private final DisplayIngredientAcceptor ingredients;
	private final RecipeIngredientRole role;

	public IngredientSlotBuilder(IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.ingredients = new DisplayIngredientAcceptor(ingredientManager);
		this.role = role;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		this.ingredients.addIngredients(ingredientType, ingredients);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		this.ingredients.addIngredient(ingredientType, ingredient);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid) {
		this.ingredients.addFluidStack(fluid);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount) {
		this.ingredients.addFluidStack(fluid, amount);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, CompoundTag tag) {
		this.ingredients.addFluidStack(fluid, amount, tag);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
		this.ingredients.addIngredientsUnsafe(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		this.ingredients.addTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		this.ingredients.addOptionalTypedIngredients(ingredients);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addItemStacks(List<ItemStack> itemStacks) {
		this.ingredients.addItemStacks(itemStacks);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addIngredients(Ingredient ingredient) {
		this.ingredients.addIngredients(ingredient);
		return this;
	}

	@Override
	public <I> IRecipeSlotBuilder addTypedIngredient(ITypedIngredient<I> typedIngredient) {
		this.ingredients.addTypedIngredient(typedIngredient);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addItemStack(ItemStack itemStack) {
		this.ingredients.addItemStack(itemStack);
		return this;
	}

	@Override
	public IRecipeSlotBuilder addItemLike(ItemLike itemLike) {
		this.ingredients.addItemLike(itemLike);
		return this;
	}

	public RecipeIngredientRole getRole() {
		return role;
	}

	public List<@Nullable ITypedIngredient<?>> getAllIngredients() {
		return this.ingredients.getAllIngredients();
	}
}
