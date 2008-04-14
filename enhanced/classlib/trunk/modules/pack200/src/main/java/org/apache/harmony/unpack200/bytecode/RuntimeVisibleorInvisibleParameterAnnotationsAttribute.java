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
package org.apache.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Paramenter annotations class file attribute, either a
 * RuntimeVisibleParamenterAnnotations attribute or a
 * RuntimeInvisibleParameterAnnotations attribute.
 */
public class RuntimeVisibleorInvisibleParameterAnnotationsAttribute extends
        AnnotationsAttribute {

    private final int num_parameters;
    private final ParameterAnnotation[] parameter_annotations;

    public RuntimeVisibleorInvisibleParameterAnnotationsAttribute(CPUTF8 name,
            ParameterAnnotation[] parameter_annotations) {
        super(name);
        this.num_parameters = parameter_annotations.length;
        this.parameter_annotations = parameter_annotations;
    }

    protected int getLength() {
        int length = 1;
        for (int i = 0; i < num_parameters; i++) {
            length += parameter_annotations[i].getLength();
        }
        return length;
    }

    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        for (int i = 0; i < parameter_annotations.length; i++) {
            parameter_annotations[i].resolve(pool);
        }
    }

    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeByte(num_parameters);
        for (int i = 0; i < num_parameters; i++) {
            parameter_annotations[i].writeBody(dos);
        }
    }

    public String toString() {
        return attributeName.underlyingString() + ": " + num_parameters
                + " parameter annotations";
    }

    /**
     * ParameterAnnotation represents the annotations on a single parameter.
     */
    public static class ParameterAnnotation {

        private final Annotation[] annotations;
        private final int num_annotations;

        public ParameterAnnotation(Annotation[] annotations) {
            this.num_annotations = annotations.length;
            this.annotations = annotations;
        }

        public void writeBody(DataOutputStream dos) throws IOException {
            dos.writeShort(num_annotations);
            for (int i = 0; i < annotations.length; i++) {
                annotations[i].writeBody(dos);
            }
        }

        public void resolve(ClassConstantPool pool) {
            for (int i = 0; i < annotations.length; i++) {
                annotations[i].resolve(pool);
            }
        }

        public int getLength() {
            int length = 2;
            for (int i = 0; i < annotations.length; i++) {
                length += annotations[i].getLength();
            }
            return length;
        }

    }

}
