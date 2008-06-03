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
package org.apache.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.AnnotationDefault;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Annotations;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.ConstantValue;
import org.apache.bcel.classfile.Deprecated;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EnclosingMethod;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.LocalVariableTypeTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotations;
import org.apache.bcel.classfile.Signature;
import org.apache.bcel.classfile.SourceFile;
import org.apache.bcel.classfile.StackMap;
import org.apache.bcel.classfile.StackMapEntry;
import org.apache.bcel.classfile.StackMapTable;
import org.apache.bcel.classfile.StackMapTableEntry;
import org.apache.bcel.classfile.Synthetic;
import org.apache.bcel.classfile.Unknown;
import org.apache.bcel.classfile.Visitor;


public class Segment implements Visitor {

    private BandSet segmentHeader;
    private CpBands cpBands;
    private AttributeDefinitionBands attributeDefinitionBands;
    private IcBands icBands;
    private ClassBands classBands;
    private BcBands bcBands;
    private FileBands fileBands;

    public void pack(List classes, OutputStream out) throws IOException {
        segmentHeader = new SegmentHeader();
        cpBands = new CpBands();
        attributeDefinitionBands = new AttributeDefinitionBands();
        icBands = new IcBands();
        classBands = new ClassBands(cpBands, classes.size());
        bcBands = new BcBands();
        fileBands = new FileBands();

        processClasses(classes);

        segmentHeader.pack(out);
        cpBands.pack(out);
        attributeDefinitionBands.pack(out);
        icBands.pack(out);
        classBands.pack(out);
        bcBands.pack(out);
        fileBands.pack(out);
    }

    private void processClasses(List classes) {
        for (Iterator iterator = classes.iterator(); iterator.hasNext();) {
            JavaClass javaClass = (JavaClass) iterator.next();
            new DescendingVisitor(javaClass, this).visit();
        }
        cpBands.sortPool();
    }

    public void visitAnnotation(Annotations obj) {
        // TODO Auto-generated method stub

    }

    public void visitAnnotationDefault(AnnotationDefault obj) {
        // TODO Auto-generated method stub

    }

    public void visitAnnotationEntry(AnnotationEntry obj) {
        // TODO Auto-generated method stub

    }

    public void visitCode(Code obj) {
        bcBands.addCode(obj);
    }

    public void visitCodeException(CodeException obj) {
        // TODO Auto-generated method stub

    }

    public void visitConstantClass(ConstantClass obj) {
        cpBands.addConstantClass(obj);
    }

    public void visitConstantDouble(ConstantDouble obj) {
        cpBands.addConstantDouble(obj);

    }

    public void visitConstantFieldref(ConstantFieldref obj) {
        cpBands.addConstantFieldref(obj);
    }

    public void visitConstantFloat(ConstantFloat obj) {
        cpBands.addConstantFloat(obj);
    }

    public void visitConstantInteger(ConstantInteger obj) {
        cpBands.addConstantInteger(obj);
    }

    public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref obj) {
        cpBands.addConstantInterfaceMethodref(obj);
    }

    public void visitConstantLong(ConstantLong obj) {
        cpBands.addConstantLong(obj);
    }

    public void visitConstantMethodref(ConstantMethodref obj) {
        cpBands.addConstantMethodref(obj);
    }

    public void visitConstantNameAndType(ConstantNameAndType obj) {
        cpBands.addConstantNameAndType(obj);
    }

    public void visitConstantPool(ConstantPool obj) {
        cpBands.setCurrentConstantPool(obj);
    }

    public void visitConstantString(ConstantString obj) {
        cpBands.addConstantString(obj);
    }

    public void visitConstantUtf8(ConstantUtf8 obj) {
        cpBands.addConstantUtf8(obj);
    }

    public void visitConstantValue(ConstantValue obj) {

    }

    public void visitDeprecated(Deprecated obj) {
        // TODO Auto-generated method stub

    }

    public void visitEnclosingMethod(EnclosingMethod obj) {
        // TODO Auto-generated method stub

    }

    public void visitExceptionTable(ExceptionTable obj) {
        // TODO Auto-generated method stub

    }

    public void visitField(Field obj) {
        cpBands.addDesc(obj.getName(), obj.getSignature());
    }

    public void visitInnerClass(InnerClass obj) {
        // TODO Auto-generated method stub

    }

    public void visitInnerClasses(InnerClasses obj) {
        icBands.addInnerClasses(obj);
    }

    public void visitJavaClass(JavaClass obj) {
        classBands.addClass(obj);
    }

    public void visitLineNumber(LineNumber obj) {
        // TODO Auto-generated method stub

    }

    public void visitLineNumberTable(LineNumberTable obj) {
        // TODO Auto-generated method stub

    }

    public void visitLocalVariable(LocalVariable obj) {
        cpBands.addSignature(obj.getSignature());
    }

    public void visitLocalVariableTable(LocalVariableTable obj) {
        // TODO Auto-generated method stub

    }

    public void visitLocalVariableTypeTable(LocalVariableTypeTable obj) {
        // TODO Auto-generated method stub

    }

    public void visitMethod(Method obj) {
        cpBands.addDesc(obj.getName(), obj.getSignature());
    }

    public void visitParameterAnnotation(ParameterAnnotations obj) {
        // TODO Auto-generated method stub

    }

    public void visitSignature(Signature obj) {

    }

    public void visitSourceFile(SourceFile obj) {
        // TODO Auto-generated method stub

    }

    public void visitStackMap(StackMap obj) {
        // TODO Auto-generated method stub

    }

    public void visitStackMapEntry(StackMapEntry obj) {
        // TODO Auto-generated method stub

    }

    public void visitStackMapTable(StackMapTable obj) {
        // TODO Auto-generated method stub

    }

    public void visitStackMapTableEntry(StackMapTableEntry obj) {
        // TODO Auto-generated method stub

    }

    public void visitSynthetic(Synthetic obj) {
        // TODO Auto-generated method stub

    }

    public void visitUnknown(Unknown obj) {
        attributeDefinitionBands.addUnknownAttribute(obj);
    }

}
