/* Copyright 1998, 2004 The Apache Software Foundation or its licensors, as applicable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package com.ibm.oti.locale;
public class Locale_in extends java.util.ListResourceBundle {
protected Object[][] getContents() {
	Object[][] contents = {
		{"First_Day",new java.lang.Integer(2),},
		{"months",new String[]{"Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","Nopember","Desember","",},
},
		{"shortMonths",new String[]{"Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nop","Des","",},
},
		{"weekdays",new String[]{"","Minggu","Senin","Selasa","Rabu","Kamis","Jumat","Sabtu",},
},
		{"shortWeekdays",new String[]{"","Minggu","Senin","Selasa","Rabu","Kamis","Jumat","Sabtu",},
},
		{"Date_SHORT","dd/MM/yy",},
		{"Date_MEDIUM","dd/MM/yyyy",},
		{"Date_LONG","d MMMM, yyyy",},
		{"Date_FULL","EEEE d MMMM, yyyy",},
		{"Time_SHORT","H:mm",},
		{"Time_MEDIUM","H:mm:ss",},
		{"Time_LONG","H:mm:ss",},
		{"Time_FULL","H:mm:ss z",},
		{"DecimalPatternChars","0#,.;%\u2030E,-",},
		{"Currency","\u00a4 #,##0.00;- \u00a4 #,##0.00",},
	};
return contents;
}
}
