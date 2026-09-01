package mezz.jei.gui.overlay.elements;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.Internal;
import mezz.jei.common.config.BookmarkTooltipFeature;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.RecipeBookmark;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.overlay.ingredients.IngredientGridTooltipHelper;
import mezz.jei.common.gui.IngredientsTooltipComponent;
import mezz.jei.gui.overlay.bookmarks.PreviewTooltipComponent;
import mezz.jei.gui.recipes.RecipeCategoryIconUtil;
import mezz.jei.gui.util.FocusUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RecipeBookmarkElement<R, I> implements IElement<I> {
	private final RecipeBookmark<R, I> recipeBookmark;
	private final IClientConfig clientConfig;
	private final RecipeTransferService recipeTransferService;
	private @Nullable PreviewTooltipComponent<R> previewTooltipComponent;
	private @Nullable IngredientsTooltipComponent ingredientsTooltipComponent;
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private @Nullable Optional<IRecipeLayoutDrawable<R>> cachedLayoutDrawable;

	public RecipeBookmarkElement(
		RecipeBookmark<R, I> recipeBookmark,
		RecipeTransferService recipeTransferService
	) {
		this.recipeBookmark = recipeBookmark;
		this.recipeTransferService = recipeTransferService;
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
			Screen screen = minecraft.gui.screen();
			Player player = minecraft.player;
			if (player != null && screen instanceof AbstractContainerScreen<?> containerScreen) {
				IRecipeLayoutDrawable<R> recipeLayout = getRecipeLayoutDrawable().orElse(null);
				if (recipeLayout == null) {
					return false;
				}

				if (input.isSimulate()) {
					IRecipeTransferError recipeTransferError = recipeTransferService.getTransferRecipeError(containerScreen, recipeLayout, player).orElse(null);
					return recipeTransferError == null || recipeTransferError.getType().allowsTransfer;
				} else {
					return recipeTransferService.transferRecipe(containerScreen, recipeLayout, player, transferMax);
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
		getTooltip(tooltip, ingredientRenderer, ingredientHelper, false);
	}

	public void getPinnedTooltip(JeiTooltip tooltip) {
		IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
		IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
		ITypedIngredient<I> displayIngredient = recipeBookmark.getDisplayIngredient();
		IIngredientType<I> ingredientType = displayIngredient.getType();
		IIngredientRenderer<I> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		IIngredientHelper<I> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		getTooltip(tooltip, ingredientRenderer, ingredientHelper, true);
	}

	public Optional<PreviewTooltipComponent<R>> getInteractivePreview() {
		IJeiKeyMappingInternal pauseRecipeCycling = Internal.getKeyMappings().getPauseRecipeCycling();
		if (pauseRecipeCycling.isUnbound() ||
			!getBookmarkTooltipFeatures().contains(BookmarkTooltipFeature.PREVIEW)
		) {
			return Optional.empty();
		}
		PreviewTooltipComponent<R> component = this.previewTooltipComponent;
		if (component == null) {
			component = createPreviewTooltipComponent();
			if (component == null) {
				return Optional.empty();
			}
			this.previewTooltipComponent = component;
		}
		return Optional.of(component);
	}

	private void getTooltip(JeiTooltip tooltip, IIngredientRenderer<I> ingredientRenderer, IIngredientHelper<I> ingredientHelper, boolean pinned) {
		ITypedIngredient<I> displayIngredient = recipeBookmark.getDisplayIngredient();
		R recipe = recipeBookmark.getRecipe();

		IRecipeCategory<R> recipeCategory = recipeBookmark.getRecipeCategory();
		JeiTooltip bookmarkTooltip = new JeiTooltip();
		boolean previewAdded = addBookmarkTooltipFeaturesIfEnabled(bookmarkTooltip, pinned);

		if (recipeBookmark.isDisplayIsOutput()) {
			IJeiRuntime jeiRuntime = Internal.getJeiRuntime();
			IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
			IModIdHelper modIdHelper = jeiRuntime.getJeiHelpers().getModIdHelper();
			boolean recipeByAdded = false;

			Identifier recipeName = recipeCategory.getIdentifier(recipe);
			if (recipeName != null) {
				String recipeModId = recipeName.getNamespace();
				Identifier ingredientId = ingredientHelper.getIdentifier(displayIngredient.getIngredient());
				String ingredientModId = ingredientId.getNamespace();
				if (!recipeModId.equals(ingredientModId)) {
					Component modName = modIdHelper.getFormattedModNameComponentForModId(recipeModId);
					MutableComponent recipeBy = Component.translatable("jei.tooltip.recipe.by", modName);
					tooltip.add(recipeBy.withStyle(ChatFormatting.GRAY));
					recipeByAdded = true;
				}
			}

			if (recipeByAdded) {
				tooltip.add(Component.empty());
			}

			SafeIngredientUtil.getRichTooltip(tooltip, ingredientManager, ingredientRenderer, displayIngredient);
		}

		if (previewAdded && !pinned) {
			IJeiKeyMappingInternal pauseRecipeCycling = Internal.getKeyMappings().getPauseRecipeCycling();
			if (!pauseRecipeCycling.isUnbound()) {
				bookmarkTooltip.addKeyUsageComponent("jei.tooltip.bookmarks.preview.pin.usage", pauseRecipeCycling);
			}
		}

		if (pinned) {
			tooltip.addAll(bookmarkTooltip);
		} else {
			tooltip.addIngredientTooltipFooter(bookmarkTooltip);
		}
	}

	private boolean addBookmarkTooltipFeaturesIfEnabled(JeiTooltip tooltip, boolean pinned) {
		JeiTooltip transferComponents = new JeiTooltip();
		if (!pinned) {
			transferComponents.addAll(createTransferComponents());
		}
		List<BookmarkTooltipFeature> bookmarkTooltipFeatures = getBookmarkTooltipFeatures();

		if (bookmarkTooltipFeatures.isEmpty() && transferComponents.isEmpty()) {
			return false;
		}

		if (!pinned && clientConfig.holdShiftToShowBookmarkTooltipFeaturesEnabled().getValue()) {
			IJeiKeyMappingInternal pauseRecipeCycling = Internal.getKeyMappings().getPauseRecipeCycling();
			if (pauseRecipeCycling.isUnbound()) {
				return false;
			}
			if (!pauseRecipeCycling.isDown()) {
				tooltip.addKeyUsageComponent(
					"jei.tooltip.bookmarks.tooltips.usage",
					pauseRecipeCycling
				);
				return false;
			}
		}

		boolean previewAdded = addBookmarkTooltipFeatures(tooltip, bookmarkTooltipFeatures);
		tooltip.addAll(transferComponents);
		return previewAdded;
	}

	private List<BookmarkTooltipFeature> getBookmarkTooltipFeatures() {
		return clientConfig.bookmarkTooltipFeatures().getValue();
	}

	private boolean addBookmarkTooltipFeatures(JeiTooltip tooltip, List<BookmarkTooltipFeature> features) {
		boolean previewAdded = false;
		for (BookmarkTooltipFeature feature : features) {
			boolean added = addBookmarkTooltipFeature(tooltip, feature);
			if (feature == BookmarkTooltipFeature.PREVIEW && added) {
				previewAdded = true;
			}
			if (!added) {
				break;
			}
		}
		return previewAdded;
	}

	private boolean addBookmarkTooltipFeature(JeiTooltip tooltip, BookmarkTooltipFeature feature) {
		return switch (feature) {
			case PREVIEW -> addPreviewTooltipComponent(tooltip);
			case INGREDIENTS -> addIngredientsTooltipComponent(tooltip);
		};
	}

	private boolean addPreviewTooltipComponent(JeiTooltip tooltip) {
		PreviewTooltipComponent<R> component = previewTooltipComponent;
		if (component == null) {
			component = createPreviewTooltipComponent();
			if (component == null) {
				return false;
			}
			previewTooltipComponent = component;
		}
		component.setStatic();
		tooltip.add(component);
		return true;
	}

	private @Nullable PreviewTooltipComponent<R> createPreviewTooltipComponent() {
		IRecipeLayoutDrawable<R> recipeLayout = getRecipeLayoutDrawable().orElse(null);
		if (recipeLayout == null) {
			return null;
		}
		return new PreviewTooltipComponent<>(recipeLayout, recipeTransferService);
	}

	private boolean addIngredientsTooltipComponent(JeiTooltip tooltip) {
		IngredientsTooltipComponent component = ingredientsTooltipComponent;
		if (component == null) {
			IRecipeLayoutDrawable<R> recipeLayout = getRecipeLayoutDrawable().orElse(null);
			if (recipeLayout == null) {
				return false;
			}
			component = new IngredientsTooltipComponent(recipeLayout);
			ingredientsTooltipComponent = component;
		}

		tooltip.add(component);
		return true;
	}

	private JeiTooltip createTransferComponents() {
		JeiTooltip results = new JeiTooltip();

		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.gui.screen();
		Player player = minecraft.player;
		if (player != null && screen instanceof AbstractContainerScreen<?> containerScreen) {
			IRecipeTransferError recipeTransferError = getRecipeLayoutDrawable()
				.flatMap(recipeLayout -> {
					return recipeTransferService.getTransferRecipeError(containerScreen, recipeLayout, player);
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

	@Override
	public void tick() {
		PreviewTooltipComponent<R> component = previewTooltipComponent;
		if (component != null) {
			component.tick();
		}
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
		public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
			var poseStack = guiGraphics.pose();
			poseStack.pushMatrix();
			{
				poseStack.translate(8 + xOffset, 8 + yOffset);
				poseStack.scale(0.5f, 0.5f);
				icon.draw(guiGraphics);
			}
			poseStack.popMatrix();
		}
	}
}
