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


public class AttributeLayout implements IMatcher {

	public static final String ACC_ABSTRACT = "ACC_ABSTRACT"; //$NON-NLS-1$
	public static final String ACC_ANNOTATION = "ACC_ANNOTATION"; //$NON-NLS-1$
	public static final String ACC_ENUM = "ACC_ENUM"; //$NON-NLS-1$
	public static final String ACC_FINAL = "ACC_FINAL"; //$NON-NLS-1$
	public static final String ACC_INTERFACE = "ACC_INTERFACE"; //$NON-NLS-1$
	public static final String ACC_NATIVE = "ACC_NATIVE"; //$NON-NLS-1$
	public static final String ACC_PRIVATE = "ACC_PRIVATE"; //$NON-NLS-1$
	public static final String ACC_PROTECTED = "ACC_PROTECTED"; //$NON-NLS-1$
	public static final String ACC_PUBLIC = "ACC_PUBLIC"; //$NON-NLS-1$
	public static final String ACC_STATIC = "ACC_STATIC"; //$NON-NLS-1$
	public static final String ACC_STRICT = "ACC_STRICT"; //$NON-NLS-1$
	public static final String ACC_SYNCHRONIZED = "ACC_SYNCHRONIZED"; //$NON-NLS-1$
	public static final String ACC_SYNTHETIC = "ACC_SYNTHETIC"; //$NON-NLS-1$
	public static final String ACC_TRANSIENT = "ACC_TRANSIENT"; //$NON-NLS-1$
	public static final String ACC_VOLATILE = "ACC_VOLATILE"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ANNOTATION_DEFAULT = "AnnotationDefault"; //$NON-NLS-1$
	public static final String ATTRIBUTE_CLASS_FILE_VERSION = "class-file version"; //$NON-NLS-1$
	public static final String ATTRIBUTE_CODE = "Code"; //$NON-NLS-1$
	public static final String ATTRIBUTE_CONSTANT_VALUE = "ConstantValue"; //$NON-NLS-1$
	public static final String ATTRIBUTE_DEPRECATED = "Deprecated"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ENCLOSING_METHOD = "EnclosingMethod"; //$NON-NLS-1$
	public static final String ATTRIBUTE_EXCEPTIONS = "Exceptions"; //$NON-NLS-1$
	public static final String ATTRIBUTE_INNER_CLASSES = "InnerClasses"; //$NON-NLS-1$
	public static final String ATTRIBUTE_LINE_NUMBER_TABLE = "LineNumberTable"; //$NON-NLS-1$
	public static final String ATTRIBUTE_LOCAL_VARIABLE_TABLE = "LocalVariableTable"; //$NON-NLS-1$
	public static final String ATTRIBUTE_LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable"; //$NON-NLS-1$
	public static final String ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations"; //$NON-NLS-1$
	public static final String ATTRIBUTE_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations"; //$NON-NLS-1$
	public static final String ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations"; //$NON-NLS-1$
	public static final String ATTRIBUTE_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SIGNATURE = "Signature"; //$NON-NLS-1$
	public static final String ATTRIBUTE_SOURCE_FILE = "SourceFile"; //$NON-NLS-1$
	public static final int CONTEXT_CLASS = 0;
	public static final int CONTEXT_CODE = 3;
	public static final int CONTEXT_FIELD = 1;
	public static final int CONTEXT_METHOD = 2;
	public static final String[] contextNames = { "Class", "Field", "Method", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"Code", }; //$NON-NLS-1$

	private static Object getValue(String layout, long value, SegmentConstantPool pool)
			throws Pack200Exception {
		if (layout.startsWith("R")) { //$NON-NLS-1$
			// references
			if (layout.indexOf('N') != -1)
				value--;
			if (layout.startsWith("RU")) { //$NON-NLS-1$
				return pool.getValue(SegmentConstantPool.UTF_8, value);
			} else if (layout.startsWith("RS")) { //$NON-NLS-1$
				return pool.getValue(SegmentConstantPool.SIGNATURE, value);
			}
		} else if (layout.startsWith("K")) { //$NON-NLS-1$
			char type = layout.charAt(1);
			switch (type) {
			case 'S': // String
				return pool.getValue(SegmentConstantPool.CP_STRING, value);
			case 'I': // Int (or byte or short)
			case 'C': // Char
				return pool.getValue(SegmentConstantPool.CP_INT, value);
			case 'F': // Float
				return pool.getValue(SegmentConstantPool.CP_FLOAT, value);
			case 'J': // Long
				return pool.getValue(SegmentConstantPool.CP_LONG, value);
			case 'D': // Double
				return pool.getValue(SegmentConstantPool.CP_DOUBLE, value);
			}
		}
		throw new Pack200Exception("Unknown layout encoding: " + layout);
	}
    
    
    private int context;

