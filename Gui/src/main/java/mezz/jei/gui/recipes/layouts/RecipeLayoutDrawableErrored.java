package mezz.jei.gui.recipes.layouts;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeLayoutDrawableErrored<R> implements IRecipeLayoutDrawable<R> {
	private static final IJeiInputHandler INPUT_HANDLER = () -> new Rect2i(0, 0, 0, 0);

	private final IRecipeCategory<R> recipeCategory;
	private final R recipe;
	private final IScalableDrawable background;
	private final int borderPadding;
	private final List<Component> lines;
	private ImmutableRect2i area;

	public RecipeLayoutDrawableErrored(IRecipeCategory<R> recipeCategory, R recipe, IScalableDrawable background, int borderPadding) {
		this.recipeCategory = recipeCategory;
		this.recipe = recipe;
		this.area = new ImmutableRect2i(0, 0, Math.max(100, recipeCategory.getWidth()), recipeCategory.getHeight());
		this.background = background;
		this.borderPadding = borderPadding;
		this.lines = createLines(recipeCategory, recipe);
	}

	private static <R> List<Component> createLines(IRecipeCategory<R> recipeCategory, R recipe) {
		List<Component> lines = new ArrayList<>();
		lines.add(Component.translatable("gui.jei.category.recipe.crashed").withStyle(ChatFormatting.RED));
		lines.add(Component.literal(recipeCategory.getRecipeType().getUid().toString()).withStyle(ChatFormatting.GRAY));
		ResourceLocation registryName = recipeCategory.getRegistryName(recipe);
		if (registryName != null) {
			lines.add(Component.literal(registryName.toString()).withStyle(ChatFormatting.GRAY));
		}
		return lines;
	}

	@Override
	public void setPosition(int posX, int posY) {
		this.area = this.area.setPosition(posX, posY);
	}

	@Override
	public void drawRecipe(PoseStack poseStack, int mouseX, int mouseY) {
		background.draw(poseStack, getRectWithBorder());

		Font font = Minecraft.getInstance().font;
		int textX = area.getX() + 4;
		int textY = area.getY() + 4;
		for (Component line : lines) {
			font.draw(poseStack, line, textX, textY, 0xFFFFFFFF);
			textY += font.lineHeight + 2;
			if (textY >= area.getY() + area.getHeight()) {
				break;
			}
		}
	}

	@Override
	public void drawOverlays(PoseStack poseStack, int mouseX, int mouseY) {

	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(int mouseX, int mouseY, IIngredientType<T> ingredientType) {
		return Optional.empty();
	}

	@Override
	public Optional<ItemStack> getItemStackUnderMouse(int mouseX, int mouseY) {
		return Optional.empty();
	}

	@Override
	public Optional<IRecipeSlotDrawable> getRecipeSlotUnderMouse(double mouseX, double mouseY) {
		return Optional.empty();
	}

	@Override
	public Rect2i getRect() {
		return area.toMutable();
	}

	@Override
	public Rect2i getRectWithBorder() {
		return area.expandBy(borderPadding).toMutable();
	}

	@Override
	public Rect2i getRecipeTransferButtonArea() {
		return new Rect2i(0, 0, 0, 0);
	}

	@Override
	public Rect2i getRecipeBookmarkButtonArea() {
		return new Rect2i(0, 0, 0, 0);
	}

	@Override
	public IRecipeSlotsView getRecipeSlotsView() {
		return List::of;
	}

	@Override
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		return Optional.empty();
	}

	@Override
	public IRecipeCategory<R> getRecipeCategory() {
		return recipeCategory;
	}

	@Override
	public R getRecipe() {
		return recipe;
	}

	@Override
	public IJeiInputHandler getInputHandler() {
		return INPUT_HANDLER;
	}

	@Override
	public void tick() {

	}
}
