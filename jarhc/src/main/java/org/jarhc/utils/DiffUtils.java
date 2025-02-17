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

package org.jarhc.utils;

import static org.jarhc.utils.Markdown.deleted;
import static org.jarhc.utils.Markdown.inserted;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiffUtils {

	private DiffUtils() {
		throw new IllegalStateException("utility class");
	}

	public static List<String> diff(List<String> lines1, List<String> lines2) {

		// TODO: implement a better strategy which takes into account
		//  the similarity of inserted and deleted lines,
		//  especially if they have a long common prefix.
		// Example:
		// - a.jar 1.0
		// + a.jar 1.1
		// - b.jar 1.0
		//   c.jar 2.0

		// TODO: do not depend on Markdown as output format

		int n = lines1.size();
		int m = lines2.size();

		double[][] table = new double[n + 1][m + 1];

		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				String line1 = lines1.get(i - 1);
				String line2 = lines2.get(j - 1);

				if (line1.equals(line2)) {
					table[i][j] = table[i - 1][j - 1] + 1;
				} else {
					double similarity = getSimilarityByPrefix(line1, line2);

					table[i][j] = Math.max(
							table[i][j - 1],
							table[i - 1][j]
					) + similarity;
				}
			}
		}

		// print table
		// for (int i = 0; i <= n; i++) {
		// 	for (int j = 0; j <= m; j++) {
		// 		System.out.print(table[i][j] + " ");
		// 	}
		// 	System.out.println();
		// }

		return collectChanges(table, lines1, lines2);
	}

	private static double getSimilarityByPrefix(String line1, String line2) {
		int diff = getCommonPrefixLength(line1, line2);
		int maxLength = Math.max(line1.length(), line2.length());
		return (double) diff / maxLength;
	}

	private static int getCommonPrefixLength(String line1, String line2) {
		int minLength = Math.min(line1.length(), line2.length());
		int prefixLength = 0;
		while (prefixLength < minLength && line1.charAt(prefixLength) == line2.charAt(prefixLength)) {
			prefixLength++;
		}
		return prefixLength;
	}

	private static List<String> collectChanges(double[][] table, List<String> lines1, List<String> lines2) {

		int i = lines1.size();
		int j = lines2.size();

		// prepare result list with max size
		List<String> result = new ArrayList<>(i + j);

		// backtrack through table and collect changes (end to start)
		while (i > 0 || j > 0) {
			String line1 = i > 0 ? lines1.get(i - 1) : "\u0000";
			String line2 = j > 0 ? lines2.get(j - 1) : "\u0001";
			if (i >= 0 && j >= 0 && line1.equals(line2)) {
				result.add(line1);
				i = i - 1;
				j = j - 1;
			} else if (j > 0 && (i == 0 || table[i][j - 1] >= table[i - 1][j])) {
				result.add(inserted(line2));
				j = j - 1;
			} else if (i > 0 && (j == 0 || table[i][j - 1] < table[i - 1][j])) {
				result.add(deleted(line1));
				i = i - 1;
			}
		}

		// reverse result list
		Collections.reverse(result);

		return result;
	}

}
