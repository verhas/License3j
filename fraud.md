# Fraud license3j

There is no 100% secure license handling and license3j can be overcome. This is
not the question how to make it total secure, but rather how much a code can do
to make so secure that there is no reason to brake the license enforcement.

License3j runs in a Java environment. The easiest way to store the key ring
file is in a resource file inside a jar or war file. The format of this file is
simple and a ZIP program can be used even by a novice user to replace the ring
file to their own ring file and generate their own licenses. To avoid this
license3j provides API to check the checksum of the key ring file and also the
id of the key that was used to decode the license.

The checksum of the key ring file has to be copied into the Java source code
and you may even apply (if you want) some obfuscation code so it will be
hard to replace it and when the key ring is loaded it will ensure that the
ring itself was not tampered.

```
4.        lic.loadKeyRingFromResource("pubring.gpg", digest);
```

License3j also provides easy way to get this byte array in Java
syntax. Just dump an encoded license onto your screen using the program
`license3j.bat` and you will get not only the license text but also the
ID of the key that was used to encode the key and also the digest of the
key ring.

In case you have more than one signing key for licenses you can
also check which key was used to sign the license. You may
limit the function of your program based on the signing key.
For example you will not accept a commercial license that was
signed by a demo license key. To do that you can access the key ID calling the
method `getDecodeKeyId()`:

```
   assert -3623885160523215197L == lic.getDecodeKeyId();
```

The code above you can see in its environment in the unit test
`TestEncoding.java` available in the source distribution of license3j.
