package mezz.jei.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotId;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.config.ServerInfo;
import mezz.jei.gui.ingredients.RecipeSlotsView;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.crafting.CraftingRecipe;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

public class PlayerRecipeTransferHandler implements IRecipeTransferHandler<InventoryMenu, CraftingRecipe> {
	/**
	 * Indexes from the crafting table that fit into the player crafting grid
	 * when we trim the right and bottom edges .
	 */
	private static final Int2IntMap INDEX_MAPPING = new Int2IntArrayMap(Map.of(
		1, 1,
		2, 2,
		4, 3,
		5, 4
	));

	private final IStackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IRecipeTransferInfo<InventoryMenu, CraftingRecipe> transferInfo;

	public PlayerRecipeTransferHandler(IStackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper) {
		this.stackHelper = stackHelper;
		this.handlerHelper = handlerHelper;
		this.transferInfo = new BasicRecipeTransferInfo<>(InventoryMenu.class, CraftingRecipe.class, VanillaRecipeCategoryUid.CRAFTING, 1, 4, 9, 36);
	}

	@Override
	public Class<InventoryMenu> getContainerClass() {
		return InventoryMenu.class;
	}

	@Override
	public Class<CraftingRecipe> getRecipeClass() {
		return CraftingRecipe.class;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(InventoryMenu container, CraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!ServerInfo.isJeiOnServer()) {
			Component tooltipMessage = new TranslatableComponent("jei.tooltip.error.recipe.transfer.no.server");
			return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}
		
		boolean allGoodInputSlots = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT, VanillaTypes.ITEM)
			.stream()
			.allMatch(slotView -> {
				OptionalInt slotIndex = slotView.getContainerSlotIndex();
				return slotIndex.isPresent() && INDEX_MAPPING.containsKey(slotIndex.getAsInt());
			});

		if (!allGoodInputSlots) {
			Component tooltipMessage = new TranslatableComponent("jei.tooltip.error.recipe.transfer.too.large.player.inventory");
			return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		// map the crafting table input slots to player inventory input slots
		List<IRecipeSlotView> recipeSlotViews = mapSlots(recipeSlotsView.getSlotViews());
		RecipeSlotsView mappedRecipeSlots = new RecipeSlotsView(recipeSlotViews);
		IRecipeTransferHandler<InventoryMenu, CraftingRecipe> handler = new BasicRecipeTransferHandler<>(stackHelper, handlerHelper, transferInfo);
		return handler.transferRecipe(container, recipe, mappedRecipeSlots, player, maxTransfer, doTransfer);
	}

	private static List<IRecipeSlotView> mapSlots(List<IRecipeSlotView> slotViews) {
		return slotViews.stream()
			.map(slotView -> {
				OptionalInt optionalSlotIndex = slotView.getContainerSlotIndex();
				if (optionalSlotIndex.isEmpty()) {
					return slotView;
				}
				int slotIndex = optionalSlotIndex.getAsInt();
				int newSlotIndex = INDEX_MAPPING.getOrDefault(slotIndex, slotIndex);
				if (newSlotIndex == slotIndex) {
					return slotView;
				}
				return new MappedRecipeSlotView(slotView, newSlotIndex);
			})
			.toList();
	}

	private static class MappedRecipeSlotView implements IRecipeSlotView {
		private final IRecipeSlotView original;
		private final Integer newSlotIndex;

		public MappedRecipeSlotView(IRecipeSlotView original, Integer newSlotIndex) {
			this.original = original;
			this.newSlotIndex = newSlotIndex;
		}

		@Override
		public Stream<ITypedIngredient<?>> getAllIngredients() {
			return original.getAllIngredients();
		}

		@Override
		public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
			return original.getIngredients(ingredientType);
		}

		@Override
		public <T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType) {
			return original.getDisplayedIngredient(ingredientType);
		}

		@Override
		public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
			return original.getDisplayedIngredient();
		}

		@Override
		public OptionalInt getContainerSlotIndex() {
			if (newSlotIndex < 0) {
				return OptionalInt.empty();
			}
			return OptionalInt.of(newSlotIndex);
		}

		@Override
		public Optional<IRecipeSlotId> getSlotId() {
			return original.getSlotId();
		}

		@Override
		public RecipeIngredientRole getRole() {
			return original.getRole();
		}

		@Override
		public void drawHighlight(PoseStack stack, int color, int xOffset, int yOffset) {
			original.drawHighlight(stack, color, xOffset, yOffset);
		}
	}
}
