package mezz.jei.library.plugins.vanilla;

import mezz.jei.api.ingredients.ISlotDisplayInterpretationBuilder;
import mezz.jei.api.ingredients.ISlotDisplayInterpreter;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

final class VanillaSlotDisplayInterpreters {
	private VanillaSlotDisplayInterpreters() {
	}

	static void interpretItem(
		SlotDisplay.ItemSlotDisplay ignoredSlotDisplay,
		ISlotDisplayInterpreter.IContext<ItemStack> context,
		ISlotDisplayInterpretationBuilder interpretationBuilder
	) {
		List<ITypedIngredient<ItemStack>> ingredients = context.getIngredients();
		if (ingredients.isEmpty()) {
			return;
		}
		ItemStack itemStack = ingredients.getFirst().getIngredient();
		if (context.getRole() == RecipeIngredientRole.INPUT && context.getIngredientHelper().hasSubtypes(itemStack)) {
			interpretationBuilder.setMatchesAllSubtypes(true);
			interpretationBuilder.setTooltipHeader(createAnyItemTooltipHeader(itemStack));
		}
	}

	static void interpretTag(
		SlotDisplay.TagSlotDisplay slotDisplay,
		ISlotDisplayInterpreter.IContext<ItemStack> context,
		ISlotDisplayInterpretationBuilder interpretationBuilder
	) {
		interpretationBuilder.setTagKey(slotDisplay.tag());
		List<ITypedIngredient<ItemStack>> ingredients = context.getIngredients();
		boolean hasSubtypes = ingredients.stream()
			.map(ITypedIngredient::getIngredient)
			.anyMatch(context.getIngredientHelper()::hasSubtypes);
		if (context.getRole() == RecipeIngredientRole.INPUT && hasSubtypes) {
			interpretationBuilder.setMatchesAllSubtypes(true);
		}
	}

	static void interpretAnyFuel(
		SlotDisplay.AnyFuel ignoredSlotDisplay,
		ISlotDisplayInterpreter.IContext<ItemStack> context,
		ISlotDisplayInterpretationBuilder interpretationBuilder
	) {
		interpretationBuilder.setTooltipHeader(createAnyFuelTooltipHeader());
		List<ITypedIngredient<ItemStack>> ingredients = context.getIngredients();
		boolean hasSubtypes = ingredients.stream()
			.map(ITypedIngredient::getIngredient)
			.anyMatch(context.getIngredientHelper()::hasSubtypes);
		if (context.getRole() == RecipeIngredientRole.INPUT && hasSubtypes) {
			interpretationBuilder.setMatchesAllSubtypes(true);
		}
	}

	static void interpretWithRemainder(
		SlotDisplay.WithRemainder slotDisplay,
		ISlotDisplayInterpreter.IContext<ItemStack> ignoredContext,
		ISlotDisplayInterpretationBuilder interpretationBuilder
	) {
		interpretationBuilder.setWrappedDisplay(slotDisplay.input());
	}

	static void interpretComposite(
		SlotDisplay.Composite slotDisplay,
		ISlotDisplayInterpretationBuilder interpretationBuilder
	) {
		interpretationBuilder.setChildDisplays(slotDisplay.contents());
	}

	private static Component createAnyItemTooltipHeader(ItemStack itemStack) {
		Component itemName = Component.translatable(itemStack.getItem().getDescriptionId());
		return Component.translatable("jei.tooltip.recipe.any", itemName)
			.withStyle(ChatFormatting.GOLD)
			.withStyle(ChatFormatting.ITALIC);
	}

	private static Component createAnyFuelTooltipHeader() {
		return Component.translatable("jei.tooltip.recipe.any_fuel")
			.withStyle(ChatFormatting.GOLD)
			.withStyle(ChatFormatting.ITALIC);
	}
}
