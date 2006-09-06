/*
 *  Copyright 2005 - 2006 The Apache Software Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
 * @author Michael Danilov
 * @version $Revision$
 */
package java.awt.datatransfer;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

final class MimeTypeProcessor {

    private static MimeTypeProcessor instance = null;

    private MimeTypeProcessor() {
    }

    static MimeType parse(String str) {
        MimeType res;

        if (instance == null) {
            instance = new MimeTypeProcessor();
        }

        res = new MimeType();
        if (str != null) {
            StringPosition pos = new StringPosition();

            retrieveType(str, res, pos);
            retrieveParams(str, res, pos);
        }

        return res;
    }

    static String assemble(MimeType type) {
        StringBuffer buf = new StringBuffer();

        buf.append(type.getFullType());
        for (Enumeration keys = type.parameters.keys(); keys.hasMoreElements();) {
            String name = (String) keys.nextElement();
            String value = (String) type.parameters.get(name);

            buf.append("; " + name + "=\"" + value + '"');
        }

        return buf.toString();
    }

    private static void retrieveType(String str, MimeType res, StringPosition pos) {
        res.primaryType = retrieveToken(str, pos).toLowerCase();
        pos.i = getNextMeaningfulIndex(str, pos.i);
        if ((pos.i >= str.length()) || (str.charAt(pos.i) != '/')) {
            throw new IllegalArgumentException();
        }
        pos.i++;
        res.subType = retrieveToken(str, pos).toLowerCase();
    }

    private static void retrieveParams(String str, MimeType res, StringPosition pos) {
        res.parameters = new Hashtable();
        res.systemParameters = new Hashtable();
        do {
            pos.i = getNextMeaningfulIndex(str, pos.i);
            if (pos.i >= str.length()) {
                return;
            } else {
                if (str.charAt(pos.i) != ';') {
                    throw new IllegalArgumentException();
                } else {
                    pos.i++;
                    retrieveParam(str, res, pos);
                }
            }
        } while (true);
    }

    private static void retrieveParam(String str, MimeType res, StringPosition pos) {
        String name = retrieveToken(str, pos).toLowerCase();

        pos.i = getNextMeaningfulIndex(str, pos.i);
        if ((pos.i >= str.length()) || (str.charAt(pos.i) != '=')) {
            throw new IllegalArgumentException();
        }
        pos.i++;
        pos.i = getNextMeaningfulIndex(str, pos.i);
        if ((pos.i >= str.length())) {
            throw new IllegalArgumentException();
        } else {
            String value;

            if (str.charAt(pos.i) == '"') {
                value = retrieveQuoted(str, pos);
            } else {
                value = retrieveToken(str, pos);
            }
            res.parameters.put(name, value);
        }
    }

    private static String retrieveQuoted(String str, StringPosition pos) {
        StringBuffer buf = new StringBuffer();
        boolean check = true;

        pos.i++;
        while ((str.charAt(pos.i) != '"') || !check) {
            char c = str.charAt(pos.i++);

            if (!check) {
                check = true;
            } else if (c == '\\') {
                check = false;
            }
            if (check) {
                buf.append(c);
            }
            if (pos.i == str.length()) {
                throw new IllegalArgumentException();
            }
        }
        pos.i++;

        return buf.toString();
    }

    private static String retrieveToken(String str, StringPosition pos) {
        StringBuffer buf = new StringBuffer();

        pos.i = getNextMeaningfulIndex(str, pos.i);
        if ((pos.i >= str.length()) || isTSpecialChar(str.charAt(pos.i))) {
            throw new IllegalArgumentException();
        } else {
            do {
                buf.append(str.charAt(pos.i++));
            } while ((pos.i < str.length())
                    && isMeaningfulChar(str.charAt(pos.i))
                    && !isTSpecialChar(str.charAt(pos.i)));
        }

        return buf.toString();
    }

    private static int getNextMeaningfulIndex(String str, int i) {
        while ((i < str.length()) && !isMeaningfulChar(str.charAt(i))) {
            i++;
        }

        return i;
    }

    private static boolean isTSpecialChar(char c) {
        return ((c == '(') || (c == ')') || (c == '[') || (c == ']') || (c == '<')
                || (c == '>') || (c == '@') || (c == ',') || (c == ';') || (c == ':')
                || (c == '\\') || (c == '\"') || (c == '/') || (c == '?') || (c == '='));
    }

    private static boolean isMeaningfulChar(char c) {
        return ((c >= '!') && (c <= '~'));
    }

    private static final class StringPosition {

        int i = 0;

    }

    static final class MimeType implements Cloneable, Serializable {

        private static final long serialVersionUID = -6693571907475992044L;
        private String primaryType = null;
        private String subType = null;
        private Hashtable parameters = null;
        private Hashtable systemParameters = null;

        MimeType() {
            primaryType = null;
            subType = null;
            parameters = null;
            systemParameters = null;
        }

        MimeType(String primaryType, String subType) {
            this.primaryType = primaryType;
            this.subType = subType;
            parameters = new Hashtable();
            systemParameters = new Hashtable();
        }

        boolean equals(MimeType that) {
            if (that == null) {
                return false;
            } else {
                return getFullType().equals(that.getFullType());
            }
        }

        String getPrimaryType() {
            return primaryType;
        }

        String getSubType() {
            return subType;
        }

        String getFullType() {
            return (primaryType + "/" + subType);
        }

        String getParameter(String name) {
            return (String) parameters.get(name);
        }

        void addParameter(String name, String value) {
            if (value == null) {
                return;
            }
            if ((value.charAt(0) == '\"') 
                    && (value.charAt(value.length() - 1) == '\"')) {
                value = value.substring(1, value.length() - 2);
            }
            if (value.length() == 0) {
                return;
            }
            parameters.put(name, value);
        }

        void removeParameter(String name) {
            parameters.remove(name);
        }

        Object getSystemParameter(String name) {
            return systemParameters.get(name);
        }

        void addSystemParameter(String name, Object value) {
            systemParameters.put(name, value);
        }

        public Object clone() {
            MimeType clone = new MimeType(primaryType, subType);

            copyParameters(parameters, clone.parameters);
            copyParameters(systemParameters, clone.systemParameters);

            return clone;
        }

        private void copyParameters(Hashtable from, Hashtable to) {
            for (Enumeration keys = from.keys(); keys.hasMoreElements();) {
                String name = (String) keys.nextElement();

                to.put(name, from.get(name));
            }
        }

    }

}
