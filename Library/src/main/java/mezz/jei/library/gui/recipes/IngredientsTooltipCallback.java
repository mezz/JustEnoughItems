package mezz.jei.library.gui.recipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.IngredientsTooltipComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class IngredientsTooltipCallback implements IRecipeSlotRichTooltipCallback {

	private final Supplier<@Nullable IRecipeLayoutDrawable<?>> recipeLayoutSupplier;

	public IngredientsTooltipCallback(Supplier<@Nullable IRecipeLayoutDrawable<?>> supplier) {
		this.recipeLayoutSupplier = supplier;
	}

	@Override
	public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
		if (Internal.getJeiClientConfigs().getClientConfig().isIngredientsSummaryEnabled()) {
			IRecipeLayoutDrawable<?> recipeLayout = recipeLayoutSupplier.get();
			if (recipeLayout != null) {
				tooltip.add(Component.translatable("jei.tooltip.recipe.tooltips.craft.ingredients").withStyle(ChatFormatting.GRAY));
				tooltip.add(new IngredientsTooltipComponent(recipeLayout));
			}
		}
	}
}
