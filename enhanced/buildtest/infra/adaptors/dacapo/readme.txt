This file describes implemetation of a runner of Dacapo benchmark which uses 
Build & Test Infrastructure 2.0 (HARMONY-3501).

This readme assumes that the reader is already familiar with BT 2.0 concepts.

The Dacapo benchmark will be downloaded by the runner at 'setup' stage from
the location specified by two optional parameters: 'download.address' and
'jar.name'. By default they are pointing to sourceforge site. If, by any 
change, you already have the Dacapo jar, and would like to skip the downloading
step, you can simply put the jar to 'build/checkouts/dacapo' directory.

Another optional parameters are: 
1) 'benchmarks' - specifying a set of benchmarks to run. By default the list 
    contains all available benchmarks. 
    NOTE: to make chart benchmark working on linux, one should run X server
    and provide proper DISPLAY variable in the environment prior to running,
    the runner doesn't yet run X server by itself.
2) 'size' - Specifies the workload size, can be 'small', 'default' or 'large'.
    By default it's 'small'

The benchmark run is considered to be PASSED, if a number of PASSED workloads 
is equal to the number of workloads specified by 'benchmarks' parameter and to 
be FAILED otherwise.
