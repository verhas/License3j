# Manage Licenses using License3j and GPG

## Introduction

This is a short tutorial that shows you how to handle your
keys (private and public) to create license using applications.

To handle coded licenses you need a key that encodes the license.
To decode the license you need another key. The coding and
decoding keys are not the same. This is a nice point in
asymetric cryptography and makes License3j available.

If both the coding and the decoding used the same key then
the license application would need the same key
as the one that encoded the license. This way the key (the only
key) would be available in the executing application and hackers
very soon would extract the key no matter how hard you try to hide
and bury it in the code. After that they could be able to create
new licenses. It is nonsense.

When you encode a license then you use a key that is known only
to you. You keep it secret. The public pair of the keys that
is needed to decode the license will go into the JAR file
of the application.

Well, almost. To be more precise it is not the keys naked that
are handled, rather so called key-ring files. These key-ring
files contain more keys. Some key-ring file contain only public
keys (`pubring.pgp`), while the other contains private keys
as well (`secring.pgp`).

To start work you need to generate keys and to store them in
key ring files. For the purpose we use GPG, an open source
version of PGP, and the demo screens will come from Windows.
It should be similar under Linux.

To start GPG you have to type `gpg -h` at the DOS
prompt and you will get the help screen:

```
E:\PROJECTS\research\licensor>gpg -h
gpg (GnuPG) 1.4.9 (Gpg4win 1.1.4)
Copyright (C) 2008 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Home: C:/Documents and Settings/verhas/Application Data/gnupg
Supported algorithms:
Pubkey: RSA, RSA-E, RSA-S, ELG-E, DSA
Cipher: 3DES, CAST5, BLOWFISH, AES, AES192, AES256, TWOFISH
Hash: MD5, SHA1, RIPEMD160, SHA256, SHA384, SHA512, SHA224
Compression: Uncompressed, ZIP, ZLIB, BZIP2

Syntax: gpg [options] [files]
sign, check, encrypt or decrypt
default operation depends on the input data

Commands:

-s, --sign [file]             make a signature
    --clearsign [file]        make a clear text signature
-b, --detach-sign             make a detached signature
-e, --encrypt                 encrypt data
-c, --symmetric               encryption only with symmetric cipher
-d, --decrypt                 decrypt data (default)
    --verify                  verify a signature
    --list-keys               list keys
    --list-sigs               list keys and signatures
    --check-sigs              list and check key signatures
    --fingerprint             list keys and fingerprints
-K, --list-secret-keys        list secret keys
    --gen-key                 generate a new key pair
    --delete-keys             remove keys from the public keyring
    --delete-secret-keys      remove keys from the secret keyring
    --sign-key                sign a key
    --lsign-key               sign a key locally
    --edit-key                sign or edit a key
    --gen-revoke              generate a revocation certificate
    --export                  export keys
    --send-keys               export keys to a key server
    --recv-keys               import keys from a key server
    --search-keys             search for keys on a key server
    --refresh-keys            update all keys from a keyserver
    --import                  import/merge keys
    --card-status             print the card status
    --card-edit               change data on a card
    --change-pin              change a card's PIN
    --update-trustdb          update the trust database
    --print-md algo [files]   print message digests

Options:

-a, --armor                   create ascii armored output
-r, --recipient NAME          encrypt for NAME
-u, --local-user              use this user-id to sign or decrypt
-z N                          set compress level N (0 disables)
    --textmode                use canonical text mode
-o, --output                  use as output file
-v, --verbose                 verbose
-n, --dry-run                 do not make any changes
-i, --interactive             prompt before overwriting
    --openpgp                 use strict OpenPGP behavior
    --pgp2                    generate PGP 2.x compatible messages

(See the man page for a complete listing of all commands and options)

Examples:

-se -r Bob [file]          sign and encrypt for user Bob
--clearsign [file]         make a clear text signature
--detach-sign [file]       make a detached signature
--list-keys [names]        show keys
--fingerprint [names]      show fingerprints

Please report bugs to <gnupg-bugs@gnu.org>.

E:\PROJECTS\research\licensor>
```

To generate a key pair you have to issue the command

```
E:\PROJECTS\research\licensor>gpg --gen-key
gpg (GnuPG) 1.4.9; Copyright (C) 2008 Free Software Foundation, Inc.
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Please select what kind of key you want:
  (1) DSA and Elgamal (default)
  (2) DSA (sign only)
  (5) RSA (sign only)
Your selection? 5
```

You can choose any of the three algorithms. However pay attention that
the third (RSA) is identified by the number 5 and not 3!

```
RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (2048) 4096
```

You should choose a key size. Let it be 4096. It could be anything between 1024
and 4096, but I am paranoid.

```
Requested keysize is 4096 bits
Please specify how long the key should be valid.
        0 = key does not expire
     <n>  = key expires in n days
     <n>w = key expires in n weeks
     <n>m = key expires in n months
     <n>y = key expires in n years
Key is valid for? (0) 0
Key does not expire at all
Is this correct? (y/N) Y
```

