package mezz.jei.gui.overlay.elements;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;
import mezz.jei.common.config.BookmarkTooltipFeature;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.IngredientGridTooltipHelper;
import mezz.jei.gui.overlay.bookmarks.IngredientsTooltipComponent;
import mezz.jei.gui.overlay.bookmarks.PreviewTooltipComponent;
import mezz.jei.gui.recipes.RecipeCategoryIconUtil;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

public class RecipeBookmarkElement<R, I> implements IElement<I> {
	private final RecipeBookmark<R, I> recipeBookmark;
	private final IClientConfig clientConfig;
	private final EnumMap<BookmarkTooltipFeature, TooltipComponent> cache = new EnumMap<>(BookmarkTooltipFeature.class);
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private @Nullable Optional<IRecipeLayoutDrawable<R>> cachedLayoutDrawable;

	public RecipeBookmarkElement(RecipeBookmark<R, I> recipeBookmark) {
		this.recipeBookmark = recipeBookmark;
		this.clientConfig = Internal.getJeiClientConfigs().getClientConfig();
	}

	@Override
	public ITypedIngredient<I> getTypedIngredient() {
		return recipeBookmark.getDisplayIngredient();
	}

	@Override
	public Optional<IBookmark> getBookmark() {
		return Optional.of(recipeBookmark);
	}

	@Override
	public IDrawable createRenderOverlay() {
		IRecipeCategory<R> recipeCategory = recipeBookmark.getRecipeCategory();
		return new RecipeBookmarkIcon(recipeCategory);
	}

	@Override
	public boolean handleClick(UserInput input, IInternalKeyMappings keyBindings) {
		boolean transferOnce = input.is(keyBindings.getTransferRecipeBookmark());
		boolean transferMax = input.is(keyBindings.getMaxTransferRecipeBookmark());
		if (transferOnce || transferMax) {
			Minecraft minecraft = Minecraft.getInstance();
			Screen screen = minecraft.screen;
			Player player = minecraft.player;
			if (player != null && screen instanceof AbstractContainerScreen<?> containerScreen) {
				IRecipeLayoutDrawable<R> recipeLayout = getRecipeLayoutDrawable().orElse(null);
				if (recipeLayout == null) {
					return false;
				}

				IRecipeTransferManager recipeTransferManager = Internal.getJeiRuntime().getRecipeTransferManager();
				AbstractContainerMenu container = containerScreen.getMenu();
				if (input.isSimulate()) {
					IRecipeTransferError recipeTransferError = RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, container, recipeLayout, player).orElse(null);
					return recipeTransferError == null || recipeTransferError.getType().allowsTransfer;
				} else {
					return RecipeTransferUtil.transferRecipe(recipeTransferManager, container, recipeLayout, player, transferMax);
				}
			}
		}
		return false;
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
	public void getTooltip(JeiTooltip tooltip, IngredientGridTooltipHelper tooltipHelper, IIngredientRenderer<I> ingredientRenderer, IIngredientHelper<I> ingredientHelper) {
		ITypedIngredient<I> displayIngredient = recipeBookmark.getDisplayIngredient();
		R recipe = recipeBookmark.getRecipe();

		IRecipeCategory<R> recipeCategory = recipeBookmark.getRecipeCategory();
		tooltip.add(Component.translatable("jei.tooltip.bookmarks.recipe", recipeCategory.getTitle()));

		addBookmarkTooltipFeaturesIfEnabled(tooltip);

		if (recipeBookmark.isDisplayIsOutput()) {
			IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
			IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
			IModIdHelper modIdHelper = jeiRuntime.getJeiHelpers().getModIdHelper();

			ResourceLocation recipeName = recipeCategory.getRegistryName(recipe);
			if (recipeName != null) {
				String recipeModId = recipeName.getNamespace();
				ResourceLocation ingredientName = ingredientHelper.getResourceLocation(displayIngredient.getIngredient());
				String ingredientModId = ingredientName.getNamespace();
				if (!recipeModId.equals(ingredientModId)) {
					String modName = modIdHelper.getFormattedModNameForModId(recipeModId);
					MutableComponent recipeBy = Component.translatable("jei.tooltip.recipe.by", modName);
					tooltip.add(recipeBy.withStyle(ChatFormatting.GRAY));
				}
			}

			tooltip.add(Component.empty());

			SafeIngredientUtil.getTooltip(tooltip, ingredientManager, ingredientRenderer, displayIngredient);
		}
	}

