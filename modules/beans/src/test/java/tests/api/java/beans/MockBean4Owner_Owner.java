/* Copyright 2005 The Apache Software Foundation or its licensors, as applicable
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
package tests.api.java.beans;

public class MockBean4Owner_Owner {
	private int v = 1;

	public void loading() {
		v = 999;
	}

	public int getV() {
		return v;
	}

	public void setV(int v) {
		this.v = v;
	}

	public boolean equals(Object o) {
		return (o != null && this.getClass() == o.getClass() && this.v == ((MockBean4Owner_Owner) o).v);
	}

}
