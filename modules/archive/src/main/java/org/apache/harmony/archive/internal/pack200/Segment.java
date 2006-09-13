/*
 *  Copyright 2006 The Apache Software Foundation or its licensors, 
 *  as applicable.
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
package org.apache.harmony.archive.internal.pack200;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * A Pack200 archive consists of one (or more) segments. Each segment is
 * standalone, in the sense that every segment has the magic number header;
 * thus, every segment is also a valid archive. However, it is possible to
 * combine (non-GZipped) archives into a single large archive by concatenation
 * alone. Thus all the hard work in unpacking an archive falls to understanding
 * a segment.
 * 
 * This class implements the Pack200 specification by an entry point ({@link #parse(InputStream)})
 * which in turn delegates to a variety of other parse methods. Each parse
 * method corresponds (roughly) to the name of the bands in the Pack200
 * specification.
 * 
 * The first component of a segment is the header; this contains (amongst other
 * things) the expected counts of constant pool entries, which in turn defines
 * how many values need to be read from the stream. Because values are variable
 * width (see {@link Codec}), it is not possible to calculate the start of the
 * next segment, although one of the header values does hint at the size of the
 * segment if non-zero, which can be used for buffering purposes.
 * 
 * Note that this does not perform any buffering of the input stream; each value
 * will be read on a byte-by-byte basis. It does not perform GZip decompression
 * automatically; both of these are expected to be done by the caller if the
 * stream has the magic header for GZip streams ({@link GZIPInputStream#GZIP_MAGIC}).
 * In any case, if GZip decompression is being performed the input stream will
 * be buffered at a higher level, and thus this can read on a byte-oriented
 * basis.
 * 
 * @author Alex Blewitt
 * @version $Revision: $
 */
public class Segment {
	/**
	 * The magic header for a Pack200 Segment is 0xCAFED00D. I wonder where they
	 * get their inspiration from ...
	 */
	private static final int[] magic = { 0xCA, 0xFE, 0xD0, 0x0D };

	/**
	 * Decode a segment from the given input stream. This does not attempt to
	 * re-assemble or export any class files, but it contains enough information
	 * to be able to re-assemble class files by external callers.
	 * 
	 * @param in
	 *            the input stream to read from TODO At this point, this must be
	 *            a non-GZipped input stream, but this decoding could be done in
	 *            this method in the future (but perhaps more likely on an
	 *            archive as a whole)
	 * @return a segment parsed from the input stream
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	public static Segment parse(InputStream in) throws IOException,
			Pack200Exception {
		Segment segment = new Segment();
		segment.parseSegment(in);
		return segment;
	}

	/**
	 * Completely reads in a byte array, akin to the implementation in
	 * {@link java.lang.DataInputStream}. TODO Refactor out into a separate
	 * InputStream handling class
	 * 
	 * @param in
	 *            the input stream to read from
	 * @param data
	 *            the byte array to read into
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private static void readFully(InputStream in, byte[] data)
			throws IOException, Pack200Exception {
		int total = in.read(data);
		if (total == -1)
			throw new EOFException("Failed to read any data from input stream");
		while (total < data.length) {
			int delta = in.read(data, total, data.length - total);
			if (delta == -1)
				throw new EOFException(
						"Failed to read some data from input stream");
			total += delta;
		}
	}

	private long archiveModtime;

	private long archiveSize;

	private int attributeDefinitionCount;

	private int[] attributeDefinitionHeader;

	private String[] attributeDefinitionLayout;

	private String[] attributeDefinitionName;

	private InputStream bandHeadersInputStream;

	private int bandHeadersSize;

	private int classCount;

	private int[] classFieldCount;

	private String[][] classInterfaces;

	private int[] classMethodCount;

	private String[] classSuper;

	private String[] classThis;

	private String[] cpClass;

	private int cpClassCount;

	private String[] cpDescriptor;

	private int cpDescriptorCount;

	private double[] cpDouble;

	private int cpDoubleCount;

	private String[] cpFieldClass;

	private int cpFieldCount;

	private Object cpFieldDescriptor;

	private float[] cpFloat;

	private int cpFloatCount;

	private String[] cpIMethodClass;

	private int cpIMethodCount;

	private String[] cpIMethodDescriptor;

	private int[] cpInt;

	private int cpIntCount;

	private long[] cpLong;

	private int cpLongCount;

	private String[] cpMethodClass;

	private int cpMethodCount;

	private String[] cpMethodDescriptor;

	private String[] cpSignature;

	private int cpSignatureCount;

	private String[] cpString;

	private int cpStringCount;

	private String[] cpUTF8;

	private int cpUTF8Count;

	private int defaultClassMajorVersion;

	private int defaultClassMinorVersion;

	private int fieldAttrCount;

	private String[][] fieldDescr;

	private long[][] fieldFlags;

	private byte[][] fileBits;

	private long[] fileModtime;

	private String[] fileName;

	private long[] fileOptions;

	private long[] fileSize;

	private int[] icFlags;

	private Object icName;

	private String[] icOuterClass;

	private String[] icThisClass;

	private int innerClassCount;

	private int major;

	private int methodAttrCount;

	private String[][] methodDescr;

	private long[][] methodFlags;

	private int minor;

	private int numberOfFiles;

	private SegmentOptions options;

	private int segmentsRemaining;

	private int classAttrCount;

	private long[] classFlags;

	/**
	 * This is a local debugging message to aid the developer in writing this
	 * class. It will be removed before going into production. If the property
	 * 'debug.pack200' is set, this will generate messages to stderr; otherwise,
	 * it will be silent.
	 * 
	 * @param message
	 * @deprecated this should be removed from production code
	 */
	private void debug(String message) {
		if (System.getProperty("debug.pack200") != null) {
			System.err.println(message);
		}
	}

