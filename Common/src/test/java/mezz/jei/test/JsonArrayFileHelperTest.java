package mezz.jei.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import mezz.jei.common.config.file.JsonArrayFileHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class JsonArrayFileHelperTest {
	@TempDir
	Path tempDir;

	@Test
	public void writeToFileReplacesExistingFile() throws IOException {
		// Setup: an existing JSON config file has stale contents.
		Path path = tempDir.resolve("bookmarks.json");
		Files.writeString(path, "old contents");

		// Operation: write a complete replacement JSON array using the path-based helper.
		JsonArrayFileHelper.write(
			path,
			1,
			List.of("new"),
			Codec.STRING,
			JsonOps.INSTANCE,
			error -> Assertions.fail("Unexpected codec error: " + error),
			(element, exception) -> Assertions.fail("Unexpected element exception: " + exception)
		);

		// Assertions: the target file is replaced, and no temporary files are left behind.
		Assertions.assertEquals("[\n  {\"version\":1},\n  \"new\"\n]", Files.readString(path));
		try (Stream<Path> files = Files.list(tempDir)) {
			Assertions.assertEquals(List.of(path), files.toList());
		}
	}

	@Test
	public void writeToFileCreatesParentDirectories() throws IOException {
		// Setup: the target config file is inside a world-specific directory that does not exist yet.
		Path path = tempDir.resolve("world").resolve("local").resolve("bookmarks.json");

		// Operation: write the JSON config through the path-based helper.
		JsonArrayFileHelper.write(
			path,
			1,
			List.of("new"),
			Codec.STRING,
			JsonOps.INSTANCE,
			error -> Assertions.fail("Unexpected codec error: " + error),
			(element, exception) -> Assertions.fail("Unexpected element exception: " + exception)
		);

		// Assertions: the helper creates parent directories before writing the file.
		Assertions.assertEquals("[\n  {\"version\":1},\n  \"new\"\n]", Files.readString(path));
	}

	@Test
	public void writeToFileKeepsExistingFileWhenCollectionIterationFails() throws IOException {
		// Setup: an existing config file is present, and the replacement collection fails while being iterated.
		Path path = tempDir.resolve("bookmarks.json");
		String originalContents = "[\n  {\"version\":1},\n  \"existing\"\n]";
		Files.writeString(path, originalContents);
		Collection<String> failingElements = new FailingIterable();

		// Operation: attempt to write the replacement JSON array.
		Assertions.assertThrows(
			ConcurrentModificationException.class,
			() -> JsonArrayFileHelper.write(
				path,
				1,
				failingElements,
				Codec.STRING,
				JsonOps.INSTANCE,
				error -> Assertions.fail("Unexpected codec error: " + error),
				(element, exception) -> Assertions.fail("Unexpected element exception: " + exception)
			)
		);

		// Assertions: the original file is intact, and the failed temporary write is cleaned up.
		Assertions.assertEquals(originalContents, Files.readString(path));
		try (Stream<Path> files = Files.list(tempDir)) {
			Assertions.assertEquals(List.of(path), files.toList());
		}
	}

	@Test
	public void writeToFileSkipsElementsThatThrowDuringEncoding() throws IOException {
		// Setup: the codec throws for one element, and the caller records that element-level failure.
		Path path = tempDir.resolve("bookmarks.json");
		List<String> elementExceptions = new ArrayList<>();
		Codec<String> codec = Codec.STRING.xmap(
			value -> value,
			value -> {
				if (value.equals("bad")) {
					throw new IllegalStateException("test");
				}
				return value;
			}
		);

		// Operation: write a collection containing valid elements around the bad element.
		JsonArrayFileHelper.write(
			path,
			1,
			List.of("first", "bad", "second"),
			codec,
			JsonOps.INSTANCE,
			error -> Assertions.fail("Unexpected codec error: " + error),
			(element, exception) -> elementExceptions.add(element)
		);

		// Assertions: only the bad element is skipped, and the valid elements are still saved.
		Assertions.assertEquals(List.of("bad"), elementExceptions);
		Assertions.assertEquals("[\n  {\"version\":1},\n  \"first\",\n  \"second\"\n]", Files.readString(path));
	}

	private static class FailingIterable extends AbstractCollection<String> {
		@Override
		public Iterator<String> iterator() {
			return new Iterator<>() {
				private int index;

				@Override
				public boolean hasNext() {
					return index < 2;
				}

				@Override
				public String next() {
					index++;
					if (index == 2) {
						throw new ConcurrentModificationException();
					}
					return "new";
				}
			};
		}

		@Override
		public int size() {
			return 2;
		}
	}
}
