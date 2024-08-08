package mezz.jei.library.plugins.jei.info;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IngredientInfoRecipeCategory implements IRecipeCategory<IJeiIngredientInfoRecipe> {
	private static final int recipeWidth = 170;
	private static final int recipeHeight = 125;
	private static final int lineSpacing = 2;

	private final IDrawable background;
	private final IGuiHelper guiHelper;
	private final IDrawable icon;
	private final IDrawable slotBackground;
	private final Component localizedName;

	public IngredientInfoRecipeCategory(IGuiHelper guiHelper, Textures textures) {
		this.background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		this.guiHelper = guiHelper;
		this.icon = textures.getInfoIcon();
		this.slotBackground = guiHelper.getSlotDrawable();
		this.localizedName = Component.translatable("gui.jei.category.itemInformation");
	}

	@Override
	public RecipeType<IJeiIngredientInfoRecipe> getRecipeType() {
		return RecipeTypes.INFORMATION;
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
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJeiIngredientInfoRecipe recipe, IFocusGroup focuses) {
		int yPos = slotBackground.getHeight() + 4;
		int height = recipeHeight - yPos;
		int width = recipeWidth - guiHelper.getScrollBoxScrollbarExtraWidth();
		IScrollBoxWidget scrollBoxWidget = guiHelper.createScrollBoxWidget(
			new Contents(recipe, width),
			height,
			0,
			yPos
		);
		builder.addWidget(scrollBoxWidget);
		builder.addInputHandler(scrollBoxWidget);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJeiIngredientInfoRecipe recipe, IFocusGroup focuses) {
		int xPos = (recipeWidth - 16) / 2;

		IRecipeSlotBuilder inputSlotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, xPos, 1)
			.setBackground(slotBackground, -1, -1);

		IIngredientAcceptor<?> outputSlotBuilder = builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT);

		for (ITypedIngredient<?> typedIngredient : recipe.getIngredients()) {
			addIngredient(typedIngredient, inputSlotBuilder);
			addIngredient(typedIngredient, outputSlotBuilder);
		}
	}

	@Override
	public @Nullable ResourceLocation getRegistryName(IJeiIngredientInfoRecipe recipe) {
		return null;
	}

	private static <T> void addIngredient(ITypedIngredient<T> typedIngredient, IIngredientAcceptor<?> slotBuilder) {
		slotBuilder.addIngredient(typedIngredient.getType(), typedIngredient.getIngredient());
	}

	private static class Contents implements IDrawable {
		private final List<FormattedText> descriptionLines;
		private final int lineHeight;
		private final int width;
		private final int height;

		public Contents(IJeiIngredientInfoRecipe recipe, int width) {
			Minecraft minecraft = Minecraft.getInstance();
			this.lineHeight = minecraft.font.lineHeight + lineSpacing;
			this.descriptionLines = StringUtil.splitLines(recipe.getDescription(), width);
			this.width = width;
			this.height = lineHeight * descriptionLines.size() - lineSpacing;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
			Language language = Language.getInstance();
			Minecraft minecraft = Minecraft.getInstance();
			Font font = minecraft.font;

			int yPos = 0;
			for (FormattedText descriptionLine : descriptionLines) {
				FormattedCharSequence charSequence = language.getVisualOrder(descriptionLine);
				guiGraphics.drawString(font, charSequence, 0, yPos, 0xFF000000, false);
				yPos += lineHeight;
			}
		}
	}
}
