/*
 * Copyright 2025 Stephan Markwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jarhc.artifacts;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ArtifactVersion implements Comparable<ArtifactVersion> {

	private static final BigInteger MIN_INTEGER = BigInteger.valueOf(Integer.MIN_VALUE);
	private static final BigInteger MAX_INTEGER = BigInteger.valueOf(Integer.MAX_VALUE);

	private final String version;

	private final List<Item> items;

	private final int hash;

	public ArtifactVersion(String version) {
		this.version = requireNonNull(version, "version cannot be null");
		items = parse(version);
		hash = items.hashCode();
	}

	public ArtifactVersion(int major, int minor, int patch) {
		this.version = major + "." + minor + "." + patch;
		items = new ArrayList<>(3);
		items.add(new Item(Item.KIND_INT, major));
		items.add(new Item(Item.KIND_INT, minor));
		items.add(new Item(Item.KIND_INT, patch));
		hash = items.hashCode();
	}

	public int getMajor() {
		return getPosition(0);
	}

	public int getMinor() {
		return getPosition(1);
	}

	public int getPatch() {
		return getPosition(2);
	}

	public int getPosition(int position) {
		if (position < 0) throw new IllegalArgumentException("position < 0");

		if (position >= items.size()) {
			return 0;
		}

		for (int i = 0; i <= position; i++) {
			Item item = items.get(i);
			if (!item.isNumber()) {
				return 0;
			}
		}

		Item item = items.get(position);
		if (item.value instanceof Integer) {
			return (Integer) item.value;
		} else if (item.value instanceof BigInteger) {
			BigInteger value = (BigInteger) item.value;
			if (value.compareTo(MIN_INTEGER) < 0) {
				return Integer.MIN_VALUE;
			} else if (value.compareTo(MAX_INTEGER) > 0) {
				return Integer.MAX_VALUE;
			} else {
				return value.intValue();
			}
		}
		return 0;
	}

	public boolean isStable() {
		for (Item item : items) {
			if (item.kind == Item.KIND_QUALIFIER) {
				Integer qualifier = (Integer) item.value;
				if (qualifier < 0) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isUnstable() {
		return !isStable();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof ArtifactVersion) && compareTo((ArtifactVersion) obj) == 0;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return version;
	}

	@Override
	public int compareTo(ArtifactVersion version) {
		return compareItems(items, version.items);
	}

	private static List<Item> parse(String version) {
		List<Item> items = new ArrayList<>();

		for (Tokenizer tokenizer = new Tokenizer(version); tokenizer.next(); ) {
			Item item = tokenizer.toItem();
			items.add(item);
		}

		trimPadding(items);

		return Collections.unmodifiableList(items);
	}

	private static void trimPadding(List<Item> items) {
		Boolean number = null;
		int end = items.size() - 1;
		for (int i = end; i > 0; i--) {
			Item item = items.get(i);
			if (!Boolean.valueOf(item.isNumber()).equals(number)) {
				end = i;
				number = item.isNumber();
			}
			if (end == i
					&& (i == items.size() - 1 || items.get(i - 1).isNumber() == item.isNumber())
					&& item.compareTo(null) == 0) {
				items.remove(i);
				end--;
			}
		}
	}

	@SuppressWarnings("java:S3776") // Cognitive Complexity of methods should not be too high
	private static int compareItems(List<Item> items1, List<Item> items2) {
		boolean number = true;

		for (int index = 0; ; index++) {
			if (index >= items1.size() && index >= items2.size()) {
				return 0;
			} else if (index >= items1.size()) {
				return -comparePadding(items2, index, null);
			} else if (index >= items2.size()) {
				return comparePadding(items1, index, null);
			}

			Item item1 = items1.get(index);
			Item item2 = items2.get(index);

			if (item1.isNumber() != item2.isNumber()) {
				if (number == item1.isNumber()) {
					return comparePadding(items1, index, number);
				} else {
					return -comparePadding(items2, index, number);
				}
			} else {
				int diff = item1.compareTo(item2);
				if (diff != 0) {
					return diff;
				}
				number = item1.isNumber();
			}
		}
	}

	private static int comparePadding(List<Item> items, int index, Boolean number) {
		for (int i = index; i < items.size(); i++) {
			Item item = items.get(i);
			if (number != null && number != item.isNumber()) {
				// do not stop here, but continue, skipping non-number members
				continue;
			}
			int diff = item.compareTo(null);
			if (diff != 0) {
				return diff;
			}
		}
		return 0;
	}

	// --------------------------------------------------------------------------------------------

	private static final class Item {

		static final int KIND_MAX = 8;
		static final int KIND_BIGINT = 5;
		static final int KIND_INT = 4;
		static final int KIND_STRING = 3;
		static final int KIND_QUALIFIER = 2;
		static final int KIND_MIN = 0;

		static final Item MAX = new Item(KIND_MAX, "max");
		static final Item MIN = new Item(KIND_MIN, "min");

		private final int kind;
		private final Object value;

		Item(int kind, Object value) {
			this.kind = kind;
			this.value = value;
		}

		public boolean isNumber() {
			return (kind & KIND_QUALIFIER) == 0; // i.e. kind != string/qualifier
		}

		public int compareTo(Item item) {

			if (item == null) {
				// null in this context denotes the pad item (0 or "ga")
				switch (kind) {
					case KIND_MIN:
						return -1;
					case KIND_MAX:
					case KIND_BIGINT:
					case KIND_STRING:
						return 1;
					case KIND_INT:
					case KIND_QUALIFIER:
						return (Integer) value;
					default:
						throw new IllegalStateException("unknown version item kind " + kind);
				}
			}

			// compare kind of items
			int diff = kind - item.kind;
			if (diff != 0) {
				return diff;
			}

			// same kind of items -> compare values
			switch (kind) {
				case KIND_MAX:
				case KIND_MIN:
					return 0;
				case KIND_BIGINT:
					return ((BigInteger) value).compareTo((BigInteger) item.value);
				case KIND_INT:
				case KIND_QUALIFIER:
					return ((Integer) value).compareTo((Integer) item.value);
				case KIND_STRING:
					return ((String) value).compareToIgnoreCase((String) item.value);
				default:
					throw new IllegalStateException("unknown version item kind " + kind);
			}
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Item) && compareTo((Item) obj) == 0;
		}

		@Override
		public int hashCode() {
			return value.hashCode() + kind * 31;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}

	// --------------------------------------------------------------------------------------------

	private static final class Tokenizer {

		private static final Integer QUALIFIER_ALPHA = -5;
		private static final Integer QUALIFIER_BETA = -4;
		private static final Integer QUALIFIER_MILESTONE = -3;
		private static final Map<String, Integer> QUALIFIERS;

		static {
			QUALIFIERS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			QUALIFIERS.put("alpha", QUALIFIER_ALPHA);
			QUALIFIERS.put("beta", QUALIFIER_BETA);
			QUALIFIERS.put("milestone", QUALIFIER_MILESTONE);
			QUALIFIERS.put("cr", -2);
			QUALIFIERS.put("rc", -2);
			QUALIFIERS.put("snapshot", -1);
			QUALIFIERS.put("ga", 0);
			QUALIFIERS.put("final", 0);
			QUALIFIERS.put("release", 0);
			QUALIFIERS.put("", 0);
			QUALIFIERS.put("sp", 1);
		}

		private final String version;
		private final int versionLength;
		private int index;
		private String token;
		private boolean number;
		private boolean terminatedByNumber;

		Tokenizer(String version) {
			this.version = version.isEmpty() ? "0" : version;
			this.versionLength = this.version.length();
		}

		@SuppressWarnings("java:S3776") // Cognitive Complexity of methods should not be too high
		public boolean next() {
			if (index >= versionLength) {
				return false;
			}

			int state = -2;

			int start = index;
			int end = versionLength;
			terminatedByNumber = false;

			for (; index < versionLength; index++) {
				char c = version.charAt(index);

				if (c == '.' || c == '-' || c == '_') {
					end = index;
					index++;
					break;
				} else {
					int digit = Character.digit(c, 10);
					if (digit >= 0) {
						if (state == -1) {
							end = index;
							terminatedByNumber = true;
							break;
						}
						if (state == 0) {
							// normalize numbers and strip leading zeros (prereq for Integer/BigInteger handling)
							start++;
						}
						state = (state > 0 || digit > 0) ? 1 : 0;
					} else {
						if (state >= 0) {
							end = index;
							break;
						}
						state = -1;
					}
				}
			}

			if (end - start > 0) {
				token = version.substring(start, end);
				number = state >= 0;
			} else {
				token = "0";
				number = true;
			}

			return true;
		}

		@SuppressWarnings("java:S3776") // Cognitive Complexity of methods should not be too high
		public Item toItem() {
			if (number) {
				try {
					if (token.length() < 10) {
						return new Item(Item.KIND_INT, Integer.parseInt(token));
					} else {
						return new Item(Item.KIND_BIGINT, new BigInteger(token));
					}
				} catch (NumberFormatException e) {
					throw new IllegalStateException(e);
				}
			} else {
				if (index >= version.length()) {
					if ("min".equalsIgnoreCase(token)) {
						return Item.MIN;
					} else if ("max".equalsIgnoreCase(token)) {
						return Item.MAX;
					}
				}
				if (terminatedByNumber && token.length() == 1) {
					switch (token.charAt(0)) {
						case 'a':
						case 'A':
							return new Item(Item.KIND_QUALIFIER, QUALIFIER_ALPHA);
						case 'b':
						case 'B':
							return new Item(Item.KIND_QUALIFIER, QUALIFIER_BETA);
						case 'm':
						case 'M':
							return new Item(Item.KIND_QUALIFIER, QUALIFIER_MILESTONE);
						default:
					}
				}
				Integer qualifier = QUALIFIERS.get(token);
				if (qualifier != null) {
					return new Item(Item.KIND_QUALIFIER, qualifier);
				} else {
					return new Item(Item.KIND_STRING, token.toLowerCase(Locale.ENGLISH));
				}
			}
		}
	}

}
