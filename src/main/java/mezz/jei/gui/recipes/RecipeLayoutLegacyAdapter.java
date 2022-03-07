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
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.ingredients.RecipeSlots;
import mezz.jei.gui.ingredients.adapters.RecipeSlotsGuiFluidStackGroupAdapter;
import mezz.jei.gui.ingredients.adapters.RecipeSlotsGuiIngredientGroupAdapter;
import mezz.jei.gui.ingredients.adapters.RecipeSlotsGuiItemStackGroupAdapter;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.ingredients.Ingredients;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unchecked", "removal"})
public class RecipeLayoutLegacyAdapter<R> implements IRecipeLayout, IRecipeLayoutDrawable {
	private static final Logger LOGGER = LogManager.getLogger();

	private final RecipeLayout<R> recipeLayout;
	private final RegisteredIngredients registeredIngredients;
	private final IFocusGroup focuses;
	private final int ingredientCycleOffset;
	private final IGuiItemStackGroup guiItemStackGroup;
	private final IGuiFluidStackGroup guiFluidStackGroup;

	public RecipeLayoutLegacyAdapter(
		RecipeLayout<R> recipeLayout,
		RegisteredIngredients registeredIngredients,
		IFocusGroup focuses,
		int ingredientCycleOffset
	) {
		this.recipeLayout = recipeLayout;
		this.registeredIngredients = registeredIngredients;
		this.focuses = focuses;
		this.ingredientCycleOffset = ingredientCycleOffset;

		RecipeSlots recipeSlots = recipeLayout.getRecipeSlots();

		IFocus<ItemStack> itemStackFocus = focuses.getFocuses(VanillaTypes.ITEM).findFirst().orElse(null);
		this.guiItemStackGroup = new RecipeSlotsGuiItemStackGroupAdapter(recipeSlots, registeredIngredients, ingredientCycleOffset);
		this.guiItemStackGroup.setOverrideDisplayFocus(itemStackFocus);

		IFocus<FluidStack> fluidStackFocus = focuses.getFocuses(VanillaTypes.FLUID).findFirst().orElse(null);
		this.guiFluidStackGroup = new RecipeSlotsGuiFluidStackGroupAdapter(recipeSlots, registeredIngredients, ingredientCycleOffset);
		this.guiFluidStackGroup.setOverrideDisplayFocus(fluidStackFocus);
	}

	public boolean setRecipeLayout(IRecipeCategory<R> recipeCategory, R recipe) {
		try {
			IIngredients ingredients = new Ingredients();
			recipeCategory.setIngredients(recipe, ingredients);
			recipeCategory.setRecipe(this, recipe, ingredients);
			return true;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getRecipeType().getUid(), e);
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
			this.registeredIngredients,
			ingredientType,
			this.ingredientCycleOffset
		);
		IFocus<V> focus = getFocus(ingredientType);
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
	public <V> IFocus<V> getFocus(IIngredientType<V> ingredientType) {
		return this.focuses.getFocuses(ingredientType)
			.findFirst()
			.orElse(null);
	}
}
