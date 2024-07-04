package com.AbdurazaaqMohammed.InjectDocumentsProvider;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_SELECT_FILE = 1;
    private static final int REQUEST_CODE_SAVE_FILE = 2;
    private static Uri uriOfFileToBePatched;
    private final static boolean supportsInbuiltAndroidFilePicker = Build.VERSION.SDK_INT > 18;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                getActionBar().hide();
            } catch (NullPointerException ignored) {

            }
        }
        setContentView(R.layout.activity_main);
        findViewById(R.id.pickOriginalFile).setOnClickListener(view -> openFilePickerToSelectFile());
        findViewById(R.id.launchSystemFiles).setOnClickListener(view -> openSystemFilesActivity());
        TextView textView = findViewById(R.id.launchInfo);
        textView.setText(Html.fromHtml(getString(R.string.launch_info)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        // Check if user shared files with the app
        final Intent fromShareOrView = getIntent();
        final String fromShareOrViewAction = fromShareOrView.getAction();
        if (Intent.ACTION_SEND.equals(fromShareOrViewAction)) {
            uriOfFileToBePatched = fromShareOrView.getParcelableExtra(Intent.EXTRA_STREAM);
        } else if (Intent.ACTION_VIEW.equals(fromShareOrViewAction)) {
            uriOfFileToBePatched = fromShareOrView.getData();
        }
        if (uriOfFileToBePatched != null) {
            openFilePickerToSaveFile();
        }
    }

    private void openFilePickerToSelectFile() {
        if (supportsInbuiltAndroidFilePicker) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"application/vnd.android.package-archive", "application/zip", "application/octet-stream"});

            startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
        } else {
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.root = Environment.getExternalStorageDirectory();
            properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
            properties.offset = new File(DialogConfigs.DEFAULT_DIR);
            properties.extensions = new String[] {"apk", "zip"};
            FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties, android.R.style.Theme_Black);
            dialog.setTitle(getString(R.string.pick_file));
            dialog.setDialogSelectionListener(files -> {
                PatchFileAsyncTask.fileToPatch = files[0];
                new PatchFileAsyncTask(this).execute();
                dialog.dismiss();
            });
            dialog.show();
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void openFilePickerToSaveFile() {
        Intent saveFileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        saveFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        saveFileIntent.setType("application/vnd.android.package-archive");
        saveFileIntent.putExtra(Intent.EXTRA_TITLE, getOriginalFileName(this, uriOfFileToBePatched).replace(".apk", "_documents-provider.apk"));
        startActivityForResult(saveFileIntent, REQUEST_CODE_SAVE_FILE);
    }

    public void openSystemFilesActivity() {
        Intent intent = new Intent();

        final String newFilesPackageName = "com.google.android.documentsui";
        final String oldFilesPackageName = "com.android.documentsui";
        final String one = "com.android.documentsui.files.FilesActivity";
        final String two = "com.android.documentsui.LauncherActivity";
        final String[][] possibleActivities = {
                {newFilesPackageName, one},
                {oldFilesPackageName, one},
                {newFilesPackageName, two},
                {oldFilesPackageName, two}
        };

        boolean activityStarted = false;

        for (String[] activity : possibleActivities) {
            intent.setClassName(activity[0], activity[1]);
            try {
                startActivity(intent);
                activityStarted = true;
                break; // Exit the loop if the activity starts successfully
            } catch (ActivityNotFoundException ignored) {
                // Ignore and try the next activity in the list
            }
        }

        if (!activityStarted) {
            showError("System Files app not found");
        }
    }

    private void showError(Exception e) {
        final String mainErr = e.toString();
        StringBuilder stackTrace = new StringBuilder().append(mainErr);
        for(StackTraceElement line : e.getStackTrace()) {
            stackTrace.append(line);
        }
        runOnUiThread(() -> {
            TextView errorBox = findViewById(R.id.errorField);
            errorBox.setVisibility(View.VISIBLE);
            errorBox.setText(stackTrace);
            Toast.makeText(this, mainErr, Toast.LENGTH_SHORT).show();
        });
    }
    private void showError(String error) {
        runOnUiThread(() -> {
            TextView errorBox = findViewById(R.id.errorField);
            errorBox.setVisibility(View.VISIBLE);
            errorBox.setText(error);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private String getOriginalFileName(Context context, Uri uri) {
        String result = null;
        try {
            if (uri.getScheme().equals("content")) {
                try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        } catch (NullPointerException e) {
            result = "could_not_find_filename.apk";
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                switch (requestCode) {
                    case REQUEST_CODE_SELECT_FILE:
                        uriOfFileToBePatched = uri;
                        openFilePickerToSaveFile();
                    break;
                    case REQUEST_CODE_SAVE_FILE:
                        new PatchFileAsyncTask(this).execute(uriOfFileToBePatched, uri);
                    break;
                }
            }
        }
    }

    static class PatchFileAsyncTask extends AsyncTask<Uri, Void, Void> {
        private static WeakReference<MainActivity> activityReference;
        // only retain a weak reference to the activity
        public PatchFileAsyncTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        private InputStream getAndroidManifestInputStreamFromZip(InputStream zipInputStream) throws IOException {
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

        static String fileToPatch;

        @Override
        protected Void doInBackground(Uri... uris) {
            MainActivity activity = activityReference.get();
            activity.runOnUiThread(() -> activity.findViewById(R.id.errorField).setVisibility(View.INVISIBLE));

            try {
                InputStream toPatchFileStream;
                FileOutputStream outputFileStream;
                String am;
                if(supportsInbuiltAndroidFilePicker) {
                    final Uri uriOfFileToPatch = uris[0];
                    final Uri outputUri = uris[1];
                    toPatchFileStream = activity.getContentResolver().openInputStream(uriOfFileToPatch);
                    outputFileStream = (FileOutputStream) activity.getContentResolver().openOutputStream(outputUri);
                    am = new com.apk.axml.aXMLDecoder().decode(getAndroidManifestInputStreamFromZip(activity.getContentResolver().openInputStream(uriOfFileToPatch)));
                } else {
                    toPatchFileStream = new FileInputStream(fileToPatch);
                    outputFileStream = new FileOutputStream(fileToPatch.replace(".apk", "_documents-provider.apk"));
                    am = new com.apk.axml.aXMLDecoder().decode(getAndroidManifestInputStreamFromZip(new FileInputStream(fileToPatch)));
                }
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
                    activity.showError(e);
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        activity.showError(e);
                    }
                }
                final byte[] encodedData = new com.apk.axml.aXMLEncoder().encodeString(activity, am.replace("</application>", "<provider\nandroid:name=\"bin.mt.file.content.MTDataFilesProvider\"\nandroid:permission=\"android.permission.MANAGE_DOCUMENTS\"\nandroid:exported=\"true\"\nandroid:authorities=\"" + packageName + ".MTDataFilesProvider\"\nandroid:grantUriPermissions=\"true\">\n<intent-filter>\n<action\nandroid:name=\"android.content.action.DOCUMENTS_PROVIDER\"/>\n</intent-filter>\n</provider>\n</application>"));
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
                    zos.putNextEntry(new ZipEntry("classes" + (classesCount + 1) + ".dex"));
                    try (InputStream is = activity.getAssets().open("a.dex")) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    }
                    zos.closeEntry();
                }
            } catch (IOException | XmlPullParserException e) {
                activity.showError(e);
            } finally {
                activity.runOnUiThread(() ->  Toast.makeText(activity, activity.getString(R.string.success), Toast.LENGTH_SHORT).show());
            }
            return null;
        }
    }
}