I chose that the license key does not expire. In that case GPG asks if that is
really what you wanted. (This time I was not paranoid.)

```
You need a user ID to identify your key; the software constructs the user ID
from the Real Name, Comment and Email Address in this form:
   "Heinrich Heine (Der Dichter) <heinrichh@duesseldorf.de>"

Real name: GrooWikiDemoKey
Email address:
Comment:
```

Then I selected an ID for the key. Because this name will be used to
reference the encoding key, and because this key is technical and does not
belong to a person I suggest that you specify a spaceless string as
real name and leave the comment and the email address empty just pressing
enter.

```
You selected this USER-ID:
   "GrooWikiDemoKey"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
```

After you have done this you have to confirm your selection of name for the key.

```
You need a Passphrase to protect your secret key.
```

This time STOP, and read first the following sentences. Now you have to specify
a password that will be used when a license is encoded. Write it down to a piece
of paper, or better use KeePass to store it. Or simply leave the passphrase
empty, but in that case store your secret key ring file in a secure place.

```
We need to generate a lot of random bytes. It is a good idea to perform
some other action (type on the keyboard, move the mouse, utilize the
disks) during the prime generation; this gives the random number
generator a better chance to gain enough entropy.
gpg: NOTE: you should run 'diskperf -y' to enable the disk statistics
gpg: NOTE: you should run 'diskperf -y' to enable the disk statistics
gpg: NOTE: you should run 'diskperf -y' to enable the disk statistics
gpg: NOTE: you should run 'diskperf -y' to enable the disk statistics
MQM could not display the text for error 536895891.
              </pre>
Now go for a coffe. It will take a long time until you see:
<pre>
gpg: key 1D83E1B0 marked as ultimately trusted
public and secret key created and signed.

gpg: checking the trustdb
gpg: 3 marginal(s) needed, 1 complete(s) needed, PGP trust model
gpg: depth: 0  valid:   2  signed:   0  trust: 0-, 0q, 0n, 0m, 0f, 2u
pub   4096R/1D83E1B0 2009-07-05
     Key fingerprint = A5AB 8CFD ED76 38AD 0347  47F1 9CBF F306 1D83 E1B0
uid                  GrooWikiDemoKey

Note that this key cannot be used for encryption.  You may want to use
the command "--edit-key" to generate a subkey for this purpose.
```

The key rings are generated in the directory

```
C:\Documents and Settings\verhas\Application Data\gnupg
```

The name of the files are `secring.pgp` and
`pubring.pgp`.

Now that you have a public and a private key in two key ring
files we go ahead and create encoded license file. To do that
open your favourite text edit (mine one is vi) and create a
licence file:

```
edition=community
valid-until=2009.12.31
```

Save the content into a file named `license-plain.txt`

This time you have to start the command line processor included
in the license3j JAR file. You also find a bat file in the
package that starts the program so you have to type:

```
E:\PROJECTS\research\licensor>license3j.bat encode --license-file=license-plain.
txt --keyring-file="c:\Documents and Settings\verhas\Application Data\gnupg\secr
ing.gpg" --key=GrooWikiDemoKey --password=******** --output=license.out
```

The `********` should of course be replaced with the
actual password of the secret key that you specified when
you created the key.

Now you can decode the license. This step will be done in your
application program. Even though there is a 'decode' command
in license3j program that decodes the license and displays the
parameters that should otherwise be checked by the application.
Now type

```
E:\PROJECTS\research\licensor>license3j.bat decode --license-file=license.out --
keyring-file="c:\Documents and Settings\verhas\Application Data\gnupg\pubring.gpg"
```

and you will get the result:

```
---LICENSE STRING PLAIN TEXT START
-- listing properties --
--=listing properties --
valid-until=2009.12.31
edition=community
--LICENSE STRING PLAIN TEXT END
Encoding license key id=7940001403357967922L
--KEY RING DIGEST START
byte [] digest = new byte[] {
(byte)0x34,
(byte)0xF6, (byte)0x86, (byte)0x9A, (byte)0xB7, (byte)0xC4, (byte)0x75, (byte)0xE4, (byte)0xB3,
(byte)0x48, (byte)0xF1, (byte)0x21, (byte)0xBA, (byte)0x75, (byte)0x08, (byte)0x1B, (byte)0x5E,
(byte)0x16, (byte)0x5D, (byte)0xB1, (byte)0x12, (byte)0x89, (byte)0x67, (byte)0x0E, (byte)0xB2,
(byte)0x3C, (byte)0x42, (byte)0x31, (byte)0xB2, (byte)0x2B, (byte)0xD7, (byte)0x24,
};
---KEY RING DIGEST END

E:\PROJECTS\research\licensor>
```

Using this command you can check that a license is decodable
but more importantly you can get the ID of the key as returned
by the method `getDecodeKeyId()` and the digest of the
key ring that you are going to package into your JAR file.

To see how to embed the license checking in your application visit the
page [sample license checking](sample.md)
