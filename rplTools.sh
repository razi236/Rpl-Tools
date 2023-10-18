#!/bin/bash
echo "****************************************"
echo "*          Welcome to RplTools         *"
echo "****************************************"
echo "Press 1 for Simulation"
echo "Press 2 for Cost Analysis"
echo "Press 3 for RPL to ABS translation"
read option
if [[ "$OSTYPE" == "darwin"* ]]; then
    {
    if [ $option = "1" ]
    then
    {
      [ -f test.csv ] && rm test.csv
      echo "Please enter the filename:"
      read file
      start=`echo $(($(gdate +%s%N)/1000000))`
      frontend/bin/absc -s ./examples/$file
      cp RABS.abs ABS.rpl

      c=1
      frontend/bin/absc -e ABS.rpl
      while [ $c -le 1 ]
      do

          # timeout 25
          gen/erl/run >> test.csv
          (( c++ ))
      done
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
      grep -o '\bmax\b' CostEquations.txt | wc -l
    }
    # shellcheck disable=SC1131
    elif [ $option = "3" ]
    then
    {
      echo "Please enter the filename:"
      read file
      start=`echo $(($(gdate +%s%N)/1000000))`
      frontend/bin/absc -t ./examples/$file
      end=`echo $(($(gdate +%s%N)/1000000))`
      echo Execution time was `expr $end - $start` mili seconds.
    }
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
      start=`date +%s`
      frontend/bin/absc -s ./examples/$file
      cp RABS.abs ABS.rpl
      frontend/bin/absc -e ABS.rpl
      gen/erl/run
      end=`date +%s`
      echo Execution time was `expr $end - $start` seconds.
    }
    # shellcheck disable=SC1073
    elif [ $option = "2" ]
    then
    {
        echo "Please enter the filename:"
        read file
        start=`date +%s`
        frontend/bin/absc -s ./examples/$file
        cp RABS.abs ABS.rpl
        frontend/bin/absc -e ABS.rpl
        gen/erl/run
        end=`date +%s`
        echo Execution time was `expr $end - $start` seconds.
        grep -o '\bmax\b' CostEquations.txt | wc -l
    }
    # shellcheck disable=SC1131
    elif [ $option = "3" ]
    then
    {
        echo "Please enter the filename:"
        read file
        #start=`echo $($(date +%s))`
        start=`date +%s`
        frontend/bin/absc -t ./examples/$file
        end=`date +%s`
        echo Execution time was `expr $end - $start` seconds.
    }
    else
    {
        echo "Wrong selection"

    }
    fi
    }
fi
