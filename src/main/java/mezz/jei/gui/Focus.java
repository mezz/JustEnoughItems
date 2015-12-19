package mezz.jei.gui;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import mezz.jei.Internal;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IRecipeCategory;

public class Focus {
	public enum Mode {
		INPUT, OUTPUT, NONE
	}

	private final ItemStack stack;
	private final Fluid fluid;
	@Nonnull
	private Mode mode = Mode.NONE;

	public static Focus create(Object focus) {
		if (focus instanceof ItemStack) {
			return new Focus((ItemStack) focus);
		} else if (focus instanceof Fluid) {
			return new Focus((Fluid) focus);
		} else if (focus instanceof FluidStack) {
			return new Focus(((FluidStack) focus).getFluid());
		} else {
			return new Focus();
		}
	}

	public Focus() {
		this.stack = null;
		this.fluid = null;
	}

	public Focus(ItemStack stack) {
		this.stack = stack;
		Item item = stack.getItem();
		if (item instanceof IFluidContainerItem) {
			IFluidContainerItem fluidContainerItem = (IFluidContainerItem) item;
			FluidStack fluidStack = fluidContainerItem.getFluid(stack);
			this.fluid = fluidStack.getFluid();
		} else if (FluidContainerRegistry.isFilledContainer(stack)) {
			FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(stack);
			this.fluid = fluidStack.getFluid();
		} else {
			this.fluid = null;
		}
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

	public void setMode(@Nonnull Mode mode) {
		this.mode = mode;
	}

	@Nonnull
	public Mode getMode() {
		return mode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Focus)) {
			return false;
		}
		Focus other = (Focus) obj;
		return ItemStack.areItemStacksEqual(this.stack, other.getStack()) && fluid == other.fluid && mode == other.mode;
	}

	@Nonnull
	public List<IRecipeCategory> getCategories() {
		IRecipeRegistry recipeRegistry = Internal.getRecipeRegistry();
		switch (mode) {
			case INPUT: {
				if (stack != null && fluid != null) {
					List<IRecipeCategory> categories = new ArrayList<>(recipeRegistry.getRecipeCategoriesWithInput(stack));
					categories.addAll(recipeRegistry.getRecipeCategoriesWithInput(fluid));
					return ImmutableSet.copyOf(categories).asList();
				}
				if (stack != null) {
					return recipeRegistry.getRecipeCategoriesWithInput(stack);
				} else {
					return recipeRegistry.getRecipeCategoriesWithInput(fluid);
				}
			}
			case OUTPUT: {
				if (stack != null && fluid != null) {
					List<IRecipeCategory> categories = new ArrayList<>(recipeRegistry.getRecipeCategoriesWithOutput(stack));
					categories.addAll(recipeRegistry.getRecipeCategoriesWithOutput(fluid));
					return ImmutableSet.copyOf(categories).asList();
				}
				if (stack != null) {
					return recipeRegistry.getRecipeCategoriesWithOutput(stack);
				} else {
					return recipeRegistry.getRecipeCategoriesWithOutput(fluid);
				}
			}
		}
		return recipeRegistry.getRecipeCategories();
	}

	@Nonnull
	public List<Object> getRecipes(IRecipeCategory recipeCategory) {
		IRecipeRegistry recipeRegistry = Internal.getRecipeRegistry();
		switch (mode) {
			case INPUT: {
				if (stack != null && fluid != null) {
					List<Object> recipes = new ArrayList<>(recipeRegistry.getRecipesWithInput(recipeCategory, stack));
					recipes.addAll(recipeRegistry.getRecipesWithInput(recipeCategory, fluid));
					return ImmutableSet.copyOf(recipes).asList();
				}
				if (stack != null) {
					return recipeRegistry.getRecipesWithInput(recipeCategory, stack);
				} else {
					return recipeRegistry.getRecipesWithInput(recipeCategory, fluid);
				}
			}
			case OUTPUT: {
				if (stack != null && fluid != null) {
					List<Object> recipes = new ArrayList<>(recipeRegistry.getRecipesWithOutput(recipeCategory, stack));
					recipes.addAll(recipeRegistry.getRecipesWithOutput(recipeCategory, fluid));
					return ImmutableSet.copyOf(recipes).asList();
				}
				if (stack != null) {
					return recipeRegistry.getRecipesWithOutput(recipeCategory, stack);
				} else {
					return recipeRegistry.getRecipesWithOutput(recipeCategory, fluid);
				}
			}
		}
		return recipeRegistry.getRecipes(recipeCategory);
	}
}
