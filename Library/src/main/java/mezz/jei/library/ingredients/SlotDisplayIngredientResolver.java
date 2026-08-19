package mezz.jei.library.ingredients;

import mezz.jei.common.ingredients.TypedIngredient;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ISlotDisplayInterpreter;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class SlotDisplayIngredientResolver {
	private SlotDisplayIngredientResolver() {
	}

	static Stream<SlotIngredient<?>> resolve(
		IIngredientManager ingredientManager,
		SlotDisplayInterpreterRegistry interpreterRegistry,
		ContextMap contextMap,
		RecipeIngredientRole role,
		SlotDisplay slotDisplay
	) {
		return ingredientManager.getRegisteredIngredientTypes()
			.stream()
			.flatMap(ingredientType -> resolve(ingredientManager, interpreterRegistry, ingredientType, contextMap, role, slotDisplay));
	}

	static <T> Stream<SlotIngredient<T>> resolve(
		IIngredientManager ingredientManager,
		SlotDisplayInterpreterRegistry interpreterRegistry,
		IIngredientType<T> ingredientType,
		ContextMap contextMap,
		RecipeIngredientRole role,
		SlotDisplay slotDisplay
	) {
		ResolutionSession<T> session = new ResolutionSession<>(
			ingredientManager,
			ingredientType,
			interpreterRegistry,
			contextMap,
			role
		);
		return session.resolveRoot(slotDisplay);
	}

	private static final class ResolutionSession<T> {
		private final IIngredientManager ingredientManager;
		private final IIngredientType<T> ingredientType;
		private final IIngredientHelper<T> ingredientHelper;
		private final SlotDisplayInterpreterRegistry interpreterRegistry;
		private final ContextMap contextMap;
		private final RecipeIngredientRole role;
		private final Map<SlotDisplay, List<ITypedIngredient<T>>> resolvedIngredients = new IdentityHashMap<>();
		private final Map<SlotDisplay, List<ResolvedGroup<T>>> resolvedGroups = new IdentityHashMap<>();
		private final Set<SlotDisplay> visiting = Collections.newSetFromMap(new IdentityHashMap<>());

		private ResolutionSession(
			IIngredientManager ingredientManager,
			IIngredientType<T> ingredientType,
			SlotDisplayInterpreterRegistry interpreterRegistry,
			ContextMap contextMap,
			RecipeIngredientRole role
		) {
			this.ingredientManager = ingredientManager;
			this.ingredientType = ingredientType;
			this.ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
			this.interpreterRegistry = interpreterRegistry;
			this.contextMap = contextMap;
			this.role = role;
		}

		private Stream<SlotIngredient<T>> resolveRoot(SlotDisplay rootDisplay) {
			List<ResolvedGroup<T>> groups = resolve(rootDisplay);
			return groups.stream()
				.map(this::resolveWildcardForSubtypes)
				.flatMap(SlotDisplayIngredientResolver::createSlotIngredients);
		}

		private ResolvedGroup<T> resolveWildcardForSubtypes(ResolvedGroup<T> group) {
			boolean hasSubtypes = role == RecipeIngredientRole.INPUT && group.ingredients()
				.stream()
				.map(ITypedIngredient::getIngredient)
				.anyMatch(ingredientHelper::hasSubtypes);
			SlotDisplayInfo resolvedInfo = group.info().resolveWildcardForSubtypes(
				hasSubtypes,
				() -> createAnySubtypeTooltipHeader(group.ingredients().getFirst())
			);
			return new ResolvedGroup<>(group.ingredients(), resolvedInfo);
		}

		private Component createAnySubtypeTooltipHeader(ITypedIngredient<T> ingredient) {
			String displayName = ingredientHelper.getDisplayName(ingredient.getIngredient());
			return Component.translatable("jei.tooltip.recipe.any", displayName)
				.withStyle(ChatFormatting.GOLD)
				.withStyle(ChatFormatting.ITALIC);
		}

		private List<ResolvedGroup<T>> resolve(SlotDisplay slotDisplay) {
			List<ResolvedGroup<T>> cached = resolvedGroups.get(slotDisplay);
			if (cached != null) {
				return cached;
			}
			if (!visiting.add(slotDisplay)) {
				return createGroup(slotDisplay, SlotDisplayInfo.EMPTY);
			}

			try {
				SlotDisplayInterpretationBuilder interpretationBuilder = new SlotDisplayInterpretationBuilder();
				Context context = new Context(slotDisplay);
				interpreterRegistry.interpret(ingredientType, slotDisplay, context, interpretationBuilder);

				SlotDisplayInfo displayInfo = interpretationBuilder.buildInfo();
				List<ResolvedGroup<T>> resolved = interpretationBuilder.getChildDisplays()
					.map(children -> resolveChildren(children, displayInfo))
					.orElseGet(() -> createGroup(slotDisplay, displayInfo));
				resolvedGroups.put(slotDisplay, resolved);
				return resolved;
			} finally {
				visiting.remove(slotDisplay);
			}
		}

		private List<ResolvedGroup<T>> resolveChildren(
			List<SlotDisplay> children,
			SlotDisplayInfo displayInfo
		) {
			List<ResolvedGroup<T>> groups = new ArrayList<>();
			for (SlotDisplay child : children) {
				resolve(child).stream()
					.map(group -> group.withOverlay(displayInfo))
					.forEach(groups::add);
			}
			return List.copyOf(groups);
		}

		private List<ResolvedGroup<T>> createGroup(SlotDisplay slotDisplay, SlotDisplayInfo displayInfo) {
			List<ITypedIngredient<T>> ingredients = getIngredients(slotDisplay);
			if (ingredients.isEmpty()) {
				return List.of();
			}
			return List.of(new ResolvedGroup<>(ingredients, displayInfo));
		}

		private List<ITypedIngredient<T>> getIngredients(SlotDisplay slotDisplay) {
			return resolvedIngredients.computeIfAbsent(
				slotDisplay,
				display -> TypedIngredient.createAndFilterInvalidList(ingredientManager, ingredientType, contextMap, display, false)
					.filter(Objects::nonNull)
					.toList()
			);
		}

		private final class Context implements ISlotDisplayInterpreter.IContext<T> {
			private final SlotDisplay slotDisplay;

			private Context(SlotDisplay slotDisplay) {
				this.slotDisplay = slotDisplay;
			}

			@Override
			public List<ITypedIngredient<T>> getIngredients() {
				return ResolutionSession.this.getIngredients(slotDisplay);
			}

			@Override
			public IIngredientManager getIngredientManager() {
				return ingredientManager;
			}

			@Override
			public IIngredientHelper<T> getIngredientHelper() {
				return ingredientHelper;
			}

			@Override
			public ContextMap getContextMap() {
				return contextMap;
			}

			@Override
			public RecipeIngredientRole getRole() {
				return role;
			}

			@Override
			public Stream<T> resolve(SlotDisplay slotDisplay) {
				return ingredientHelper.getDisplayContentsFactory()
					.stream()
					.flatMap(displayContentsFactory -> slotDisplay.resolve(contextMap, displayContentsFactory));
			}
		}
	}

	private record ResolvedGroup<T>(
		List<ITypedIngredient<T>> ingredients,
		SlotDisplayInfo info
	) {
		private ResolvedGroup<T> withOverlay(SlotDisplayInfo overlay) {
			return new ResolvedGroup<>(ingredients, overlay.overlayOn(info));
		}
	}

	private static <T> Stream<SlotIngredient<T>> createSlotIngredients(
		ResolvedGroup<T> group
	) {
		SlotDisplayData<T> slotDisplayData = new SlotDisplayData<>(group.ingredients(), group.info());
		return group.ingredients().stream()
			.map(ingredient -> new SlotIngredient<>(ingredient, slotDisplayData));
	}
}