    private int index;

	private final String layout;

	private long mask;
    
    private String name;

	public AttributeLayout(String name, int context, String layout, int index)
			throws Pack200Exception {
		super();
        this.index = index;
        this.context = context;
		if (index >= 0) {
			this.mask = 1L << index;
		} else {
			this.mask = 0;
		}
        if (context != CONTEXT_CLASS && context != CONTEXT_CODE
                && context != CONTEXT_FIELD && context != CONTEXT_METHOD)
            throw new Pack200Exception("Attribute context out of range: "
                    + context);
		if (layout == null) // || layout.length() == 0)
			throw new Pack200Exception("Cannot have a null layout");
        if (name == null || name.length() == 0)
                throw new Pack200Exception("Cannot have an unnamed layout");
        this.name = name;
		this.layout = layout;
	}
    
    
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AttributeLayout other = (AttributeLayout) obj;
		if (layout == null) {
			if (other.layout != null)
				return false;
		} else if (!layout.equals(other.layout))
			return false;
        if(index != other.index) 
            return false;
        if(context != other.context)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
		return true;
	}

	public Codec getCodec() {
		if (layout.indexOf("O") >= 0) { //$NON-NLS-1$
			return Codec.BRANCH5;
		} else if (layout.indexOf("P") >= 0) { //$NON-NLS-1$
			return Codec.BCI5;
		} else if (layout.indexOf("S") >= 0 && layout.indexOf("KS") < 0 //$NON-NLS-1$ //$NON-NLS-2$
				&& layout.indexOf("RS") < 0) { //$NON-NLS-1$
			return Codec.SIGNED5;
		} else if (layout.indexOf("B") >= 0) { //$NON-NLS-1$
			return Codec.BYTE1;
		} else {
			return Codec.UNSIGNED5;
		}
	}

	public String getLayout() {
		return layout;
	}

	public Object getValue(long value, SegmentConstantPool pool) throws Pack200Exception {
		return getValue(layout, value, pool);
	}

	public Object getValue(long value, String type, SegmentConstantPool pool)
			throws Pack200Exception {
		// TODO This really needs to be better tested, esp. the different types
		// TODO This should have the ability to deal with RUN stuff too, and
		// unions
		if (layout.startsWith("KQ")) { //$NON-NLS-1$
			if (type.equals("Ljava/lang/String;")) { //$NON-NLS-1$
				Object value2 = getValue("KS", value, pool); //$NON-NLS-1$
				return value2;
			} else {
				return getValue("K" + type + layout.substring(2), value, //$NON-NLS-1$
						pool);
			}
		} else {
			return getValue(layout, value, pool);
		}
	}

	public int hashCode() {
        int PRIME = 31;
        int r = 1;
        if(name != null) {
            r = r * PRIME + name.hashCode();
        }
		if(layout != null) {
            r = r* PRIME + layout.hashCode();
        }
        r = r * PRIME + index;
        r = r * PRIME + context;
        return r;
	}

	public boolean isClass() {
		return context == CONTEXT_CLASS;
	}

	public boolean isCode() {
		return context == CONTEXT_CODE;
	}

	public boolean isField() {
		return context == CONTEXT_FIELD;
	}

	public boolean isMethod() {
		return context == CONTEXT_METHOD;
	}

	/* (non-Javadoc)
	 * @see org.apache.harmony.pack200.IMatches#matches(long)
	 */
	public boolean matches(long value) {
		return (value & mask) != 0;
	}

	public String toString() {
        return contextNames[context] + ": " + name;
	}

    public int getContext() {
        return context;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
    
    public int numBackwardsCallables() {
        if(layout == "*") {
            return 1; // TODO: complicated attributes (shouldn't be *'s at all...)
        }
        int num = 0;
        String[] split = layout.split("\\(");
        if(split.length > 0) {
            for (int i = 1; i < split.length; i++) {
                if(split[i].startsWith("-") || split[i].startsWith("0")) {
                    num++;
                }
            }
        }
        return num;
    }

}
