package com.swatt.util.io;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

public class DataStreamUtilities {
	private static final int NULL = 1;
	private static final int DATA_STREAM_SERIALIZABLE = 2;
	private static final int STRING = 3;
	
	public static final void write(String fileName, DataStreamSerializable value) throws SerializationException, FileNotFoundException {
		write(new File(fileName), value);
	}
 
	public static final void write(File file, DataStreamSerializable value) throws SerializationException, FileNotFoundException {
		FileOutputStream fout = new FileOutputStream(file);
		DataOutput dout = new DataOutputStream(fout);
		writeDataStreamSerializable(dout, value);
	}
	
	public static final DataStreamSerializable read(String fileName, Class<?> clazz) throws SerializationException, FileNotFoundException {
		return read(new File(fileName), clazz);
	}
	
	public static final DataStreamSerializable read(File file, Class<?> clazz) throws SerializationException, FileNotFoundException {
		FileInputStream fin = new FileInputStream(file);
		DataInput din = new DataInputStream(fin);
		return readDataStreamSerializable(din, clazz);
	}
	
	
	public static final void writeString(DataOutput dout, String str) throws IOException {
		if (str == null) 
			dout.writeBoolean(false);
		else {
			dout.writeBoolean(true);
			dout.writeUTF(str);
		}
	}
	
	public static final String readString(DataInput din) throws IOException {
		if (din.readBoolean())  {
			return din.readUTF();
		} else
			return null;
	}
	
	public static final void writeBoolean(DataOutput dout, boolean value) throws IOException { dout.writeBoolean(value); }
	public static final void writeInt(DataOutput dout, int value) throws IOException { dout.writeInt(value); }
	public static final void writeLong(DataOutput dout, long value) throws IOException { dout.writeLong(value); }
	public static final void writeFloat(DataOutput dout, float value) throws IOException { dout.writeFloat(value); }
	public static final void writeDouble(DataOutput dout, double value) throws IOException { dout.writeDouble(value); }
	
	public static final boolean readBoolean(DataInput din) throws IOException { return din.readBoolean(); }
	public static final int readInt(DataInput din) throws IOException { return din.readInt(); }
	public static final long readLong(DataInput din) throws IOException { return din.readLong(); }
	public static final float readFloat(DataInput din) throws IOException { return din.readFloat(); }
	public static final double readDouble(DataInput din) throws IOException { return din.readDouble(); }
		

	public static final void writeBytes(DataOutput dout, byte buf[]) throws IOException {
		if (buf == null) 
			dout.writeInt(-1);
		else {
			dout.writeInt(buf.length);
			dout.write(buf);
		}
	}
	
	public static final void writeBooleans(DataOutput dout, boolean buf[]) throws IOException {
		if (buf == null) 
			dout.writeInt(-1);
		else {
			dout.writeInt(buf.length);
			for (int i=0; i < buf.length; i++) 
				dout.writeBoolean(buf[i]);
		}
	}
	
	public static final void writeInts(DataOutput dout, int buf[]) throws IOException {
		if (buf == null) 
			dout.writeInt(-1);
		else {
			dout.writeInt(buf.length);
			for (int i=0; i < buf.length; i++) 
				dout.writeInt(buf[i]);
		}
	}
	
	public static final void writeLongs(DataOutput dout, long buf[]) throws IOException {
		if (buf == null) 
			dout.writeInt(-1);
		else {
			dout.writeInt(buf.length);
			for (int i=0; i < buf.length; i++) 
				dout.writeLong(buf[i]);
		}
	}
	
	public static final void writeFloats(DataOutput dout, float buf[]) throws IOException {
		if (buf == null) 
			dout.writeInt(-1);
		else {
			dout.writeInt(buf.length);
			for (int i=0; i < buf.length; i++) 
				dout.writeFloat(buf[i]);
		}
	}	
	public static final void writeDoubles(DataOutput dout, double buf[]) throws IOException {
		if (buf == null) 
			dout.writeInt(-1);
		else {
			dout.writeInt(buf.length);
			for (int i=0; i < buf.length; i++) 
				dout.writeDouble(buf[i]);
		}
	}
	
