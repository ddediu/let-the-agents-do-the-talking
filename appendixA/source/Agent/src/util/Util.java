package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import agent.AbstractSolution;

public final class Util {
	private Util() {
	}
	
	public static class GenerationComparator implements Comparator<File>{
		@Override
		public int compare(final File file1, final File file2) {
			return this.getIndex(file1) - this.getIndex(file2);
		}
		
		private int getIndex(final File file) {
			final String name = file.getName();
			return Integer.parseInt(name.substring(0, name.indexOf("_")));
		}
	}
	
	public static List<File> filterExtension(final File[] files, final String targetSubstring,
			final boolean end) {
		
		return Util.filterExtension(files, targetSubstring, end, null);
	}
	
	public static List<File> filterExtension(final File[] files, final String targetSubstring,
			final boolean end, final List<File> remainingFiles) {
		
		final List<File> filteredFiles = new ArrayList<File>();
		final int extLength = targetSubstring.length();
		
		for(final File snapshotFile: files) {
			final String filename = snapshotFile.getName();
			
			final String extension;
			if(end) {
				extension = filename.substring(filename.length() - extLength , filename.length());
			}
			else {
				extension = filename.substring(0, extLength);
			}
			
			if (extension.equals(targetSubstring)) {
				filteredFiles.add(snapshotFile);
			}
			else {
				if(remainingFiles != null) {
					remainingFiles.add(snapshotFile);
				}
			}
		}
		
		return filteredFiles;
	}
	
	public static <T> List<List<T>> zip(final List<List<T>> unzippedList) {
		final List<List<T>> zippedList = new LinkedList<List<T>>();
		
		for(final List<T> sublist: unzippedList) {
			for(int i = 0; i < sublist.size(); i++) {
				if(i == zippedList.size()) {
					zippedList.add(new LinkedList<T>());
				}
				zippedList.get(i).add(sublist.get(i));
			}
		}
		
		return zippedList;
	}
	
	public static double sum(final List<? extends Number> values) {
		double sum = 0;
		
		for(final Number value: values) {
			sum += value.doubleValue();
		}
		
		return sum;
	}
	
	public static double mean(final List<? extends Number> values) {
		return Util.sum(values) / values.size();
	}
	
	public static double sd(final List<? extends Number> values) {
		final double mean = Util.mean(values);
		
		double SSE = 0;
		
		for(final Number value: values) {
			SSE += Math.pow(mean - value.doubleValue(), 2);
		}
		
		final double variance = SSE / values.size();
		final double sd = Math.sqrt(variance);
		
		return sd;
	}
	
	public static double round(final double value, final int nDigits) {
		final double exponent = Math.pow(10, nDigits);
		return Math.round(value * exponent) / exponent;
	}
	
	public static List<Double> stringsToDoubles(final List<String> input) {
		final List<Double> output = new LinkedList<Double>();
		
		for(final String value: input) {
			output.add(Double.valueOf(value));
		}
		
		return output;
	}
	
	public static double[] listToArray(List<Double> list) {
		final double[] array = new double[list.size()];
		
		for(int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}
		
		return array;
	}
	
	public static List<Double> arrayToList(final double[] array) {
		final List<Double> list = new ArrayList<Double>(array.length);
		
		for(final double value: array) {
			list.add(value);
		}
		
		return list;
	}
	
	public static List<List<String>> readCsv(final String filename) {		
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
			final List<List<String>> data = new LinkedList<List<String>>();
			
			String line;
			while((line = bufferedReader.readLine()) != null) {
				String[] lineValues = line.split(",");
				data.add(new LinkedList<String>(Arrays.asList(lineValues)));
			}
			
			System.out.println("read from " + filename);
			
			return data;
			
		} 
		catch (IOException e) {
			Util.printError("Failed to read csv: " + filename, e);
			System.exit(-1);
			return null;
		}
	}
	
	public static int countLines(final String filename) {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
			int counter = 0;
			
			while((bufferedReader.readLine()) != null) {
				counter++;
			}
			
			return counter;
		} 
		catch (final IOException e) {
			Util.printError("Failed to open file: " + filename, e);
			System.exit(-1);
			return -1;
		}
	}
	
	public static <T extends AbstractSolution> void writePopulation(
			final List<T> population, final String filename) throws IOException {
		Util.writeObject(population, filename);
	}
	
	private static void writeObject(final Object solution, final String filename) throws IOException {
		final String filePath = filename + ".ser";
		
		try (ObjectOutputStream objectWriter = new ObjectOutputStream(new BufferedOutputStream(
				new FileOutputStream(filePath)))) {
			
			objectWriter.writeObject(solution);
			
			System.out.println("written to " + filePath);
		}
		catch(final IOException e) {
			Util.printError("Failed to write serialized: " + filePath, e);
			throw e;
		}
	}
	
	public static List<AbstractSolution> readPopulation(final String filename) {
		return Util.readObject(filename);
	}
	
	private static <T> T readObject(final String filePath) {
		try (ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(filePath)))){
			@SuppressWarnings("unchecked")
			final T returnValue = (T) objectInputStream.readObject();
			
			System.out.println("read from " + filePath);
			return returnValue;
		} 
		catch (final IOException | ClassNotFoundException e) {
			Util.printError("Failed to read serialized: " + filePath, e);
			System.exit(-1);
			return null;
		}		
	}
	
	public static void copyFile(final String source, final String dest) throws IOException {		
		Path sourcePath = Paths.get(source);
		Path destPath = Paths.get(dest);
		
		try {
			Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
		} 
		catch (final IOException e) {
			Util.printError("Failed to copy file: " + source + " >> " + dest, e);
			throw e;
		}
	}
	
	public static void createDir(final String dirname) {
		final Path path = Paths.get(dirname);
		
		try {
			if(!Files.exists(path)) {
				Files.createDirectory(path);
			}
		} 
		catch (IOException e) {
			Util.printError("Failed to create dir: " + dirname, e);
			System.exit(-1);
		}
	}
	
	public static void deleteIfExists(final String filename) {
		if(Files.exists(Paths.get(filename))) {
			Util.delete(filename);
		}
	}
	
	public static void delete(final String filename) {		
		final Path path = Paths.get(filename);
		
		try {
			Files.delete(path);
			System.out.println("deleted " + filename);
		}
		catch (IOException e) {
			Util.printError("FAILED TO DELETE FILE: " + filename, e);
			System.exit(-1);
		}
	}
	
	public static void printError(final String error, final Exception e) {
		System.out.println(error);
		System.err.println(error);
		e.printStackTrace();
	}
	
	
	public static boolean isNumber(final String string) {
		for(char c: string.toCharArray()) {
			if(!Character.isDigit(c)) {
				return false;
			}
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> deepCopyList(List<T> list) {
		final List<T> copy = new ArrayList<T>(list.size());
		
		for(final T item: list) {
			if(item instanceof List) {
				copy.add((T) Util.deepCopyList((List<T>) item));
			}
			else {
				copy.add(item);
			}
		}
		
		return copy;
	}
}
