package mezz.jei.gui.overlay.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;
import mezz.jei.common.config.BookmarkTooltipFeature;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.overlay.bookmarks.IngredientsTooltipComponent;
import mezz.jei.gui.overlay.bookmarks.PreviewTooltipComponent;
import mezz.jei.gui.overlay.ingredients.IngredientGridTooltipHelper;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

public class RecipeBookmarkElement<T, R> implements IElement<R> {
	private final RecipeBookmark<T, R> recipeBookmark;
	private final IDrawable icon;
	private final IClientConfig clientConfig;
	private final EnumMap<BookmarkTooltipFeature, ClientTooltipComponent> cache = new EnumMap<>(BookmarkTooltipFeature.class);

	public RecipeBookmarkElement(RecipeBookmark<T, R> recipeBookmark, IDrawable icon) {
		this.recipeBookmark = recipeBookmark;
		this.icon = icon;
		this.clientConfig = Internal.getJeiClientConfigs().getClientConfig();
	}

	@Override
	public ITypedIngredient<R> getTypedIngredient() {
		return recipeBookmark.getRecipeOutput();
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.of(recipeBookmark);
	}

	@Override
	public @Nullable IDrawable createRenderOverlay() {
		return new RecipeBookmarkOverlay(icon);
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		// ignore roles, always display the bookmarked recipe if it's clicked

		IRecipeCategory<T> recipeCategory = recipeBookmark.getRecipeCategory();
		T recipe = recipeBookmark.getRecipe();
		ITypedIngredient<?> ingredient = getTypedIngredient();
		List<IFocus<?>> focuses = focusUtil.createFocuses(ingredient, List.of(RecipeIngredientRole.OUTPUT));
		recipesGui.showRecipes(recipeCategory, List.of(recipe), focuses);
	}

	@Override
	public void getTooltip(JeiTooltip tooltip, IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<R> ingredientRenderer, IIngredientHelper<R> ingredientHelper) {
		ITypedIngredient<R> recipeOutput = recipeBookmark.getRecipeOutput();
		T recipe = recipeBookmark.getRecipe();
		IRecipeCategory<T> recipeCategory = recipeBookmark.getRecipeCategory();

		tooltip.add(Component.translatable("jei.tooltip.bookmarks.recipe", recipeCategory.getTitle()));
		addBookmarkTooltipFeaturesIfEnabled(tooltip);

		IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
		IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
		IModIdHelper modIdHelper = jeiRuntime.getJeiHelpers().getModIdHelper();

		ResourceLocation recipeName = recipeCategory.getRegistryName(recipe);
		if (recipeName != null) {
			String recipeModId = recipeName.getNamespace();
			ResourceLocation ingredientName = ingredientHelper.getResourceLocation(recipeOutput.getIngredient());
			String ingredientModId = ingredientName.getNamespace();
			if (!recipeModId.equals(ingredientModId)) {
				String modName = modIdHelper.getFormattedModNameForModId(recipeModId);
				MutableComponent recipeBy = Component.translatable("jei.tooltip.recipe.by", modName);
				tooltip.add(recipeBy.withStyle(ChatFormatting.GRAY));
			}
		}

		tooltip.add(Component.empty());
		SafeIngredientUtil.getTooltip(tooltip, ingredientManager, ingredientRenderer, recipeOutput);
	}

	private void addBookmarkTooltipFeaturesIfEnabled(JeiTooltip tooltip) {
		if (clientConfig.getBookmarkTooltipFeatures().isEmpty()) {
			return;
		}
		if (clientConfig.isHoldShiftToShowBookmarkTooltipFeaturesEnabled()) {
			IJeiKeyMappingInternal showBookmarkTooltipFeatures = Internal.getKeyMappings().getShowBookmarkTooltipFeatures();
			if (showBookmarkTooltipFeatures.isDown()) {
				addBookmarkTooltipFeatures(tooltip);
			} else {
				tooltip.addKeyUsageComponent(
					"jei.tooltip.bookmarks.tooltips.usage",
					showBookmarkTooltipFeatures
				);
			}
		} else {
			addBookmarkTooltipFeatures(tooltip);
		}
	}

	private void addBookmarkTooltipFeatures(JeiTooltip tooltip) {
		@Nullable IRecipeLayoutDrawable<T> layoutDrawable = null;

		for (BookmarkTooltipFeature feature : clientConfig.getBookmarkTooltipFeatures()) {
			ClientTooltipComponent component = cache.get(feature);
			if (component == null) {
				if (layoutDrawable == null) {
					layoutDrawable = createRecipeLayoutDrawable().orElse(null);
					if (layoutDrawable == null) {
						break;
					}
				}
				component = createComponent(feature, layoutDrawable);
				cache.put(feature, component);
			}
			tooltip.addClientTooltipComponent(component);
		}
	}

	private ClientTooltipComponent createComponent(BookmarkTooltipFeature feature, IRecipeLayoutDrawable<T> layoutDrawable) {
		return switch (feature) {
			case PREVIEW -> new PreviewTooltipComponent<>(layoutDrawable);
			case INGREDIENTS -> new IngredientsTooltipComponent(layoutDrawable);
		};
	}

	private Optional<IRecipeLayoutDrawable<T>> createRecipeLayoutDrawable() {
		IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
		IRecipeManager recipeManager = jeiRuntime.getRecipeManager();
		IFocusFactory focusFactory = jeiRuntime.getJeiHelpers().getFocusFactory();
		IScalableDrawable recipePreviewBackground = Internal.getTextures().getRecipePreviewBackground();

		return recipeManager.createRecipeLayoutDrawable(
			recipeBookmark.getRecipeCategory(),
			recipeBookmark.getRecipe(),
			focusFactory.getEmptyFocusGroup(),
			recipePreviewBackground,
			4
		);
	}

	@Override
	public boolean isVisible() {
		return recipeBookmark.isVisible();
	}

	private record RecipeBookmarkOverlay(IDrawable icon) implements IDrawable {
		@Override
		public int getWidth() {
			return 16;
		}

		@Override
		public int getHeight() {
			return 16;
		}

		@Override
		public void draw(PoseStack poseStack, int xOffset, int yOffset) {
			poseStack.pushPose();
			{
				// this z level seems to be the sweet spot so that
				// 2D icons draw above the items, and
				// 3D icons draw still draw under tooltips.
				poseStack.translate(xOffset + 8, yOffset + 8, 200);
				poseStack.scale(0.5f, 0.5f, 0.5f);
				icon.draw(poseStack);
			}
			poseStack.popPose();
		}
	}
}
