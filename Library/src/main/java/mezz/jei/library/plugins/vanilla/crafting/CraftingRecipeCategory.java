package mezz.jei.library.plugins.vanilla.crafting;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import mezz.jei.common.Constants;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.library.recipes.CraftingExtensionHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class CraftingRecipeCategory implements IExtendableCraftingRecipeCategory, IRecipeCategory<RecipeHolder<CraftingRecipe>> {
	public static final int width = 116;
	public static final int height = 54;

	private final IDrawable background;
	private final IDrawable icon;
	private final Component localizedName;
	private final ICraftingGridHelper craftingGridHelper;
	private final CraftingExtensionHelper extendableHelper = new CraftingExtensionHelper();

	public CraftingRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation location = Constants.RECIPE_GUI_VANILLA;
		background = guiHelper.createDrawable(location, 0, 60, width, height);
		icon = guiHelper.createDrawableItemLike(Blocks.CRAFTING_TABLE);
		localizedName = Component.translatable("gui.jei.category.craftingTable");
		craftingGridHelper = guiHelper.createCraftingGridHelper();
	}

	@Override
	public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
		return RecipeTypes.CRAFTING;
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
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CraftingRecipe> recipeHolder, IFocusGroup focuses) {
		var recipeExtension = this.extendableHelper.getRecipeExtension(recipeHolder);
		recipeExtension.setRecipe(recipeHolder, builder, craftingGridHelper, focuses);
	}

	@Override
	public void onDisplayedIngredientsUpdate(RecipeHolder<CraftingRecipe> recipeHolder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
		var recipeExtension = this.extendableHelper.getRecipeExtension(recipeHolder);
		recipeExtension.onDisplayedIngredientsUpdate(recipeHolder, recipeSlots, focuses);
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder acceptor, RecipeHolder<CraftingRecipe> recipeHolder, IFocusGroup focuses) {
		var recipeExtension = this.extendableHelper.getRecipeExtension(recipeHolder);
		recipeExtension.createRecipeExtras(recipeHolder, acceptor, craftingGridHelper, focuses);
	}

	@Override
	public void draw(RecipeHolder<CraftingRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		var extension = this.extendableHelper.getRecipeExtension(recipeHolder);
		int recipeWidth = this.getWidth();
		int recipeHeight = this.getHeight();
		extension.drawInfo(recipeHolder, recipeWidth, recipeHeight, guiGraphics, mouseX, mouseY);
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<CraftingRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		var extension = this.extendableHelper.getRecipeExtension(recipeHolder);
		extension.getTooltip(tooltip, recipeHolder, mouseX, mouseY);
	}

	@SuppressWarnings({"removal"})
	@Override
	public List<Component> getTooltipStrings(RecipeHolder<CraftingRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		var extension = this.extendableHelper.getRecipeExtension(recipeHolder);
		return extension.getTooltipStrings(recipeHolder, mouseX, mouseY);
	}

	@SuppressWarnings("removal")
	@Override
	public boolean handleInput(RecipeHolder<CraftingRecipe> recipeHolder, double mouseX, double mouseY, InputConstants.Key input) {
		var extension = this.extendableHelper.getRecipeExtension(recipeHolder);
		return extension.handleInput(recipeHolder, mouseX, mouseY, input);
	}

	@Override
	public boolean isHandled(RecipeHolder<CraftingRecipe> recipeHolder) {
		return this.extendableHelper.getOptionalRecipeExtension(recipeHolder)
			.isPresent();
	}

	@Override
	public <R extends CraftingRecipe> void addExtension(Class<? extends R> recipeClass, ICraftingCategoryExtension<R> extension) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");
		ErrorUtil.checkNotNull(extension, "extension");
		extendableHelper.addRecipeExtension(recipeClass, extension);
	}

	@SuppressWarnings("removal")
	@Override
	public ResourceLocation getRegistryName(RecipeHolder<CraftingRecipe> recipeHolder) {
		ErrorUtil.checkNotNull(recipeHolder, "recipeHolder");
		return this.extendableHelper.getOptionalRecipeExtension(recipeHolder)
			.flatMap(extension -> extension.getRegistryName(recipeHolder))
			.orElseGet(recipeHolder::id);
	}

	@Override
	public Codec<RecipeHolder<CraftingRecipe>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
		return codecHelper.getRecipeHolderCodec();
	}

	public ImmutableSize2i getRecipeSize(RecipeHolder<CraftingRecipe> recipeHolder) {
		ErrorUtil.checkNotNull(recipeHolder, "recipeHolder");
		return this.extendableHelper.getOptionalRecipeExtension(recipeHolder)
			.map(extension -> {
				int width = extension.getWidth(recipeHolder);
				int height = extension.getHeight(recipeHolder);
				return new ImmutableSize2i(width, height);
			})
			.orElse(ImmutableSize2i.EMPTY);
	}
}
