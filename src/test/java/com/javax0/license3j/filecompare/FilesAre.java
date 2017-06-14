package com.javax0.license3j.filecompare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FilesAre {

  public static boolean theSame(String fnA, String fnB)
      throws IOException {
    try (final BufferedReader rA = new BufferedReader(new FileReader(new File(fnA)));
         final BufferedReader rB = new BufferedReader(new FileReader(new File(fnB)))) {
      do {
        final SourceLine a = new SourceLine(rA.readLine());
        final SourceLine b = new SourceLine(rB.readLine());
        if (bothAreEof(a, b)) {
          return true;
        }
        if (oneIsEof(a, b)) {
          return false;
        }
        if (theyAreNotComments(a, b) && a.doesNotEqual(b)) {
          return false;
        }
      } while (true);
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
