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

package org.apache.harmony.test.lab_stat_app;

import java.util.Calendar;
import java.util.Date;

public class LabStatClient extends LabStatServiceStub {

    final static int ERROR_CREATE   = 31;
    final static int ERROR_PREPARE  = 41;
    final static int ERROR_UPLOAD   = 51;
    final static int ERROR_GETSTAT  = 61;
    final static int ERROR_CHECK    = 91;

    final static int MAX_SHIFT    = 3;
    final static int LAST_PDAY    = 5;

    Calendar base_date = Calendar.getInstance();
    boolean verbose = false;
    int step; // in the range 1..MAX_SHIFT
//--------------------------------------------------------------
    
    public LabStatClient() throws org.apache.axis2.AxisFault {
		super();
    }

    public static void main(String[] args) {
        LabStatClient client = null;
        try {
            
            client = new LabStatClient();

        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("\n\n");
            System.exit(ERROR_CREATE);
        }

        client.step = Integer.parseInt(args[0]);
        if (System.getenv("AXIS2_APP_VERBOSE") != null) {
			client.verbose = true;
		}

        client.upLoad(1);
        client.upLoad(2);
        client.check(2);

        client.upLoad(MAX_SHIFT);
        client.check(MAX_SHIFT);
    }
//--------------------------------------------------------------

    public Date genDate(int day) {
        base_date.clear();
        base_date.set(2007, Calendar.MARCH, 10+day);
        return base_date.getTime();
	}

    public int firstDay(int step, int shift) {
		return (step-1)*5+shift;
    }

    public String serverName(int step, int shift) {
        return "nst-"+ (step*10 + shift);
	}

    public String serverName(int shift) {
        return serverName(step, shift);
	}
//--------------------------------------------------------------

    public void upLoad(int shift) {
        ServerStat sst = new ServerStat();
        StatItem[] sia = new StatItem[6];

        try {
            for (int d=LAST_PDAY; d>=0; d--) {
                StatData s_data = new StatData();
                int m = shift*10 + d;
                
                s_data.setCPU_load(m + step/100f);
                s_data.setRAM_load(step + m/100f);
                s_data.setActive_users(step*100 + m);

                StatItem s_item = new StatItem();
                s_item.setDate(genDate( firstDay(step, shift)+d ));
                s_item.setStat(s_data);

                sia[d] = s_item; 
		    } 

            sst.setServer_name(serverName(shift));
            sst.setStatItem(sia);
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("\n\n");
            System.exit(ERROR_PREPARE);
        }

        try {
            update(sst);
            //System.err.println("done");
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("\n\n");
            System.exit(ERROR_UPLOAD);
        }
    }
//--------------------------------------------------------------

    public void check(int shift) {
        StatResponse resp = null;
        StatId req = new StatId();
        int last_day = firstDay(step, shift)+LAST_PDAY;

		System.out.println("\n========= Check step "+ step +", shift "+ shift);
        for (int day=1; day<=last_day; day++) {
			VerboseLines v = new VerboseLines();
			int bound = MAX_SHIFT;
			String sname = null;

			for (int s=1; s<=step; s++) {
			    if (s == step) 
			        bound = shift;
			    for (int b=1; b<=bound; b++) { //====================
                    try {
                        sname = serverName(s, b);
                        req.setDate(genDate(day));
                        req.setServer_name(sname);

                        resp = getStat(req); // call in synchro mode

                    } catch(Exception e) {
                        e.printStackTrace();
                        System.err.println("\n\n");
                        System.exit(ERROR_GETSTAT);
                    }
                    try {
                        if (resp.getEmpty()) {
                            v.addEmpty(sname);
                            if (! isEmpty(s, b, day))
                                throw new Exception("Got empty stat for "+ sname +
                                                                   " on "+ genDate(day));
                        } else {
                            StatData s_data = resp.getStatData();
                            v.add(sname, s_data);

                            checkNonEmpty(s_data, s, b, day); 
						}
                    } catch(Exception e) {
                        e.printStackTrace();
                        System.err.println("\n\n");
                        System.exit(ERROR_CHECK);
                    }
				} // b<=bound ==========================================
			}
			v.print(day);
		}
    }
//--------------------------------------------------------------

    public boolean isEmpty(int step, int shift, int day) {
		int r_day = day - firstDay(step, shift);
		return (r_day < 0) || (r_day > LAST_PDAY);
    }

    public void checkNonEmpty(StatData s_data, int step, int shift, int day)
                                                                     throws Exception {
        if (isEmpty(step, shift, day))
            throw new Exception("Got non-empty stat for "+ serverName(step, shift) +
                                                   " on "+ genDate(day) +
                                                   "\n- "+ toString(s_data));

        int m = shift*10 + (day - firstDay(step, shift));
                
//                s_data.setCPU_load(m + step/100f);
//                s_data.setRAM_load(step + m/100f);
//                s_data.setActive_users(step*100 + m);

		String wrong = "";
		if (Math.abs(s_data.getCPU_load() - (m + step/100f)) > Float.MIN_VALUE*8)
		    wrong += "CPU";
		if (Math.abs(s_data.getRAM_load() - (step + m/100f)) > Float.MIN_VALUE*8)
		    wrong += " RAM";
		if (         s_data.getActive_users() != (step*100 + m)                 )
		    wrong += " Users";
		if (wrong.length() > 0)
		    throw new Exception("Got wrong "+ wrong +" for "+ serverName(step, shift) +
                                                   " on "+ genDate(day) +
                                                   "\n- "+ toString(s_data));
    }

    public String toString(StatData s_data) {
		return "CPU: "+ s_data.getCPU_load() +
              " RAM: "+ s_data.getRAM_load() +
              " Users: "+ s_data.getActive_users();
    }
//--------------------------------------------------------------
//--------------------------------------------------------------

    class VerboseLines {
		String names = "Server";
		String CPU   = "CPU";
		String RAM   = "RAM";
		String users = "Users";

		void addEmpty(String sname) {
            names += "\t"+ sname;
            CPU   += "\t -";
            RAM   += "\t -";
            users += "\t -";
		}

		void add(String sname, StatData s_data) {
            names += "\t"+ sname;
            CPU   += "\t"+ s_data.getCPU_load();
            RAM   += "\t"+ s_data.getRAM_load();
            users += "\t"+ s_data.getActive_users();
		}

		void print(int day) {
			if (! verbose)
			    return;
			System.out.println("===== Date "+ genDate(day));
			System.out.println(names);
			System.out.println(CPU);
			System.out.println(RAM);
			System.out.println(users);
		}
	}
}
