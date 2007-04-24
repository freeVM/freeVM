This file describes implemetation of a runner of Dacapo benchmark which uses 
Build & Test Infrastructure 2.0 (HARMONY-3501).

This readme assumes that the reader is already familiar with BT 2.0 concepts.

The Dacapo benchmark will be downloaded by the runner at 'setup' stage from
the location specified by two optional parameters: 'download.address' and
'jar.name'. By default they are pointing to sourceforge site. If, by any 
change, you already have the Dacapo jar, and would like to skip the downloading
step, you can simply put tjhe jar to 'build/checkouts/dacapo' directory.

Another optional parameters are: 
1) 'benchmarks' - specifying a set of benchmarks to run. By default the list 
    contains all available benchmarks.
2) 'size' - Specifies the workload size, can be 'small', 'default' or 'large'.
    By default it's 'small'
3) 'always.mail' - when set to 'true', notifications are being sent after each 
    run; when 'false', only changes between passed/failed state are reported.

The benchmark run is considered to be PASSED, if a number of PASSED workloads 
is equal to the number of workloads specified by 'benchmarks' parameter and to 
be FAILED otherwise.

The notification contains finishing lines of each workload's log in case of
normal execution of the benchmark, and the whole log if the run was considered 
FAILED.

