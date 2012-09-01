#! /bin/sh

cp=""
for i in lib/* 
do
cp=$cp:$i
done
java -cp $cp License3j $*
echo $cp