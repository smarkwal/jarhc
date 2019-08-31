/*
 * Copyright 2019 Stephan Markwalder
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

import static org.junit.jupiter.api.Assertions.assertSame;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class MavenCentralRepositoryTest {

	@Test
	void findBestMatch_forSingleDocument() {

		// prepare
		JSONObject doc = new JSONObject();
		doc.put("id", "artifact-id");

		JSONArray docs = new JSONArray();
		docs.put(doc);

		// test
		JSONObject result = MavenCentralRepository.findBestMatch(docs);

		// assert
		assertSame(doc, result);

	}

	@Test
	void findBestMatch_forMultipleDocuments() {

		// prepare
		JSONObject doc1 = new JSONObject();
		doc1.put("id", "medium-artifact-id");
		JSONObject doc2 = new JSONObject();
		doc2.put("id", "short-artifact-id");
		JSONObject doc3 = new JSONObject();
		doc3.put("id", "loooooong-artifact-id");

		JSONArray docs = new JSONArray();
		docs.put(doc1);
		docs.put(doc2);
		docs.put(doc3);

		// test
		JSONObject result = MavenCentralRepository.findBestMatch(docs);

		// assert
		assertSame(doc2, result);

	}

}