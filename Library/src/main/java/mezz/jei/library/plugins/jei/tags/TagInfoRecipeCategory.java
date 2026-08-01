package mezz.jei.library.plugins.jei.tags;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class TagInfoRecipeCategory<R extends ITagInfoRecipe, T extends IRecipeType<R>> extends AbstractRecipeCategory<R> {
	private static final int WIDTH = 142;
	private static final int HEIGHT = 110;

	public TagInfoRecipeCategory(IGuiHelper guiHelper, T recipeType, Identifier id) {
		super(
			recipeType,
			createTitle(id),
			guiHelper.createDrawableItemLike(Items.NAME_TAG),
			WIDTH,
			HEIGHT
		);
	}

	private static Component createTitle(Identifier id) {
		String registryName = ResourceLocationUtil.sanitizePath(id.getPath());
		String registryNameTranslationKey = "gui.jei.category.tagInformation." + registryName;

		Language language = Language.getInstance();
		if (language.has(registryNameTranslationKey)) {
			return Component.translatable(registryNameTranslationKey);
		}

		return Component.translatable("gui.jei.category.tagInformation", StringUtils.capitalize(id.getPath()));
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, R recipe, IFocusGroup focuses) {
		builder.addInputSlot()
			.addTypedIngredients(recipe.getTypedIngredients())
			.setStandardSlotBackground();

		for (ITypedIngredient<?> stack : recipe.getTypedIngredients()) {
			builder.addOutputSlot()
				.add(stack);
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, R recipe, IFocusGroup focuses) {
		TagKey<?> tag = recipe.getTag();

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		Component tagName = renderHelper.getName(tag);
		List<FormattedText> text = List.of(
			tagName,
			Component.literal(tag.location().toString())
				.withStyle(style -> style.withColor(TextColor.fromRgb(JeiGuiColors.getColor(GuiColor.TAG_INFORMATION_IDENTIFIER_TEXT) & 0xFFFFFF)))
		);
		builder.addText(text, getWidth() - 22, 20)
			.setPosition(22, 0)
			.setColor(JeiGuiColors.getColor(GuiColor.TAG_INFORMATION_TEXT))
			.setLineSpacing(0)
			.setTextAlignment(VerticalAlignment.CENTER)
			.setTextAlignment(HorizontalAlignment.CENTER);

		IRecipeSlotDrawablesView recipeSlots = builder.getRecipeSlots();
		List<IRecipeSlotDrawable> outputSlots = recipeSlots.getSlots(RecipeIngredientRole.OUTPUT);

		IScrollGridWidget scrollGridWidget = builder.addScrollGridWidget(outputSlots, 7, 5);
		scrollGridWidget.setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);

		IRecipeSlotDrawable inputSlot = recipeSlots.getSlots(RecipeIngredientRole.INPUT)
			.getFirst();
		inputSlot.setPosition(scrollGridWidget.getScreenRectangle().position().x() + 1, 1);
	}

	@Override
	public Identifier getIdentifier(R recipe) {
		return recipe.getTag().location();
	}
}
