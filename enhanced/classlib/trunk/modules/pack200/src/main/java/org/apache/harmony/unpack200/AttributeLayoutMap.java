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
package org.apache.harmony.unpack200;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Stores a mapping from attribute names to their corresponding layout types.
 * Note that names of attribute layouts and their formats are <emph>not</emph>
 * internationalized, and should not be translated.
 */
public class AttributeLayoutMap {

    // Create all the default AttributeLayouts here
    private static AttributeLayout[] getDefaultAttributeLayouts()
            throws Pack200Exception {
        return new AttributeLayout[] {
                new AttributeLayout(AttributeLayout.ACC_PUBLIC,
                        AttributeLayout.CONTEXT_CLASS, "", 0),
                new AttributeLayout(AttributeLayout.ACC_PUBLIC,
                        AttributeLayout.CONTEXT_FIELD, "", 0),
                new AttributeLayout(AttributeLayout.ACC_PUBLIC,
                        AttributeLayout.CONTEXT_METHOD, "", 0),
                new AttributeLayout(AttributeLayout.ACC_PRIVATE,
                        AttributeLayout.CONTEXT_CLASS, "", 1),
                new AttributeLayout(AttributeLayout.ACC_PRIVATE,
                        AttributeLayout.CONTEXT_FIELD, "", 1),
                new AttributeLayout(AttributeLayout.ACC_PRIVATE,
                        AttributeLayout.CONTEXT_METHOD, "", 1),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_LINE_NUMBER_TABLE,
                        AttributeLayout.CONTEXT_CODE, "NH[PHH]", 1),

                new AttributeLayout(AttributeLayout.ACC_PROTECTED,
                        AttributeLayout.CONTEXT_CLASS, "", 2),
                new AttributeLayout(AttributeLayout.ACC_PROTECTED,
                        AttributeLayout.CONTEXT_FIELD, "", 2),
                new AttributeLayout(AttributeLayout.ACC_PROTECTED,
                        AttributeLayout.CONTEXT_METHOD, "", 2),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_LOCAL_VARIABLE_TABLE,
                        AttributeLayout.CONTEXT_CODE, "NH[PHOHRUHRSHH]", 2),

