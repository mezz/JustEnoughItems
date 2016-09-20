package mezz.jei.gui;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import mezz.jei.Internal;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.util.StackHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class MasterFocus {
	public static MasterFocus create(IRecipeRegistry recipeRegistry, IFocus<?> focus) {
		MasterFocus masterFocus;
		Object value = focus.getValue();
		if (value instanceof ItemStack) {
			masterFocus = new MasterFocus(recipeRegistry, (ItemStack) value);
		} else if (value instanceof FluidStack) {
			masterFocus = new MasterFocus(recipeRegistry, (FluidStack) value);
		} else {
			masterFocus = new MasterFocus(recipeRegistry);
		}

		masterFocus.setMode(focus.getMode());
		return masterFocus;
	}

	private final IRecipeRegistry recipeRegistry;
	private final ItemStack itemStack;
	private final FluidStack fluidStack;
	private IFocus.Mode mode = IFocus.Mode.NONE;

	public MasterFocus(IRecipeRegistry recipeRegistry) {
		this.recipeRegistry = recipeRegistry;
		this.itemStack = null;
		this.fluidStack = null;
	}

	public MasterFocus(IRecipeRegistry recipeRegistry, ItemStack itemStack) {
		this.recipeRegistry = recipeRegistry;
		this.itemStack = itemStack;
		this.fluidStack = getFluidFromItemStack(itemStack);
	}

	public MasterFocus(IRecipeRegistry recipeRegistry, FluidStack fluidStack) {
		this.recipeRegistry = recipeRegistry;
		this.itemStack = null;
		this.fluidStack = fluidStack;
	}

	@Nullable
	private static FluidStack getFluidFromItemStack(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).getBlock();
			Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
			if (fluid != null) {
				return new FluidStack(fluid, Fluid.BUCKET_VOLUME);
			}
		}

		FluidStack fluidContained = FluidUtil.getFluidContained(stack);
		if (fluidContained != null) {
			return fluidContained;
		}

		return null;
	}

	public FluidStack getFluidStack() {
		return fluidStack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public void setMode(IFocus.Mode mode) {
		this.mode = mode;
	}

	public IFocus.Mode getMode() {
		return mode;
	}

	public boolean equalsFocus(MasterFocus other) {
		return ItemStack.areItemStacksEqual(this.itemStack, other.getItemStack()) && fluidStack == other.getFluidStack() && mode == other.getMode();
	}

	public List<IRecipeCategory> getCategories() {
		if (mode == IFocus.Mode.INPUT) {
			return getInputCategories(recipeRegistry);
		} else if (mode == IFocus.Mode.OUTPUT) {
			return getOutputCategories(recipeRegistry);
		} else {
			return recipeRegistry.getRecipeCategories();
		}
	}

	private List<IRecipeCategory> getInputCategories(IRecipeRegistry recipeRegistry) {
		if (itemStack != null && fluidStack != null) {
			List<IRecipeCategory> categories = new ArrayList<IRecipeCategory>(recipeRegistry.getRecipeCategoriesWithInput(itemStack));
			categories.addAll(recipeRegistry.getRecipeCategoriesWithInput(fluidStack));
			return ImmutableSet.copyOf(categories).asList();
		}
		if (itemStack != null) {
			return recipeRegistry.getRecipeCategoriesWithInput(itemStack);
		} else {
			return recipeRegistry.getRecipeCategoriesWithInput(fluidStack);
		}
	}

	private List<IRecipeCategory> getOutputCategories(IRecipeRegistry recipeRegistry) {
		if (itemStack != null && fluidStack != null) {
			List<IRecipeCategory> categories = new ArrayList<IRecipeCategory>(recipeRegistry.getRecipeCategoriesWithOutput(itemStack));
			categories.addAll(recipeRegistry.getRecipeCategoriesWithOutput(fluidStack));
			return ImmutableSet.copyOf(categories).asList();
		}
		if (itemStack != null) {
			return recipeRegistry.getRecipeCategoriesWithOutput(itemStack);
		} else {
			return recipeRegistry.getRecipeCategoriesWithOutput(fluidStack);
		}
	}

	public List<Object> getRecipes(IRecipeCategory recipeCategory) {
		if (mode == IFocus.Mode.INPUT) {
			return getInputRecipes(recipeRegistry, recipeCategory);
		} else if (mode == IFocus.Mode.OUTPUT) {
			return getOutputRecipes(recipeRegistry, recipeCategory);
		} else {
			return recipeRegistry.getRecipes(recipeCategory);
		}
	}

	public Collection<ItemStack> getRecipeCategoryCraftingItems(IRecipeCategory recipeCategory) {
		Collection<ItemStack> craftingItems = recipeRegistry.getCraftingItems(recipeCategory);
		if (itemStack != null && mode == IFocus.Mode.INPUT) {
			StackHelper stackHelper = Internal.getStackHelper();
			ItemStack matchingStack = stackHelper.containsStack(craftingItems, itemStack);
			if (matchingStack != null) {
				return Collections.singletonList(matchingStack);
			}
		}
		return craftingItems;
	}

	private List<Object> getInputRecipes(IRecipeRegistry recipeRegistry, IRecipeCategory recipeCategory) {
		if (itemStack != null && fluidStack != null) {
			List<Object> recipes = new ArrayList<Object>(recipeRegistry.getRecipesWithInput(recipeCategory, itemStack));
			recipes.addAll(recipeRegistry.getRecipesWithInput(recipeCategory, fluidStack));
			return ImmutableSet.copyOf(recipes).asList();
		}
		if (itemStack != null) {
			return recipeRegistry.getRecipesWithInput(recipeCategory, itemStack);
		} else {
			return recipeRegistry.getRecipesWithInput(recipeCategory, fluidStack);
		}
	}

	private List<Object> getOutputRecipes(IRecipeRegistry recipeRegistry, IRecipeCategory recipeCategory) {
		if (itemStack != null && fluidStack != null) {
			List<Object> recipes = new ArrayList<Object>(recipeRegistry.getRecipesWithOutput(recipeCategory, itemStack));
			recipes.addAll(recipeRegistry.getRecipesWithOutput(recipeCategory, fluidStack));
			return ImmutableSet.copyOf(recipes).asList();
		}
		if (itemStack != null) {
			return recipeRegistry.getRecipesWithOutput(recipeCategory, itemStack);
		} else {
			return recipeRegistry.getRecipesWithOutput(recipeCategory, fluidStack);
		}
	}
}
