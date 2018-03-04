package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public final class FileHandler {
	private final String path;
	private BufferedWriter writer;
	private boolean status;
	
	public FileHandler(String path) {
		this.path = path;
	}
	
	public void writeValue(final double value, final int nDigits, final boolean print) throws IOException {
		final double roundedValue = nDigits < 0 ? value : Util.round(value, nDigits);
		
		this.write(roundedValue + ",");
		
		if(print) {
			System.out.println(value + " ");
		}
	}
	
	public void writeValue(final double value, final int nDigits) throws IOException {
		this.writeValue(value, nDigits, false);
	}
	
	public void writeValues(final List<Double> values, final int nDigits) throws IOException {
		for(double value: values) {
			this.writeValue(value, nDigits, false);
		}
	}
	
	public void writeValues(final List<Double> values) throws IOException {
		for(double value: values) {
			this.writeValue(value, -1, false);
		}
	}
	
	public void write(final String string) throws IOException {
		if(!status) {
			this.createWriter();
		}
		
		try {
			this.writer.write(string);
		} 
		catch (IOException e) {
			Util.printError("Failed to write to file: " + this.path, e);
			this.close();
			throw e;
		}
	}
	
	private void createWriter() throws IOException {
		try {
			this.writer = new BufferedWriter(new FileWriter(this.path, true));
			this.status = true;
		}
		catch(IOException e) {
			Util.printError("Failed to create writer: " + this.path, e);
			this.close();
			throw e;
		}			
	}
	
	public void newLine() throws IOException {
		try {
			this.writer.newLine();
		}
		catch (IOException e) {
			Util.printError("Failed to write new line: " + this.path, e);
			throw e;
		}
		finally {
			this.close();
		}
	}
	
	private void close() {
		try {
			this.status = false;
			this.writer.close();
		} 
		catch (IOException e) {
			Util.printError("Failed to close reader: " + this.path, e);
			System.exit(-1);
		}
	}
}