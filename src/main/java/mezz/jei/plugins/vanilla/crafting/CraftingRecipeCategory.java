package mezz.jei.plugins.vanilla.crafting;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.config.Constants;
import mezz.jei.gui.recipes.builder.RecipeLayoutBuilder;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.recipes.ExtendableRecipeCategoryHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class CraftingRecipeCategory implements IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> {
	private static final int craftInputSlot1 = 1;

	public static final int width = 116;
	public static final int height = 54;

	private final IDrawable background;
	private final IDrawable icon;
	private final Component localizedName;
	private final ICraftingGridHelper craftingGridHelper;
	private final ExtendableRecipeCategoryHelper<Recipe<?>, ICraftingCategoryExtension> extendableHelper = new ExtendableRecipeCategoryHelper<>(CraftingRecipe.class);

	public CraftingRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation location = Constants.RECIPE_GUI_VANILLA;
		background = guiHelper.createDrawable(location, 0, 60, width, height);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(Blocks.CRAFTING_TABLE));
		localizedName = new TranslatableComponent("gui.jei.category.craftingTable");
		craftingGridHelper = guiHelper.createCraftingGridHelper(craftInputSlot1);
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public Class<? extends CraftingRecipe> getRecipeClass() {
		return CraftingRecipe.class;
	}

	@Override
	public Component getTitle() {
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
	public void setRecipe(IRecipeLayoutBuilder builder, CraftingRecipe recipe, List<? extends IFocus<?>> focuses) {
		ICraftingCategoryExtension recipeExtension = this.extendableHelper.getRecipeExtension(recipe);
		recipeExtension.setRecipe(builder, craftingGridHelper, focuses);

		// temporary hack to detect if the plugin needs legacy support
		if (builder instanceof RecipeLayoutBuilder b && b.isUsed()) {
			return;
		}
		legacySetRecipe(builder, recipeExtension);
	}

	@SuppressWarnings({"removal"})
	private void legacySetRecipe(IRecipeLayoutBuilder builder, ICraftingCategoryExtension recipeExtension) {
		Ingredients ingredients = new Ingredients();
		recipeExtension.setIngredients(ingredients);
		List<@Nullable List<@Nullable ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
		List<@Nullable List<@Nullable ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

		int width = recipeExtension.getWidth();
		int height = recipeExtension.getHeight();
		craftingGridHelper.setInputs(builder, VanillaTypes.ITEM, inputs, width, height);
		craftingGridHelper.setOutputs(builder, VanillaTypes.ITEM, outputs.get(0));
	}

	@Override
	public void draw(CraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		ICraftingCategoryExtension extension = this.extendableHelper.getRecipeExtension(recipe);
		int recipeWidth = this.background.getWidth();
		int recipeHeight = this.background.getHeight();
		extension.drawInfo(recipeWidth, recipeHeight, poseStack, mouseX, mouseY);
	}

	@Override
	public List<Component> getTooltipStrings(CraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		ICraftingCategoryExtension extension = this.extendableHelper.getRecipeExtension(recipe);
		return extension.getTooltipStrings(mouseX, mouseY);
	}

	@Override
	public boolean handleInput(CraftingRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
		ICraftingCategoryExtension extension = this.extendableHelper.getRecipeExtension(recipe);
		return extension.handleInput(mouseX, mouseY, input);
	}

	@Override
	public boolean isHandled(CraftingRecipe recipe) {
		return this.extendableHelper.getOptionalRecipeExtension(recipe)
			.isPresent();
	}

	@Override
	public <R extends CraftingRecipe> void addCategoryExtension(Class<? extends R> recipeClass, Function<R, ? extends ICraftingCategoryExtension> extensionFactory) {
		extendableHelper.addRecipeExtensionFactory(recipeClass, null, extensionFactory);
	}

	@Override
	public <R extends CraftingRecipe> void addCategoryExtension(Class<? extends R> recipeClass, Predicate<R> extensionFilter, Function<R, ? extends ICraftingCategoryExtension> extensionFactory) {
		extendableHelper.addRecipeExtensionFactory(recipeClass, extensionFilter, extensionFactory);
	}

	@Override
	public ResourceLocation getRegistryName(CraftingRecipe recipe) {
		return this.extendableHelper.getOptionalRecipeExtension(recipe)
			.flatMap(extension -> Optional.ofNullable(extension.getRegistryName()))
			.orElseGet(recipe::getId);
	}
}
