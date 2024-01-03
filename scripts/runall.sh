#!/bin/bash
set -e
rm -f alice-*.txt
rm -f bob-*.txt

for i in {1..10}
do
	for j in {1..10}
	do
		echo "Running with ${i}00 samples (Alice) and ${j}00 samples (Bob)."
		./DecisionTree alice n=${i}00 > alice-$i-$j.txt 2>&1 &
		echo "Waiting for completion"
		./DecisionTree bob n=${j}00 > bob-$i-$j.txt 2>&1
		wait
	done
done
