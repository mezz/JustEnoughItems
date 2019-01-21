package mezz.jei.plugins.vanilla.crafting;

import java.util.List;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.category.extensions.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.category.extensions.ICustomCraftingRecipeWrapper;
import mezz.jei.api.recipe.category.extensions.IShapedCraftingRecipeWrapper;
import mezz.jei.config.Constants;
import mezz.jei.recipes.ExtendableRecipeCategoryHelper;
import mezz.jei.util.Translator;

public class CraftingRecipeCategory implements IExtendableRecipeCategory<IRecipe, ICraftingRecipeWrapper> {
	private static final int craftOutputSlot = 0;
	private static final int craftInputSlot1 = 1;

	public static final int width = 116;
	public static final int height = 54;

	private final IDrawable background;
	private final IDrawable icon;
	private final String localizedName;
	private final ICraftingGridHelper craftingGridHelper;
	private final IModIdHelper modIdHelper;
	private final ExtendableRecipeCategoryHelper<IRecipe, ICraftingRecipeWrapper> extendableHelper = new ExtendableRecipeCategoryHelper<>(IRecipe.class);

	public CraftingRecipeCategory(IGuiHelper guiHelper, IModIdHelper modIdHelper) {
		this.modIdHelper = modIdHelper;
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
	public Class<? extends IRecipe> getRecipeClass() {
		return IRecipe.class;
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
	public void setRecipe(IRecipeLayout recipeLayout, IRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(craftOutputSlot, false, 94, 18);

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				int index = craftInputSlot1 + x + (y * 3);
				guiItemStacks.init(index, true, x * 18, y * 18);
			}
		}

		ICraftingRecipeWrapper recipeWrapper = this.extendableHelper.getRecipeWrapper(recipe);

		if (recipeWrapper instanceof ICustomCraftingRecipeWrapper) {
			ICustomCraftingRecipeWrapper customWrapper = (ICustomCraftingRecipeWrapper) recipeWrapper;
			customWrapper.setRecipe(recipeLayout, ingredients);
			return;
		}

		List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
		List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

		if (recipeWrapper instanceof IShapedCraftingRecipeWrapper) {
			IShapedCraftingRecipeWrapper wrapper = (IShapedCraftingRecipeWrapper) recipeWrapper;
			craftingGridHelper.setInputs(guiItemStacks, inputs, wrapper.getWidth(), wrapper.getHeight());
		} else {
			craftingGridHelper.setInputs(guiItemStacks, inputs);
			recipeLayout.setShapeless();
		}
		guiItemStacks.set(craftOutputSlot, outputs.get(0));

		ResourceLocation registryName = recipeWrapper.getRegistryName();
		if (registryName != null) {
			guiItemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
				if (slotIndex == craftOutputSlot) {
					if (modIdHelper.isDisplayingModNameEnabled()) {
						String recipeModId = registryName.getNamespace();
						boolean modIdDifferent = false;
						ResourceLocation itemRegistryName = ingredient.getItem().getRegistryName();
						if (itemRegistryName != null) {
							String itemModId = itemRegistryName.getNamespace();
							modIdDifferent = !recipeModId.equals(itemModId);
						}

						if (modIdDifferent) {
							String modName = modIdHelper.getFormattedModNameForModId(recipeModId);
							tooltip.add(TextFormatting.GRAY + Translator.translateToLocalFormatted("jei.tooltip.recipe.by", modName));
						}
					}

					boolean showAdvanced = Minecraft.getInstance().gameSettings.advancedItemTooltips || GuiScreen.isShiftKeyDown();
					if (showAdvanced) {
						tooltip.add(TextFormatting.DARK_GRAY + Translator.translateToLocalFormatted("jei.tooltip.recipe.id", registryName.toString()));
					}
				}
			});
		}
	}

	@Override
	public void setIngredients(IRecipe recipe, IIngredients ingredients) {
		ICraftingRecipeWrapper recipeWrapper = this.extendableHelper.getRecipeWrapper(recipe);
		recipeWrapper.setIngredients(ingredients);
	}

	@Override
	public void draw(IRecipe recipe, double mouseX, double mouseY) {
		ICraftingRecipeWrapper recipeWrapper = this.extendableHelper.getRecipeWrapper(recipe);
		int recipeWidth = this.background.getWidth();
		int recipeHeight = this.background.getHeight();
		recipeWrapper.drawInfo(recipeWidth, recipeHeight, mouseX, mouseY);
	}

	@Override
	public List<String> getTooltipStrings(IRecipe recipe, double mouseX, double mouseY) {
		ICraftingRecipeWrapper recipeWrapper = this.extendableHelper.getRecipeWrapper(recipe);
		return recipeWrapper.getTooltipStrings(mouseX, mouseY);
	}

	@Override
	public boolean handleClick(IRecipe recipe, double mouseX, double mouseY, int mouseButton) {
		ICraftingRecipeWrapper recipeWrapper = this.extendableHelper.getRecipeWrapper(recipe);
		return recipeWrapper.handleClick(mouseX, mouseY, mouseButton);
	}

	@Override
	public <R extends IRecipe> void addRecipeWrapperFactory(Class<? extends R> recipeClass, Function<R, ? extends ICraftingRecipeWrapper> recipeWrapperFactory) {
		extendableHelper.addRecipeWrapperFactory(recipeClass, recipeWrapperFactory);
	}
}
