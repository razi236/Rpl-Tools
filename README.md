RplTools
=========
`RplTools` comprises two modules, simulation and a static analysis. The simulation is built on top of [ABSTools](https://github.com/abstools/abstools). To simulate an `Rpl` program, we first translate it into a corresponding `ABS` program. Afterwards, we compile and execute it with the `ABS` compiler.


## **Installing Dependencies**

Running the `RplTools` requires Java (version 11 or greater) and `ABSTools` installed. `ABSTools`'s installation requires Erlang (version 23 or greater). 

Java can be downloaded, e.g., from https://adoptopenjdk.net. Erlang is available at https://www.erlang.org/downloads (but also check below for platform-specific instructions).

### **Installing dependencies on MacOS**

On MacOS, the homebrew package manager can be used to install the dependencies. After installing homebrew, run the following commands in a terminal:

`brew tap adoptopenjdk/openjdk`

`brew install erlang git adoptopenjdk11`

### Installing dependencies on Windows

On Windows, the chocolatey package manager can be used to install the dependencies. First install chocolatey following the instructions at https://chocolatey.org/install, then run the following command in a terminal with Administrator rights:

`choco install openjdk11 git erlang visualstudio2019buildtools`

To compile the RplTools, make sure to run the command `./gradlew` build from a developer shell (Start -> Visual Studio 2019 -> Developer PowerShell for VS 2019).

### Installing dependencies on Linux

On Linux, check if your distribution offers the necessary programs pre-packaged in the version needed (JDK11, Erlang >= 23, a C compiler); otherwise download from the distribution pages linked above.

## Installing the RplTools and ABS compiler from source
### Installing the RplTools from source

To install the `RplTools`, clone the git repository and run gradle (after installing the necessary dependencies):

`git clone https://github.com/razi236/Rpl-Tools.git`

`cd Rpl-Tools`

`./gradlew assemble`

### Installing the ABS compiler from source
After installing the `RplTool`, install the ABS compiler in the `Rpl-Tools` directory using the following commands:

`git clone https://github.com/abstools/abstools.git`

`cd abstools`

`./gradlew assemble`

## Compiling the Rpl source file

The directory `Rpl-Tools/examples` has some examples. For simplicity, please put your source file (`*.rpl`) in the same directory (`Rpl-Tools/examples`). 

Afterwards, move back to `Rpl-Tools` directory and use the following command to run Rpl-Tools.

`make`

### Note:

After choosing the option, `1` for simulation and `2` for the static cost analysis, provide the name of source file (`*.rpl`) stored in the directory `Rpl-Tools/examples`. The translation of an `Rpl` program into corresponding `ABS` program can be seen in the file `RABS.abs`.
The result of static cost analysis (cost equations) can be found in the file `CostEquations.txt`
