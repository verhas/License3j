# Binding a license to network card

You may want to restrict the use of your software to a certain machine. To do
this license3j provides a class `HardwareBinder` that makes it possible to
get a hardware ID. You can encode this ID into the license as a <feature> and
when the license is checked you can calculate it again and compare the value to
the one that is in the license.

This is more restrictive than just a plain license, because it will not allow
the user to move the license from one machine to another, and it will also
require some means to generate the ID of the machine that he wants the software
to run on before the license is created.

This usually means that you have to provide an auxiliary program, that
contains license3j and calculates the machine ID. Another approach is to
provide the program you want to protect and even without a valid license it
can be used to print out the ID.

This ID is calculated from the network interfaces of the machine. The code in
`HardwareBinder` uses the network interfaces information to generate a
UUID for the machine. The code will take all network cards into account as
well as the architecture of the machine running your program. The "architecture"
of the machine is simply the string returned by the Java api
`System.getProperty("os.arch")`.

You can alter the behavior of `HardwareBinder` using methods
`ignore`_xxx_`()` and the method `setUseHwAddress()`. If you are
sure that your code runs under Java6 or later you can tell `HardwareBinder`
to use the MAC address of the interface cards when calculating the UUID.
The default is not to use the MAC address into account.

Virtual, loopback and point-to-point network cards are ignores during the
calculation of the UUID.
