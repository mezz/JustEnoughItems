package mezz.jei.library.plugins.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ErrorRecipeCategory implements IRecipeCategory<ErrorRecipe> {
	public static final RecipeType<ErrorRecipe> TYPE = RecipeType.create(ModIds.JEI_ID, "error", ErrorRecipe.class);

	private final IDrawable background;

	public ErrorRecipeCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createBlankDrawable(160, 60);
	}

	@Override
	public RecipeType<ErrorRecipe> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return Component.literal("error");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return Internal.getTextures().getConfigButtonIcon();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ErrorRecipe recipe, IFocusGroup focuses) {
		if (recipe.getType().equals(ErrorRecipe.CrashType.SetRecipe)) {
			throw new RuntimeException("JEI ErrorRecipe is intentionally crashing for testing purposes");
		}
	}

	@Override
	public void draw(ErrorRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		if (recipe.getType().equals(ErrorRecipe.CrashType.Draw)) {
			throw new RuntimeException("JEI ErrorRecipe is intentionally crashing for testing purposes");
		}
	}

	@Override
	public List<Component> getTooltipStrings(ErrorRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (recipe.getType().equals(ErrorRecipe.CrashType.GetTooltip)) {
			throw new RuntimeException("JEI ErrorRecipe is intentionally crashing for testing purposes");
		}
		return List.of();
	}

	@Override
	public @Nullable ResourceLocation getRegistryName(ErrorRecipe recipe) {
		ErrorRecipe.CrashType type = recipe.getType();
		return new ResourceLocation(ModIds.JEI_ID, "error." + type.name().toLowerCase(Locale.ROOT));
	}
}
