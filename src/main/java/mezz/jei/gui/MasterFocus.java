package mezz.jei.gui;

import javax.annotation.Nonnull;
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
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class MasterFocus {
	public static MasterFocus create(IFocus<?> focus) {
		MasterFocus masterFocus;
		Object value = focus.getValue();
		if (value instanceof ItemStack) {
			masterFocus = new MasterFocus((ItemStack) value);
		} else if (value instanceof FluidStack) {
			masterFocus = new MasterFocus((FluidStack) value);
		} else {
			masterFocus = new MasterFocus();
		}

		masterFocus.setMode(focus.getMode());
		return masterFocus;
	}

	private final ItemStack itemStack;
	private final FluidStack fluidStack;
	@Nonnull
	private IFocus.Mode mode = IFocus.Mode.NONE;
	private boolean allowsCheating = false;

	public MasterFocus() {
		this.itemStack = null;
		this.fluidStack = null;
	}

	public MasterFocus(ItemStack itemStack) {
		this.itemStack = itemStack;
		this.fluidStack = getFluidFromItemStack(itemStack);
	}

	public MasterFocus(FluidStack fluidStack) {
		this.itemStack = null;
		this.fluidStack = fluidStack;
	}

	@Nullable
	private static FluidStack getFluidFromItemStack(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof ItemBlock) {
			Block block = ((ItemBlock) item).getBlock();
			Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
			if (fluid != null) {
				return new FluidStack(fluid, Fluid.BUCKET_VOLUME);
			}
		}

		// workaround for broken FluidContainerRegistry entry for potions
		if (item instanceof ItemPotion) {
			return null;
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

	public void setMode(@Nonnull IFocus.Mode mode) {
		this.mode = mode;
	}

	@Nonnull
	public IFocus.Mode getMode() {
		return mode;
	}

	public void setAllowsCheating() {
		allowsCheating = true;
	}

	public boolean allowsCheating() {
		return allowsCheating;
	}

	public boolean equalsFocus(@Nonnull MasterFocus other) {
		return ItemStack.areItemStacksEqual(this.itemStack, other.getItemStack()) && fluidStack == other.getFluidStack() && mode == other.getMode();
	}

	@Nonnull
	public List<IRecipeCategory> getCategories() {
		IRecipeRegistry recipeRegistry = Internal.getRuntime().getRecipeRegistry();
		if (mode == IFocus.Mode.INPUT) {
			return getInputCategories(recipeRegistry);
		} else if (mode == IFocus.Mode.OUTPUT) {
			return getOutputCategories(recipeRegistry);
		} else {
			return recipeRegistry.getRecipeCategories();
		}
	}

	@Nonnull
	private List<IRecipeCategory> getInputCategories(@Nonnull IRecipeRegistry recipeRegistry) {
		if (itemStack != null && fluidStack != null) {
			List<IRecipeCategory> categories = new ArrayList<>(recipeRegistry.getRecipeCategoriesWithInput(itemStack));
			categories.addAll(recipeRegistry.getRecipeCategoriesWithInput(fluidStack));
			return ImmutableSet.copyOf(categories).asList();
		}
		if (itemStack != null) {
			return recipeRegistry.getRecipeCategoriesWithInput(itemStack);
		} else {
			return recipeRegistry.getRecipeCategoriesWithInput(fluidStack);
		}
	}

	@Nonnull
	private List<IRecipeCategory> getOutputCategories(@Nonnull IRecipeRegistry recipeRegistry) {
		if (itemStack != null && fluidStack != null) {
			List<IRecipeCategory> categories = new ArrayList<>(recipeRegistry.getRecipeCategoriesWithOutput(itemStack));
			categories.addAll(recipeRegistry.getRecipeCategoriesWithOutput(fluidStack));
			return ImmutableSet.copyOf(categories).asList();
		}
		if (itemStack != null) {
			return recipeRegistry.getRecipeCategoriesWithOutput(itemStack);
		} else {
			return recipeRegistry.getRecipeCategoriesWithOutput(fluidStack);
		}
	}

	@Nonnull
	public List<Object> getRecipes(@Nonnull IRecipeCategory recipeCategory) {
		IRecipeRegistry recipeRegistry = Internal.getRuntime().getRecipeRegistry();
		if (mode == IFocus.Mode.INPUT) {
			return getInputRecipes(recipeRegistry, recipeCategory);
		} else if (mode == IFocus.Mode.OUTPUT) {
			return getOutputRecipes(recipeRegistry, recipeCategory);
		} else {
			return recipeRegistry.getRecipes(recipeCategory);
		}
	}

	@Nonnull
	public Collection<ItemStack> getRecipeCategoryCraftingItems(@Nonnull IRecipeCategory recipeCategory) {
		IRecipeRegistry recipeRegistry = Internal.getRuntime().getRecipeRegistry();
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

	@Nonnull
	private List<Object> getInputRecipes(@Nonnull IRecipeRegistry recipeRegistry, @Nonnull IRecipeCategory recipeCategory) {
		if (itemStack != null && fluidStack != null) {
			List<Object> recipes = new ArrayList<>(recipeRegistry.getRecipesWithInput(recipeCategory, itemStack));
			recipes.addAll(recipeRegistry.getRecipesWithInput(recipeCategory, fluidStack));
			return ImmutableSet.copyOf(recipes).asList();
		}
		if (itemStack != null) {
			return recipeRegistry.getRecipesWithInput(recipeCategory, itemStack);
		} else {
			return recipeRegistry.getRecipesWithInput(recipeCategory, fluidStack);
		}
	}

	@Nonnull
	private List<Object> getOutputRecipes(@Nonnull IRecipeRegistry recipeRegistry, @Nonnull IRecipeCategory recipeCategory) {
		if (itemStack != null && fluidStack != null) {
			List<Object> recipes = new ArrayList<>(recipeRegistry.getRecipesWithOutput(recipeCategory, itemStack));
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
