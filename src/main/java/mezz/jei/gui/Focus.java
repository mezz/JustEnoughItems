package mezz.jei.gui;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;

import mezz.jei.api.JEIManager;
import mezz.jei.api.recipe.IRecipeCategory;

public class Focus {
	private final ItemStack stack;
	private final Fluid fluid;

	public Focus() {
		this.stack = null;
		this.fluid = null;
	}

	public Focus(Object focus) {
		if (focus instanceof ItemStack) {
			this.stack = (ItemStack) focus;
			this.fluid = null;
		} else if (focus instanceof Fluid) {
			this.stack = null;
			this.fluid = (Fluid) focus;
		} else {
			this.stack = null;
			this.fluid = null;
		}
	}

	public Focus(ItemStack stack) {
		this.stack = stack;
		this.fluid = null;
	}

	public Focus(Fluid fluid) {
		this.stack = null;
		this.fluid = fluid;
	}

	public Fluid getFluid() {
		return fluid;
	}

	public ItemStack getStack() {
		return stack;
	}

	public boolean isBlank() {
		return stack == null && fluid == null;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Focus)) {
			return false;
		}
		Focus other = (Focus) obj;
		return ItemStack.areItemStacksEqual(this.stack, other.getStack()) && fluid == other.fluid;
	}

	@Nonnull
	public ImmutableList<IRecipeCategory> getCategoriesWithInput() {
		if (stack != null) {
			return JEIManager.recipeRegistry.getRecipeCategoriesWithInput(stack);
		} else {
			return JEIManager.recipeRegistry.getRecipeCategoriesWithInput(fluid);
		}
	}

	@Nonnull
	public ImmutableList<IRecipeCategory> getCategoriesWithOutput() {
		if (stack != null) {
			return JEIManager.recipeRegistry.getRecipeCategoriesWithOutput(stack);
		} else {
			return JEIManager.recipeRegistry.getRecipeCategoriesWithOutput(fluid);
		}
	}

	@Nonnull
	public ImmutableList<Object> getRecipesWithInput(IRecipeCategory recipeCategory) {
		if (stack != null) {
			return JEIManager.recipeRegistry.getRecipesWithInput(recipeCategory, stack);
		} else {
			return JEIManager.recipeRegistry.getRecipesWithInput(recipeCategory, fluid);
		}
	}

	@Nonnull
	public ImmutableList<Object> getRecipesWithOutput(IRecipeCategory recipeCategory) {
		if (stack != null) {
			return JEIManager.recipeRegistry.getRecipesWithOutput(recipeCategory, stack);
		} else {
			return JEIManager.recipeRegistry.getRecipesWithOutput(recipeCategory, fluid);
		}
	}
}
