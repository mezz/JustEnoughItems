package mezz.jei.plugins.vanilla.crafting;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.List;
import java.util.function.Function;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Size2i;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICustomCraftingCategoryExtension;
import mezz.jei.config.Constants;
import mezz.jei.recipes.ExtendableRecipeCategoryHelper;
import mezz.jei.util.Translator;

public class CraftingRecipeCategory implements IExtendableRecipeCategory<ICraftingRecipe, ICraftingCategoryExtension> {
	private static final int craftOutputSlot = 0;
	private static final int craftInputSlot1 = 1;

	public static final int width = 116;
	public static final int height = 54;

	private final IDrawable background;
	private final IDrawable icon;
	private final String localizedName;
	private final ICraftingGridHelper craftingGridHelper;
	private final ExtendableRecipeCategoryHelper<IRecipe<?>, ICraftingCategoryExtension> extendableHelper = new ExtendableRecipeCategoryHelper<>(ICraftingRecipe.class);

	public CraftingRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation location = Constants.RECIPE_GUI_VANILLA;
		background = guiHelper.createDrawable(location, 0, 60, width, height);
		icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.CRAFTING_TABLE));
		localizedName = Translator.translateToLocal("gui.jei.category.craftingTable");
		craftingGridHelper = guiHelper.createCraftingGridHelper(craftInputSlot1);
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public Class<? extends ICraftingRecipe> getRecipeClass() {
		return ICraftingRecipe.class;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ICraftingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(craftOutputSlot, false, 94, 18);

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				int index = craftInputSlot1 + x + (y * 3);
				guiItemStacks.init(index, true, x * 18, y * 18);
			}
		}

		ICraftingCategoryExtension recipeExtension = this.extendableHelper.getRecipeExtension(recipe);

		if (recipeExtension instanceof ICustomCraftingCategoryExtension) {
			ICustomCraftingCategoryExtension customExtension = (ICustomCraftingCategoryExtension) recipeExtension;
			customExtension.setRecipe(recipeLayout, ingredients);
			return;
		}

		List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
		List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

		Size2i size = recipeExtension.getSize();
		if (size != null && size.width > 0 && size.height > 0) {
			craftingGridHelper.setInputs(guiItemStacks, inputs, size.width, size.height);
		} else {
			craftingGridHelper.setInputs(guiItemStacks, inputs);
			recipeLayout.setShapeless();
		}
		guiItemStacks.set(craftOutputSlot, outputs.get(0));
	}

	@Override
	public void setIngredients(ICraftingRecipe recipe, IIngredients ingredients) {
		ICraftingCategoryExtension extension = this.extendableHelper.getRecipeExtension(recipe);
		extension.setIngredients(ingredients);
	}

	@Override
	public void draw(ICraftingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		ICraftingCategoryExtension extension = this.extendableHelper.getRecipeExtension(recipe);
		int recipeWidth = this.background.getWidth();
		int recipeHeight = this.background.getHeight();
		extension.drawInfo(recipeWidth, recipeHeight, matrixStack, mouseX, mouseY);
	}

	@Override
	public List<ITextComponent> getTooltipStrings(ICraftingRecipe recipe, double mouseX, double mouseY) {
		ICraftingCategoryExtension extension = this.extendableHelper.getRecipeExtension(recipe);
		return extension.getTooltipStrings(mouseX, mouseY);
	}

	@Override
	public boolean handleClick(ICraftingRecipe recipe, double mouseX, double mouseY, int mouseButton) {
		ICraftingCategoryExtension extension = this.extendableHelper.getRecipeExtension(recipe);
		return extension.handleClick(mouseX, mouseY, mouseButton);
	}

	@Override
	public <R extends ICraftingRecipe> void addCategoryExtension(Class<? extends R> recipeClass, Function<R, ? extends ICraftingCategoryExtension> extensionFactory) {
		extendableHelper.addRecipeExtensionFactory(recipeClass, extensionFactory);
	}
}
