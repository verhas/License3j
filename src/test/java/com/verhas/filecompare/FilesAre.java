package com.verhas.filecompare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FilesAre {

	public static boolean theSame(String fileName1, String fileName2)
			throws IOException {
		BufferedReader reader1 = new BufferedReader(new FileReader(new File(
				fileName1)));
		BufferedReader reader2 = new BufferedReader(new FileReader(new File(
				fileName2)));
		try {
			do {
				final SourceLine line1 = new SourceLine(reader1.readLine());
				final SourceLine line2 = new SourceLine(reader2.readLine());
				if (bothAreEof(line1, line2)) {
					return true;
				}
				if (oneIsEof(line1, line2)) {
					return false;
				}
				if (theyAreNotComments(line1, line2) && line1.doesNotEqual(line2)) {
					return false;
				}
			} while (true);
		} finally {
			reader1.close();
			reader2.close();
		}
	}

	private static boolean bothAreEof(SourceLine l1, SourceLine l2) {
		return l1.eof() && l2.eof();
	}

	private static boolean oneIsEof(SourceLine l1, SourceLine l2) {
		return l1.eof() || l2.eof();
	}

	private static boolean theyAreNotComments(SourceLine l1, SourceLine l2) {
		return !(l1.isComment() && l2.isComment());
	}
}
