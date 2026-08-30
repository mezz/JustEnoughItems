package mezz.jei.library.plugins.vanilla.crafting;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.library.recipes.ExtendableRecipeCategoryHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class CraftingRecipeCategory extends AbstractRecipeCategory<CraftingRecipe> implements IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> {
	public static final int width = 116;
	public static final int height = 54;

	private final ICraftingGridHelper craftingGridHelper;
	private final ExtendableRecipeCategoryHelper<Recipe<?>, ICraftingCategoryExtension> extendableHelper = new ExtendableRecipeCategoryHelper<>(CraftingRecipe.class);

	public CraftingRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.CRAFTING,
			Component.translatable("gui.jei.category.craftingTable"),
			guiHelper.createDrawableItemLike(Blocks.CRAFTING_TABLE),
			width,
			height
		);
		craftingGridHelper = guiHelper.createCraftingGridHelper();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CraftingRecipe recipe, IFocusGroup focuses) {
		var recipeExtension = this.extendableHelper.getRecipeExtension(this, recipe);
		recipeExtension.setRecipe(builder, craftingGridHelper, focuses);
	}

	@Override
	public void onDisplayedIngredientsUpdate(CraftingRecipe recipe, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
		var recipeExtension = this.extendableHelper.getRecipeExtension(this, recipe);
		recipeExtension.onDisplayedIngredientsUpdate(recipeSlots, focuses);
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, CraftingRecipe recipe, IFocusGroup focuses) {
		var recipeExtension = this.extendableHelper.getRecipeExtension(this, recipe);
		int recipeWidth = this.getWidth();
		int recipeHeight = this.getHeight();
		builder.addWidget(new CraftingExtensionRecipeWidget(recipeExtension, recipeWidth, recipeHeight));

		builder.addRecipeArrowWidget()
			.setPosition(61, 0, width - 61, height, HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

		recipeExtension.createRecipeExtras(builder, craftingGridHelper, focuses);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, CraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		ICraftingCategoryExtension extension = this.extendableHelper.getRecipeExtension(this, recipe);
		extension.getTooltip(tooltip, mouseX, mouseY);
	}

	@SuppressWarnings("removal")
	@Override
	public boolean handleInput(CraftingRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
		ICraftingCategoryExtension extension = this.extendableHelper.getRecipeExtension(this, recipe);
		return extension.handleInput(mouseX, mouseY, input);
	}

	@Override
	public boolean isHandled(CraftingRecipe recipe) {
		return this.extendableHelper.getOptionalRecipeExtension(recipe)
			.isPresent();
	}

	@Override
	public <R extends CraftingRecipe> void addCategoryExtension(Class<? extends R> recipeClass, Function<R, ? extends ICraftingCategoryExtension> extensionFactory) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");
		ErrorUtil.checkNotNull(extensionFactory, "extensionFactory");
		extendableHelper.addRecipeExtensionFactory(recipeClass, null, extensionFactory);
	}

	@Override
	public <R extends CraftingRecipe> void addCategoryExtension(Class<? extends R> recipeClass, Predicate<R> extensionFilter, Function<R, ? extends ICraftingCategoryExtension> extensionFactory) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");
		ErrorUtil.checkNotNull(extensionFilter, "extensionFilter");
		ErrorUtil.checkNotNull(extensionFactory, "extensionFactory");
		extendableHelper.addRecipeExtensionFactory(recipeClass, extensionFilter, extensionFactory);
	}

	@Override
	public ResourceLocation getRegistryName(CraftingRecipe recipe) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		return this.extendableHelper.getOptionalRecipeExtension(recipe)
			.flatMap(extension -> Optional.ofNullable(extension.getRegistryName()))
			.orElseGet(recipe::getId);
	}

	public ImmutableSize2i getRecipeSize(CraftingRecipe recipe) {
		ErrorUtil.checkNotNull(recipe, "recipe");
		return this.extendableHelper.getOptionalRecipeExtension(recipe)
			.map(extension -> {
				int width = extension.getWidth();
				int height = extension.getHeight();
				return new ImmutableSize2i(width, height);
			})
			.orElse(ImmutableSize2i.EMPTY);
	}

	private record CraftingExtensionRecipeWidget(
		ICraftingCategoryExtension recipeExtension,
		int recipeWidth,
		int recipeHeight
	) implements IRecipeWidget {
		@Override
		public ScreenPosition getPosition() {
			return new ScreenPosition(0, 0);
		}

		@Override
		public ScreenRectangle getScreenRectangle() {
			return new ScreenRectangle(0, 0, recipeWidth, recipeHeight);
		}

		@Override
		public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			recipeExtension.drawInfo(recipeWidth, recipeHeight, guiGraphics, mouseX, mouseY);
		}
	}
}
