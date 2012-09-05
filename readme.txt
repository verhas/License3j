License3j Free License management for Java

 License3j is a Java library that can be used to create and
assert license files. This way Java programs can enforce the
users to compensate their use of the software in the form of
a payment. This is the usual way when closed source programs
are distributed.

 License management alone does not guarantee that the program
will not be stolen, pirated or used in any illegal way. However
license management may increase the difficulty to use the
program illegal and therefore may drive users to become
customers. There is another effect of license management, and
that is legal. If there is sufficient license management
illegal users have less probability to successfully claim their
use was based on the lack or false of knowledge of license
conditions.

 License3j is an open source license manager that you can use
free of charge for non-profit purposes. (only non-profit???)
However...

 what is the use of a license manager for nonprofit purposes?
Nothing. And we did not want to make a software that is of
no use. Therefore this license manager is free to use for
profit purposes as well under the license terms covered by
LGPL. (If it were GPL you could not embed it into closed source
application.)

 A license for license3j is a <properties> file that is signed
using digital signature. For example an old groowiki license is

--------------------------------------------------------------------------------
person=Peter Verhas
edition=asp
phone=+36(30)9306805
email=peter@verhas.com
company=vvsc
application=groowiki
release-version=1.0.0
valid-date=2009-08-30
release-date=2009-08-30
issue-date=2009-07-29
--------------------------------------------------------------------------------

 You create the license file using a text editor
or programmatically and encode it using license3j. When your
program is used you load the license file, verify it using the
Api of license3j and then get the property values and decide
programmatically what features are available for your customer.

 Not to reinvent the wheel we utilize the library from Bouncy
Castle to perform the encryption and decryption and we use
the format PGP to store the files. Therefore you have a wide
range of tools readily available.

 You can create and encode your licenses totally programmatically,
or create the license manual and encode it using license3j or
encode it using GPG. It is your choice.

Name of the game

 There are many names that contain '2'. In these cases '2' stands
for 'to' instead of 'two'. There are names containing '4' that
stands for 'for'. For example license4j.

 '3' in license3j stands for 'free' instead of 'three'. Because
this is a free program.