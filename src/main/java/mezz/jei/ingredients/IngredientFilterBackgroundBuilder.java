package mezz.jei.ingredients;

import mezz.jei.config.SearchMode;
import mezz.jei.events.EventBusHelper;
import mezz.jei.search.PrefixInfo;
import mezz.jei.search.suffixtree.GeneralizedSuffixTree;
import net.minecraft.client.Minecraft;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class IngredientFilterBackgroundBuilder {
	private final Map<PrefixInfo, PrefixedSearchable<GeneralizedSuffixTree>> prefixedSearchTrees;
	private final NonNullList<IIngredientListElementInfo<?>> elementList;
	private final Consumer<TickEvent.ClientTickEvent> onTickHandler;

	public IngredientFilterBackgroundBuilder(
		Map<PrefixInfo, PrefixedSearchable<GeneralizedSuffixTree>> prefixedSearchTrees,
		NonNullList<IIngredientListElementInfo<?>> elementList
	) {
		this.prefixedSearchTrees = prefixedSearchTrees;
		this.elementList = elementList;
		this.onTickHandler = this::onClientTick;
	}

	public void start() {
		boolean finished = run(10000);
		if (!finished) {
			EventBusHelper.addListener(this, TickEvent.ClientTickEvent.class, this.onTickHandler);
		}
	}

	private void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.side == LogicalSide.CLIENT && Minecraft.getInstance().player != null) {
			boolean finished = run(20);
			if (!finished) {
				return;
			}
		}
		EventBusHelper.removeListener(this, this.onTickHandler);
	}

	private boolean run(final int timeoutMs) {
		final long startTime = System.currentTimeMillis();
		List<PrefixedSearchable<GeneralizedSuffixTree>> activeTrees = new ArrayList<>();
		int startIndex = Integer.MAX_VALUE;
		for (PrefixedSearchable<GeneralizedSuffixTree> prefixedTree : this.prefixedSearchTrees.values()) {
			SearchMode mode = prefixedTree.getMode();
			if (mode != SearchMode.DISABLED) {
				GeneralizedSuffixTree searchable = prefixedTree.getSearchable();
				int nextFreeIndex = searchable.getHighestIndex() + 1;
				startIndex = Math.min(nextFreeIndex, startIndex);
				if (nextFreeIndex < elementList.size()) {
					activeTrees.add(prefixedTree);
				}
			}
		}

		if (activeTrees.isEmpty()) {
			return true;
		}

		for (int i = startIndex; i < elementList.size(); i++) {
			IIngredientListElementInfo<?> info = elementList.get(i);
			for (PrefixedSearchable<GeneralizedSuffixTree> prefixedTree : activeTrees) {
				GeneralizedSuffixTree searchable = prefixedTree.getSearchable();
				int nextFreeIndex = searchable.getHighestIndex() + 1;
				if (nextFreeIndex >= i) {
					Collection<String> strings = prefixedTree.getStrings(info);
					if (strings.isEmpty()) {
						searchable.put("", i);
					} else {
						for (String string : strings) {
							searchable.put(string, i);
						}
					}
				}
			}
			if (System.currentTimeMillis() - startTime >= timeoutMs) {
				return false;
			}
		}
		return true;
	}
}
