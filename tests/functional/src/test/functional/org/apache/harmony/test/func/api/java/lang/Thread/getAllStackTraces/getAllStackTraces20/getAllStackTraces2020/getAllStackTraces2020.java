/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 *          Date: Dec 13, 2005
 *          Time: 12:33:48
 */

package org.apache.harmony.test.func.api.java.lang.Thread.getAllStackTraces.getAllStackTraces20.getAllStackTraces2020;

import org.apache.harmony.share.Test;

import java.security.Permission;

public class getAllStackTraces2020 extends Test {
    public static final String TESTED_THREAD_NAME = "SecurityManagerTestThread";

    volatile static boolean tStop = false;

    class locSecurityManager extends SecurityManager{
/* This Security Manager blocks any access to the thtread wint given name */
        private String prohibitedThreadGroupName;
        public locSecurityManager( String prohibitedThreadGroupName){
            super();
            this.prohibitedThreadGroupName = prohibitedThreadGroupName;
        }


        public void checkAccess(ThreadGroup tg){
            if( tg.getName().equals( prohibitedThreadGroupName ))
                throw new SecurityException( "Acces denied to ThreadGroup "+ tg.getName() +
                        " by SecurityManager " + getClass().getName());
            else
                super.checkAccess( tg );
        }

        public void checkPermission( Permission perm ){
            info(" started checkPermission( " + perm +" )from Security Manager " + getClass().getName());
            if( perm.equals( new RuntimePermission("setSecurityManager") ) ) {
                log.info("checking RuntimePermission( " + perm.getName()+ " ): PERMITTED");
            } else if( perm.equals( new RuntimePermission("getStackTrace") ) ) {
                log.info("checking RuntimePermission( " + perm.getName()+ " ): PERMITTED");
            } else if( perm.equals( new RuntimePermission("modifyThreadGroup") ) ) {
                log.info("checking RuntimePermission( " + perm.getName()+ " ): PROHIBITED");
                throw new SecurityException();
            } else {
                //super.checkPermission( perm );
              return;
            }
        }
    }

    public static void main(String args[]) {
        System.exit(new getAllStackTraces2020().test());
    }

    public int test() {
        info( getClass().getName() );
        if( ! checkJavaVersionGreateOrEqual(1.5)  ){
            return fail("Test has not been started.");
        }
        SecurityManager ts = new locSecurityManager( Thread.currentThread().getThreadGroup().getName() );
        SecurityManager sSM = System.getSecurityManager();
        info( "System Security Manager is: " + sSM );

        Thread[] tarray  =  {null};
        System.setSecurityManager( ts );
        info( "System Security Manager was changed to: " + System.getSecurityManager());
        try{
            Thread.getAllStackTraces();
            tStop = true;
            System.setSecurityManager( sSM );
            info( "System Security Manager has been changed to: " + System.getSecurityManager());
            return fail( "Expected SecurityException has NOT been thrown" );
        }catch( SecurityException se ){
            tStop = true;
            System.setSecurityManager( sSM );
            info( "System Security Manager has been changed to: " + System.getSecurityManager());
            return pass( "Expected SecurityException has been thrown" );
        }
    }


    void info(String text){
        log.info( text );
//        System.out.println(text); System.out.flush();
    }
    public boolean checkJavaVersionGreateOrEqual( double checkedVer ){
        double ver = Float.parseFloat( System.getProperties().getProperty("java.version").substring(0,3) );
        if( ver >= checkedVer )
            return true;
        else {
            info("Java version:\t"+ System.getProperties().getProperty("java.version"));
            info("Test supports java version " +checkedVer+ " and higher");
            return false;
        }
    }
}
