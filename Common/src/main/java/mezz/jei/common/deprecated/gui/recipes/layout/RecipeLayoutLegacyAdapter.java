package mezz.jei.common.deprecated.gui.recipes.layout;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.gui.ingredients.RecipeSlots;
import mezz.jei.common.gui.recipes.layout.IRecipeLayoutInternal;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.deprecated.gui.ingredients.adapters.RecipeSlotsGuiIngredientGroupAdapter;
import mezz.jei.common.deprecated.gui.ingredients.adapters.RecipeSlotsGuiItemStackGroupAdapter;
import mezz.jei.common.deprecated.ingredients.Ingredients;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unchecked", "removal", "DeprecatedIsStillUsed"})
@Deprecated
public class RecipeLayoutLegacyAdapter<R> implements IRecipeLayout, IRecipeLayoutDrawable {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IRecipeLayoutInternal<R> recipeLayout;
	private final RegisteredIngredients registeredIngredients;
	private final IIngredientVisibility ingredientVisibility;
	private final IFocusGroup focuses;
	private final int ingredientCycleOffset;
	private final IGuiItemStackGroup guiItemStackGroup;

	public RecipeLayoutLegacyAdapter(
		IRecipeLayoutInternal<R> recipeLayout,
		RegisteredIngredients registeredIngredients,
		IIngredientVisibility ingredientVisibility,
		IFocusGroup focuses,
		int ingredientCycleOffset
	) {
		this.recipeLayout = recipeLayout;
		this.registeredIngredients = registeredIngredients;
		this.ingredientVisibility = ingredientVisibility;
		this.focuses = focuses;
		this.ingredientCycleOffset = ingredientCycleOffset;

		RecipeSlots recipeSlots = recipeLayout.getRecipeSlots();

		IFocus<ItemStack> itemStackFocus = focuses.getFocuses(VanillaTypes.ITEM_STACK).findFirst().orElse(null);
		this.guiItemStackGroup = new RecipeSlotsGuiItemStackGroupAdapter(recipeSlots, registeredIngredients, ingredientVisibility, ingredientCycleOffset);
		this.guiItemStackGroup.setOverrideDisplayFocus(itemStackFocus);
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
	public <V> IGuiIngredientGroup<V> getIngredientsGroup(IIngredientType<V> ingredientType) {
		if (ingredientType == VanillaTypes.ITEM_STACK) {
			return (IGuiIngredientGroup<V>) this.guiItemStackGroup;
		}
		RecipeSlotsGuiIngredientGroupAdapter<V> adapter = new RecipeSlotsGuiIngredientGroupAdapter<>(
			this.recipeLayout.getRecipeSlots(),
			this.registeredIngredients,
			ingredientType,
            ingredientVisibility,
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