	public static final byte[] readBytes(DataInput din) throws IOException {
		int size = din.readInt();
		
		if (size < 0)
			return null;
		else {
			byte buf[] = new byte[size];
			din.readFully(buf);
			return buf;
		}
	}
	
	public static final boolean[] readBooleans(DataInput din) throws IOException {
		int size = din.readInt();
		
		if (size < 0)
			return null;
		else {
			boolean buf[] = new boolean[size];
			
			for (int i=0; i < size; i++)
				buf[i] = din.readBoolean();
			
			return buf;
		}
	}
	
	public static final int[] readInts(DataInput din) throws IOException {
		int size = din.readInt();
		
		if (size < 0)
			return null;
		else {
			int buf[] = new int[size];
			
			for (int i=0; i < size; i++)
				buf[i] = din.readInt();
			
			return buf;
		}
	}
	
	public static final long[] readLongs(DataInput din) throws IOException {
		int size = din.readInt();
		
		if (size < 0)
			return null;
		else {
			long buf[] = new long[size];
			
			for (int i=0; i < size; i++)
				buf[i] = din.readLong();
			
			return buf;
		}
	}
	
	public static final float[] readFloats(DataInput din) throws IOException {
		int size = din.readInt();
		
		if (size < 0)
			return null;
		else {
			float buf[] = new float[size];
			
			for (int i=0; i < size; i++)
				buf[i] = din.readFloat();
			
			return buf;
		}
	}
	
	public static final double[] readDoubles(DataInput din) throws IOException {
		int size = din.readInt();
		
		if (size < 0)
			return null;
		else {
			double buf[] = new double[size];
			
			for (int i=0; i < size; i++)
				buf[i] = din.readDouble();
			
			return buf;
		}
	}
	
	public static final void writeDataStreamSerializable(DataOutput dout, DataStreamSerializable value) throws SerializationException {
		try {
			if (value == null) 
				dout.writeBoolean(false);
			else {
				dout.writeBoolean(true);
				value.write(dout);
			}
		} catch (SerializationException e) {
			throw e;
		} catch (IOException e) {
			throw new SerializationException("Unable to do a nested write", e);
		}
	}

	public static final DataStreamSerializable readDataStreamSerializable(DataInput din, Class<?> clazz) throws SerializationException {
		try {
			if (din.readBoolean()) {
				DataStreamSerializable dataStreamSerializable = (DataStreamSerializable) clazz.newInstance();
				dataStreamSerializable.read(din);
				return dataStreamSerializable;
			} else
				return null;
		} catch (SerializationException e) {
			throw e;
		} catch (Exception e) {
			throw new SerializationException("Unable to do a nested read", e);
		}
	}
	
	public static final void writeDataStreamSerializables(DataOutput dout, DataStreamSerializable buf[]) throws IOException {
		if (buf == null) 
			dout.writeInt(-1);
		else {
			dout.writeInt(buf.length);
			for (int i=0; i < buf.length; i++) 
				writeDataStreamSerializable(dout, buf[i]);
		}
	}