	/**
	 * Decode a band and return an array of <code>int[]</code> values
	 * 
	 * @param name
	 *            the name of the band (primarily for logging/debugging
	 *            purposes)
	 * @param in
	 *            the InputStream to decode from
	 * @param defaultCodec
	 *            the default codec for this band
	 * @param count
	 *            the number of elements to read
	 * @return an array of decoded <code>int[]</code> values
	 * @throws IOException
	 *             if there is a problem reading from the underlying input
	 *             stream
	 * @throws Pack200Exception
	 *             if there is a problem decoding the value or that the value is
	 *             invalid
	 */
	private int[] decodeBandInt(String name, InputStream in,
			BHSDCodec defaultCodec, int count) throws IOException,
			Pack200Exception {
		// TODO Might be able to improve this directly.
		int[] result = new int[count];

		// TODO We need to muck around in the scenario where the first value
		// read indicates
		// an uber-codec
		long[] longResult = decodeBandLong(name, in, defaultCodec, count);
		for (int i = 0; i < count; i++) {
			result[i] = (int) longResult[i];
		}
		return result;
	}

	/**
	 * Decode a band and return an array of <code>long[]</code> values
	 * 
	 * @param name
	 *            the name of the band (primarily for logging/debugging
	 *            purposes)
	 * @param in
	 *            the InputStream to decode from
	 * @param codec
	 *            the default codec for this band
	 * @param count
	 *            the number of elements to read
	 * @return an array of decoded <code>long[]</code> values
	 * @throws IOException
	 *             if there is a problem reading from the underlying input
	 *             stream
	 * @throws Pack200Exception
	 *             if there is a problem decoding the value or that the value is
	 *             invalid
	 */
	private long[] decodeBandLong(String name, InputStream in, BHSDCodec codec,
			int count) throws IOException, Pack200Exception {
		long[] result = codec.decode(count, in);
		if (result.length > 0) {
			int first = (int) result[0];
			if (codec.isSigned() && first >= -256 && first <= -1) {
				// TODO Well, switch codecs then ...
				Codec weShouldHaveUsed = CodecEncoding.getCodec(-1 - first,
						getBandHeadersInputStream(), codec);
				throw new Error("Bugger. We should have switched codec to "
						+ weShouldHaveUsed);
			} else if (!codec.isSigned() && first >= codec.getL()
					&& first <= codec.getL() + 255) {
				Codec weShouldHaveUsed = CodecEncoding.getCodec(first
						- codec.getL(), getBandHeadersInputStream(), codec);
				// TODO Well, switch codecs then ...
				throw new Error("Bugger. We should have switched codec to "
						+ weShouldHaveUsed);
			}
		}
		// TODO Remove debugging code
		debug("Parsed *" + name + " (" + result.length + ")");
		return result;
	}

	/**
	 * Decode a scalar from the band file. A scalar is like a band, but does not
	 * perform any band code switching.
	 * 
	 * @param name
	 *            the name of the scalar (primarily for logging/debugging
	 *            purposes)
	 * @param in
	 *            the input stream to read from
	 * @param codec
	 *            the codec for this scalar
	 * @return the decoded value
	 * @throws IOException
	 *             if there is a problem reading from the underlying input
	 *             stream
	 * @throws Pack200Exception
	 *             if there is a problem decoding the value or that the value is
	 *             invalid
	 */
	private long decodeScalar(String name, InputStream in, BHSDCodec codec)
			throws IOException, Pack200Exception {
		debug("Parsed #" + name + " (1)");
		return codec.decode(in);
	}

	/**
	 * Decode a number of scalars from the band file. A scalar is like a band,
	 * but does not perform any band code switching.
	 * 
	 * @param name
	 *            the name of the scalar (primarily for logging/debugging
	 *            purposes)
	 * @param in
	 *            the input stream to read from
	 * @param codec
	 *            the codec for this scalar
	 * @return an array of decoded <code>long[]</code> values
	 * @throws IOException
	 *             if there is a problem reading from the underlying input
	 *             stream
	 * @throws Pack200Exception
	 *             if there is a problem decoding the value or that the value is
	 *             invalid
	 */
	private long[] decodeScalar(String name, InputStream in, BHSDCodec codec,
			int n) throws IOException, Pack200Exception {
		// TODO Remove debugging code
		debug("Parsed #" + name + " (" + n + ")");
		return codec.decode(n, in);
	}

	public long getArchiveModtime() {
		return archiveModtime;
	}

	public long getArchiveSize() {
		return archiveSize;
	}

	/**
	 * Obtain the band headers data as an input stream. If no band headers are
	 * present, this will return an empty input stream to prevent any further
	 * reads taking place.
	 * 
	 * Note that as a stream, data consumed from this input stream can't be
	 * re-used. Data is only read from this stream if the encoding is such that
	 * additional information needs to be decoded from the stream itself.
	 * 
	 * @return the band headers input stream
	 */
	public InputStream getBandHeadersInputStream() {
		if (bandHeadersInputStream == null) {
			bandHeadersInputStream = new ByteArrayInputStream(new byte[0]);
		}
		return bandHeadersInputStream;

	}

