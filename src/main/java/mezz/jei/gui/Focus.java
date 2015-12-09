package mezz.jei.gui;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;

import mezz.jei.api.JEIManager;
import mezz.jei.api.recipe.IRecipeCategory;

public class Focus {
	public enum Mode {
		INPUT, OUTPUT, NONE
	}

	private final ItemStack stack;
	private final Fluid fluid;
	@Nonnull
	private Mode mode = Mode.NONE;

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
	public ImmutableList<IRecipeCategory> getCategories() {
		switch (mode) {
			case INPUT: {
				if (stack != null) {
					return JEIManager.recipeRegistry.getRecipeCategoriesWithInput(stack);
				} else {
					return JEIManager.recipeRegistry.getRecipeCategoriesWithInput(fluid);
				}
			}
			case OUTPUT: {
				if (stack != null) {
					return JEIManager.recipeRegistry.getRecipeCategoriesWithOutput(stack);
				} else {
					return JEIManager.recipeRegistry.getRecipeCategoriesWithOutput(fluid);
				}
			}
		}
		return JEIManager.recipeRegistry.getRecipeCategories();
	}

	@Nonnull
	public List<Object> getRecipes(IRecipeCategory recipeCategory) {
		switch (mode) {
			case INPUT: {
				if (stack != null) {
					return JEIManager.recipeRegistry.getRecipesWithInput(recipeCategory, stack);
				} else {
					return JEIManager.recipeRegistry.getRecipesWithInput(recipeCategory, fluid);
				}
			}
			case OUTPUT: {
				if (stack != null) {
					return JEIManager.recipeRegistry.getRecipesWithOutput(recipeCategory, stack);
				} else {
					return JEIManager.recipeRegistry.getRecipesWithOutput(recipeCategory, fluid);
				}
			}
		}
		return JEIManager.recipeRegistry.getRecipes(recipeCategory);
	}
}
