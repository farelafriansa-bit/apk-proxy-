import sys

filepath = 'iny proxy/app/src/main/java/com/whatsap/whatunban/MainActivity.java'
with open(filepath, 'r') as f:
    content = f.read()

# Fix 1: Add PendingIntent and more imports
imports_to_add = """import android.app.PendingIntent;
"""
if 'import android.app.PendingIntent;' not in content:
    content = content.replace('import android.app.Notification;', 'import android.app.Notification;\n' + imports_to_add)

# Fix 2: Enable file access in WebView
webview_settings = """        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);"""
if 'setAllowFileAccess(true)' not in content:
    content = content.replace('webView.getSettings().setUseWideViewPort(true);', 'webView.getSettings().setUseWideViewPort(true);\n' + webview_settings)

# Fix 3: Fix checkDebugStatus logic for Shizuku
shizuku_logic = """                boolean shizukuRunning = false;
                try {
                    Process process = Runtime.getRuntime().exec("sh -c getprop moe.shizuku.privileged.api");
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
                    String line = reader.readLine();
                    shizukuRunning = line != null && line.trim().length() > 0;
                    process.destroy();
                } catch (Exception e) {}"""
old_shizuku_logic = """                boolean shizukuRunning = false;
                try {
                    Process process = Runtime.getRuntime().exec("sh /system/bin/getprop moe.shizuku.privileged.api");
                    shizukuRunning = true;
                } catch (Exception e) {}"""
content = content.replace(old_shizuku_logic, shizuku_logic)

# Fix 4: Add PendingIntent to Notification
notification_logic = """                    showToast("🔑 Masukkan kode pairing di panel notifikasi");

                    Intent intent = new Intent("android.settings.ADB_WIFI_SETTINGS");
                    PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification.Builder builder;"""
old_notification_logic = """                    showToast("🔑 Masukkan kode pairing di panel notifikasi");

                    Notification.Builder builder;"""
content = content.replace(old_notification_logic, notification_logic)

if '.setContentText("Klik di sini untuk memasukkan kode pairing Anda")' in content:
    content = content.replace('.setContentText("Klik di sini untuk memasukkan kode pairing Anda")', '.setContentText("Klik di sini untuk memasukkan kode pairing Anda")\n                            .setContentIntent(pendingIntent)')

with open(filepath, 'w') as f:
    f.write(content)