	public int getNumberOfFiles() {
		return numberOfFiles;
	}

	private SegmentOptions getOptions() {
		return options;
	}

	public int getSegmentsRemaining() {
		return segmentsRemaining;
	}

	private void parseArchiveFileCounts(InputStream in) throws IOException,
			Pack200Exception {
		if (getOptions().hasArchiveFileCounts()) {
			setArchiveSize(decodeScalar("archive_size_hi", in, Codec.UNSIGNED5) << 32
					| decodeScalar("archive_size_lo", in, Codec.UNSIGNED5));
			setSegmentsRemaining(decodeScalar("archive_next_count", in,
					Codec.UNSIGNED5));
			setArchiveModtime(decodeScalar("archive_modtime", in,
					Codec.UNSIGNED5));
			setNumberOfFiles(decodeScalar("file_count", in, Codec.UNSIGNED5));
		}
	}

	private void parseArchiveSpecialCounts(InputStream in) throws IOException,
			Pack200Exception {
		if (getOptions().hasSpecialFormats()) {
			setBandHeadersSize(decodeScalar("band_headers_size", in,
					Codec.UNSIGNED5));
			setAttributeDefinitionCount(decodeScalar("attr_definition_count",
					in, Codec.UNSIGNED5));
		}
	}

	/**
	 * Reads {@link #attributeDefinitionCount} attribute definitions from the
	 * stream, into {@link #attributeDefinitionHeader},
	 * {@link #attributeDefinitionName} and {@link #attributeDefinitionLayout}.
	 * This affects the codecs that are used to parse non-standard bands. TODO
	 * Currently, these values if present cause a failure in the parsing.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private void parseAttributeDefinition(InputStream in) throws IOException,
			Pack200Exception {
		attributeDefinitionHeader = decodeBandInt("attr_definition_headers",
				in, Codec.BYTE1, attributeDefinitionCount);
		attributeDefinitionName = parseReferences("attr_definition_name", in,
				Codec.UNSIGNED5, attributeDefinitionCount, cpUTF8);
		attributeDefinitionLayout = parseReferences("attr_definition_layout",
				in, Codec.UNSIGNED5, attributeDefinitionCount, cpUTF8);
		if (attributeDefinitionCount > 0)
			throw new Error("No idea what the adc is for yet");
	}

	private void parseBcBands(InputStream in) {
		debug("Unimplemented bc_bands");
	}

	private void parseClassAttrBands(InputStream in) throws IOException,
			Pack200Exception {
		classFlags = parseFlags("class_flags", in, classCount, Codec.UNSIGNED5,
				options.hasClassFlagsHi());
		for (int i = 0; i < classCount; i++) {
			long flag = classFlags[i];
			if ((flag & (1 << 16)) != 0)
				classAttrCount++;
		}
		if (classAttrCount > 0)
			throw new Error(
					"There are attribute flags, and I don't know what to do with them");
		debug("unimplemented class_attr_count");
		debug("unimplemented class_attr_indexes");
		debug("unimplemented class_attr_calls");
		debug("unimplemented class_SourceFile_RUN");
		debug("unimplemented class_EnclosingMethod_RC");
		debug("unimplemented class_EnclosingMethod_RDN");
		debug("unimplemented class_Signature_RS");
		parseMetadataBands("class");
		debug("unimplemented class_InnerClasses_N");
		debug("unimplemented class_InnerClasses_RC");
		debug("unimplemented class_InnerClasses_F");
		debug("unimplemented class_InnerClasses_outer_RCN");
		debug("unimplemented class_InnerClasses_inner_RCN");
		debug("unimplemented class_file_version_minor_H");
		debug("unimplemented class_file_version_major_H");
	}

	private void parseClassBands(InputStream in) throws IOException,
			Pack200Exception {
		classThis = parseReferences("class_this", in, Codec.DELTA5, classCount,
				cpClass);
		classSuper = parseReferences("class_super", in, Codec.DELTA5,
				classCount, cpClass);
		classInterfaces = new String[classCount][];
		int[] classInterfaceLengths = decodeBandInt("class_interface_count",
				in, Codec.DELTA5, classCount);
		for (int i = 0; i < classCount; i++) {
			classInterfaces[i] = parseReferences("class_interface", in,
					Codec.DELTA5, classInterfaceLengths[i], cpClass);
		}
		classFieldCount = decodeBandInt("class_field_count", in, Codec.DELTA5,
				classCount);
		classMethodCount = decodeBandInt("class_method_count", in,
				Codec.DELTA5, classCount);
		parseFieldBands(in);
		parseMethodBands(in);
		parseClassAttrBands(in);
		parseCodeBands(in);
	}

	private void parseClassCounts(InputStream in) throws IOException,
			Pack200Exception {
		setInnerClassCount(decodeScalar("ic_count", in, Codec.UNSIGNED5));
		setDefaultClassMinorVersion(decodeScalar("default_class_minver", in,
				Codec.UNSIGNED5));
		setDefaultClassMajorVersion(decodeScalar("default_class_majver", in,
				Codec.UNSIGNED5));
		setClassCount(decodeScalar("class_count", in, Codec.UNSIGNED5));
	}

	private void parseCodeBands(InputStream in) {
		debug("unimplemented code_headers");
		debug("unimplemented code_max_stack");
		debug("unimplemented code_max_na_locals");
		debug("unimplemented code_hander_count");
		debug("unimplemented code_hander_start_P");
		debug("unimplemented code_hander_end_PO");
		debug("unimplemented code_hander_catch_PO");
		debug("unimplemented code_hander_class_RC");
		parseCodeAttrBands(in);
	}

	private void parseCodeAttrBands(InputStream in) {
		debug("unimplemented code_flags");
		debug("unimplemented code_attr_count");
		debug("unimplemented code_attr_indexes");
		debug("unimplemented code_attr_calls");
		debug("unimplemented code_LineNumberTable_N");
		debug("unimplemented code_LineNumberTable_bci_P");
		debug("unimplemented code_LineNumberTable_line");
		String[] types = { "LocalVariableTable", "LocalVariableTypeTable" };
		for (int i = 0; i < types.length; i++) {
			String type = types[i];
			debug("unimplemented code_" + type + "_N");
			debug("unimplemented code_" + type + "_bci_P");
			debug("unimplemented code_" + type + "_span_O");
			debug("unimplemented code_" + type + "_name_RU");
			debug("unimplemented code_" + type + "_type_RS");
			debug("unimplemented code_" + type + "_slot");
		}
	}

	/**
	 * Parses the constant pool class names, using {@link #cpClassCount} to
	 * populate {@link #cpClass} from {@link #cpUTF8}.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private void parseCpClass(InputStream in) throws IOException,
			Pack200Exception {
		cpClass = parseReferences("cp_Class", in, Codec.UDELTA5, cpClassCount,
				cpUTF8);
	}

	private void parseCpCounts(InputStream in) throws IOException,
			Pack200Exception {
		setCPUtf8Count(decodeScalar("cp_Utf8_count", in, Codec.UNSIGNED5));
		if (getOptions().hasCPNumberCounts()) {
			setCPIntCount(decodeScalar("cp_Int_count", in, Codec.UNSIGNED5));
			setCPFloatCount(decodeScalar("cp_Float_count", in, Codec.UNSIGNED5));
			setCPLongCount(decodeScalar("cp_Long_count", in, Codec.UNSIGNED5));
			setCPDoubleCount(decodeScalar("cp_Double_count", in,
					Codec.UNSIGNED5));
		}
		setCPStringCount(decodeScalar("cp_String_count", in, Codec.UNSIGNED5));
		setCPClassCount(decodeScalar("cp_Class_count", in, Codec.UNSIGNED5));
		setCPSignatureCount(decodeScalar("cp_Signature_count", in,
				Codec.UNSIGNED5));
		setCPDescriptorCount(decodeScalar("cp_Descr_count", in, Codec.UNSIGNED5));
		setCPFieldCount(decodeScalar("cp_Field_count", in, Codec.UNSIGNED5));
		setCPMethodCount(decodeScalar("cp_Method_count", in, Codec.UNSIGNED5));
		setCPIMethodCount(decodeScalar("cp_Imethod_count", in, Codec.UNSIGNED5));
	}

	/**
	 * Parses the constant pool descriptor definitions, using
	 * {@link #cpDescriptorCount} to populate {@link #cpDescriptor}. For ease
	 * of use, the cpDescriptor is stored as a string of the form <i>name:type</i>,
	 * largely to make it easier for representing field and method descriptors
	 * (e.g. <code>out:java.lang.PrintStream</code>) in a way that is
	 * compatible with passing String arrays.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private void parseCpDescriptor(InputStream in) throws IOException,
			Pack200Exception {
		String[] cpDescriptorNames = parseReferences("cp_Descr_name", in,
				Codec.DELTA5, cpDescriptorCount, cpUTF8);
		String[] cpDescriptorTypes = parseReferences("cp_Descr_type", in,
				Codec.UDELTA5, cpDescriptorCount, cpSignature);
		cpDescriptor = new String[cpDescriptorCount];
		for (int i = 0; i < cpDescriptorCount; i++) {
			cpDescriptor[i] = cpDescriptorNames[i] + ":" + cpDescriptorTypes[i];
		}
	}

	private void parseCpDouble(InputStream in) throws IOException,
			Pack200Exception {
		cpDouble = new double[cpDoubleCount];
		long[] hiBits = decodeBandLong("cp_Double_hi", in, Codec.UDELTA5,
				cpDoubleCount);
		long[] loBits = decodeBandLong("cp_Double_lo", in, Codec.DELTA5,
				cpDoubleCount);
		for (int i = 0; i < cpDoubleCount; i++) {
			cpDouble[i] = Double.longBitsToDouble(hiBits[i] << 32 | loBits[i]);
		}
	}

	/**
	 * Parses the constant pool field definitions, using {@link #cpFieldCount}
	 * to populate {@link #cpFieldClass} and {@link #cpFieldDescriptor}.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private void parseCpField(InputStream in) throws IOException,
			Pack200Exception {
		cpFieldClass = parseReferences("cp_Field_class", in, Codec.DELTA5,
				cpFieldCount, cpClass);
		cpFieldDescriptor = parseReferences("cp_Field_desc", in, Codec.UDELTA5,
				cpFieldCount, cpDescriptor);
	}

	private void parseCpFloat(InputStream in) throws IOException,
			Pack200Exception {
		cpFloat = new float[cpFloatCount];
		int floatBits[] = decodeBandInt("cp_Float", in, Codec.UDELTA5,
				cpFloatCount);
		for (int i = 0; i < cpFloatCount; i++) {
			cpFloat[i] = Float.intBitsToFloat(floatBits[i]);
		}
	}

	/**
	 * Parses the constant pool interface method definitions, using
	 * {@link #cpIMethodCount} to populate {@link #cpIMethodClass} and
	 * {@link #cpIMethodDescriptor}.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private void parseCpIMethod(InputStream in) throws IOException,
			Pack200Exception {
		cpIMethodClass = parseReferences("cp_Imethod_class", in, Codec.DELTA5,
				cpIMethodCount, cpClass);
		cpIMethodDescriptor = parseReferences("cp_Imethod_desc", in,
				Codec.UDELTA5, cpIMethodCount, cpDescriptor);
	}

	private void parseCpInt(InputStream in) throws IOException,
			Pack200Exception {
		cpInt = new int[cpIntCount];
		long last = 0;
		for (int i = 0; i < cpIntCount; i++) {
			last = Codec.UDELTA5.decode(in, last);
			cpInt[i] = (int) last;
		}
	}

	private void parseCpLong(InputStream in) throws IOException,
			Pack200Exception {
		cpLong = parseFlags("cp_Long", in, cpLongCount, Codec.UDELTA5,
				Codec.DELTA5);
	}

	/**
	 * Parses the constant pool method definitions, using {@link #cpMethodCount}
	 * to populate {@link #cpMethodClass} and {@link #cpMethodDescriptor}.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private void parseCpMethod(InputStream in) throws IOException,
			Pack200Exception {
		cpMethodClass = parseReferences("cp_Method_class", in, Codec.DELTA5,
				cpMethodCount, cpClass);
		cpMethodDescriptor = parseReferences("cp_Method_desc", in,
				Codec.UDELTA5, cpMethodCount, cpDescriptor);
	}

	/**
	 * Parses the constant pool signature classes, using
	 * {@link #cpSignatureCount} to populate {@link #cpSignature}. A signature
	 * form is akin to the bytecode representation of a class; Z for boolean, I
	 * for int, [ for array etc. However, although classes are started with L,
	 * the classname does not follow the form; instead, there is a separate
	 * array of classes. So an array corresponding to
	 * <code>public static void main(String args[])</code> has a form of
	 * <code>[L(V)</code> and a classes array of
	 * <code>[java.lang.String]</code>. The {@link #cpSignature} is a string
	 * represenation identical to the bytecode equivalent
	 * <code>[Ljava/lang/String;(V)</code> TODO Check that the form is as
	 * above and update other types e.g. J
	 * 
	 * @param in
	 *            the input stream to read from
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private void parseCpSignature(InputStream in) throws IOException,
			Pack200Exception {
		String[] cpSignatureForm = parseReferences("cp_Signature_form", in,
				Codec.DELTA5, cpSignatureCount, cpUTF8);
		cpSignature = new String[cpSignatureCount];
		long last = 0;
		for (int i = 0; i < cpSignatureCount; i++) {
			String form = cpSignatureForm[i];
			int len = form.length();
			StringBuffer signature = new StringBuffer(64);
			ArrayList list = new ArrayList();
			for (int j = 0; j < len; j++) {
				char c = form.charAt(j);
				signature.append(c);
				if (c == 'L') {
					int index = (int) (last = Codec.UDELTA5.decode(in, last));
					String className = cpClass[index];
					list.add(className);
					signature.append(className);
				}
			}
			cpSignature[i] = signature.toString();
		}
	}

	/**
	 * Parses the constant pool strings, using {@link #cpStringCount} to
	 * populate {@link #cpString} from indexes into {@link #cpUTF8}.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private void parseCpString(InputStream in) throws IOException,
			Pack200Exception {
		cpString = new String[cpStringCount];
		long last = 0;
		for (int i = 0; i < cpStringCount; i++) {
			int index = (int) (last = Codec.UDELTA5.decode(in, last));
			cpString[i] = cpUTF8[index];
		}
	}

	private void parseCpUtf8(InputStream in) throws IOException,
			Pack200Exception {
		// TODO Update codec.decode -> decodeScalar
		cpUTF8 = new String[(int) cpUTF8Count];
		cpUTF8[0] = "";
		int[] prefix = new int[cpUTF8Count];
		int[] suffix = new int[cpUTF8Count];
		if (cpUTF8Count > 0) {
			prefix[0] = 0;
			suffix[0] = 0;
			if (cpUTF8Count > 1)
				prefix[1] = 0;
		}
		long last = 0;
		for (int i = 2; i < cpUTF8Count; i++) {
			last = prefix[i] = (int) Codec.DELTA5.decode(in, last);
		}
		int chars = 0;
		int bigSuffix = 0;
		for (int i = 1; i < cpUTF8Count; i++) {
			last = suffix[i] = (int) Codec.UNSIGNED5.decode(in);
			if (last == 0) {
				bigSuffix++;
			} else {
				chars += last;
			}
		}
		char data[] = new char[chars];
		for (int i = 0; i < data.length; i++) {
			data[i] = (char) Codec.CHAR3.decode(in);
		}
		// read in the big suffix data
		char bigSuffixData[][] = new char[bigSuffix][];
		last = 0;
		for (int i = 0; i < bigSuffix; i++) {
			last = (int) Codec.DELTA5.decode(in, last);
			bigSuffixData[i] = new char[(int) last];
		}
		// initialise big suffix data
		for (int i = 0; i < bigSuffix; i++) {
			char[] singleBigSuffixData = bigSuffixData[i];
			last = 0;
			for (int j = 0; j < singleBigSuffixData.length; j++) {
				last = singleBigSuffixData[j] = (char) Codec.DELTA5.decode(in,
						last);
			}
		}
		// go through the strings
		chars = 0;
		bigSuffix = 0;
		for (int i = 1; i < cpUTF8Count; i++) {
			String lastString = cpUTF8[i - 1];
			if (suffix[i] == 0) {
				// The big suffix stuff hasn't been tested, and I'll be
				// surprised if it works first time w/o errors ...
				cpUTF8[i] = lastString.substring(0, prefix[i])
						+ new String(bigSuffixData[bigSuffix++]);
			} else {
				cpUTF8[i] = lastString.substring(0, prefix[i])
						+ new String(data, chars, suffix[i]);
				chars += suffix[i];
			}
		}
	}

	private void parseFieldBands(InputStream in) throws IOException,
			Pack200Exception {
		fieldDescr = new String[classCount][];
		for (int i = 0; i < classCount; i++) {
			fieldDescr[i] = parseReferences("field_descr", in, Codec.DELTA5,
					classFieldCount[i], cpDescriptor);
		}
		fieldFlags = new long[classCount][];
		for (int i = 0; i < classCount; i++) {
			fieldFlags[i] = parseFlags("field_flags", in, classFieldCount[i],
					Codec.UNSIGNED5, options.hasFieldFlagsHi());
		}
		for (int i = 0; i < classCount; i++) {
			for (int j = 0; j < fieldFlags[i].length; j++) {
				long flag = fieldFlags[i][j];
				if ((flag & (1 << 16)) != 0)
					fieldAttrCount++;
			}
		}
		if (fieldAttrCount > 0)
			throw new Error(
					"There are attribute flags, and I don't know what to do with them");
		debug("unimplemented field_attr_indexes");
		debug("unimplemented field_attr_calls");
		debug("unimplemented field_ConstantValueKQ");
		debug("unimplemented field_Signature_RS");
		parseMetadataBands("field");
	}

	/**
	 * Parses the file band headers (not including the actual bits themselves).
	 * At the end of this parse call, the input stream will be positioned at the
	 * start of the file_bits themselves, and there will be Sum(file_size) bits
	 * remaining in the stream with BYTE1 compression. A decent implementation
	 * will probably just stream the bytes out to the reconstituted Jar rather
	 * than caching them.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private void parseFileBands(InputStream in) throws IOException,
			Pack200Exception {
		long last;
		fileName = parseReferences("file_name", in, Codec.UNSIGNED5,
				numberOfFiles, cpUTF8);
		fileSize = new long[numberOfFiles];
		if (options.hasFileSizeHi()) {
			last = 0;
			for (int i = 0; i < numberOfFiles; i++) {
				fileSize[i] = (last = Codec.UNSIGNED5.decode(in, last)) << 32;
			}
		}
		last = 0;
		for (int i = 0; i < numberOfFiles; i++) {
			fileSize[i] |= (last = Codec.UNSIGNED5.decode(in, last));
		}
		fileModtime = new long[numberOfFiles];
		if (options.hasFileModtime()) {
			last = 0;
			for (int i = 0; i < numberOfFiles; i++) {
				fileModtime[i] |= (last = Codec.DELTA5.decode(in, last));
			}
		}
		fileOptions = new long[numberOfFiles];
		if (options.hasFileOptions()) {
			last = 0;
			for (int i = 0; i < numberOfFiles; i++) {
				fileOptions[i] |= (last = Codec.UNSIGNED5.decode(in, last));
			}
		}
	}

	private long[] parseFlags(String name, InputStream in, int count,
			Codec codec) throws IOException, Pack200Exception {
		return parseFlags(name, in, count, codec, true);
	}

	private long[] parseFlags(String name, InputStream in, int count,
			Codec codec, boolean hasHi) throws IOException, Pack200Exception {
		return parseFlags(name, in, count, (hasHi ? codec : null), codec);
	}

	private long[] parseFlags(String name, InputStream in, int count,
			Codec hiCodec, Codec loCodec) throws IOException, Pack200Exception {
		long[] result = new long[count];
		// TODO Refactor into band parsing
		long last = 0;
		for (int i = 0; i < count && hiCodec != null; i++) {
			last = hiCodec.decode(in, last);
			result[i] = last << 32;
		}
		for (int i = 0; i < count; i++) {
			last = loCodec.decode(in, last);
			result[i] = result[i] | last;
		}
		// TODO Remove debugging code
		debug("Parsed *" + name + " (" + result.length + ")");
		return result;
	}

	private void parseIcBands(InputStream in) throws IOException,
			Pack200Exception {
		icThisClass = parseReferences("ic_this_class", in, Codec.UDELTA5,
				innerClassCount, cpClass);
		icFlags = new int[innerClassCount];
		long last = 0;
		int outerClasses = 0;
		// ic_flags
		for (int i = 0; i < innerClassCount; i++) {
			icFlags[i] = (int) (last = Codec.UNSIGNED5.decode(in, last));
			if ((icFlags[i] & 1 << 16) != 0)
				outerClasses++;
		}
		icOuterClass = parseReferences("ic_outer_class", in, Codec.DELTA5,
				outerClasses, cpClass);
		icName = parseReferences("ic_name", in, Codec.DELTA5, outerClasses,
				cpUTF8);
	}

	private void parseMethodBands(InputStream in) throws IOException,
			Pack200Exception {
		methodDescr = new String[classCount][];
		for (int i = 0; i < classCount; i++) {
			methodDescr[i] = parseReferences("method_descr", in, Codec.MDELTA5,
					classMethodCount[i], cpDescriptor);
		}
		methodFlags = new long[classCount][];
		for (int i = 0; i < classCount; i++) {
			methodFlags[i] = parseFlags("method_flags", in,
					classMethodCount[i], Codec.UNSIGNED5, options
							.hasMethodFlagsHi());
		}
		for (int i = 0; i < classCount; i++) {
			for (int j = 0; j < methodFlags[i].length; j++) {
				long flag = methodFlags[i][j];
				if ((flag & (1 << 16)) != 0)
					methodAttrCount++;
			}
		}
		if (methodAttrCount > 0)
			throw new Error(
					"There are method attribute flags, and I don't know what to do with them");
		debug("unimplemented method_attr_count");
		debug("unimplemented method_attr_indexes");
		debug("unimplemented method_attr_calls");
		debug("unimplemented method_Exceptions_N");
		debug("unimplemented method_Exceptions_RC");
		debug("unimplemented method_Signature_RS");
		parseMetadataBands("method");
	}

	private void parseMetadataBands(String unit) throws Pack200Exception {
		String[] RxA;
		if ("method".equals(unit)) {
			RxA = new String[] { "RVA", "RIA", "RVPA", "RIPA", "AD" };
		} else if ("field".equals(unit) || "class".equals(unit)) {
			RxA = new String[] { "RVA", "RIA" };
		} else {
			throw new Pack200Exception("Unknown type of metadata unit " + unit);
		}
		for (int i = 0; i < RxA.length; i++) {
			String rxa = RxA[i];
			if (rxa.indexOf("P") >= 0) {
				debug("unimplemented " + unit + "_" + rxa + "_param_NB");
			}
			if (!rxa.equals("AD")) {
				debug("unimplemented " + unit + "_" + rxa + "_anno_N");
				debug("unimplemented " + unit + "_" + rxa + "_type_RS");
				debug("unimplemented " + unit + "_" + rxa + "_pair_N");
				debug("unimplemented " + unit + "_" + rxa + "_name_RU");
			}
			debug("unimplemented " + unit + "_" + rxa + "_T");
			debug("unimplemented " + unit + "_" + rxa + "_caseI_KI");
			debug("unimplemented " + unit + "_" + rxa + "_caseD_KD");
			debug("unimplemented " + unit + "_" + rxa + "_caseF_KF");
			debug("unimplemented " + unit + "_" + rxa + "_caseJ_KJ");
			debug("unimplemented " + unit + "_" + rxa + "_casec_RS");
			debug("unimplemented " + unit + "_" + rxa + "_caseet_RS");
			debug("unimplemented " + unit + "_" + rxa + "_caseec_RU");
			debug("unimplemented " + unit + "_" + rxa + "_cases_RU");
			debug("unimplemented " + unit + "_" + rxa + "_casearray_N");
			debug("unimplemented " + unit + "_" + rxa + "_nesttype_RS");
			debug("unimplemented " + unit + "_" + rxa + "_nestpair_N");
			debug("unimplemented " + unit + "_" + rxa + "_nestname_RU");
		}
	}

	/**
	 * Helper method to parse <i>count</i> references from <code>in</code>,
	 * using <code>codec</code> to decode the values as indexes into
	 * <code>reference</code> (which is populated prior to this call). An
	 * exception is thrown if a decoded index falls outside the range
	 * [0..reference.length-1].
	 * 
	 * @param name
	 *            TODO
	 * @param in
	 *            the input stream to read from
	 * @param codec
	 *            the codec to use for decoding
	 * @param count
	 *            the number of references to decode
	 * @param reference
	 *            the array of values to use for the indexes; often
	 *            {@link #cpUTF8}
	 * 
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private String[] parseReferences(String name, InputStream in,
			BHSDCodec codec, int count, String[] reference) throws IOException,
			Pack200Exception {
		String[] result = new String[count];
		int[] decode = decodeBandInt(name, in, codec, count);
		for (int i = 0; i < count; i++) {
			int index = decode[i];
			if (index < 0 || index >= reference.length)
				throw new Pack200Exception(
						"Something has gone wrong during parsing references");
			result[i] = reference[index];
		}
		return result;
	}

	/**
	 * This performs the actual work of parsing against a non-static instance of
	 * Segment.
	 * 
	 * @param in
	 *            the input stream to read from
	 * @throws IOException
	 *             if a problem occurs during reading from the underlying stream
	 * @throws Pack200Exception
	 *             if a problem occurs with an unexpected value or unsupported
	 *             codec
	 */
	private void parseSegment(InputStream in) throws IOException,
			Pack200Exception {
		debug("-------");
		parseSegmentHeader(in);
		if (bandHeadersSize > 0) {
			byte[] bandHeaders = new byte[(int) bandHeadersSize];
			readFully(in, bandHeaders);
			setBandHeadersData(bandHeaders);
		}
		parseCpUtf8(in);
		parseCpInt(in);
		parseCpFloat(in);
		parseCpLong(in);
		parseCpDouble(in);
		parseCpString(in);
		parseCpClass(in);
		parseCpSignature(in);
		parseCpDescriptor(in);
		parseCpField(in);
		parseCpMethod(in);
		parseCpIMethod(in);
		parseAttributeDefinition(in);
		parseIcBands(in);
		parseClassBands(in);
		parseBcBands(in);
		// TODO Re-enable these after completing class/bytecode bands
		// parseFileBands(in);
		// processFileBits(in); // this just caches them in file_bits; it should
		// probably start writing here?
	}

