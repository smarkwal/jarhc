/*
 * Copyright 2022 Stephan Markwalder
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

package org.jarhc.loader.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jarhc.utils.ByteBuffer;
import org.junit.jupiter.api.Test;

class ZipStreamArchiveTest {

	@Test
	void test() throws IOException {

		File file = new File("src/unitTest/resources/org/jarhc/loader/archive/a.jar");
		try (Archive archive = new ZipStreamArchive(new FileInputStream(file))) {

			List<String> files = new ArrayList<>();
			while (true) {
				ArchiveEntry entry = archive.getNextEntry();
				if (entry == null) break;
				ByteBuffer data = entry.getData();
				files.add(entry.getName() + " : " + data.getLength());
			}

			assertEquals("b2de6f7c6eff51a28729be9c4f6555354f16a1ca", archive.getFileChecksum());
			assertEquals(678, archive.getFileSize());
			assertEquals(Arrays.asList("META-INF/MANIFEST.MF : 61", "a/A.class : 178"), files);
		}
	}

}