                new AttributeLayout(AttributeLayout.ACC_STATIC,
                        AttributeLayout.CONTEXT_CLASS, "", 3),
                new AttributeLayout(AttributeLayout.ACC_STATIC,
                        AttributeLayout.CONTEXT_FIELD, "", 3),
                new AttributeLayout(AttributeLayout.ACC_STATIC,
                        AttributeLayout.CONTEXT_METHOD, "", 3),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_LOCAL_VARIABLE_TYPE_TABLE,
                        AttributeLayout.CONTEXT_CODE, "NH[PHOHRUHRSHH]", 3),

                new AttributeLayout(AttributeLayout.ACC_FINAL,
                        AttributeLayout.CONTEXT_CLASS, "", 4),
                new AttributeLayout(AttributeLayout.ACC_FINAL,
                        AttributeLayout.CONTEXT_FIELD, "", 4),
                new AttributeLayout(AttributeLayout.ACC_FINAL,
                        AttributeLayout.CONTEXT_METHOD, "", 4),
                new AttributeLayout(AttributeLayout.ACC_SYNCHRONIZED,
                        AttributeLayout.CONTEXT_CLASS, "", 5),
                new AttributeLayout(AttributeLayout.ACC_SYNCHRONIZED,
                        AttributeLayout.CONTEXT_FIELD, "", 5),
                new AttributeLayout(AttributeLayout.ACC_SYNCHRONIZED,
                        AttributeLayout.CONTEXT_METHOD, "", 5),
                new AttributeLayout(AttributeLayout.ACC_VOLATILE,
                        AttributeLayout.CONTEXT_CLASS, "", 6),
                new AttributeLayout(AttributeLayout.ACC_VOLATILE,
                        AttributeLayout.CONTEXT_FIELD, "", 6),
                new AttributeLayout(AttributeLayout.ACC_VOLATILE,
                        AttributeLayout.CONTEXT_METHOD, "", 6),
                new AttributeLayout(AttributeLayout.ACC_TRANSIENT,
                        AttributeLayout.CONTEXT_CLASS, "", 7),
                new AttributeLayout(AttributeLayout.ACC_TRANSIENT,
                        AttributeLayout.CONTEXT_FIELD, "", 7),
                new AttributeLayout(AttributeLayout.ACC_TRANSIENT,
                        AttributeLayout.CONTEXT_METHOD, "", 7),
                new AttributeLayout(AttributeLayout.ACC_NATIVE,
                        AttributeLayout.CONTEXT_CLASS, "", 8),
                new AttributeLayout(AttributeLayout.ACC_NATIVE,
                        AttributeLayout.CONTEXT_FIELD, "", 8),
                new AttributeLayout(AttributeLayout.ACC_NATIVE,
                        AttributeLayout.CONTEXT_METHOD, "", 8),
                new AttributeLayout(AttributeLayout.ACC_INTERFACE,
                        AttributeLayout.CONTEXT_CLASS, "", 9),
                new AttributeLayout(AttributeLayout.ACC_INTERFACE,
                        AttributeLayout.CONTEXT_FIELD, "", 9),
                new AttributeLayout(AttributeLayout.ACC_INTERFACE,
                        AttributeLayout.CONTEXT_METHOD, "", 9),
                new AttributeLayout(AttributeLayout.ACC_ABSTRACT,
                        AttributeLayout.CONTEXT_CLASS, "", 10),
                new AttributeLayout(AttributeLayout.ACC_ABSTRACT,
                        AttributeLayout.CONTEXT_FIELD, "", 10),
                new AttributeLayout(AttributeLayout.ACC_ABSTRACT,
                        AttributeLayout.CONTEXT_METHOD, "", 10),
                new AttributeLayout(AttributeLayout.ACC_STRICT,
                        AttributeLayout.CONTEXT_CLASS, "", 11),
                new AttributeLayout(AttributeLayout.ACC_STRICT,
                        AttributeLayout.CONTEXT_FIELD, "", 11),
                new AttributeLayout(AttributeLayout.ACC_STRICT,
                        AttributeLayout.CONTEXT_METHOD, "", 11),
                new AttributeLayout(AttributeLayout.ACC_SYNTHETIC,
                        AttributeLayout.CONTEXT_CLASS, "", 12),
                new AttributeLayout(AttributeLayout.ACC_SYNTHETIC,
                        AttributeLayout.CONTEXT_FIELD, "", 12),
                new AttributeLayout(AttributeLayout.ACC_SYNTHETIC,
                        AttributeLayout.CONTEXT_METHOD, "", 12),
                new AttributeLayout(AttributeLayout.ACC_ANNOTATION,
                        AttributeLayout.CONTEXT_CLASS, "", 13),
                new AttributeLayout(AttributeLayout.ACC_ANNOTATION,
                        AttributeLayout.CONTEXT_FIELD, "", 13),
                new AttributeLayout(AttributeLayout.ACC_ANNOTATION,
                        AttributeLayout.CONTEXT_METHOD, "", 13),
                new AttributeLayout(AttributeLayout.ACC_ENUM,
                        AttributeLayout.CONTEXT_CLASS, "", 14),
                new AttributeLayout(AttributeLayout.ACC_ENUM,
                        AttributeLayout.CONTEXT_FIELD, "", 14),
                new AttributeLayout(AttributeLayout.ACC_ENUM,
                        AttributeLayout.CONTEXT_METHOD, "", 14),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_SOURCE_FILE,
                        AttributeLayout.CONTEXT_CLASS, "RUNH", 17),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_CONSTANT_VALUE,
                        AttributeLayout.CONTEXT_FIELD, "KQH", 17),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_CODE,
                        AttributeLayout.CONTEXT_METHOD, "", 17),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_ENCLOSING_METHOD,
                        AttributeLayout.CONTEXT_CLASS, "RCHRDNH", 18),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_EXCEPTIONS,
                        AttributeLayout.CONTEXT_METHOD, "NH[RCH]", 18),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_SIGNATURE,
                        AttributeLayout.CONTEXT_CLASS, "RSH", 19),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_SIGNATURE,
                        AttributeLayout.CONTEXT_FIELD, "RSH", 19),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_SIGNATURE,
                        AttributeLayout.CONTEXT_METHOD, "RSH", 19),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_DEPRECATED,
                        AttributeLayout.CONTEXT_CLASS, "", 20),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_DEPRECATED,
                        AttributeLayout.CONTEXT_FIELD, "", 20),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_DEPRECATED,
                        AttributeLayout.CONTEXT_METHOD, "", 20),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS,
                        AttributeLayout.CONTEXT_CLASS, "*", 21),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS,
                        AttributeLayout.CONTEXT_FIELD, "*", 21),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS,
                        AttributeLayout.CONTEXT_METHOD, "*", 21),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS,
                        AttributeLayout.CONTEXT_CLASS, "*", 22),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS,
                        AttributeLayout.CONTEXT_FIELD, "*", 22),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS,
                        AttributeLayout.CONTEXT_METHOD, "*", 22),
                new AttributeLayout(AttributeLayout.ATTRIBUTE_INNER_CLASSES,
                        AttributeLayout.CONTEXT_CLASS, "", 23),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS,
                        AttributeLayout.CONTEXT_METHOD, "*", 23),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_CLASS_FILE_VERSION,
                        AttributeLayout.CONTEXT_CLASS, "", 24),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS,
                        AttributeLayout.CONTEXT_METHOD, "*", 24),
                new AttributeLayout(
                        AttributeLayout.ATTRIBUTE_ANNOTATION_DEFAULT,
                        AttributeLayout.CONTEXT_METHOD, "*", 25) };
    }

    private final Map classLayouts = new HashMap();
    private final Map fieldLayouts = new HashMap();
    private final Map methodLayouts = new HashMap();
    private final Map codeLayouts = new HashMap();

    // The order of the maps in this array should not be changed as their
    // indices correspond to
    // the value of their context constants (AttributeLayout.CONTEXT_CLASS etc.)
    private final Map[] layouts = new Map[] { classLayouts, fieldLayouts,
            methodLayouts, codeLayouts };

    private final Map layoutsToBands = new HashMap();

    public AttributeLayoutMap() throws Pack200Exception {
        AttributeLayout[] defaultAttributeLayouts = getDefaultAttributeLayouts();
        for (int i = 0; i < defaultAttributeLayouts.length; i++) {
            add(defaultAttributeLayouts[i]);
        }
    }

    public void add(AttributeLayout layout) {
        layouts[layout.getContext()]
                .put(new Integer(layout.getIndex()), layout);
    }

    public void add(AttributeLayout layout, NewAttributeBands newBands) {
        add(layout);
        layoutsToBands.put(layout, newBands);
    }

    public AttributeLayout getAttributeLayout(String name, int context)
            throws Pack200Exception {
        Map map = layouts[context];
        for (Iterator iter = map.values().iterator(); iter.hasNext();) {
            AttributeLayout layout = (AttributeLayout) iter.next();
            if (layout.getName().equals(name)) {
                return layout;
            }
        }
        return null;
    }

    public AttributeLayout getAttributeLayout(int index, int context)
            throws Pack200Exception {
        Map map = layouts[context];
        return (AttributeLayout) map.get(new Integer(index));
    }

    /**
     * The map should not contain the same layout and name combination more than
     * once for each context.
     * 
     * @throws Pack200Exception
     * 
     */
    public void checkMap() throws Pack200Exception {
        for (int i = 0; i < layouts.length; i++) {
            Map map = layouts[i];
            Collection c = map.values();
            if (!(c instanceof List)) {
                c = new ArrayList(c);
            }
            List l = (List) c;
            for (int j = 0; j < l.size(); j++) {
                AttributeLayout layout1 = (AttributeLayout) l.get(j);
                for (int j2 = j + 1; j2 < l.size(); j2++) {
                    AttributeLayout layout2 = (AttributeLayout) l.get(j2);
                    if (layout1.getName().equals(layout2.getName())
                            && layout1.getLayout().equals(layout2.getLayout())) {
                        throw new Pack200Exception(
                                "Same layout/name combination: "
                                        + layout1.getLayout()
                                        + "/"
                                        + layout1.getName()
                                        + " exists twice for context: "
                                        + AttributeLayout.contextNames[layout1
                                                .getContext()]);
                    }
                }
            }
        }
    }

    public NewAttributeBands getAttributeBands(AttributeLayout layout) {
        return (NewAttributeBands) layoutsToBands.get(layout);
    }

}