	private void parseSegmentHeader(InputStream in) throws IOException,
			Pack200Exception, Error, Pack200Exception {
		long word[] = decodeScalar("archive_magic_word", in, Codec.BYTE1,
				magic.length);
		for (int m = 0; m < magic.length; m++)
			if (word[m] != magic[m])
				throw new Error("Bad header");
		setMinorVersion((int) decodeScalar("archive_minver", in,
				Codec.UNSIGNED5));
		setMajorVersion((int) decodeScalar("archive_majver", in,
				Codec.UNSIGNED5));
		setOptions(new SegmentOptions((int) decodeScalar("archive_options", in,
				Codec.UNSIGNED5)));
		parseArchiveFileCounts(in);
		parseArchiveSpecialCounts(in);
		parseCpCounts(in);
		parseClassCounts(in);
	}

	private void processFileBits(InputStream in) throws IOException,
			Pack200Exception {
		// now read in the bytes
		fileBits = new byte[numberOfFiles][];
		for (int i = 0; i < numberOfFiles; i++) {
			int size = (int) fileSize[i];
			// TODO This buggers up if file_size > 2^32. Probably an array is
			// not the right choice, and
			// we should just serialise the bugger here?
			fileBits[i] = new byte[size];
			for (int j = 0; j < size; j++) {
				fileBits[i][j] = (byte) Codec.BYTE1.decode(in);
			}
		}
	}

