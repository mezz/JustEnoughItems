package mezz.jei.common.transfer;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.common.util.StringUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RecipeTransferUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private RecipeTransferUtil() {
	}

	public static void addTransferRecipeTooltip(@Nullable IRecipeTransferError recipeTransferError, ITooltipBuilder tooltip) {
		if (recipeTransferError == null) {
			tooltip.add(Component.translatable("jei.tooltip.transfer"));
		} else {
			recipeTransferError.getTooltip(tooltip);
		}
	}

	public static boolean validateSlots(
		Player player,
		Collection<TransferOperation> transferOperations,
		Collection<Slot> craftingSlots,
		Collection<Slot> inventorySlots
	) {
		AbstractContainerMenu container = player.containerMenu;
		List<Integer> invalidOperationSlotIndexes = transferOperations.stream()
			.flatMap(op -> Stream.of(op.inventorySlotId(), op.craftingSlotId()))
			.distinct()
			.filter(slotId -> !isValidSlotId(container, slotId))
			.toList();
		if (!invalidOperationSlotIndexes.isEmpty()) {
			LOGGER.error(
				"Transfer request has invalid slot ids in its transfer operations: {}",
				StringUtil.intsToString(invalidOperationSlotIndexes)
			);
			return false;
		}

		Set<Integer> inventorySlotIndexes = inventorySlots.stream()
			.map(s -> s.index)
			.collect(Collectors.toSet());
		Set<Integer> craftingSlotIndexes = craftingSlots.stream()
			.map(s -> s.index)
			.collect(Collectors.toSet());

		List<Integer> invalidItemHandlerSourceIndexes = transferOperations.stream()
			.filter(TransferOperation::hasItemHandlerSource)
			.map(TransferOperation::inventorySlotId)
			.filter(slotId -> !inventorySlotIndexes.contains(slotId))
			.toList();
		if (!invalidItemHandlerSourceIndexes.isEmpty()) {
			LOGGER.error(
				"Transfer request has item handler sources outside its allowed inventory slots: {}",
				StringUtil.intsToString(invalidItemHandlerSourceIndexes)
			);
			return false;
		}

		// check that all craftingTargetSlots are included in craftingSlots
		{
			List<Integer> invalidRecipeIndexes = transferOperations.stream()
				.map(op -> op.craftingSlot(player.containerMenu))
				.map(s -> s.index)
				.filter(s -> !craftingSlotIndexes.contains(s))
				.toList();
			if (!invalidRecipeIndexes.isEmpty()) {
				LOGGER.error(
					"Transfer request has invalid slots for the destination of the recipe,  the slots are not included in the list of crafting slots. {}",
					StringUtil.intsToString(invalidRecipeIndexes)
				);
				return false;
			}
		}

		// check that all ingredientTargetSlots are included in inventorySlots or recipeSlots
		{
			List<Integer> invalidInventorySlotIndexes = transferOperations.stream()
				.map(op -> op.inventorySlot(player.containerMenu))
				.map(s -> s.index)
				.filter(s -> !inventorySlotIndexes.contains(s) && !craftingSlotIndexes.contains(s))
				.toList();
			if (!invalidInventorySlotIndexes.isEmpty()) {
				LOGGER.error(
					"Transfer request has invalid source slots for the inventory stacks for the recipe, the slots are not included in the list of inventory slots or recipe slots. {}\n inventory slots: {}\n crafting slots: {}",
					StringUtil.intsToString(invalidInventorySlotIndexes),
					StringUtil.intsToString(inventorySlotIndexes),
					StringUtil.intsToString(craftingSlotIndexes)
				);
				return false;
			}
		}

		// check that crafting slots and inventory slots do not overlap
		{
			Set<Integer> overlappingSlots = inventorySlotIndexes.stream()
				.filter(craftingSlotIndexes::contains)
				.collect(Collectors.toSet());
			if (!overlappingSlots.isEmpty()) {
				LOGGER.error(
					"Transfer request has invalid slots, inventorySlots and craftingSlots should not share any slot, but both have: {}",
					StringUtil.intsToString(overlappingSlots)
				);
				return false;
			}
		}

		// check that all slots are real (not output slots)
		{
			List<Integer> invalidFakeSlots = Stream.concat(
					craftingSlots.stream(),
					inventorySlots.stream()
				)
				.filter(Slot::isFake)
				.map(slot -> slot.index)
				.toList();
			if (!invalidFakeSlots.isEmpty()) {
				LOGGER.error(
					"Transfer request has invalid slots, they are fake slots (recipe outputs): {}",
					StringUtil.intsToString(invalidFakeSlots)
				);
				return false;
			}
		}

		return true;
	}

	private static boolean isValidSlotId(AbstractContainerMenu container, int slotId) {
		return slotId >= 0 && slotId < container.slots.size();
	}

	/**
	 * Returns a list of items in slots that complete the recipe defined by requiredStacksList.
	 * Returns a result that contains missingItems if there are not enough items in availableItemStacks.
	 */
	public static RecipeTransferOperationsResult getRecipeTransferOperations(
		IStackHelper stackhelper,
		Map<RecipeTransferSource, ItemStack> availableItemStacks,
		List<IRecipeSlotView> requiredItemStacks,
		List<Slot> craftingSlots
	) {
		RecipeTransferOperationsResult transferOperations = new RecipeTransferOperationsResult();
		List<RequiredSlot> requiredSlots = new ArrayList<>();
		Map<IRecipeSlotView, Map<Object, Integer>> slotRequirementCache = new IdentityHashMap<>();
		Map<RecipeTransferSource, Integer> availableCounts = new HashMap<>();
		Map<RecipeTransferSource, Object> availableUids = new HashMap<>();
		availableItemStacks.forEach((slot, stack) -> {
			if (!stack.isEmpty()) {
				availableCounts.put(slot, stack.getCount());
				availableUids.put(slot, stackhelper.getUidForStack(stack, UidContext.Recipe));
			}
		});

		for (int i = 0; i < requiredItemStacks.size(); i++) {
			IRecipeSlotView requiredItemStack = requiredItemStacks.get(i);

			if (requiredItemStack.isEmpty()) {
				continue;
			}

			Slot craftingSlot = craftingSlots.get(i);
			Map<Object, Integer> requiredCountsByUid = slotRequirementCache.computeIfAbsent(requiredItemStack, s -> calculateRequiredCountsByUid(s, stackhelper));
			List<CandidateGroup> candidateGroups = getCandidateGroups(availableItemStacks, availableUids, requiredCountsByUid);

			if (candidateGroups.isEmpty()) {
				transferOperations.missingItems.add(requiredItemStack);
			} else {
				requiredSlots.add(new RequiredSlot(i, craftingSlot, candidateGroups));
			}
		}

		if (!transferOperations.missingItems.isEmpty()) {
			return transferOperations;
		}

		AssignmentResult assignmentResult = findAssignments(requiredSlots, availableCounts);
		if (assignmentResult.assignedIndexes().size() != requiredSlots.size()) {
			for (RequiredSlot requiredSlot : requiredSlots) {
				if (!assignmentResult.assignedIndexes().contains(requiredSlot.index)) {
					transferOperations.missingItems.add(requiredItemStacks.get(requiredSlot.index));
				}
			}
			return transferOperations;
		}

		assignmentResult.assignments().stream()
			.sorted(Comparator.comparingInt(Assignment::requiredIndex))
			.map(assignment -> assignment.source.createTransferOperation(assignment.craftingSlot, assignment.count))
			.forEach(transferOperations.results::add);

		return transferOperations;
	}

	@Nullable
	public static List<TransferOperation> getExactRecipeTransferOperations(
		Map<RecipeTransferSource, ItemStack> availableItemStacks,
		List<RecipeTransferRequirement> requirements,
		List<Slot> craftingSlots
	) {
		Map<Integer, Slot> craftingSlotsById = new HashMap<>();
		for (Slot craftingSlot : craftingSlots) {
			craftingSlotsById.putIfAbsent(craftingSlot.index, craftingSlot);
		}
		Map<RecipeTransferSource, Integer> availableCounts = new HashMap<>();
		Map<RecipeTransferSource, Object> availableKeys = new HashMap<>();
		availableItemStacks.forEach((source, stack) -> {
			if (!stack.isEmpty()) {
				availableCounts.put(source, stack.getCount());
				availableKeys.put(source, new ItemStackKey(stack));
			}
		});

		List<RequiredSlot> requiredSlots = new ArrayList<>(requirements.size());
		for (int i = 0; i < requirements.size(); i++) {
			RecipeTransferRequirement requirement = requirements.get(i);
			Slot craftingSlot = craftingSlotsById.get(requirement.craftingSlotId());
			if (craftingSlot == null || requirement.acceptedStacks().isEmpty()) {
				return null;
			}

			Map<Object, Integer> requiredCountsByKey = new HashMap<>();
			for (ItemStack acceptedStack : requirement.acceptedStacks()) {
				if (acceptedStack.isEmpty()) {
					return null;
				}
				requiredCountsByKey.merge(
					new ItemStackKey(acceptedStack),
					Math.max(1, acceptedStack.getCount()),
					Math::max
				);
			}
			List<CandidateGroup> candidateGroups = getCandidateGroups(
				availableItemStacks,
				availableKeys,
				requiredCountsByKey
			);
			if (candidateGroups.isEmpty()) {
				return null;
			}
			requiredSlots.add(new RequiredSlot(i, craftingSlot, candidateGroups));
		}

		AssignmentResult assignmentResult = findAssignments(requiredSlots, availableCounts);
		if (assignmentResult.assignedIndexes().size() != requiredSlots.size()) {
			return null;
		}

		return assignmentResult.assignments().stream()
			.sorted(Comparator.comparingInt(Assignment::requiredIndex))
			.map(assignment -> assignment.source.createTransferOperation(assignment.craftingSlot, assignment.count))
			.toList();
	}

	private static List<CandidateGroup> getCandidateGroups(
		Map<RecipeTransferSource, ItemStack> availableItemStacks,
		Map<RecipeTransferSource, Object> availableUids,
		Map<Object, Integer> requiredCountsByUid
	) {
		Map<Object, List<CandidateSlot>> candidatesByUid = new HashMap<>();

		availableItemStacks.forEach((source, stack) -> {
			Object uid = availableUids.get(source);
			if (uid != null && requiredCountsByUid.containsKey(uid)) {
				candidatesByUid.computeIfAbsent(uid, ignored -> new ArrayList<>())
					.add(new CandidateSlot(source, stack));
			}
		});

		List<CandidateGroup> candidateGroups = new ArrayList<>();
		candidatesByUid.forEach((uid, candidates) -> {
			candidates.sort((a, b) -> {
				int compare = Boolean.compare(a.source.isItemHandlerSource(), b.source.isItemHandlerSource());
				if (compare == 0) {
					compare = Integer.compare(a.stack.getCount(), b.stack.getCount());
				}
				if (compare == 0) {
					compare = Integer.compare(a.source.slot().index, b.source.slot().index);
				}
				if (compare == 0) {
					compare = Integer.compare(a.source.itemHandlerSlotId(), b.source.itemHandlerSlotId());
				}
				return compare;
			});
			int totalCount = candidates.stream()
				.mapToInt(candidate -> candidate.stack.getCount())
				.sum();
			int requiredCount = requiredCountsByUid.getOrDefault(uid, 1);
			if (totalCount >= requiredCount) {
				candidateGroups.add(new CandidateGroup(uid, requiredCount, candidates, totalCount));
			}
		});

		candidateGroups.sort((a, b) -> {
			int compare = Integer.compare(b.totalCount, a.totalCount);
			if (compare == 0) {
				compare = Integer.compare(a.getFirstSlotIndex(), b.getFirstSlotIndex());
			}
			return compare;
		});

		return candidateGroups;
	}

	private static AssignmentResult findAssignments(List<RequiredSlot> requiredSlots, Map<RecipeTransferSource, Integer> availableCounts) {
		List<Assignment> assignments = new ArrayList<>();
		List<Assignment> bestAssignments = new ArrayList<>();
		Set<Integer> assignedIndexes = new HashSet<>();
		Set<Integer> bestAssignedIndexes = new HashSet<>();
		assignRequiredSlots(requiredSlots, availableCounts, new HashSet<>(), assignedIndexes, assignments, bestAssignments, bestAssignedIndexes);
		return new AssignmentResult(bestAssignments, bestAssignedIndexes);
	}

	private static boolean assignRequiredSlots(
		List<RequiredSlot> requiredSlots,
		Map<RecipeTransferSource, Integer> availableCounts,
		Set<Integer> processedIndexes,
		Set<Integer> assignedIndexes,
		List<Assignment> assignments,
		List<Assignment> bestAssignments,
		Set<Integer> bestAssignedIndexes
	) {
		if (assignedIndexes.size() > bestAssignedIndexes.size()) {
			bestAssignments.clear();
			bestAssignments.addAll(assignments);
			bestAssignedIndexes.clear();
			bestAssignedIndexes.addAll(assignedIndexes);
		}

		if (processedIndexes.size() == requiredSlots.size()) {
			return assignedIndexes.size() == requiredSlots.size();
		}

		RequiredSlot requiredSlot = getMostConstrainedRequiredSlot(requiredSlots, availableCounts, processedIndexes);
		if (requiredSlot == null) {
			return assignedIndexes.size() == requiredSlots.size();
		}

		processedIndexes.add(requiredSlot.index);
		boolean hasAvailableCandidate = false;
		for (CandidateGroup candidateGroup : requiredSlot.candidateGroups) {
			List<Assignment> takenAssignments = takeRequiredItems(requiredSlot, candidateGroup, availableCounts);
			if (takenAssignments.isEmpty()) {
				continue;
			}

			hasAvailableCandidate = true;
			assignments.addAll(takenAssignments);
			assignedIndexes.add(requiredSlot.index);
			if (assignRequiredSlots(requiredSlots, availableCounts, processedIndexes, assignedIndexes, assignments, bestAssignments, bestAssignedIndexes)) {
				return true;
			}
			assignedIndexes.remove(requiredSlot.index);
			assignments.subList(assignments.size() - takenAssignments.size(), assignments.size()).clear();
			restoreAssignments(takenAssignments, availableCounts);
		}

		if (!hasAvailableCandidate && assignRequiredSlots(requiredSlots, availableCounts, processedIndexes, assignedIndexes, assignments, bestAssignments, bestAssignedIndexes)) {
			return true;
		}

		processedIndexes.remove(requiredSlot.index);
		return false;
	}

	private static List<Assignment> takeRequiredItems(
		RequiredSlot requiredSlot,
		CandidateGroup candidateGroup,
		Map<RecipeTransferSource, Integer> availableCounts
	) {
		int remainingCount = candidateGroup.requiredCount;
		List<Assignment> takenAssignments = new ArrayList<>();
		for (CandidateSlot candidate : candidateGroup.candidates) {
			int availableCount = availableCounts.getOrDefault(candidate.source, 0);
			if (availableCount <= 0) {
				continue;
			}

			int count = Math.min(availableCount, remainingCount);
			availableCounts.put(candidate.source, availableCount - count);
			takenAssignments.add(new Assignment(requiredSlot.index, requiredSlot.craftingSlot, candidate.source, count));
			remainingCount -= count;

			if (remainingCount == 0) {
				return takenAssignments;
			}
		}

		restoreAssignments(takenAssignments, availableCounts);
		return List.of();
	}

	private static void restoreAssignments(List<Assignment> assignments, Map<RecipeTransferSource, Integer> availableCounts) {
		for (Assignment assignment : assignments) {
			availableCounts.merge(assignment.source, assignment.count, Integer::sum);
		}
	}

	@Nullable
	private static RequiredSlot getMostConstrainedRequiredSlot(
		List<RequiredSlot> requiredSlots,
		Map<RecipeTransferSource, Integer> availableCounts,
		Set<Integer> processedIndexes
	) {
		RequiredSlot best = null;
		int bestAvailableCandidateCount = Integer.MAX_VALUE;
		for (RequiredSlot requiredSlot : requiredSlots) {
			if (processedIndexes.contains(requiredSlot.index)) {
				continue;
			}
			int availableCandidateCount = countAvailableCandidates(requiredSlot, availableCounts);
			if (best == null || availableCandidateCount < bestAvailableCandidateCount) {
				best = requiredSlot;
				bestAvailableCandidateCount = availableCandidateCount;
			}
		}
		return best;
	}

	private static int countAvailableCandidates(RequiredSlot requiredSlot, Map<RecipeTransferSource, Integer> availableCounts) {
		int count = 0;
		for (CandidateGroup candidateGroup : requiredSlot.candidateGroups) {
			int availableCount = candidateGroup.candidates.stream()
				.mapToInt(candidate -> availableCounts.getOrDefault(candidate.source, 0))
				.sum();
			if (availableCount >= candidateGroup.requiredCount) {
				count++;
			}
		}
		return count;
	}

	private static Map<Object, Integer> calculateRequiredCountsByUid(IRecipeSlotView recipeSlotView, IStackHelper stackhelper) {
		List<@Nullable ITypedIngredient<?>> allIngredientsList = recipeSlotView.getAllIngredientsList();
		Map<Object, Integer> requiredCountsByUid = new HashMap<>(allIngredientsList.size());
		for (ITypedIngredient<?> typedIngredient : allIngredientsList) {
			if (typedIngredient == null) {
				continue;
			}
			ITypedIngredient<ItemStack> typedItemStack = typedIngredient.castToItemStackType();
			if (typedItemStack != null) {
				Object uid = stackhelper.getUidForStack(typedItemStack, UidContext.Recipe);
				int count = Math.max(1, typedItemStack.getIngredient().getCount());
				requiredCountsByUid.merge(uid, count, Math::max);
			}
		}
		return requiredCountsByUid;
	}

	private record RequiredSlot(int index, Slot craftingSlot, List<CandidateGroup> candidateGroups) {}

	private record CandidateGroup(Object uid, int requiredCount, List<CandidateSlot> candidates, int totalCount) {
		private int getFirstSlotIndex() {
			return candidates.stream()
				.mapToInt(candidate -> candidate.source.slot().index)
				.min()
				.orElse(Integer.MAX_VALUE);
		}
	}

	private record CandidateSlot(RecipeTransferSource source, ItemStack stack) {}

	private record Assignment(int requiredIndex, Slot craftingSlot, RecipeTransferSource source, int count) {}

	private record AssignmentResult(List<Assignment> assignments, Set<Integer> assignedIndexes) {}

	private static final class ItemStackKey {
		private final ItemStack stack;
		private final int hashCode;

		private ItemStackKey(ItemStack stack) {
			this.stack = stack.copyWithCount(1);
			this.hashCode = ItemStack.hashItemAndComponents(stack);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof ItemStackKey other &&
				ItemStack.isSameItemSameComponents(stack, other.stack);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}
}
