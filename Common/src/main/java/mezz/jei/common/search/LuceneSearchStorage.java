package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LRUQueryCache;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.UsageTrackingQueryCachingPolicy;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.IOConsumer;
import org.apache.lucene.util.UnicodeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Stores values in memory for literal, case-sensitive substring searches.
 * <p>
 * Keys exceeding {@link IndexWriter#MAX_TERM_LENGTH} UTF-8 bytes are not searchable.
 * Their values remain available through {@link #getAllElements(Consumer)}.
 */
public class LuceneSearchStorage<T> implements ISearchStorage<T> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";
	// Keep the seven default indexes within Lucene's usual shared-cache budget.
	private static final int MAX_CACHED_QUERIES = 128;
	private static final long MAX_QUERY_CACHE_BYTES = Math.min(4L * 1024 * 1024, Runtime.getRuntime().maxMemory() / 160);
	private static final Analyzer ANALYZER = new Analyzer() {
		@Override
		protected TokenStreamComponents createComponents(String fieldName) {
			throw new UnsupportedOperationException("StringField does not use an analyzer");
		}
	};

	private final Directory directory = new ByteBuffersDirectory();
	private final LRUQueryCache queryCache = new LRUQueryCache(MAX_CACHED_QUERIES, MAX_QUERY_CACHE_BYTES);
	private final UsageTrackingQueryCachingPolicy queryCachingPolicy = new UsageTrackingQueryCachingPolicy();
	private final Map<T, Integer> valueIds = new IdentityHashMap<>();
	private final List<@Nullable T> values = new ArrayList<>();
	private final ArrayDeque<Integer> freeValueIds = new ArrayDeque<>();
	private final ArrayList<Entry> pending = new ArrayList<>();
	@Nullable
	private DirectoryReader reader;
	@Nullable
	private IndexSearcher searcher;
	private boolean readerDirty;

	@Override
	public void put(String key, T value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		int valueId = valueIds.computeIfAbsent(value, v -> {
			if (!freeValueIds.isEmpty()) {
				int id = freeValueIds.removeFirst();
				values.set(id, v);
				return id;
			}
			int id = values.size();
			values.add(v);
			return id;
		});
		int utf8Length = getUtf8Length(key);
		if (utf8Length > IndexWriter.MAX_TERM_LENGTH) {
			String preview = key.substring(0, key.offsetByCodePoints(0, 80))
				.replace("\r", "\\r")
				.replace("\n", "\\n")
				.replace("\t", "\\t");
			LOGGER.warn("Skipping ingredient search string with {} UTF-8 bytes, exceeding Lucene's {}-byte limit. Starts with: \"{}...\"", utf8Length, IndexWriter.MAX_TERM_LENGTH, preview);
			return;
		}
		pending.add(new Entry(key, valueId));
	}

	@Override
	public boolean supportsDeletion() {
		return true;
	}

	@Override
	public boolean removeAll(Collection<T> removedValues) {
		Set<Integer> removedIds = new HashSet<>();
		for (T value : removedValues) {
			Integer id = valueIds.get(value);
			if (id != null) {
				removedIds.add(id);
			}
		}
		if (!removedIds.isEmpty()) {
			writeToIndex(writer -> writer.deleteDocuments(IntPoint.newSetQuery(VALUE_FIELD, removedIds)));
			pending.removeIf(entry -> removedIds.contains(entry.valueId()));
			for (Integer id : removedIds) {
				valueIds.remove(values.get(id));
				values.set(id, null);
				freeValueIds.addLast(id);
			}
		}
		return true;
	}

	private void writeToIndex(IOConsumer<IndexWriter> action) {
		try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(ANALYZER))) {
			try {
				action.accept(writer);
			} catch (IOException | RuntimeException e) {
				try {
					writer.rollback();
				} catch (IOException rollbackException) {
					e.addSuppressed(rollbackException);
				}
				throw e;
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		readerDirty = true;
	}

	void refresh() {
		try {
			if (!pending.isEmpty()) {
				writeToIndex(writer -> {
					for (Entry entry : pending) {
						Document document = new Document();
						document.add(new StringField(KEY_FIELD, entry.key(), Field.Store.NO));
						// Document ids can change during merges; use our own stable value ids.
						document.add(new NumericDocValuesField(VALUE_FIELD, entry.valueId()));
						document.add(new IntPoint(VALUE_FIELD, entry.valueId()));
						writer.addDocument(document);
					}
				});
				pending.clear();
				pending.trimToSize();
			}
			if (readerDirty) {
				DirectoryReader previous = reader;
				DirectoryReader updated;
				if (previous == null) {
					updated = DirectoryReader.open(directory);
				} else {
					updated = DirectoryReader.openIfChanged(previous);
				}
				if (updated != null) {
					reader = updated;
					searcher = new IndexSearcher(updated);
					// The shared cache relies on close notifications; this cache dies with the storage.
					searcher.setQueryCache(queryCache);
					searcher.setQueryCachingPolicy(queryCachingPolicy);
					readerDirty = false;
					if (previous != null) {
						previous.close();
					}
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {
		refresh();
		IndexSearcher searcher = this.searcher;
		if (searcher != null && getUtf8Length(token) <= IndexWriter.MAX_TERM_LENGTH) {
			WildcardQuery query = new WildcardQuery(new Term(KEY_FIELD, containsPattern(token)));
			try {
				searcher.search(query, new CollectorManager<ValueCollector, Void>() {
					@Override
					public ValueCollector newCollector() {
						return new ValueCollector();
					}

					@Override
					public @Nullable Void reduce(Collection<ValueCollector> collectors) {
						for (ValueCollector collector : collectors) {
							if (!collector.results.isEmpty()) {
								resultsConsumer.accept(collector.results);
							}
						}
						return null;
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private static int getUtf8Length(String text) {
		return UnicodeUtil.calcUTF16toUTF8Length(text, 0, text.length());
	}

	private static String containsPattern(String token) {
		StringBuilder pattern = new StringBuilder(token.length() + 2);
		pattern.append('*');
		for (int i = 0; i < token.length(); i++) {
			char c = token.charAt(i);
			if (c == '*' || c == '?' || c == '\\') {
				pattern.append('\\');
			}
			pattern.append(c);
		}
		return pattern.append('*').toString();
	}

	@Override
	public void getAllElements(Consumer<Collection<T>> resultsConsumer) {
		resultsConsumer.accept(Collections.unmodifiableSet(valueIds.keySet()));
	}

	@Override
	public String statistics() {
		try {
			long bytes = 0;
			for (String file : directory.listAll()) {
				bytes += directory.fileLength(file);
			}
			return "LuceneSearchStorage: values=" + valueIds.size() + ", indexBytes=" + bytes +
				", pending=" + pending.size();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private record Entry(String key, int valueId) {}

	private class ValueCollector implements Collector {
		private final List<T> results = new ArrayList<>();

		@Override
		public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
			NumericDocValues valueIds = DocValues.getNumeric(context.reader(), VALUE_FIELD);
			return new LeafCollector() {
				@Override
				public void setScorer(Scorable scorer) {
				}

				@Override
				public void collect(int doc) throws IOException {
					if (valueIds.advanceExact(doc)) {
						results.add(Objects.requireNonNull(values.get(Math.toIntExact(valueIds.longValue()))));
					}
				}
			};
		}

		@Override
		public ScoreMode scoreMode() {
			return ScoreMode.COMPLETE_NO_SCORES;
		}
	}
}
