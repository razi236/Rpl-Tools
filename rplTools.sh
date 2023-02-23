#!/bin/bash
echo "****************************************"
echo "*          Welcome to RplTools         *"
echo "****************************************"
echo "Press 1 for Simulation"
echo "Press 2 for Cost Analysis"
read option
if [[ "$OSTYPE" == "darwin"* ]]; then
    {
    if [ $option = "1" ]
    then
    {
      echo "Please enter the filename:"
      read file
      start=`echo $(($(gdate +%s%N)/1000000))`
      frontend/bin/absc -s ./examples/$file
      end=`echo $(($(gdate +%s%N)/1000000))`
      echo Execution time was `expr $end - $start` mili seconds.
    }
    # shellcheck disable=SC1073
    elif [ $option = "2" ]
    then
    {
      echo "Please enter the filename:"
      read file
      start=`echo $(($(gdate +%s%N)/1000000))`
      frontend/bin/absc -c ./examples/$file
      end=`echo $(($(gdate +%s%N)/1000000))`
      echo Execution time was `expr $end - $start` mili seconds.
      grep -o '\bmax\b' Cost-Analysis/CostEquations.txt | wc -l
    }
    # shellcheck disable=SC1131
    else
    {
        echo "Wrong selection"
    }
    fi
    }
else
    {
    if [ $option = "1" ]
    then
    {
      echo "Please enter the filename:"
      read file
      start=`echo $(date +%s)`
      frontend/bin/absc -s ./examples/$file
      end=`echo $($(date +%s))`
      echo Execution time was `expr $end - $start` seconds.
    }
    # shellcheck disable=SC1073
    elif [ $option = "2" ]
    then
    {
      echo "Please enter the filename:"
      read file
      start=`echo $(gdate +%s)`
      frontend/bin/absc -c ./examples/$file
      end=`echo $(gdate +%s)`
      echo Execution time was `expr $end - $start` mili seconds.
      grep -o '\bmax\b' Cost-Analysis/CostEquations.txt | wc -l
    }
    else
    {
        echo "Wrong selection"

    }
    fi
    }
fi
