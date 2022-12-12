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

package org.jarhc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {

	private static final String ALGORITHM = "SHA-1";

	private static final Pool<MessageDigest> MESSAGE_DIGEST_POOL = new Pool<>(DigestUtils::createMessageDigest, MessageDigest::reset);

	private static MessageDigest createMessageDigest() {
		try {
			return MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new JarHcException("Algorithm not found: " + ALGORITHM, e);
		}
	}

	private DigestUtils() {
		throw new IllegalStateException("utility class");
	}

	public static MessageDigest getDigest() {
		return MESSAGE_DIGEST_POOL.doBorrow();
	}

	public static void returnDigest(MessageDigest digest) {
		MESSAGE_DIGEST_POOL.doReturn(digest);
	}

	public static String sha1Hex(byte[] input) {
		return sha1Hex(input, 0, input.length);
	}

	public static String sha1Hex(byte[] input, int offset, int length) {
		if (input == null) throw new IllegalArgumentException("input");
		MessageDigest digest = getDigest();
		digest.update(input, offset, length);
		byte[] array = digest.digest();
		returnDigest(digest);
		return hex(array);
	}

	public static String sha1Hex(String input) {
		if (input == null) throw new IllegalArgumentException("input");
		byte[] data = input.getBytes(StandardCharsets.UTF_8);
		return sha1Hex(data);
	}

	public static String sha1Hex(InputStream input) throws IOException {
		if (input == null) throw new IllegalArgumentException("input");
		MessageDigest digest = getDigest();
		byte[] buffer = new byte[1024];
		while (true) {
			int len = input.read(buffer);
			if (len < 0) break;
			digest.update(buffer, 0, len);
		}
		byte[] array = digest.digest();
		returnDigest(digest);
		return hex(array);
	}

	public static String hex(byte[] input) {
		StringBuilder result = new StringBuilder(input.length * 2);
		for (byte value : input) {
			result.append(Character.forDigit((value >> 4) & 0xF, 16));
			result.append(Character.forDigit((value & 0xF), 16));
		}
		return result.toString();
	}

}
