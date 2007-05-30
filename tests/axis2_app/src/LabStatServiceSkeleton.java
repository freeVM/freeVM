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
 * LabStatServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
 */

package org.apache.harmony.test.lab_stat_app;

import org.apache.harmony.test.lab_stat_app.xsd.ServerStat;
import org.apache.harmony.test.lab_stat_app.xsd.StatResponse;
import org.apache.harmony.test.lab_stat_app.xsd.StatId;
import org.apache.harmony.test.lab_stat_app.xsd.StatData;
import org.apache.harmony.test.lab_stat_app.xsd.StatItem;

import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *  LabStatServiceSkeleton java skeleton for the axisService
 */
public class LabStatServiceSkeleton implements LabStatServiceSkeletonInterface{

    private TreeMap storageServers = new TreeMap();
    private TreeMap storageDates   = new TreeMap();

    void put(TreeMap storage, Object prim_key, Object sec_key, Object data) {
        HashMap hm = (HashMap)storage.get(prim_key);
        boolean newHM = hm == null;
        if (newHM)
			hm = new HashMap();
		hm.put(sec_key, data);
		
        if (newHM)
			storage.put(prim_key, hm);
	}

    public void update (ServerStat stat) {
        String server = stat.getServer_name();
        StatItem[] si_list = stat.getStatItem();
        //Date curDate = Date();
        
        for(int i=0; i<si_list.length; i++) {
			StatItem si = si_list[i];
			Date date   = si.getDate();

			put(storageServers, server, date, si.getStat());
			put(storageDates,   date, server, si.getStat());
		}
    }
 
    public StatResponse getStat(StatId serverDate) {
		StatResponse res = new StatResponse();
		res.setEmpty(true);

		String server = serverDate.getServer_name();
		HashMap hm = (HashMap)storageServers.get(server);
		if (hm == null)
			return res;

		Date date = serverDate.getDate();
		StatData sd = (StatData)hm.get(date);
		if (sd != null)
			res.setEmpty(false);

		res.setStatData(sd);
		return res;
    }
 
}
/*
class serverData {
    String server;
    StatData data;

    serverData(String server, StatData data) {
        this.server = server;
        this.data = data;
	}
}
*/