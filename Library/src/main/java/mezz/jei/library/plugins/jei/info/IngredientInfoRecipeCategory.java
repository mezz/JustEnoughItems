package mezz.jei.library.plugins.jei.info;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class IngredientInfoRecipeCategory implements IRecipeCategory<IJeiIngredientInfoRecipe> {
	private static final int recipeWidth = 170;
	private static final int recipeHeight = 125;

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotBackground;
	private final Component localizedName;

	public IngredientInfoRecipeCategory(IGuiHelper guiHelper, Textures textures) {
		this.background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		this.icon = textures.getInfoIcon();
		this.slotBackground = guiHelper.getSlotDrawable();
		this.localizedName = Component.translatable("gui.jei.category.itemInformation");
	}

	@Override
	public RecipeType<IJeiIngredientInfoRecipe> getRecipeType() {
		return RecipeTypes.INFORMATION;
	}

	@Override
	public Component getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJeiIngredientInfoRecipe recipe, IFocusGroup focuses) {
		int yPos = slotBackground.getHeight() + 4;
		int height = recipeHeight - yPos;
		builder.addScrollBoxWidget(
			recipeWidth,
			height,
			0,
			yPos
		)
		.setContents(recipe.getDescription());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJeiIngredientInfoRecipe recipe, IFocusGroup focuses) {
		int xPos = (recipeWidth - 16) / 2;

		IRecipeSlotBuilder inputSlotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, xPos, 1)
			.setStandardSlotBackground();

		IIngredientAcceptor<?> outputSlotBuilder = builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT);

		for (ITypedIngredient<?> typedIngredient : recipe.getIngredients()) {
			addIngredient(typedIngredient, inputSlotBuilder);
			addIngredient(typedIngredient, outputSlotBuilder);
		}
	}

	@Override
	public @Nullable ResourceLocation getRegistryName(IJeiIngredientInfoRecipe recipe) {
		return null;
	}

	private static <T> void addIngredient(ITypedIngredient<T> typedIngredient, IIngredientAcceptor<?> slotBuilder) {
		slotBuilder.addIngredient(typedIngredient.getType(), typedIngredient.getIngredient());
	}

}
