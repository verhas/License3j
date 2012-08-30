#! /bin/sh

cp = ""
for i in lib/* do; cp = $cp:$i; done
echo $cp