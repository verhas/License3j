# Sample use of license3j

This document describes via a simple example how to use license3j in your
application to check the validity of a license. We assume that you are
familiar up to some extent with public key cryptography and you read
the introduction of license3j as well as the [document](tuto.md) that
describes how to create key rings and how to encode license.

When the program is loaded you should load the encoded license
programmatically (this is the only point where you can not use GPG and this
is where you get license3j handy).

License3j can use external key ring file that is installed
along with the Java file or a key ring file installed into the
JAR file (as a resource) or you can programmatically load the license string
into the java code by any means you want to program.

The example will show you the code of Groowiki license checking. Note that
this part of groowiki is not open source. This is in the edition part and
even exposing the code that checks the license do not decrease the license
enforcement capabilities. The code presented here may not be the last version
of Groowiki, nevertheless it still works as a demonstration example. Also note
that some of the code (checking for `null` arguments, catching exceptions)
was removed to ease readability of the examples.

The class that checks the license of Groowiki has a field:

```
import com.verhas.licensor.License;

. . .

 private License lic = null;
```

The public method `checkLicense()` uses this field:

```
1.    public void checkLicense() {
2.        String licenseFileName = config.getString("edition.license.file");
3.        lic = new License();
4.        lic.loadKeyRingFromResource("pubring.gpg", digest);
5.        lic.setLicenseEncodedFromFile(licenseFileName);
6.        checkDateAndVersionValidity();
7.        edition = lic.getFeature("edition");
8.    }
```

The line 2 gets the license file name. Line 3 generates a new license. This
class is provided by license3j. It should be used to load the key ring and
to set the encoded license file. Loading the key ring the call to method
`loadKeyRingFromResource()` has two arguments.

```
4.        lic.loadKeyRingFromResource("pubring.gpg", digest);
```

`"pubring.gpg"` the name of the resource file that contains the key ring

`digest` the digest of the key ring

The key ring is a binary file. It is usually named `pubring.gpg` in your
key management. You copy this file into the Java project so that it will be
available as a resource and thus `loadKeyRingFromResource()` can load it.
(Never copy your secring into your java project!)
To load this safely you have to provide a second argument, a `byte []` byte
array that should contain the digest of the key ring. To get this array you can
use `license3j.bat`. If you type

```
license3j decode --license-file=lic.txt --keyring-file=pubring.gpg
```

it will not only print the decoded open form of the license but also the
digest of the key ring file in a format that can be copy/paste put into
your Java code:

```
---KEY RING DIGEST START
byte [] digest = new byte[] {
(byte)0x83,
(byte)0x41, (byte)0xD1, (byte)0x19, (byte)0x92, (byte)0x7B, (byte)0xC0, (byte)0x91, (byte)0x8D,
(byte)0x46, (byte)0x99, (byte)0xFF, (byte)0x29, (byte)0x24, (byte)0x26, (byte)0x22, (byte)0x80,
(byte)0x0F, (byte)0x89, (byte)0xEB, (byte)0x1C, (byte)0x03, (byte)0x10, (byte)0x23, (byte)0x3A,
(byte)0x54, (byte)0xA3, (byte)0x8C, (byte)0x4A, (byte)0x61, (byte)0x61, (byte)0xB3,
};
---KEY RING DIGEST END
```

If you are debugging, not serious or for some other reason do not want this
extra security check been performed then you can pass `null` as second
argument to method `loadKeyRingFromResource()`. For more information why
you need to include your digest into the code and pass it to the key ring
loading have a look at the page {{./fraud.html}}.

If you look at the API you can see that there are other versions of this
method, namely the overloaded `loadKeyRing()` with different parameters
that you can use to load the key ring from a file. All these methods check
the digest of the file or bypass the check if you provide `null` instead of
the digest.

The line 5 is used to load the license file itself:

```
5.        lic.setLicenseEncodedFromFile(licenseFileName);
```

The license file is encoded but this is a text file to ease the transfer via
email. For example a license file may look like this:

