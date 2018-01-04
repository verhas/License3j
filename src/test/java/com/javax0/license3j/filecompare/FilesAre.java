package com.javax0.license3j.filecompare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import java.util.List;

public class FilesAre {
    private static int TYPICAL_TEST_FILE_SIZE = 25;

    public static boolean theSame(String fnA, String fnB)
            throws IOException {
        final List<SourceLine> linesA = new ArrayList<>();
        final List<SourceLine> linesB = new ArrayList<>();
        try (final BufferedReader rA = new BufferedReader(new FileReader(new File(fnA)));
             final BufferedReader rB = new BufferedReader(new FileReader(new File(fnB)))) {
            do {
                final SourceLine a = new SourceLine(rA.readLine());
                final SourceLine b = new SourceLine(rB.readLine());
                if (bothAreEof(a, b)) {
                    return listAreTheSame(linesA, linesB);
                }
                if (oneIsEof(a, b)) {
                    return false;
                }
                if (theyAreNotComments(a, b)) {
                    linesA.add(a);
                    linesB.add(b);
                }
            } while (true);
        }
    }

    /**
     * Compares the lines passed as argument regardless of the ordering.
     * <p>
     * The lines that contain the key value pairs may not be listed in the same order as in the
     * source file. It may change with Java releases.
     *
     * @param linesA the first list of source lines to compare
     * @param linesB the second list of source lines to compare
     * @return true if the lists contain the same lines (even if the ordering s different)
     */
    private static boolean listAreTheSame(List<SourceLine> linesA, List<SourceLine> linesB) {
        if (linesA.size() != linesB.size()) {
            return false;
        }
        Collections.sort(linesA);
        Collections.sort(linesB);
        Iterator<SourceLine> itA = linesA.iterator();
        Iterator<SourceLine> itB = linesB.iterator();
        while (itA.hasNext()) {
            final SourceLine a = itA.next();
            final SourceLine b = itB.next();
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
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