	public static final DataStreamSerializable[] readDataStreamSerializables(DataInput din, Class<?> clazz) throws IOException {
		int size = din.readInt();
		
		if (size < 0)
			return null;
		else {
			DataStreamSerializable buf[] = (DataStreamSerializable[]) Array.newInstance(clazz, size);
			
			for (int i=0; i < size; i++)
				buf[i] = readDataStreamSerializable(din, clazz);
			
			return buf;
		}
	}
	
//	public static final void writeDataStreamSerializables(DataOutput dout, List<?> dataStreamSerializables) throws SerializationException {
//		try {
//			if (dataStreamSerializables == null) 
//				dout.writeInt(-1);
//			else {
//				dout.writeInt(dataStreamSerializables.size());
//				for (Iterator<?> i = dataStreamSerializables.iterator(); i.hasNext();) {
//					DataStreamSerializable dataStreamSerializable = (DataStreamSerializable) i.next();
//					writeDataStreamSerializable(dout, dataStreamSerializable);
//				}
//			}
//		} catch (Throwable t) {
//			throw new SerializationException("Unable to write Collection: " + t);
//		}
//	}
//
//	public static final Collection readDataStreamSerializables(DataInput din, Class<?> collectionClass, Class elementClass) throws SerializationException {
//		try {
//			int size = din.readInt();
//			
//			if (size < 0)
//				return null;
//			else {
//				Collection result = (Collection<?>) collectionClass.newInstance();
//				
//				for (int i=0; i < size; i++) {
//					DataStreamSerializable dataStreamSerializable = readDataStreamSerializable(din, elementClass);
//					result.add(dataStreamSerializable);
//				}
//				
//				return result;
//			}
//		} catch (Throwable t) {
//			throw new SerializationException("Unable to read Collection: " + t);
//		}
//	}
	
	
	public static final void writeObject(DataOutput dout, Object value) throws SerializationException {
		try {
			if (value == null) 
				dout.writeByte(NULL);
			else if (value.getClass().isArray())
				throw new SerializationException("DataStreamSerializableUtilities.writeObject() does not support arrays yet (sorry)");
			else if (value instanceof DataStreamSerializable){
				dout.writeByte(DATA_STREAM_SERIALIZABLE);
				dout.writeUTF(value.getClass().getName());		// Very Inefficient...
				DataStreamSerializable dataStreamSerializable = (DataStreamSerializable) value;
				dataStreamSerializable.write(dout);
			} else if (value instanceof String) {
				dout.writeByte(STRING);
				String s = (String) value;
				dout.writeUTF(s);
			} else
				throw new SerializationException("Unserializable Type: " + value.getClass());
		} catch (SerializationException e) {
			throw e;
		} catch (IOException e) {
			throw new SerializationException("Unable to do a nested write", e);
		}
	}
	
	public static final Object readObject(DataInput din) throws IOException {
		Class<?> clazz = null;
		
		try {
			
			switch(din.readByte()) {
				case NULL:
					return null;
					
				case DATA_STREAM_SERIALIZABLE:
					String className = din.readUTF();
					clazz = Class.forName(className);		// VERY Inefficient...
					DataStreamSerializable dataStreamSerializable;
						dataStreamSerializable = (DataStreamSerializable) clazz.newInstance();
					dataStreamSerializable.read(din);
					return dataStreamSerializable;
					
				case STRING:
					return din.readUTF();
					
				default:
					throw new SerializationException("Corrupted Stream");
			}
		} catch (ClassNotFoundException e) {
			throw new SerializationException("Class Not Found: " + clazz, e);
		} catch (InstantiationException e) {
			throw new SerializationException("Unable to instantiate: " + clazz, e);
		} catch (IllegalAccessException e) {
			throw new SerializationException("Unable to access: " + clazz, e);

		}
	}
	
	public static final DataStreamSerializable copy(DataStreamSerializable dataStreamSerializable) throws SerializationException {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(bout);
			writeDataStreamSerializable(dout, dataStreamSerializable);
			dout.close();
			
			ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
			DataInputStream din = new DataInputStream(bin);
			DataStreamSerializable result = readDataStreamSerializable(din, dataStreamSerializable.getClass());
			bin.close();
			
			return result;
		} catch (Throwable e) {
			throw new SerializationException("Unable to Copy", e);
		}
	}
	
	public static final DataStreamSerializable[] copy(DataStreamSerializable dataStreamSerializables[]) throws SerializationException {
		try {
			if (dataStreamSerializables == null)
				return null;

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(bout);
			writeDataStreamSerializables(dout, dataStreamSerializables);
			dout.close();

			ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
			DataInputStream din = new DataInputStream(bin);
			DataStreamSerializable result[] = readDataStreamSerializables(din, dataStreamSerializables.getClass().getComponentType());
			bin.close();
			
			return result;
		} catch (Throwable e) {
			throw new SerializationException("Unable to Copy", e);
		}
	}
}
