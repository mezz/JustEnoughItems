package mezz.jei.plugins.jei.info;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Collections;
import java.util.List;

import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.plugins.jei.JeiInternalPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.textures.Textures;
import net.minecraft.network.chat.TranslatableComponent;

@SuppressWarnings("rawtypes")
public class IngredientInfoRecipeCategory implements IRecipeCategory<IngredientInfoRecipe> {
	public static final int recipeWidth = 160;
	public static final int recipeHeight = 125;
	private static final int lineSpacing = 2;

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotBackground;
	private final JeiInternalPlugin jeiPlugin;
	private final Component localizedName;

	public IngredientInfoRecipeCategory(IGuiHelper guiHelper, Textures textures, JeiInternalPlugin jeiPlugin) {
		this.background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		this.icon = textures.getInfoIcon();
		this.slotBackground = guiHelper.getSlotDrawable();
		this.jeiPlugin = jeiPlugin;
		this.localizedName = new TranslatableComponent("gui.jei.category.itemInformation");
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.INFORMATION;
	}

	@Override
	public Class<? extends IngredientInfoRecipe> getRecipeClass() {
		return IngredientInfoRecipe.class;
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
	public void setIngredients(IngredientInfoRecipe recipe, IIngredients ingredients) {
		setIngredientsTyped((IngredientInfoRecipe<?>) recipe, ingredients);
	}

	private static <T> void setIngredientsTyped(IngredientInfoRecipe<T> recipe, IIngredients ingredients) {
		IIngredientType<T> ingredientType = recipe.getIngredientType();
		List<List<T>> recipeIngredients = Collections.singletonList(recipe.getIngredients());
		ingredients.setInputLists(ingredientType, recipeIngredients);
		ingredients.setOutputLists(ingredientType, recipeIngredients);
	}

	@Override
	public void draw(IngredientInfoRecipe recipe, PoseStack poseStack, double mouseX, double mouseY) {
		drawTyped(poseStack, (IngredientInfoRecipe<?>) recipe);
	}

	private <T> void drawTyped(PoseStack poseStack, IngredientInfoRecipe<T> recipe) {
		int xPos = 0;
		int yPos = slotBackground.getHeight() + 4;

		Minecraft minecraft = Minecraft.getInstance();
		for (FormattedText descriptionLine : recipe.getDescription()) {
			minecraft.font.draw(poseStack, Language.getInstance().getVisualOrder(descriptionLine), xPos, yPos, 0xFF000000);
			yPos += minecraft.font.lineHeight + lineSpacing;
		}
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IngredientInfoRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		int xPos = (recipeWidth - 18) / 2;
		guiItemStacks.init(0, true, xPos, 0);
		guiItemStacks.setBackground(0, slotBackground);
		guiItemStacks.set(ingredients);

		IIngredientManager ingredientManager = jeiPlugin.ingredientManager;
		if (ingredientManager != null) {
			for (IIngredientType<?> type : ingredientManager.getRegisteredIngredientTypes()) {
				IGuiIngredientGroup<?> group = recipeLayout.getIngredientsGroup(type);
				group.init(0, true, xPos + 1, 1);
				group.set(ingredients);
			}
		}
	}
}