	private void addBookmarkTooltipFeaturesIfEnabled(JeiTooltip tooltip) {
		JeiTooltip transferComponents = createTransferComponents();

		if (clientConfig.getBookmarkTooltipFeatures().isEmpty() && transferComponents.isEmpty()) {
			return;
		}

		if (clientConfig.isHoldShiftToShowBookmarkTooltipFeaturesEnabled()) {
			if (Screen.hasShiftDown()) {
				addBookmarkTooltipFeatures(tooltip);
				tooltip.addAll(transferComponents);
			} else {
				tooltip.addKeyUsageComponent(
					"jei.tooltip.bookmarks.tooltips.usage",
					Component.keybind("jei.key.shift")
				);
			}
		} else {
			addBookmarkTooltipFeatures(tooltip);
			tooltip.addAll(transferComponents);
		}
	}

	private void addBookmarkTooltipFeatures(JeiTooltip tooltip) {
		for (BookmarkTooltipFeature feature : clientConfig.getBookmarkTooltipFeatures()) {
			TooltipComponent component = cache.get(feature);
			if (component == null) {
				IRecipeLayoutDrawable<R> recipeLayout = getRecipeLayoutDrawable().orElse(null);
				if (recipeLayout == null) {
					break;
				}

				component = switch (feature) {
					case PREVIEW -> new PreviewTooltipComponent<>(recipeLayout);
					case INGREDIENTS -> new IngredientsTooltipComponent(recipeLayout);
				};
				cache.put(feature, component);
			}
			tooltip.add(component);
		}
	}

	private JeiTooltip createTransferComponents() {
		JeiTooltip results = new JeiTooltip();

		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		Player player = minecraft.player;
		if (player != null && screen instanceof AbstractContainerScreen<?> containerScreen) {
			IRecipeTransferError recipeTransferError = getRecipeLayoutDrawable()
				.flatMap(recipeLayout -> {
					IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
					IRecipeTransferManager recipeTransferManager = jeiRuntime.getRecipeTransferManager();
					AbstractContainerMenu container = containerScreen.getMenu();
					return RecipeTransferUtil.getTransferRecipeError(recipeTransferManager, container, recipeLayout, player);
				})
				.orElse(null);

			if (recipeTransferError == null || recipeTransferError.getType().allowsTransfer) {
				IInternalKeyMappings keyMappings = Internal.getKeyMappings();
				IJeiKeyMapping transferRecipeBookmark = keyMappings.getTransferRecipeBookmark();
				if (!transferRecipeBookmark.isUnbound()) {
					results.addKeyUsageComponent(
						"jei.tooltip.bookmarks.tooltips.transfer.usage",
						transferRecipeBookmark
					);
				}

				IJeiKeyMapping maxTransferRecipeBookmark = keyMappings.getMaxTransferRecipeBookmark();
				if (!maxTransferRecipeBookmark.isUnbound()) {
					results.addKeyUsageComponent(
						"jei.tooltip.bookmarks.tooltips.transfer.max.usage",
						maxTransferRecipeBookmark
					);
				}
			}
		}
		return results;
	}

	private Optional<IRecipeLayoutDrawable<R>> getRecipeLayoutDrawable() {
		//noinspection OptionalAssignedToNull
		if (cachedLayoutDrawable == null) {
			IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
			IRecipeManager recipeManager = jeiRuntime.getRecipeManager();
			IFocusFactory focusFactory = jeiRuntime.getJeiHelpers().getFocusFactory();
			IScalableDrawable recipePreviewBackground = Internal.getTextures().getRecipePreviewBackground();

			cachedLayoutDrawable = recipeManager.createRecipeLayoutDrawable(
				recipeBookmark.getRecipeCategory(),
				recipeBookmark.getRecipe(),
				focusFactory.getEmptyFocusGroup(),
				recipePreviewBackground,
				4
			);
		}
		return cachedLayoutDrawable;
	}

	@Override
	public boolean isVisible() {
		return recipeBookmark.isVisible();
	}

	private static class RecipeBookmarkIcon implements IDrawable {
		private final IDrawable icon;

		public RecipeBookmarkIcon(IRecipeCategory<?> recipeCategory) {
			IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
			IRecipeManager recipeManager = jeiRuntime.getRecipeManager();
			IJeiHelpers jeiHelpers = jeiRuntime.getJeiHelpers();
			IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
			icon = RecipeCategoryIconUtil.create(
				recipeCategory,
				recipeManager,
				guiHelper
			);
		}

		@Override
		public int getWidth() {
			return 16;
		}

		@Override
		public int getHeight() {
			return 16;
		}

		@Override
		public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
			var poseStack = guiGraphics.pose();
			poseStack.pushPose();
			{
				// this z level seems to be the sweet spot so that
				// 2D icons draw above the items, and
				// 3D icons draw still draw under tooltips.
				poseStack.translate(8 + xOffset, 8 + yOffset, 200);
				poseStack.scale(0.5f, 0.5f, 0.5f);
				icon.draw(guiGraphics);
			}
			poseStack.popPose();
		}
	}
}
