package mezz.jei.plugins.jei.info;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayoutView;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.textures.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class IngredientInfoRecipeCategory implements IRecipeCategory<IngredientInfoRecipe<?>> {
	public static final int recipeWidth = 160;
	public static final int recipeHeight = 125;
	private static final int lineSpacing = 2;

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotBackground;
	private final Component localizedName;

	public IngredientInfoRecipeCategory(IGuiHelper guiHelper, Textures textures) {
		this.background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		this.icon = textures.getInfoIcon();
		this.slotBackground = guiHelper.getSlotDrawable();
		this.localizedName = new TranslatableComponent("gui.jei.category.itemInformation");
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.INFORMATION;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends IngredientInfoRecipe<?>> getRecipeClass() {
		return (Class<? extends IngredientInfoRecipe<?>>) (Object) IngredientInfoRecipe.class;
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
	public void draw(IngredientInfoRecipe<?> recipe, IRecipeLayoutView recipeLayoutView, PoseStack poseStack, double mouseX, double mouseY) {
		int xPos = 0;
		int yPos = slotBackground.getHeight() + 4;

		Minecraft minecraft = Minecraft.getInstance();
		for (FormattedText descriptionLine : recipe.getDescription()) {
			minecraft.font.draw(poseStack, Language.getInstance().getVisualOrder(descriptionLine), xPos, yPos, 0xFF000000);
			yPos += minecraft.font.lineHeight + lineSpacing;
		}
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IngredientInfoRecipe<?> recipe, List<? extends IFocus<?>> focuses) {
		setRecipeTyped(builder, recipe, focuses);
	}

	private <T> void setRecipeTyped(IRecipeLayoutBuilder builder, IngredientInfoRecipe<T> recipe, List<? extends IFocus<?>> focuses) {
		int xPos = (recipeWidth - 18) / 2;
		builder.addSlot(0, RecipeIngredientRole.INPUT, xPos, 0)
			.setBackground(slotBackground)
			.addIngredients(recipe.getIngredientType(), recipe.getIngredients());

		builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
			.addIngredients(recipe.getIngredientType(), recipe.getIngredients());
	}
}
