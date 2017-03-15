/*
 * Copyright 2012 Alessandro Bahgat Shehata
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.abahgat.suffixtree;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import gnu.trove.set.TIntSet;

/**
 * Represents a node of the generalized suffix tree graph
 *
 * @see GeneralizedSuffixTree
 * <p>
 * Edited by mezz:
 * - Use Java 6 features
 * - improve performance of search by passing a set around instead of creating new ones and using addAll
 * - only allow full searches
 * - add nullable/nonnull annotations
 * - formatting
 */
class Node {

	/**
	 * The payload array used to store the data (indexes) associated with this node.
	 * In this case, it is used to store all property indexes.
	 * <p>
	 * As it is handled, it resembles an ArrayList: when it becomes full it
	 * is copied to another bigger array (whose size is equals to data.length +
	 * INCREMENT).
	 * <p>
	 * Originally it was a List<Integer> but it took too much memory, changing
	 * it to int[] take less memory because indexes are stored using native
	 * types.
	 */
	private int[] data;
	/**
	 * Represents index of the last position used in the data int[] array.
	 * <p>
	 * It should always be less than data.length
	 */
	private int lastIdx = 0;
	/**
	 * The starting size of the int[] array containing the payload
	 */
	private static final int START_SIZE = 0;
	/**
	 * The increment in size used when the payload array is full
	 */
	private static final int INCREMENT = 1;
	/**
	 * The set of edges starting from this node
	 */
	private final EdgeBag edges;
	/**
	 * The suffix link as described in Ukkonen's paper.
	 * if str is the string denoted by the path from the root to this, this.suffix
	 * is the node denoted by the path that corresponds to str without the first char.
	 */
	@Nullable
	private Node suffix;
	/**
	 * The total number of <em>different</em> results that are stored in this
	 * node and in underlying ones (i.e. nodes that can be reached through paths
	 * starting from <tt>this</tt>.
	 * <p>
	 * This must be calculated explicitly using computeAndCacheCount
	 *
	 * @see Node#computeAndCacheCount()
	 */
	private int resultCount = -1;

	/**
	 * Creates a new Node
	 */
	Node() {
		edges = new EdgeBag();
		suffix = null;
		data = new int[START_SIZE];
	}

	/**
	 * Gets the first <tt>numElements</tt> elements from the ones associated to this node.
	 * <p>
	 * Gets data from the payload of both this node and its children, the string representation
	 * of the path to this node is a substring of the one of the children nodes.
	 */
	void getData(TIntSet ret) {
		ret.addAll(data);

		for (Edge e : edges.getValues()) {
			e.getDest().getData(ret);
		}
	}

	/**
	 * Adds the given <tt>index</tt> to the set of indexes associated with <tt>this</tt>
	 */
	void addRef(int index) {
		if (contains(index)) {
			return;
		}

		addIndex(index);

		// add this reference to all the suffixes as well
		Node iter = this.suffix;
		while (iter != null) {
			if (iter.contains(index)) {
				break;
			}
			iter.addRef(index);
			iter = iter.suffix;
		}

	}

	/**
	 * Tests whether a node contains a reference to the given index.
	 * <p>
	 * <b>IMPORTANT</b>: it works because the array is sorted by construction
	 *
	 * @param index the index to look for
	 * @return true <tt>this</tt> contains a reference to index
	 */
	private boolean contains(int index) {
		return Arrays.binarySearch(data, 0, lastIdx, index) >= 0;
	}

	/**
	 * Computes the number of results that are stored on this node and on its
	 * children, and caches the result.
	 * <p>
	 * Performs the same operation on subnodes as well
	 *
	 * @return the number of results
	 */
	protected int computeAndCacheCount() {
		computeAndCacheCountRecursive();
		return resultCount;
	}

	private Set<Integer> computeAndCacheCountRecursive() {
		Set<Integer> ret = new HashSet<Integer>();
		for (int num : data) {
			ret.add(num);
		}
		for (Edge e : edges.getValues()) {
			ret.addAll(e.getDest().computeAndCacheCountRecursive());
		}

		resultCount = ret.size();
		return ret;
	}

	void addEdge(char ch, Edge e) {
		edges.put(ch, e);
	}

	@Nullable
	Edge getEdge(char ch) {
		return edges.get(ch);
	}

	@Nullable
	Node getSuffix() {
		return suffix;
	}

	void setSuffix(Node suffix) {
		this.suffix = suffix;
	}

	private void addIndex(int index) {
		if (lastIdx == data.length) {
			int[] copy = new int[data.length + INCREMENT];
			System.arraycopy(data, 0, copy, 0, data.length);
			data = copy;
		}
		data[lastIdx++] = index;
	}
}
