package mezz.jei.gui.search;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.common.search.LuceneSearchStorageBuilder;
import mezz.jei.gui.ingredients.ListElementInfo;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TooltipSearchTest {
	@Test
	void searchesPhrasesWithinNormalizedTooltipLines() {
		Set<String> strings = ListElementInfo.getStrings(List.of(
			Component.literal("  §aAttack ").append(Component.literal("Damage: +7  ")),
			Component.literal("ATTACK DAMAGE: +7"),
			Component.literal("Attack Speed: +2"),
			Component.literal(" \t ")
		));
		assertEquals(Set.of("attack damage: +7", "attack speed: +2"), strings);

		LuceneSearchStorageBuilder<String> builder = new LuceneSearchStorageBuilder<>();
		for (String string : strings) {
			builder.put(string, "sword");
		}
		ISearchStorage<String> storage = builder.build();
		assertEquals(Set.of("sword"), search(storage, "attack damage"));
		assertEquals(Set.of("sword"), search(storage, "damage"));
		assertEquals(Set.of("sword"), search(storage, "ack dam"));
		assertEquals(Set.of(), search(storage, "+7 attack speed"));
	}

	private static Set<String> search(ISearchStorage<String> storage, String token) {
		Set<String> results = new HashSet<>();
		storage.getSearchResults(token, results::addAll);
		return results;
	}
}
