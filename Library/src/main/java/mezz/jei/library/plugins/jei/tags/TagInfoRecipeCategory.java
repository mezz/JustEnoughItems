package mezz.jei.library.plugins.jei.tags;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollGridWidgetFactory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class TagInfoRecipeCategory<R extends ITagInfoRecipe, T extends RecipeType<R>> extends AbstractRecipeCategory<R> {
	private static final int WIDTH = 142;
	private static final int HEIGHT = 110;

	private final IScrollGridWidgetFactory<?> scrollGridFactory;

	public TagInfoRecipeCategory(IGuiHelper guiHelper, T recipeType, ResourceLocation registryLocation) {
		super(
			recipeType,
			createTitle(registryLocation),
			guiHelper.createDrawableItemLike(Items.NAME_TAG),
			WIDTH,
			HEIGHT
		);

		this.scrollGridFactory = guiHelper.createScrollGridFactory(7, 5);
		ScreenRectangle gridArea = this.scrollGridFactory.getArea();
		this.scrollGridFactory.setPosition(
			(WIDTH - gridArea.width()) / 2,
			20
		);
	}

	private static Component createTitle(ResourceLocation registryLocation) {
		Component registryName = Component.translatableWithFallback(
			"gui.jei.category.registry." + ResourceLocationUtil.sanitizePath(registryLocation.getPath()),
			StringUtils.capitalize(registryLocation.getPath())
		);
		return Component.translatable("gui.jei.category.tagInformation", registryName);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, R recipe, IFocusGroup focuses) {
		ScreenRectangle gridArea = scrollGridFactory.getArea();
		builder.addInputSlot(gridArea.position().x() + 1, 1)
			.addTypedIngredients(recipe.getTypedIngredients())
			.setStandardSlotBackground();

		for (ITypedIngredient<?> stack : recipe.getTypedIngredients()) {
			builder.addSlotToWidget(RecipeIngredientRole.OUTPUT, scrollGridFactory)
				.addTypedIngredient(stack);
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, R recipe, IRecipeSlotsView recipeSlotsView, IFocusGroup focuses) {
		TagKey<?> tag = recipe.getTag();

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		Component tagName = renderHelper.getName(tag);
		List<FormattedText> text = List.of(
			tagName,
			Component.literal(tag.location().toString()).withStyle(ChatFormatting.GRAY)
		);
		builder.addText(text, 22, 0, getWidth() - 22, 20)
			.setColor(0xFF505050)
			.setLineSpacing(0)
			.alignVerticalCenter()
			.alignHorizontalCenter();
	}

	@Override
	public ResourceLocation getRegistryName(R recipe) {
		return recipe.getTag().location();
	}
}
