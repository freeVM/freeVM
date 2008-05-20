This file describes implemetation of a runner of SPECjvm2008 benchmark which 
uses Build & Test Infrastructure 2.0 (HARMONY-3501).

This readme assumes that the reader is already familiar with BT 2.0 concepts.

It is assumed also that the one who wants to run SPECjvm2008 has the SPEC
itself installed somewhere. The path to the SPEC location should be provided
as a value to the parameter 'specjvm2008.required.parameter.spec.home'.
Customization of SPEC execution is performed through editing of an
_optional_ property named 'specjvm2008.tests' containing names of
tests to execute. The property contains no tests by default which
means the whole test suite execution. File parameters.xml also contains
commented tag <tests> with list of all the 38 tests names for the future
execution parametrizing.

There are two notification options available - if an optional parameter 
'always.mail' is set to 'true', notifications are being sent after each run;
if the parameter is set to 'false', only changes between passed/failed
state are reported.

The SPEC is considered to be PASSED, if number of measured workloads is equal
to the number of specified workloads and to be FAILED otherwise.

The notification contains finishing lines of each workload's log in case of
normal execution of the SPEC, and the whole log if the SPEC was considered 
FAILED.
