package mezz.jei.test;

import mezz.jei.common.search.BakedSubstringIndexSearchStorage;
import net.mezzdev.bakedsubstring.BakedSubstringIndex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class BakedSubstringIndexSearchStorageTest {
	@Test
	public void searchesBakedIndex() {
		BakedSubstringIndexSearchStorage<String> storage = createStorage(
			BakedSubstringIndex.<String>builder()
				.put("alpha", "first")
				.build()
		);

		Assertions.assertEquals(Set.of("first"), search(storage, "alp"));
	}

	@Test
	public void searchesRuntimeAdditionsAfterBake() {
		BakedSubstringIndexSearchStorage<String> storage = createStorage(
			BakedSubstringIndex.<String>builder()
				.put("alpha", "first")
				.build()
		);

		storage.put("beta", "second");

		Assertions.assertEquals(Set.of("first"), search(storage, "alp"));
		Assertions.assertEquals(Set.of("second"), search(storage, "bet"));
		Assertions.assertEquals(Set.of("first", "second"), allElements(storage));
	}

	private static <T> BakedSubstringIndexSearchStorage<T> createStorage(BakedSubstringIndex<T> bakedIndex) {
		return new BakedSubstringIndexSearchStorage<>(bakedIndex);
	}

	private static <T> Set<T> search(BakedSubstringIndexSearchStorage<T> storage, String token) {
		Set<T> results = Collections.newSetFromMap(new IdentityHashMap<>());
		storage.getSearchResults(token, results::addAll);
		return results;
	}

	private static <T> Set<T> allElements(BakedSubstringIndexSearchStorage<T> storage) {
		Set<T> results = Collections.newSetFromMap(new IdentityHashMap<>());
		storage.getAllElements(results::addAll);
		return results;
	}
}
