package com.javax0.license3j.filecompare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilesAre {
    private static int TYPICAL_TEST_FILE_SIZE = 25;

    /**
     * <p>
     * Compares two files after reading the lines of the files from the disk and sorting their lines.
     * Comment lines (starting with #) are ignored.
     * </p>
     * <p>
     * This method is used to compare the decoded license file with a reference file that the test relies on.
     * Since different Java versions may sort the properties in different order the reference file may contain
     * the properties to different order. To cope with this the implementation of this method sorts the lines
     * of the files before comparing them. This may lead to some undetected error when the decoded file contains
     * the digest part or the separator lines in wrong order, but otherwise excatly same as in the reference file.
     * The probability of this is extremely small.
     * </p>
     *
     * @param fnA the name of the first file
     * @param fnB the name of the second file
     * @return true if the files contain the same lines
     * @throws IOException if one of the files can not be read
     */
    public static boolean theSame(String fnA, String fnB)
            throws IOException {
        final List<String> linesA = new ArrayList<>(TYPICAL_TEST_FILE_SIZE);
        final List<String> linesB = new ArrayList<>(TYPICAL_TEST_FILE_SIZE);
        boolean sizesAreDifferent = readFilesToLists(fnA, fnB, linesA, linesB);
        if (sizesAreDifferent) {
            return false;
        }
        Collections.sort(linesA);
        Collections.sort(linesB);
        int i = 0;
        while (i < linesA.size()) {
            if (i >= linesA.size()) {
                return true;
            }
            final SourceLine a = new SourceLine(linesA.get(i));
            final SourceLine b = new SourceLine(linesB.get(i));
            if (theyAreNotComments(a, b) && a.doesNotEqual(b)) {
                return false;
            }
            i++;
        }
        return true;
    }

    /**
     * Read the content of the files in to the lists.
     *
     * @param fnA    name of the first file
     * @param fnB    name of the second file
     * @param linesA lines for the first files
     * @param linesB lines for the second file
     * @return true if the number of the lines in one file is not the same as in the other, in which case the two
     * files are not the same obviously.
     * @throws IOException if one of the files cannot be read
     */
    private static boolean readFilesToLists(String fnA, String fnB, List<String> linesA, List<String> linesB)
            throws IOException {
        try (final BufferedReader rA = new BufferedReader(new FileReader(new File(fnA)));
             final BufferedReader rB = new BufferedReader(new FileReader(new File(fnB)))) {
            while (true) {
                final String a = rA.readLine();
                final String b = rB.readLine();
                if (a == null && b == null) {
                    return false;
                }
                if ((a == null) != (b == null)) {
                    return true;
                }
                linesA.add(a);
                linesB.add(b);
            }
        }
    }


    private static boolean bothAreEof(SourceLine a, SourceLine b) {
        return a.eof() && b.eof();
    }

    private static boolean oneIsEof(SourceLine a, SourceLine b) {
        return a.eof() || b.eof();
    }

    private static boolean theyAreNotComments(SourceLine a, SourceLine b) {
        return a.isNotComment() || b.isNotComment();
    }
}