	public void setArchiveModtime(long archiveModtime) {
		this.archiveModtime = archiveModtime;
	}

	public void setArchiveSize(long archiveSize) {
		this.archiveSize = archiveSize;
	}

	private void setAttributeDefinitionCount(long valuie) {
		this.attributeDefinitionCount = (int) valuie;
	}

	private void setBandHeadersData(byte[] bandHeaders) {
		this.bandHeadersInputStream = new ByteArrayInputStream(bandHeaders);
	}

	private void setBandHeadersSize(long value) {
		this.bandHeadersSize = (int) value;
	}

	private void setClassCount(long value) {
		classCount = (int) value;
	}

	private void setCPClassCount(long value) {
		cpClassCount = (int) value;
	}

	private void setCPDescriptorCount(long value) {
		cpDescriptorCount = (int) value;
	}

	private void setCPDoubleCount(long value) {
		cpDoubleCount = (int) value;
	}

	private void setCPFieldCount(long value) {
		cpFieldCount = (int) value;
	}

	private void setCPFloatCount(long value) {
		cpFloatCount = (int) value;
	}

	private void setCPIMethodCount(long value) {
		cpIMethodCount = (int) value;
	}

	private void setCPIntCount(long value) {
		cpIntCount = (int) value;
	}

	private void setCPLongCount(long value) {
		cpLongCount = (int) value;
	}

