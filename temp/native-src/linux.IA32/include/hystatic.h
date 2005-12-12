/* Copyright 1991, 2005 The Apache Software Foundation or its licensors, as applicable
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

#if !defined(hystatic_h)
#define hystatic_h

typedef struct {
	char * name;
	void * funcOrTable;
} HyPrimitiveTableSlot;

typedef	HyPrimitiveTableSlot HyPrimitiveTable[];
#define	HyDefinePrimitiveTable(name) HyPrimitiveTableSlot name [] = {
#define	HySubTable(table) HyPrimitiveTableEntry(0, (table))
#define	HyPrimitiveTableEntry(name, fn) { (char *) (name), (void *) (fn) },
#define	HyEndPrimitiveTable { (char *) 0, (void *) 0} };

#endif     /* hystatic_h */
