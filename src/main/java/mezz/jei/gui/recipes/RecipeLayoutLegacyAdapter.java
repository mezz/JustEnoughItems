package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.gui.ingredients.adapters.RecipeSlotsGuiFluidStackGroupAdapter;
import mezz.jei.gui.ingredients.adapters.RecipeSlotsGuiIngredientGroupAdapter;
import mezz.jei.gui.ingredients.adapters.RecipeSlotsGuiItemStackGroupAdapter;
import mezz.jei.ingredients.IngredientTypeHelper;
import mezz.jei.ingredients.Ingredients;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings({"unchecked", "removal"})
public class RecipeLayoutLegacyAdapter<R> implements IRecipeLayout, IRecipeLayoutDrawable {
	private static final Logger LOGGER = LogManager.getLogger();

	private final RecipeLayout<R> recipeLayout;
	private final IIngredientManager ingredientManager;
	private final List<Focus<?>> focuses;
	private final int ingredientCycleOffset;
	private final IGuiItemStackGroup guiItemStackGroup;
	private final IGuiFluidStackGroup guiFluidStackGroup;

	public RecipeLayoutLegacyAdapter(
		RecipeLayout<R> recipeLayout,
		IIngredientManager ingredientManager,
		List<Focus<?>> focuses,
		int ingredientCycleOffset
	) {
		this.recipeLayout = recipeLayout;
		this.ingredientManager = ingredientManager;
		this.focuses = focuses;
		this.ingredientCycleOffset = ingredientCycleOffset;

		RecipeSlots recipeSlots = recipeLayout.getRecipeSlots();

		Focus<ItemStack> itemStackFocus = IngredientTypeHelper.findAndCheckedCast(focuses, VanillaTypes.ITEM);
		this.guiItemStackGroup = new RecipeSlotsGuiItemStackGroupAdapter(recipeSlots, ingredientManager, ingredientCycleOffset);
		this.guiItemStackGroup.setOverrideDisplayFocus(itemStackFocus);

		Focus<FluidStack> fluidStackFocus = IngredientTypeHelper.findAndCheckedCast(focuses, VanillaTypes.FLUID);
		this.guiFluidStackGroup = new RecipeSlotsGuiFluidStackGroupAdapter(recipeSlots, ingredientManager, ingredientCycleOffset);
		this.guiFluidStackGroup.setOverrideDisplayFocus(fluidStackFocus);
	}

	public boolean setRecipeLayout(IRecipeCategory<R> recipeCategory, R recipe) {
		try {
			IIngredients ingredients = new Ingredients();
			recipeCategory.setIngredients(recipe, ingredients);
			recipeCategory.setRecipe(this, recipe, ingredients);
			return true;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getUid(), e);
		}
		return false;
	}

	@Override
	public void setPosition(int posX, int posY) {
		this.recipeLayout.setPosition(posX, posY);
	}

	@Override
	public void drawRecipe(PoseStack poseStack, int mouseX, int mouseY) {
		this.recipeLayout.drawRecipe(poseStack, mouseX, mouseY);
	}

	@Override
	public void drawOverlays(PoseStack poseStack, int mouseX, int mouseY) {
		this.recipeLayout.drawOverlays(poseStack, mouseX, mouseY);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.recipeLayout.isMouseOver(mouseX, mouseY);
	}

	@Override
	@Nullable
	public <I> I getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<I> ingredientType) {
		return this.recipeLayout.getRecipeSlotUnderMouse(mouseX, mouseY)
			.flatMap(slot -> slot.getDisplayedIngredient(ingredientType))
			.orElse(null);
	}

	@Override
	public IGuiItemStackGroup getItemStacks() {
		return guiItemStackGroup;
	}

	@Override
	public IGuiFluidStackGroup getFluidStacks() {
		return guiFluidStackGroup;
	}

	@Override
	public <V> IGuiIngredientGroup<V> getIngredientsGroup(IIngredientType<V> ingredientType) {
		if (ingredientType == VanillaTypes.ITEM) {
			return (IGuiIngredientGroup<V>) this.guiItemStackGroup;
		}
		if (ingredientType == VanillaTypes.FLUID) {
			return (IGuiIngredientGroup<V>) this.guiFluidStackGroup;
		}
		RecipeSlotsGuiIngredientGroupAdapter<V> adapter = new RecipeSlotsGuiIngredientGroupAdapter<>(
			this.recipeLayout.getRecipeSlots(),
			this.ingredientManager,
			ingredientType,
			this.ingredientCycleOffset
		);
		Focus<V> focus = getFocus(ingredientType);
		adapter.setOverrideDisplayFocus(focus);
		return adapter;
	}

	@Override
	public void moveRecipeTransferButton(int posX, int posY) {
		this.recipeLayout.moveRecipeTransferButton(posX, posY);
	}

	@Override
	public void setShapeless() {
		this.recipeLayout.setShapeless();
	}

	@Nullable
	@Override
	public <V> Focus<V> getFocus(IIngredientType<V> ingredientType) {
		return IngredientTypeHelper.findAndCheckedCast(this.focuses, ingredientType);
	}
}
