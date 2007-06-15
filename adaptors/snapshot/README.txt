This is draft automation for snapshots testing:
The snapshot's adaptor downloads and unpacks the latest snapshot. And all
configured suites are run against snapshot's jre. (A list of suites for
snapshot testing can be found in framework.local.properties file)

So to run tests with snapshot the following steps should be done:
1. Check out Build Test from SVN
     svn co -r HEAD -N http://svn.apache.org/repos/asf/harmony/enhanced/buildtest/branches/2.0

2. copy file adaptors/snapshot/framework.local.properties to the infra root

3. Setup Build Test and required suites with the following command:
     ant -Dtest.suites="snapshot,suite1,suite2" setup

4. Set values of required parameters if necessary in generated file:
     required-parameters.properties

5. Run the tests in continuous mode with command:
     ant -Dtest.suites="snapshot,suite1,suite2" run-cc
