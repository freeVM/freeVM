/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.text.tests.java.text;

import java.text.DateFormatSymbols;
import java.text.spi.DateFormatSymbolsProvider;
import java.util.Locale;

public class MockedDateFormatSymbolsProvider extends DateFormatSymbolsProvider {
    private static Locale supportLocale = new Locale("mock");

    @Override
    public DateFormatSymbols getInstance(Locale locale) {
        if (locale == null) {
            throw new NullPointerException();
        }
        if (!locale.equals(supportLocale)) {
            throw new IllegalArgumentException();
        }

        return new MockedDateFormatSymbols();
    }

    @Override
    public Locale[] getAvailableLocales() {
        return new Locale[] { supportLocale };
    }

    public static class MockedDateFormatSymbols extends DateFormatSymbols {
        
    }
}

