package mezz.jei.gui.overlay.elements;

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
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.overlay.bookmarks.IngredientsTooltipComponent;
import mezz.jei.gui.overlay.bookmarks.PreviewTooltipComponent;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

public class RecipeBookmarkElement<R, I> implements IElement<I> {
	private final RecipeBookmark<R, I> recipeBookmark;
	private final IDrawable icon;
	private final IClientConfig clientConfig;
	private final EnumMap<BookmarkTooltipFeature, ClientTooltipComponent> cache = new EnumMap<>(BookmarkTooltipFeature.class);

	public RecipeBookmarkElement(RecipeBookmark<R, I> recipeBookmark, IDrawable icon) {
		this.recipeBookmark = recipeBookmark;
		this.icon = icon;
		this.clientConfig = Internal.getJeiClientConfigs().getClientConfig();
	}

	@Override
	public ITypedIngredient<I> getTypedIngredient() {
		return recipeBookmark.getRecipeOutput();
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.of(recipeBookmark);
	}

	@Override
	public void renderExtras(GuiGraphics guiGraphics) {
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			// this z level seems to be the sweet spot so that
			// 2D icons draw above the items, and
			// 3D icons draw still draw under tooltips.
			poseStack.translate(8, 8, 200);
			poseStack.scale(0.5f, 0.5f, 0.5f);
			icon.draw(guiGraphics);
		}
		poseStack.popPose();
	}

	@Override
	public void show(IRecipesGui recipesGui, FocusUtil focusUtil, List<RecipeIngredientRole> roles) {
		// ignore roles, always display the bookmarked recipe if it's clicked

		IRecipeCategory<R> recipeCategory = recipeBookmark.getRecipeCategory();
		R recipe = recipeBookmark.getRecipe();
		ITypedIngredient<?> ingredient = getTypedIngredient();
		List<IFocus<?>> focuses = focusUtil.createFocuses(ingredient, List.of(RecipeIngredientRole.OUTPUT));
		recipesGui.showRecipes(recipeCategory, List.of(recipe), focuses);
	}

	@Override
	public List<ClientTooltipComponent> getTooltipComponents(IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<I> ingredientRenderer, IIngredientHelper<I> ingredientHelper) {
		List<ClientTooltipComponent> components = new ArrayList<>();

		ITypedIngredient<I> recipeOutput = recipeBookmark.getRecipeOutput();
		R recipe = recipeBookmark.getRecipe();

		IRecipeCategory<R> recipeCategory = recipeBookmark.getRecipeCategory();
		addTooltipComponent(components, Component.translatable("jei.tooltip.bookmarks.recipe", recipeCategory.getTitle()));

		addBookmarkTooltipFeaturesIfEnabled(components);

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
				addTooltipComponent(components, recipeBy.withStyle(ChatFormatting.GRAY));
			}
		}

		addTooltipComponent(components, Component.empty());

		List<Component> outputTooltip = SafeIngredientUtil.getTooltip(ingredientManager, ingredientRenderer, recipeOutput);

		addAllTooltipComponents(components, outputTooltip);
		List<Component> modIdTooltip = new ArrayList<>();
		modIdTooltip = modIdHelper.addModNameToIngredientTooltip(modIdTooltip, recipeOutput.getIngredient(), ingredientHelper);
		addAllTooltipComponents(components, modIdTooltip);

		return components;
	}

	private void addAllTooltipComponents(List<ClientTooltipComponent> tooltipComponents, List<Component> components) {
		for (Component component : components) {
			addTooltipComponent(tooltipComponents, component);
		}
	}

	private void addTooltipComponent(List<ClientTooltipComponent> tooltipComponents, Component component) {
		ClientTooltipComponent clientTooltipComponent = ClientTooltipComponent.create(component.getVisualOrderText());
		tooltipComponents.add(clientTooltipComponent);
	}

	private void addBookmarkTooltipFeaturesIfEnabled(List<ClientTooltipComponent> components) {
		if (clientConfig.getBookmarkTooltipFeatures().isEmpty()) {
			return;
		}
		if (clientConfig.isHoldShiftToShowBookmarkTooltipFeaturesEnabled()) {
			if (Screen.hasShiftDown()) {
				addBookmarkTooltipFeatures(components);
			} else {
				Component shiftKey = Component.keybind("jei.key.shift")
					.withStyle(ChatFormatting.BOLD);
				Component component = Component.translatable("jei.tooltip.bookmarks.tooltips.usage", shiftKey)
					.withStyle(ChatFormatting.ITALIC)
					.withStyle(ChatFormatting.GRAY);
				ClientTooltipComponent tooltipComponent = ClientTooltipComponent.create(component.getVisualOrderText());
				components.add(tooltipComponent);
			}
		} else {
			addBookmarkTooltipFeatures(components);
		}
	}

	private void addBookmarkTooltipFeatures(List<ClientTooltipComponent> components) {
		@Nullable IRecipeLayoutDrawable<R> layoutDrawable = null;

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
			components.add(component);
		}
	}

	private ClientTooltipComponent createComponent(BookmarkTooltipFeature feature, IRecipeLayoutDrawable<R> layoutDrawable) {
		return switch (feature) {
			case PREVIEW -> new PreviewTooltipComponent<>(layoutDrawable);
			case INGREDIENTS -> new IngredientsTooltipComponent(layoutDrawable);
		};
	}

	private Optional<IRecipeLayoutDrawable<R>> createRecipeLayoutDrawable() {
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
}
