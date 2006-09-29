/*
 *  Copyright 2005 - 2006 The Apache Software Foundation or its licensors, as applicable.
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
 * @author Anton Avtamonov, Alexey A. Ivanov
 * @version $Revision$
 */
package org.apache.harmony.x.swing.text.html.cssparser.metamodel;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

public final class Sheet {
    private final List imports = new LinkedList();
    private final List ruleSets = new LinkedList();


    public void addImport(final String imp) {
        if (imp == null) {
            return;
        }

        imports.add(imp);
    }

    public void addRuleSet(final RuleSet ruleSet) {
        if (ruleSet == null) {
            return;
        }

        ruleSets.add(ruleSet);
    }

    public List getRuleSets() {
        return ruleSets;
    }

    public Iterator getRuleSetIterator() {
        return getRuleSets().iterator();
    }

    public List getImports() {
        return imports;
    }

    public Iterator getImportsIterator() {
        return getImports().iterator();
    }


    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("imports:")
              .append(imports)
              .append("\n")
              .append("ruleSets:")
              .append(ruleSets);

        return result.toString();
    }
}
