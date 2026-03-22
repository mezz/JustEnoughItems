package mezz.jei.library.plugins.debug;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.Internal;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ErrorRecipeCategory extends AbstractRecipeCategory<ErrorRecipe> {
	public static final IRecipeType<ErrorRecipe> TYPE = IRecipeType.create(ModIds.JEI_ID, "error", ErrorRecipe.class);

	public ErrorRecipeCategory() {
		super(
			TYPE,
			Component.literal("error"),
			Internal.getTextures().getConfigButtonIcon(),
			160,
			60
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ErrorRecipe recipe, IFocusGroup focuses) {
		if (recipe.getType().equals(ErrorRecipe.CrashType.SetRecipe)) {
			throw new RuntimeException("JEI ErrorRecipe is intentionally crashing for testing purposes");
		}
	}

	@Override
	public void draw(ErrorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
		if (recipe.getType().equals(ErrorRecipe.CrashType.Draw)) {
			throw new RuntimeException("JEI ErrorRecipe is intentionally crashing for testing purposes");
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, ErrorRecipe recipe, IFocusGroup focuses) {
		if (recipe.getType().equals(ErrorRecipe.CrashType.CreateRecipeExtras)) {
			throw new RuntimeException("JEI ErrorRecipe is intentionally crashing for testing purposes");
		}
	}

	@Override
	public void onDisplayedIngredientsUpdate(ErrorRecipe recipe, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
		if (recipe.getType().equals(ErrorRecipe.CrashType.OnDisplayedIngredientsUpdate)) {
			throw new RuntimeException("JEI ErrorRecipe is intentionally crashing for testing purposes");
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, ErrorRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (recipe.getType().equals(ErrorRecipe.CrashType.GetTooltip)) {
			throw new RuntimeException("JEI ErrorRecipe is intentionally crashing for testing purposes");
		}
	}

	@Override
	public Identifier getIdentifier(ErrorRecipe recipe) {
		ErrorRecipe.CrashType type = recipe.getType();
		return Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "error." + type.name().toLowerCase());
	}
}
