package com.apk.axml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

public class Main {
	private static int count = 0;
	private static String looping(boolean fuss) throws IOException, XmlPullParserException {
		count = count + 1;

		String filePath = "/str/ready" + count + ".txt";
        int maxRetries = 100; // Maximum number of retries
        int retryCount = 0;
        final long retryInterval = 2000; // Time between retries in milliseconds
        String argument = null;
        while (retryCount < maxRetries) {
            File file = new File(filePath);
            if (file.exists()) {
            	File d = new File("/str/d" +  count + ".txt");
            	if(d.exists()) {
            		d.delete();
            		argument= "d";
            	} else {
            		File e = new File("/str/e" +  count + ".txt");
            		if(e.exists()) {
            			e.delete();
            			argument= "e";
            		}
            	}
            	file.delete();
                break; // Exit the loop if the file is found
            } else {
                retryCount++;
                try {
                    Thread.sleep(retryInterval); // Wait before retrying
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (retryCount == maxRetries) {
        	//TODO: Restart count when user interact with the page
            System.out.println("File was not found after " + maxRetries + " attempts.");
        }
        System.out.println("Text2");

		return argument;
        
	}
	
	private static boolean first = false;

	public static void main(String[] args) throws IOException, XmlPullParserException {
System.out.println("Text");
		final String encodeOrDecode = looping(first);
		final String input = "/str/fileName.xml";
		final String outputty = "/files/" +  count + "test.xml";
		final FileOutputStream output = new FileOutputStream(outputty);
		
		if(input.endsWith("xml") || input.endsWith("txt")) {
			if (encodeOrDecode.startsWith("e")) {
	        	new aXMLEncoder().encodeFile(new Context(), new FileInputStream(input), output);
	            System.out.print("En");
	        } else if (encodeOrDecode.startsWith("d")) {
	    		new aXMLDecoder().decode(new FileInputStream(input), output);
	            System.out.print("De");
	        } else {
	        	showUsage();
	        }
		} /*else if(arguments.length > 3) {
			if(arguments[3].equals("-am")) {
				final boolean split = input.endsWith(".apks") || input.endsWith(".apkm") || input.endsWith(".xapk") || input.endsWith(".aspk");
				try(ZipFile apk = new ZipFile(input)) {
	        		new aXMLDecoder().decode(split ? getFileInputStreamFromZip(apk.getInputStream(apk.getEntry("base.apk")), "AndroidManifest.xml") : apk.getInputStream(apk.getEntry("AndroidManifest.xml")), output);
	    		}
			}
			else {
				showUsage();
			}
		} else {
			final boolean split = input.endsWith(".apks") || input.endsWith(".apkm") || input.endsWith(".xapk") || input.endsWith(".aspk");
			printXmlFilesFromZip(new ZipInputStream(new BufferedInputStream(split ? getFileInputStreamFromZipFile(input, "base.apk") : new FileInputStream(input))));

        	System.out.print("Enter the file you want to decode from the list above: ");
        	Scanner scanner = new Scanner(System.in);
        	String fileName = scanner.nextLine();
        	scanner.close();
    		try(ZipFile apk = new ZipFile(input)){
        		new aXMLDecoder().decode(split ? getFileInputStreamFromZip(apk.getInputStream(apk.getEntry("base.apk")), fileName) : apk.getInputStream(apk.getEntry(fileName)), output);
    		}
		}*/
		System.out.println("coded to " + (outputty.contains(File.separator) ? outputty : new File(outputty).getAbsolutePath()));
		first = true;
    	main(null);
	}
	
	private static void showUsage() {
		System.out.println("Usage:");
		System.out.println("java -jar aXML.jar d[ecode] input_file output_file");
		System.out.println("java -jar aXML.jar e[ncode] input_file output_file");
        System.out.println("Optional argument: -am (Decode AndroidManifest.xml from APK file immediately instead of listing all XML files)");
        System.exit(1);
    }
	
	private static InputStream getFileInputStreamFromZipFile(String zipFile, String filename) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile)) {
            ZipEntry manifestEntry = zip.getEntry(filename);
            if (manifestEntry != null) return zip.getInputStream(manifestEntry);

            // Check for base.apk in case of xapk files
            else try (InputStream baseApkStream = zip.getInputStream(zip.getEntry("base.apk"))) {
                return getFileInputStreamFromZip(baseApkStream, "AndroidManifest.xml");
            }
        }
    }
	
	private static InputStream getFileInputStreamFromZip(InputStream zipInputStream, String filename) throws IOException {
        ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(zipInputStream));
        ZipEntry entry;
        while ((entry = zipInput.getNextEntry()) != null) {
            if (entry.getName().equals(filename)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = zipInput.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                return new ByteArrayInputStream(outputStream.toByteArray());
            } //no need in this case since only called when this is base.apk else if (entry.getName().equals("base.apk")) return getAndroidManifestInputStreamFromZip(zipInput); // Somehow this works on xapk files even though base.apk is renamed to the app package name
            zipInput.closeEntry();
        }

        // AndroidManifest.xml not found
        return null;
    }
	
	public static List<String> getListOfXmlFilesFromZip(ZipInputStream zipInputStream) {
        List<String> xmlFiles = new ArrayList<>();
        
        try {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final String entryName = entry.getName();
                if (entryName.endsWith(".xml")) {
                    xmlFiles.add(entryName);
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return xmlFiles;
    }
    
    public static void printXmlFilesFromZip(ZipInputStream zipInputStream) {        
        try {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final String entryName = entry.getName();
                if (entryName.endsWith(".xml")) {
                    System.out.println(entryName);
                }
                zipInputStream.closeEntry();
        	}
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
