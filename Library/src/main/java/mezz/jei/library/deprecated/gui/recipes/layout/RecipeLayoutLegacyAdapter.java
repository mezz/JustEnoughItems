package mezz.jei.library.deprecated.gui.recipes.layout;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.library.deprecated.gui.ingredients.adapters.RecipeSlotsGuiIngredientGroupAdapter;
import mezz.jei.library.deprecated.gui.ingredients.adapters.RecipeSlotsGuiItemStackGroupAdapter;
import mezz.jei.library.deprecated.ingredients.Ingredients;
import mezz.jei.library.gui.ingredients.RecipeSlots;
import mezz.jei.library.gui.recipes.RecipeLayout;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings({"unchecked", "removal", "DeprecatedIsStillUsed"})
@Deprecated
public class RecipeLayoutLegacyAdapter<R> implements IRecipeLayout, IRecipeLayoutDrawable {
	private static final Logger LOGGER = LogManager.getLogger();

	private final RecipeLayout<R> recipeLayout;
	private final RecipeSlots recipeSlots;
	private final IIngredientManager ingredientManager;
	private final IIngredientVisibility ingredientVisibility;
	private final IFocusGroup focuses;
	private final int ingredientCycleOffset;
	private final IGuiItemStackGroup guiItemStackGroup;

	public RecipeLayoutLegacyAdapter(
		RecipeLayout<R> recipeLayout,
		RecipeSlots recipeSlots,
		IIngredientManager ingredientManager,
		IIngredientVisibility ingredientVisibility,
		IFocusGroup focuses,
		int ingredientCycleOffset
	) {
		this.recipeLayout = recipeLayout;
		this.recipeSlots = recipeSlots;
		this.ingredientManager = ingredientManager;
		this.ingredientVisibility = ingredientVisibility;
		this.focuses = focuses;
		this.ingredientCycleOffset = ingredientCycleOffset;

		IFocus<ItemStack> itemStackFocus = focuses.getFocuses(VanillaTypes.ITEM_STACK).findFirst().orElse(null);
		this.guiItemStackGroup = new RecipeSlotsGuiItemStackGroupAdapter(recipeSlots, ingredientManager, ingredientVisibility, ingredientCycleOffset);
		this.guiItemStackGroup.setOverrideDisplayFocus(itemStackFocus);
	}

	public boolean setRecipeLayout(IRecipeCategory<R> recipeCategory, R recipe) {
		try {
			IIngredients ingredients = new Ingredients();
			recipeCategory.setIngredients(recipe, ingredients);
			recipeCategory.setRecipe(this, recipe, ingredients);
			return true;
		} catch (RuntimeException | LinkageError e) {
			LOGGER.error("Error caught from Recipe Category: {}", recipeCategory.getRecipeType(), e);
		}
		return false;
	}

	public RecipeSlots getRecipeSlots() {
		return recipeSlots;
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
	public <T> T getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType) {
		return this.recipeLayout.getRecipeSlotUnderMouse(mouseX, mouseY)
			.flatMap(slot -> slot.getDisplayedIngredient(ingredientType))
			.orElse(null);
	}

	@Override
	public Optional<IRecipeSlotDrawable> getRecipeSlotUnderMouse(double mouseX, double mouseY) {
		return this.recipeLayout.getRecipeSlotUnderMouse(mouseX, mouseY);
	}

	@Override
	public Rect2i getRect() {
		return this.recipeLayout.getRect();
	}

	@Override
	public Rect2i getRecipeTransferButtonArea() {
		return this.recipeLayout.getRecipeTransferButtonArea();
	}

	@Override
	public IRecipeSlotsView getRecipeSlotsView() {
		return this.recipeLayout.getRecipeSlotsView();
	}

	@Override
	public IRecipeCategory<?> getRecipeCategory() {
		return this.recipeLayout.getRecipeCategory();
	}

	@Override
	public Object getRecipe() {
		return this.recipeLayout.getRecipe();
	}

	@Override
	public IJeiInputHandler getInputHandler() {
		return this.recipeLayout.getInputHandler();
	}

	@Override
	public void tick() {
		this.recipeLayout.tick();
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
			this.recipeSlots,
			this.ingredientManager,
			ingredientType,
			this.ingredientVisibility,
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