```
-----BEGIN PGP MESSAGE-----
Version: BCPG v1.43

owJ4nJvAy8zAxFh5I1Td/rnvDMbTBzyTxHMyk1PzilPdMnNS/RJzU3U90/Pyi1JT
vIrl5ujqKuRkFpdk5qUrFBTlF6QWlWSmFivo6vJyAdnF+Xm2AaklqUUKYalFGYnF
vFypKZklmUDRxOICoIqM/LxUW21jMw1jA01LYwMzCwNToJLcxMwc2wKQNocysDa9
5PxcXi4gUZCYV2lbVlaczMuVWFAAdFUi2LD0ovz88szsTF6uotSc1MTiVF2gvmKQ
jKGegZ4BL1dZYk5mim5KYkmqrZGBgaWugYWusQFCNYZEZnFxKYqwua6RJS9XJ5Mu
CwMjE4M4KxPI6wIy7kCLw4EWu6Tm5nunVjJwcQrAAu5WswDD5C2TS0pyrr0OatXi
Vvi2iElDPGXa/FCts/ZBZ1+IWzwyl46y+fbn3YJZ921CX2/vNVi9QNn6xkQLgxUK
Tf3JudM/3tvKteBKsMKdk66nJgV9qd5xJmSNb2Js/Nbpx9fEche3WHzsOWYU+twk
5lHGt55Y+cPzlSZOcPecHBrrdfO7p1HH7Xo+pv+emWZ319r/WXQ1SfRIpaLPfb0+
Viul7lbuWV+e3aox/L9vmxf3AauXB11sLje+PHNv7aUv28Xf9C9wvrfsq/Zt9vZD
N3iiW07p529leNfJaG6z+tikN1p2E/czSSYl/p8f/8JEhnnWxbldUX0sa2/cVWFU
EOCyYGTezbjrXNvLJ1Z9vR/l2N2zZ7YkXF0pJH9yT2hAqLa/ZVX/pv2XHn4sWsLn
mriBSd6367JhaP4JJ00L69ZH/Tf2zFa7ti5ajX0ti8u9jvf/Jm680FrW8vjLxsdv
lL/sjNtQtGTlb79gprQE960WCftNXgntXlf0P9Flxt6Jttvsi2Y9aD1y+JIP2/V3
edWOJyYVerf/vPmu+IjuMduyH/88Yr84P/iu8a7v2medirP95tqmM9+fXHRx4WVl
Zc6FZ5/e+RdYz7FnW8DMGjOT5uNyN//vPlDyWsP6ek719d1HFuxwCQu64Xhx022h
Q245ZglvTodtk71xkqOr12WzbAtHn6wOQ+vlZImw3U4bFVedbH1Y77M+2vTBJeO1
TgBy3m93
=DOIM
-----END PGP MESSAGE-----
```

The name of this file has to be passed to the method
`setLicenseEncodedFromFile()` but again there are many variations of this
method you can use to move the license information into the memory of JVM. If
you consult the JavaDoc API you will see that you can specify the license string
itself as an argument to method `setLicenseEncoded()` or load the license
string from a resource or from an input stream. At the end all these methods
do the same: the license text will be loaded into `License` object and
it will be decoded. If the file can not be found or the license can not be
decoded then the method will throw some exception. This can be `IOException`
or some exception from the underlying crypto API.

The line 6 checks the date and the validity of the license. This is a code
specific tot he application and is not provided by the license3j library. You
have total freedom to check the validity of a license. You can use dates, like
issue date, validity date, validity end date, user count and so on. They are
license <features> that you can get via the method `getFeature` providing
the key to the feature, like in the line 7:

```
6.        checkDateAndVersionValidity();
7.        edition = lic.getFeature("edition");
```

As a reference and to further help your education we present here an abbreviated
version of the method `checkDateAndVersionValidity()`:

```
1.    protected void checkDateAndVersionValidity() {
2.        String issueDate = lic.getFeature("issue-date");
3.        String today = getTodayString();
4.        LicenseDate todayLD = new LicenseDate(today);
5.        if (!todayLD.isLaterThan(issueDate)) {
6.            throw new IllegalArgumentException(
7.                    "Issue date is too late, probably tampered system time");
8.        }
9.        String validDate = lic.getFeature("valid-date");
10.        if (validDate != null) {
11.            LicenseDate validDateLD = new LicenseDate(validDate);
12.            if (todayLD.isLaterThan(validDateLD)) {
13.                throw new IllegalArgumentException("License expired.");
14.            }
15.        }
     . . .
16.    }
```

The class `LicenseDate` is a proprietary class of Groowiki that provides
date comparison for dates provided in the format `YYYY-MM-DD` which is
exclusively used for Groowiki licenses.

More or less this is all that license3j provides and that you should use. The
details of license handling above features are up to other packages.

You may be interested in [how to bind](macaddr-binding.md) a license to
the Ethernet address.
