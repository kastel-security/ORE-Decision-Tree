#!/bin/bash

set -e

for dataset in "mnist 100" "titanic 357" "boston-housing 253"; do
  info=($dataset)
  datasetname=${info[0]}
  n=${info[1]}
  echo "Running with dataset $datasetname with $n samples per party."
  ./DecisionTree alice dataset=$datasetname n=$n > $datasetname-alice.txt 2>&1 &
  echo "Waiting for completion"
  sleep 1
  ./DecisionTree bob dataset=$datasetname n=$n > $datasetname-bob.txt 2>&1
  wait
  sleep 1
  echo "Repeating single-threaded"
  ./DecisionTreeSingleThreaded alice dataset=$datasetname n=$n > $datasetname-alice-sequential.txt 2>&1 &
  echo "Waiting for completion"
  sleep 1
  ./DecisionTreeSingleThreaded bob dataset=$datasetname n=$n > $datasetname-bob-sequential.txt 2>&1
  wait
  sleep 1
done 
