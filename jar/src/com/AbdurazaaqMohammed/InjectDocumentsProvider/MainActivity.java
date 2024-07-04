package com.AbdurazaaqMohammed.InjectDocumentsProvider;


import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MainActivity {
    
	 private static InputStream getAndroidManifestInputStreamFromZip(InputStream zipInputStream) throws IOException {
        ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(zipInputStream));
        ZipEntry entry;
        while ((entry = zipInput.getNextEntry()) != null) {
            if (entry.getName().equals("AndroidManifest.xml")) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = zipInput.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return new ByteArrayInputStream(outputStream.toByteArray());
        } else if (entry.getName().equals("base.apk")) return getAndroidManifestInputStreamFromZip(zipInput); // Somehow this works on xapk files even though base.apk is renamed to the app package name
        zipInput.closeEntry();
    }

    // AndroidManifest.xml not found in the zip file
    return null;
}

public static void main(String[] args) throws IOException, XmlPullParserException {
    
	if(args.length != 1) {
		System.err.println("Usage: java -jar InjectDocumentsProvider.jar path_to_apk");
		System.exit(1);
	}
	
	try {
        InputStream toPatchFileStream;
        FileOutputStream outputFileStream;
        String fileToPatch = args[0];
        String am;
            toPatchFileStream = new FileInputStream(fileToPatch);
            outputFileStream = new FileOutputStream(fileToPatch.replace(".apk", "_documents-provider.apk"));
            System.out.println("Patching AndroidManifest.xml...");
            
            am = new com.apk.axml.aXMLDecoder().decode(getAndroidManifestInputStreamFromZip(new FileInputStream(fileToPatch)));


            BufferedReader reader = new BufferedReader(new StringReader(am));
            String line;
            String packageName = "com.andatsoft.myapk.fwa";
            try {
                while ((line = reader.readLine()) != null) {
                    if(line.contains("package=\"")) {
                        packageName = line.split("\"")[1];
                        break;
                    }
                }
            } catch (IOException e) {
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        final byte[] encodedData = new com.apk.axml.aXMLEncoder().encodeString(new Context(), am.replace("</application>", "<provider\nandroid:name=\"bin.mt.file.content.MTDataFilesProvider\"\nandroid:permission=\"android.permission.MANAGE_DOCUMENTS\"\nandroid:exported=\"true\"\nandroid:authorities=\"" + packageName + ".MTDataFilesProvider\"\nandroid:grantUriPermissions=\"true\">\n<intent-filter>\n<action\nandroid:name=\"android.content.action.DOCUMENTS_PROVIDER\"/>\n</intent-filter>\n</provider>\n</application>"));
        try (ZipInputStream zis = new ZipInputStream(toPatchFileStream);
             ZipOutputStream zos = new ZipOutputStream(outputFileStream)) {
            // This doesn't work on split APKs and no point making it work till it can sign as all apks in the split APK need to be signed
            int classesCount = 0;
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                final String filename = entry.getName();
                ZipEntry newEntry = new ZipEntry(filename);
                if(filename.startsWith("res/") && !filename.contains(".xml")) {
                    zos.setMethod(ZipOutputStream.STORED);
                    newEntry.setSize(entry.getSize());
                    newEntry.setCrc(entry.getCrc());
                } else {
                    if (filename.startsWith("classes") /*ensure it dont count audience_network.dex or anything. Filename include the file path so it should not count even if theres "classes.dex" in assets for some reason*/
                            && filename.endsWith(".dex")) classesCount++;
                    zos.setMethod(ZipOutputStream.DEFLATED);
                }
                zos.putNextEntry(newEntry);

                if ("AndroidManifest.xml".equals(filename)) {
                    zos.write(encodedData, 0, encodedData.length);
                } else {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
                zis.closeEntry();

            }
            final String classesName = "classes" + (classesCount + 1) + ".dex";
            System.out.println("Adding " + classesName);
            zos.putNextEntry(new ZipEntry(classesName));
            try (InputStream is = MainActivity.class.getResourceAsStream("/a.dex")) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        zos.write(buffer, 0, bytesRead);
                    }
                }
                zos.closeEntry();
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
		System.out.println("Saved successfully");
    }
 
}