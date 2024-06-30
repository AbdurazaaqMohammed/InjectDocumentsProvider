# InjectDocumentsProvider
Access /Android/data and all data files of an Android app without any permissions by patching its APK file.

**I did not discover or create this method. The code allowing it was created by [Lin Jin Bin](https://github.com/L-JINBIN) the developer of MT Manager**.

There are two versions, an Android app and a command line JAR. It is recommended to use the app on Android because to access the files the system Files app must be used, and a shortcut to it is present in the app. This is important not just for convenience but because some device manufacturers like Xiaomi hide the Android system Files app to promote their own file managers.

# Note
Whenever an app is created or modified the APK file must be signed to be able to install it. This app will not sign the APK for you, you need to sign it using any tool like [apk-signer](https://play.google.com/store/apps/details?id=com.haibison.apksigner). The signature will always be different to the original signature of the app so you can't update the app over the existing one, you would have to uninstall and reinstall it probably losing all your data. Also, some apps perform signature verification to check if the APK was modified and won't work if it was.

# Why
Google disabled the ability to access the /Android/data and /Android/obb folders on newer versions of Android. This reduces users' control over their own data and makes it difficult to backup app data or modify files like adding custom songs to a game or installing OBB files for games. Accessing private data of apps can also be useful for reverse engineering and malware analysis.

MT Manager and Apktool M already have this feature but I made this app just so there can be an open source app that can do it.

# How it works
[DocumentsProvider](https://developer.android.com/reference/android/provider/DocumentsProvider) was designed to provide long term access to files including cloud storage. If you have Google Drive on your phone you can see it there. This never really gained popularity due to some device manufacturers like Xiaomi hiding the Android system Files app to promote their own file managers which don't support DocumentsProvider.

![image](https://github.com/AbdurazaaqMohammed/InjectDocumentsProvider/assets/56937889/6c9f2c82-bd12-4ce8-83e8-e08ecad0480a)

Since all private app data of an app can be accessed only by the app itself, a DocumentsProvider can be injected to allow users access to these files. This turns out to be very easy since no actual modification of *existing* code is needed. Instead a single class file can be added to the APK and its AndroidManifest.xml file edited to enable the DocumentsProvider.

The Smali class (and decompiled Java) written by Lin Jin Bin can be seen [here](https://github.com/AbdurazaaqMohammed/InjectDocumentsProvider/tree/main/injector). This project uses the [aXML library](https://github.com/apk-editor/aXML) to edit the AndroidManifest.xml file. 

You could also patch the apk yourself by using [axml2xml](https://github.com/codyi96/xml2axml) or [apktool](https://github.com/iBotPeaches/Apktool) to decompile and recompile AndroidManifest.xml to add the following text at the bottom of the file before the </application> closing tag.

```<provider\nandroid:name="bin.mt.file.content.MTDataFilesProvider"\nandroid:permission="android.permission.MANAGE_DOCUMENTS"\nandroid:exported="true"\nandroid:authorities="com.andatsoft.myapk.fwa.MTDataFilesProvider"\nandroid:grantUriPermissions="true">\n<intent-filter>\n<action\nandroid:name="android.content.action.DOCUMENTS_PROVIDER"/>\n</intent-filter>\n</provider>```

Open the APK as a ZIP archive and add the update manifest. Download the [classes.dex file](https://github.com/AbdurazaaqMohammed/InjectDocumentsProvider/raw/main/app/src/main/assets/a.dex) from here and rename it to "classesN.dex" where N is the count of existing classes.dex + 1. 
(If there are 5 classes.dex files in the APK, rename it to classes6.dex)

After installing the patched APK, launching the system Files app and opening the sidebar, you should see the name of the app you patched.
![image](https://github.com/AbdurazaaqMohammed/InjectDocumentsProvider/assets/56937889/ab61129d-67ba-484c-aae8-686c9afc4646)

<br />
Clicking on it will show the files of that apps' private data directories. 

![image](https://github.com/AbdurazaaqMohammed/InjectDocumentsProvider/assets/56937889/cbb66763-e8b5-4eb8-b298-b31fb30cf6d4)
