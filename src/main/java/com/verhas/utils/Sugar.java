package com.verhas.utils;

import java.util.Iterator;
import java.util.Set;

/**
 * Some static function to be used in other classes to have some syntactic
 * sugar. Some day I will move these to a separate package so that you can
 * import it from maven repo to other projects. At the moment they are small and
 * simple, copy them if you like.
 *
 * @author Peter Verhas
 */
public class Sugar {

    /**
     * Use this method to create a new style (java 1.5+) 'for' loop when some
     * old library method returns iterator. For example the
     * <tt>PGPSecretKeyRingCollection.getKeyRings()</tt> returns an
     * <tt>Iterator</tt>. You can write the code
     * <pre>
     * for (final PGPSecretKeyRing kRing : in((Iterator<PGPSecretKeyRing>) pgpSec
     * .getKeyRings())) {
     * ...
     * }
     * </pre> (actual code was copied from License.java).
     *
     * @param <T> note that you can not pass a raw <tt>Iterator</tt>. You have
     * to cast it to the specific generic type Iterator as in the example above.
     * @param iterator is 'converted'
     * @return an Iterable that will iterate through the elements of the
     * iterator
     */
    public static <T> Iterable<T> in(final Iterator<T> iterator) {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                return iterator;
            }
        };
    }

    /**
     * Simple syntactic sugar to provide some reading information to the code.
     * You can use this method to embrace any argument when a method is called.
     * For example:
     * <pre>
     * encode(licensePlain, using(signatureGenerator), to(outputStream));
     * </pre> (actual code was copied from License.java).
     * {@link #to(java.lang.Object)} is also a similar method.
     * <p>
     * Note that these methods increase the size of the VM code, but any modern
     * Java implementation will optimize them off in the JIT phase.
     *
     * @param <T>
     * @param t argument to pass back on to the called
     * @return the argument as it is.
     */
    public static <T> T using(T t) {
        return t;
    }

    /**
     * For documentation see {@link #using(java.lang.Object)}.
     *
     * @param <T>
     * @param t
     * @return
     */
    public static <T> T to(T t) {
        return t;
    }

    /**
     * 
     * @param string to match
     * @param regexSet regular expressions provided as set of strings
     * @return true if the {@code string} matches any of the regular expressions
     */
    public static boolean matchesAny(String string, Set<String> regexSet) {
        for (String regex : regexSet) {
            if (string.matches(regex)) {
                return true;
            }
        }
        return false;
    }
}