	private void setCPMethodCount(long value) {
		cpMethodCount = (int) value;
	}

	private void setCPSignatureCount(long value) {
		cpSignatureCount = (int) value;
	}

	private void setCPStringCount(long value) {
		cpStringCount = (int) value;
	}

	private void setCPUtf8Count(long value) {
		cpUTF8Count = (int) value;
	}

	private void setDefaultClassMajorVersion(long value) {
		defaultClassMajorVersion = (int) value;
	}

	private void setDefaultClassMinorVersion(long value) {
		defaultClassMinorVersion = (int) value;
	}

	private void setInnerClassCount(long value) {
		innerClassCount = (int) value;
	}

	/**
	 * Sets the major version of this archive.
	 * 
	 * @param version
	 *            the minor version of the archive
	 * @throws Pack200Exception
	 *             if the major version is not 150
	 */
	private void setMajorVersion(int version) throws Pack200Exception {
		if (version != 150)
			throw new Pack200Exception("Invalid segment major version");
		major = version;
	}

	/**
	 * Sets the minor version of this archive
	 * 
	 * @param version
	 *            the minor version of the archive
	 * @throws Pack200Exception
	 *             if the minor version is not 7
	 */
	private void setMinorVersion(int version) throws Pack200Exception {
		if (version != 7)
			throw new Pack200Exception("Invalid segment minor version");
		minor = version;
	}

	public void setNumberOfFiles(long value) {
		numberOfFiles = (int) value;
	}

	private void setOptions(SegmentOptions options) {
		this.options = options;
	}

	public void setSegmentsRemaining(long value) {
		segmentsRemaining = (int) value;
	}
}
