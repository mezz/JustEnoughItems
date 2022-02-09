package mezz.jei.gui.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.UserInput;
import mezz.jei.input.mouse.IUserInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeCategoryTab extends RecipeGuiTab {
	private final IRecipeGuiLogic logic;
	private final IRecipeCategory<?> category;

	public RecipeCategoryTab(IRecipeGuiLogic logic, IRecipeCategory<?> category, int x, int y) {
		super(x, y);
		this.logic = logic;
		this.category = category;
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input) {
		if (!isMouseOver(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}
		if (!input.isSimulate()) {
			logic.setRecipeCategory(category);
			SoundManager soundHandler = Minecraft.getInstance().getSoundManager();
			soundHandler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
		return Optional.of(this);
	}

	@Override
	public void draw(boolean selected, PoseStack poseStack, int mouseX, int mouseY) {
		super.draw(selected, poseStack, mouseX, mouseY);

		int iconX = x + 4;
		int iconY = y + 4;

		IDrawable icon = category.getIcon();
		//noinspection ConstantConditions
		if (icon != null) {
			iconX += (16 - icon.getWidth()) / 2;
			iconY += (16 - icon.getHeight()) / 2;
			icon.draw(poseStack, iconX, iconY);
		} else {
			List<ITypedIngredient<?>> recipeCatalysts = logic.getRecipeCatalysts(category);
			if (!recipeCatalysts.isEmpty()) {
				ITypedIngredient<?> ingredient = recipeCatalysts.get(0);
				renderIngredient(poseStack, iconX, iconY, ingredient);
			} else {
				String text = category.getTitle().getString().substring(0, 2);
				Minecraft minecraft = Minecraft.getInstance();
				Font fontRenderer = minecraft.font;
				int textCenterX = x + (TAB_WIDTH / 2);
				int textCenterY = y + (TAB_HEIGHT / 2) - 3;
				int color = isMouseOver(mouseX, mouseY) ? 0xFFFFA0 : 0xE0E0E0;
				int stringCenter = fontRenderer.width(text) / 2;
				fontRenderer.drawShadow(poseStack, text, textCenterX - stringCenter, textCenterY, color);
				RenderSystem.setShaderColor(1, 1, 1, 1);
			}
		}
	}

	private static <T> void renderIngredient(PoseStack poseStack, int iconX, int iconY, ITypedIngredient<T> ingredient) {
		IngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredient.getType());
		poseStack.pushPose();
		{
			poseStack.translate(iconX, iconY, 0);
			RenderSystem.enableDepthTest();
			ingredientRenderer.render(poseStack, ingredient.getIngredient());
			RenderSystem.disableDepthTest();
		}
		poseStack.popPose();
	}

	@Override
	public boolean isSelected(IRecipeCategory<?> selectedCategory) {
		return category.getUid().equals(selectedCategory.getUid());
	}

	@Override
	public List<Component> getTooltip() {
		List<Component> tooltip = new ArrayList<>();
		Component title = category.getTitle();
		//noinspection ConstantConditions
		if (title != null) {
			tooltip.add(title);
		}

		ResourceLocation uid = category.getUid();
		String modId = uid.getNamespace();
		IModIdHelper modIdHelper = Internal.getHelpers().getModIdHelper();
		if (modIdHelper.isDisplayingModNameEnabled()) {
			String modName = modIdHelper.getFormattedModNameForModId(modId);
			tooltip.add(new TextComponent(modName));
		}
		return tooltip;
	}
}
