package bin.mt.file.content;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsProvider;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class MTDataFilesProvider extends DocumentsProvider {
    public static final String[] g = {"root_id", "mime_types", "flags", "icon", "title", "summary", "document_id"};
    public static final String[] h = {"document_id", "mime_type", "_display_name", "last_modified", "flags", "_size", "mt_extras"};
    public String b;
    public File c;
    public File d;
    public File e;
    public File f;

    /* JADX WARN: Removed duplicated region for block: B:17:0x002f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean a(File file) {
        boolean z;
        File[] listFiles;
        if (file.isDirectory()) {
            try {
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
            if ((Os.lstat(file.getPath()).st_mode & 61440) == 40960) {
                z = true;
                if (!z && (listFiles = file.listFiles()) != null) {
                    for (File file2 : listFiles) {
                        if (!a(file2)) {
                            return false;
                        }
                    }
                }
            }
            z = false;
            if (!z) {
                while (r3 < r2) {
                }
            }
        }
        return file.delete();
    }

    public static String c(File file) {
        if (file.isDirectory()) {
            return "vnd.android.document/directory";
        }
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(46);
        if (lastIndexOf >= 0) {
            String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substring(lastIndexOf + 1).toLowerCase());
            return mimeTypeFromExtension != null ? mimeTypeFromExtension : "application/octet-stream";
        }
        return "application/octet-stream";
    }

    @Override // android.provider.DocumentsProvider, android.content.ContentProvider
    public final void attachInfo(Context context, ProviderInfo providerInfo) {
        super.attachInfo(context, providerInfo);
        this.b = context.getPackageName();
        File parentFile = context.getFilesDir().getParentFile();
        this.c = parentFile;
        String path = parentFile.getPath();
        if (path.startsWith("/data/user/")) {
            this.d = new File("/data/user_de/" + path.substring(11));
        }
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            this.e = externalFilesDir.getParentFile();
        }
        this.f = context.getObbDir();
    }

    public final File b(String str, boolean z) {
        String substring;
        if (str.startsWith(this.b)) {
            String substring2 = str.substring(this.b.length());
            if (substring2.startsWith("/")) {
                substring2 = substring2.substring(1);
            }
            File file = null;
            if (substring2.isEmpty()) {
                return null;
            }
            int indexOf = substring2.indexOf(47);
            if (indexOf == -1) {
                substring = "";
            } else {
                String substring3 = substring2.substring(0, indexOf);
                substring = substring2.substring(indexOf + 1);
                substring2 = substring3;
            }
            if (substring2.equalsIgnoreCase("data")) {
                file = new File(this.c, substring);
            } else if (substring2.equalsIgnoreCase("android_data") && this.e != null) {
                file = new File(this.e, substring);
            } else if (substring2.equalsIgnoreCase("android_obb") && this.f != null) {
                file = new File(this.f, substring);
            } else if (substring2.equalsIgnoreCase("user_de_data") && this.d != null) {
                file = new File(this.d, substring);
            }
            if (file != null) {
                if (z) {
                    try {
                        Os.lstat(file.getPath());
                    } catch (Exception unused) {
                        throw new FileNotFoundException(str.concat(" not found"));
                    }
                }
                return file;
            }
            throw new FileNotFoundException(str.concat(" not found"));
        }
        throw new FileNotFoundException(str.concat(" not found"));
    }

    /* JADX WARN: Removed duplicated region for block: B:33:0x0074  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x0074  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x0074  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x0074  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x0074  */
    /* JADX WARN: Removed duplicated region for block: B:53:0x00c8 A[Catch: Exception -> 0x00dd, TryCatch #1 {Exception -> 0x00dd, blocks: (B:10:0x001d, B:12:0x0031, B:13:0x0036, B:15:0x003e, B:35:0x0078, B:36:0x007f, B:37:0x0083, B:39:0x0089, B:40:0x008d, B:41:0x0093, B:44:0x009f, B:45:0x00a7, B:48:0x00ae, B:49:0x00b4, B:52:0x00c0, B:53:0x00c8, B:56:0x00cf, B:22:0x0053, B:25:0x005d, B:28:0x0067, B:14:0x0039), top: B:63:0x001d, inners: #0, #2 }] */
    /* JADX WARN: Removed duplicated region for block: B:53:0x00c8 A[Catch: Exception -> 0x00dd, TryCatch #1 {Exception -> 0x00dd, blocks: (B:10:0x001d, B:12:0x0031, B:13:0x0036, B:15:0x003e, B:35:0x0078, B:36:0x007f, B:37:0x0083, B:39:0x0089, B:40:0x008d, B:41:0x0093, B:44:0x009f, B:45:0x00a7, B:48:0x00ae, B:49:0x00b4, B:52:0x00c0, B:53:0x00c8, B:56:0x00cf, B:22:0x0053, B:25:0x005d, B:28:0x0067, B:14:0x0039), top: B:63:0x001d, inners: #0, #2 }] */
    /* JADX WARN: Removed duplicated region for block: B:53:0x00c8 A[Catch: Exception -> 0x00dd, TryCatch #1 {Exception -> 0x00dd, blocks: (B:10:0x001d, B:12:0x0031, B:13:0x0036, B:15:0x003e, B:35:0x0078, B:36:0x007f, B:37:0x0083, B:39:0x0089, B:40:0x008d, B:41:0x0093, B:44:0x009f, B:45:0x00a7, B:48:0x00ae, B:49:0x00b4, B:52:0x00c0, B:53:0x00c8, B:56:0x00cf, B:22:0x0053, B:25:0x005d, B:28:0x0067, B:14:0x0039), top: B:63:0x001d, inners: #0, #2 }] */
    /* JADX WARN: Removed duplicated region for block: B:53:0x00c8 A[Catch: Exception -> 0x00dd, TryCatch #1 {Exception -> 0x00dd, blocks: (B:10:0x001d, B:12:0x0031, B:13:0x0036, B:15:0x003e, B:35:0x0078, B:36:0x007f, B:37:0x0083, B:39:0x0089, B:40:0x008d, B:41:0x0093, B:44:0x009f, B:45:0x00a7, B:48:0x00ae, B:49:0x00b4, B:52:0x00c0, B:53:0x00c8, B:56:0x00cf, B:22:0x0053, B:25:0x005d, B:28:0x0067, B:14:0x0039), top: B:63:0x001d, inners: #0, #2 }] */
    /* JADX WARN: Removed duplicated region for block: B:53:0x00c8 A[Catch: Exception -> 0x00dd, TryCatch #1 {Exception -> 0x00dd, blocks: (B:10:0x001d, B:12:0x0031, B:13:0x0036, B:15:0x003e, B:35:0x0078, B:36:0x007f, B:37:0x0083, B:39:0x0089, B:40:0x008d, B:41:0x0093, B:44:0x009f, B:45:0x00a7, B:48:0x00ae, B:49:0x00b4, B:52:0x00c0, B:53:0x00c8, B:56:0x00cf, B:22:0x0053, B:25:0x005d, B:28:0x0067, B:14:0x0039), top: B:63:0x001d, inners: #0, #2 }] */
    @Override // android.provider.DocumentsProvider, android.content.ContentProvider
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Bundle call(String str, String str2, Bundle bundle) {
        String str3;
        int hashCode;
        char c;
        String message;
        Bundle call = super.call(str, str2, bundle);
        if (call != null) {
            return call;
        }
        if (str.startsWith("mt:")) {
            Bundle bundle2 = new Bundle();
            try {
                List<String> pathSegments = ((Uri) bundle.getParcelable("uri")).getPathSegments();
                str3 = pathSegments.size() >= 4 ? pathSegments.get(3) : pathSegments.get(1);
                hashCode = str.hashCode();
            } catch (Exception e) {
                bundle2.putBoolean("result", false);
                bundle2.putString("message", e.toString());
            }
            if (hashCode == -1645162251) {
                if (str.equals("mt:setPermissions")) {
                    c = 1;
                    if (c != 0) {
                    }
                    bundle2.putBoolean("result", false);
                    return bundle2;
                }
                c = 65535;
                if (c != 0) {
                }
                bundle2.putBoolean("result", false);
                return bundle2;
            } else if (hashCode == 214442514) {
                if (str.equals("mt:createSymlink")) {
                    c = 2;
                    if (c != 0) {
                    }
                    bundle2.putBoolean("result", false);
                    return bundle2;
                }
                c = 65535;
                if (c != 0) {
                }
                bundle2.putBoolean("result", false);
                return bundle2;
            } else {
                if (hashCode == 1713485102 && str.equals("mt:setLastModified")) {
                    c = 0;
                    if (c != 0) {
                        File b = b(str3, true);
                        if (b != null) {
                            bundle2.putBoolean("result", b.setLastModified(bundle.getLong("time")));
                            return bundle2;
                        }
                    } else if (c != 1) {
                        if (c != 2) {
                            bundle2.putBoolean("result", false);
                            message = "Unsupported method: ".concat(str);
                        } else {
                            File b2 = b(str3, false);
                            if (b2 != null) {
                                try {
                                    Os.symlink(bundle.getString("path"), b2.getPath());
                                    bundle2.putBoolean("result", true);
                                } catch (ErrnoException e2) {
                                    bundle2.putBoolean("result", false);
                                    message = e2.getMessage();
                                }
                                return bundle2;
                            }
                        }
                        bundle2.putString("message", message);
                        return bundle2;
                    } else {
                        File b3 = b(str3, true);
                        if (b3 != null) {
                            try {
                                Os.chmod(b3.getPath(), bundle.getInt("permissions"));
                                bundle2.putBoolean("result", true);
                            } catch (ErrnoException e3) {
                                bundle2.putBoolean("result", false);
                                message = e3.getMessage();
                            }
                            return bundle2;
                        }
                    }
                    bundle2.putBoolean("result", false);
                    return bundle2;
                }
                c = 65535;
                if (c != 0) {
                }
                bundle2.putBoolean("result", false);
                return bundle2;
            }
            bundle2.putBoolean("result", false);
            bundle2.putString("message", e.toString());
            return bundle2;
        }
        return null;
    }

    @Override // android.provider.DocumentsProvider
    public final String createDocument(String str, String str2, String str3) {
        StringBuilder sb;
        File b = b(str, true);
        if (b != null) {
            File file = new File(b, str3);
            int i = 2;
            while (file.exists()) {
                file = new File(b, str3 + " (" + i + ")");
                i++;
            }
            try {
                if ("vnd.android.document/directory".equals(str2) ? file.mkdir() : file.createNewFile()) {
                    if (str.endsWith("/")) {
                        sb = new StringBuilder();
                        sb.append(str);
                        sb.append(file.getName());
                    } else {
                        sb = new StringBuilder();
                        sb.append(str);
                        sb.append("/");
                        sb.append(file.getName());
                    }
                    str = sb.toString();
                    return str;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new FileNotFoundException("Failed to create document in " + str + " with name " + str3);
    }

    public final void d(MatrixCursor matrixCursor, String str, File file) {
        int i;
        String name;
        if (file == null) {
            file = b(str, true);
        }
        boolean z = false;
        if (file == null) {
            MatrixCursor.RowBuilder newRow = matrixCursor.newRow();
            newRow.add("document_id", this.b);
            newRow.add("_display_name", this.b);
            newRow.add("_size", 0L);
            newRow.add("mime_type", "vnd.android.document/directory");
            newRow.add("last_modified", 0);
            newRow.add("flags", 0);
            return;
        }
        if (file.isDirectory()) {
            if (file.canWrite()) {
                i = 8;
            }
            i = 0;
        } else {
            if (file.canWrite()) {
                i = 2;
            }
            i = 0;
        }
        if (file.getParentFile().canWrite()) {
            i = i | 4 | 64;
        }
        String path = file.getPath();
        if (path.equals(this.c.getPath())) {
            name = "data";
        } else {
            File file2 = this.e;
            if (file2 == null || !path.equals(file2.getPath())) {
                File file3 = this.f;
                if (file3 == null || !path.equals(file3.getPath())) {
                    File file4 = this.d;
                    if (file4 == null || !path.equals(file4.getPath())) {
                        name = file.getName();
                        z = true;
                    } else {
                        name = "user_de_data";
                    }
                } else {
                    name = "android_obb";
                }
            } else {
                name = "android_data";
            }
        }
        MatrixCursor.RowBuilder newRow2 = matrixCursor.newRow();
        newRow2.add("document_id", str);
        newRow2.add("_display_name", name);
        newRow2.add("_size", Long.valueOf(file.length()));
        newRow2.add("mime_type", c(file));
        newRow2.add("last_modified", Long.valueOf(file.lastModified()));
        newRow2.add("flags", Integer.valueOf(i));
        newRow2.add("mt_path", file.getAbsolutePath());
        if (z) {
            try {
                StringBuilder sb = new StringBuilder();
                StructStat lstat = Os.lstat(path);
                sb.append(lstat.st_mode);
                sb.append("|");
                sb.append(lstat.st_uid);
                sb.append("|");
                sb.append(lstat.st_gid);
                if ((lstat.st_mode & 61440) == 40960) {
                    sb.append("|");
                    sb.append(Os.readlink(path));
                }
                newRow2.add("mt_extras", sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override // android.provider.DocumentsProvider
    public final void deleteDocument(String str) {
        File b = b(str, true);
        if (b == null || !a(b)) {
            throw new FileNotFoundException("Failed to delete document ".concat(str));
        }
    }

    @Override // android.provider.DocumentsProvider
    public final String getDocumentType(String str) {
        File b = b(str, true);
        return b == null ? "vnd.android.document/directory" : c(b);
    }

    @Override // android.provider.DocumentsProvider
    public final boolean isChildDocument(String str, String str2) {
        return str2.startsWith(str);
    }

    @Override // android.provider.DocumentsProvider
    public final String moveDocument(String str, String str2, String str3) {
        File b = b(str, true);
        File b2 = b(str3, true);
        if (b != null && b2 != null) {
            File file = new File(b2, b.getName());
            if (!file.exists() && b.renameTo(file)) {
                if (str3.endsWith("/")) {
                    return str3 + file.getName();
                }
                return str3 + "/" + file.getName();
            }
        }
        throw new FileNotFoundException("Filed to move document " + str + " to " + str3);
    }

    @Override // android.content.ContentProvider
    public final boolean onCreate() {
        return true;
    }

    @Override // android.provider.DocumentsProvider
    public final ParcelFileDescriptor openDocument(String str, String str2, CancellationSignal cancellationSignal) {
        File b = b(str, false);
        if (b != null) {
            return ParcelFileDescriptor.open(b, ParcelFileDescriptor.parseMode(str2));
        }
        throw new FileNotFoundException(str.concat(" not found"));
    }

    @Override // android.provider.DocumentsProvider
    public final Cursor queryChildDocuments(String str, String[] strArr, String str2) {
        if (str.endsWith("/")) {
            str = str.substring(0, str.length() - 1);
        }
        if (strArr == null) {
            strArr = h;
        }
        MatrixCursor matrixCursor = new MatrixCursor(strArr);
        File b = b(str, true);
        if (b == null) {
            d(matrixCursor, str.concat("/data"), this.c);
            File file = this.e;
            if (file != null && file.exists()) {
                d(matrixCursor, str.concat("/android_data"), this.e);
            }
            File file2 = this.f;
            if (file2 != null && file2.exists()) {
                d(matrixCursor, str.concat("/android_obb"), this.f);
            }
            File file3 = this.d;
            if (file3 != null && file3.exists()) {
                d(matrixCursor, str.concat("/user_de_data"), this.d);
            }
        } else {
            File[] listFiles = b.listFiles();
            if (listFiles != null) {
                for (File file4 : listFiles) {
                    d(matrixCursor, str + "/" + file4.getName(), file4);
                }
            }
        }
        return matrixCursor;
    }

    @Override // android.provider.DocumentsProvider
    public final Cursor queryDocument(String str, String[] strArr) {
        if (strArr == null) {
            strArr = h;
        }
        MatrixCursor matrixCursor = new MatrixCursor(strArr);
        d(matrixCursor, str, null);
        return matrixCursor;
    }

    @Override // android.provider.DocumentsProvider
    public final Cursor queryRoots(String[] strArr) {
        ApplicationInfo applicationInfo = getContext().getApplicationInfo();
        String charSequence = applicationInfo.loadLabel(getContext().getPackageManager()).toString();
        if (strArr == null) {
            strArr = g;
        }
        MatrixCursor matrixCursor = new MatrixCursor(strArr);
        MatrixCursor.RowBuilder newRow = matrixCursor.newRow();
        newRow.add("root_id", this.b);
        newRow.add("document_id", this.b);
        newRow.add("summary", this.b);
        newRow.add("flags", 17);
        newRow.add("title", charSequence);
        newRow.add("mime_types", "*/*");
        newRow.add("icon", Integer.valueOf(applicationInfo.icon));
        return matrixCursor;
    }

    @Override // android.provider.DocumentsProvider
    public final void removeDocument(String str, String str2) {
        deleteDocument(str);
    }

    @Override // android.provider.DocumentsProvider
    public final String renameDocument(String str, String str2) {
        File b = b(str, true);
        if (b == null || !b.renameTo(new File(b.getParentFile(), str2))) {
            throw new FileNotFoundException("Failed to rename document " + str + " to " + str2);
        }
        int lastIndexOf = str.lastIndexOf(47, str.length() - 2);
        return str.substring(0, lastIndexOf) + "/" + str2;
    }
}
