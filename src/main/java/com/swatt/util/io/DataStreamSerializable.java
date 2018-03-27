package com.swatt.util.io;


import java.io.DataInput;
import java.io.DataOutput;

public interface DataStreamSerializable {
	public void write(DataOutput dout) throws SerializationException;
	public void read(DataInput din) throws SerializationException;
}
