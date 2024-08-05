package mezz.jei.library.plugins.jei.tags;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IScrollGridWidgetFactory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class TagInfoRecipeCategory<R extends ITagInfoRecipe, T extends RecipeType<R>> implements IRecipeCategory<R> {
	private static final int WIDTH = 142;
	private static final int HEIGHT = 110;

	private final IDrawable background;
	private final IDrawable icon;
	private final T recipeType;
	private final Component localizedName;
	private final ImmutableRect2i nameArea;
	private final IScrollGridWidgetFactory<?> scrollGridFactory;
	private final IDrawableStatic slotDrawable;

	public TagInfoRecipeCategory(IGuiHelper guiHelper, T recipeType, ResourceLocation registryLocation) {
		this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
		this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.NAME_TAG));
		this.slotDrawable = guiHelper.getSlotDrawable();
		this.recipeType = recipeType;
		String registryName = StringUtils.capitalize(registryLocation.getPath());
		this.localizedName = Component.translatable("gui.jei.category.tagInformation", registryName);
		int titleHeight = Minecraft.getInstance().font.lineHeight;
		this.nameArea = new ImmutableRect2i(22, 5, WIDTH - 22, titleHeight);

		this.scrollGridFactory = guiHelper.createScrollGridFactory(7, 5);
		ScreenRectangle gridArea = this.scrollGridFactory.getArea();
		this.scrollGridFactory.setPosition(
			(WIDTH - gridArea.width()) / 2,
			20
		);
	}

	@Override
	public T getRecipeType() {
		return recipeType;
	}

	@Override
	public Component getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, R recipe, IFocusGroup focuses) {
		ScreenRectangle gridArea = scrollGridFactory.getArea();
		builder.addSlot(RecipeIngredientRole.INPUT, gridArea.position().x() + 1, 1)
			.addTypedIngredients(recipe.getTypedIngredients())
			.setBackground(slotDrawable, -1, -1);

		for (ITypedIngredient<?> stack : recipe.getTypedIngredients()) {
			builder.addSlotToWidget(RecipeIngredientRole.OUTPUT, scrollGridFactory)
				.addTypedIngredient(stack);
		}
	}

	@Override
	public void draw(R recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;

		TagKey<?> tag = recipe.getTag();

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		Component tagName = renderHelper.getName(tag);

		if (font.width(tagName) > this.nameArea.width()) {
			tagName = StringUtil.truncateStringToWidth(tagName, this.nameArea.width(), font);
		}

		ImmutableRect2i nameArea = MathUtil.centerTextArea(this.nameArea, font, tagName);
		guiGraphics.drawString(font, tagName, nameArea.x(), nameArea.getY(), 0xFF505050, false);
	}

	@SuppressWarnings({"removal"})
	@Override
	public List<Component> getTooltipStrings(R recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (nameArea.contains(mouseX, mouseY)) {
			TagKey<?> tag = recipe.getTag();
			IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
			Component tagName = renderHelper.getName(tag);
			return List.of(
				tagName,
				Component.literal(tag.location().toString()).withStyle(ChatFormatting.GRAY)
			);
		}
		return List.of();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, R recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (!nameArea.contains(mouseX, mouseY)) {
			return;
		}
		TagKey<?> tag = recipe.getTag();
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		Component tagName = renderHelper.getName(tag);
		tooltip.add(tagName);
		tooltip.add(Component.literal(tag.location().toString()).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public ResourceLocation getRegistryName(R recipe) {
		return recipe.getTag().location();
	}
}
