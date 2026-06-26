package com.whatsap.whatunban;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private WebView webView;
    private Handler handler = new Handler(Looper.getMainLooper());

    private static final String CHANNEL_ID = "debug_channel";
    private static final int NOTIFICATION_ID = 101;
    private static final int PERMISSION_REQUEST_CODE = 123;

    // List game yang sudah ditambahkan user
    private List<String> userGameList = new ArrayList<>();

    // State menu
    private boolean isAutoBody = false;
    private boolean isAutoLock = false;
    private boolean isAutoSpeed = false;
    private boolean isAutoJump = false;
    private boolean isAutoBypass = false;
    private boolean isAutoRefresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();
        requestNecessaryPermissions();

        webView = (WebView) findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // ========================================
        // LOAD LOGIN PAGE DULU
        // ========================================
        webView.loadUrl("file:///android_asset/login.html");

        webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
				}

				// Biarkan navigasi antar halaman berjalan normal
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					return true;
				}
			});

        webView.setWebChromeClient(new WebChromeClient());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Debugging Notifications";
            String description = "Notifications for Wireless Debugging and Pairing";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNecessaryPermissions() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    // ========================================
    // WEB APP INTERFACE
    // ========================================
    class WebAppInterface {

        @JavascriptInterface
        public void executeCommand(final String menu, final String action) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (menu.equals("body")) {
                        isAutoBody = action.equals("start");
                        showToast("aim body: " + (isAutoBody ? "ON ✅" : "OFF ❌"));
                    } else if (menu.equals("lock")) {
                        isAutoLock = action.equals("start");
                        showToast("aim lock: " + (isAutoLock ? "ON ✅" : "OFF ❌"));
                    } else if (menu.equals("speed")) {
                        isAutoSpeed = action.equals("start");
                        showToast("speed up: " + (isAutoSpeed ? "ON ✅" : "OFF ❌"));
                    } else if (menu.equals("jump")) {
                        isAutoJump = action.equals("start");
                        showToast("back jump: " + (isAutoJump ? "ON ✅" : "OFF ❌"));
                    } else if (menu.equals("bypass")) {
                        isAutoBypass = action.equals("start");
                        showToast("bypass: " + (isAutoBypass ? "ON ✅" : "OFF ❌"));
                    } else if (menu.equals("refresh")) {
                        isAutoRefresh = action.equals("start");
                        showToast("auto refresh: " + (isAutoRefresh ? "ON ✅" : "OFF ❌"));
                    }
                }
            });
        }

        @JavascriptInterface
        public void openWirelessDebugging() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent intent = new Intent("android.settings.ADB_WIFI_SETTINGS");
                        startActivity(intent);
                        showToast("🌐 Membuka Pengaturan Debugging Nirkabel...");
                    } catch (Exception e) {
                        try {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                            startActivity(intent);
                            showToast("🛠️ Buka Opsi Developer > Debugging Nirkabel");
                        } catch (Exception e2) {
                            showToast("❌ Gagal membuka pengaturan!");
                        }
                    }
                }
            });
        }

        @JavascriptInterface
        public void openShizuku() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api");
                        if (intent != null) {
                            startActivity(intent);
                            showToast("🏮 Membuka Shizuku...");
                        } else {
                            Intent playStore = new Intent(Intent.ACTION_VIEW);
                            playStore.setData(Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api"));
                            startActivity(playStore);
                            showToast("📥 Shizuku belum terinstall!");
                        }
                    } catch (Exception e) {
                        showToast("❌ Gagal membuka Shizuku!");
                    }
                }
            });
        }

        @JavascriptInterface
        public String checkDebugStatus() {
            JSONObject status = new JSONObject();
            try {
                boolean adbEnabled = android.provider.Settings.Global.getInt(getContentResolver(), "adb_enabled", 0) > 0;
                status.put("wireless", adbEnabled);

                boolean shizukuRunning = false;
                try {
                    Process process = Runtime.getRuntime().exec("sh /system/bin/getprop moe.shizuku.privileged.api");
                    shizukuRunning = true;
                } catch (Exception e) {}

                status.put("shizuku", shizukuRunning);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return status.toString();
        }

        @JavascriptInterface
        public void showPairingNotification() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    showToast("🔑 Masukkan kode pairing di panel notifikasi");

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle("Pairing Debugging Nirkabel")
                            .setContentText("Klik di sini untuk memasukkan kode pairing Anda")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                    try {
                        notificationManager.notify(NOTIFICATION_ID, builder.build());
                    } catch (SecurityException e) {
                        showToast("⚠️ Berikan izin notifikasi!");
                    }
                }
            });
        }

        // ========================================
        // SCAN ALL GAME DI HP
        // ========================================
        @JavascriptInterface
        public String scanAllGames() {
            JSONArray gamesArray = new JSONArray();
            PackageManager pm = getPackageManager();

            try {
                List<ApplicationInfo> apps = pm.getInstalledApplications(0);

                for (ApplicationInfo appInfo : apps) {
                    if (isGameApp(appInfo, pm)) {
                        JSONObject game = new JSONObject();
                        game.put("packageName", appInfo.packageName);
                        game.put("appName", pm.getApplicationLabel(appInfo).toString());
                        game.put("isInstalled", true);
                        game.put("isAdded", userGameList.contains(appInfo.packageName));

                        Drawable icon = pm.getApplicationIcon(appInfo);
                        String iconBase64 = drawableToBase64(icon);
                        game.put("iconBase64", iconBase64);

                        gamesArray.put(game);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return gamesArray.toString();
        }

        private boolean isGameApp(ApplicationInfo appInfo, PackageManager pm) {
            String[] gamePackages = {
                "com.dts.freefireth", "com.dts.freefiremax", "com.tencent.ig",
                "com.pubg.krmobile", "com.garena.game.kg", "com.mobile.legends",
                "com.supercell.clashofclans", "com.supercell.brawlstars",
                "com.roblox.client", "com.mojang.minecraftpe",
                "com.activision.callofduty.shooter", "com.riotgames.league.wildrift",
                "com.tencent.tmgp.sgame", "com.tencent.tmgp.pubgmhd",
                "com.gameloft.android.ANMP.GloftA9HM", "com.gameloft.android.ANMP.GloftA8HM",
                "com.ea.games.simsmobile", "com.supercell.clashroyale",
                "com.netease.heartpro", "com.dragonnest", "com.vng.pubgmobile",
                "com.tencent.tmgp.cod", "com.pubg.imobile"
            };

            for (String pkg : gamePackages) {
                if (appInfo.packageName.equals(pkg)) {
                    return true;
                }
            }
            return false;
        }

        @JavascriptInterface
        public void addGame(final String packageName) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (!userGameList.contains(packageName)) {
                        userGameList.add(packageName);
                        showToast("✅ Game berhasil ditambahkan!");
                    } else {
                        showToast("⚠️ Game sudah ada di daftar");
                    }
                }
            });
        }

        @JavascriptInterface
        public void removeGame(final String packageName) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (userGameList.contains(packageName)) {
                        userGameList.remove(packageName);
                        showToast("🗑️ Game dihapus dari daftar");
                    }
                }
            });
        }

        @JavascriptInterface
        public String getUserGames() {
            JSONArray gamesArray = new JSONArray();
            PackageManager pm = getPackageManager();

            try {
                List<String> listCopy = new ArrayList<>(userGameList);
                for (String pkg : listCopy) {
                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                        JSONObject game = new JSONObject();
                        game.put("packageName", pkg);
                        game.put("appName", pm.getApplicationLabel(appInfo).toString());

                        Drawable icon = pm.getApplicationIcon(appInfo);
                        String iconBase64 = drawableToBase64(icon);
                        game.put("iconBase64", iconBase64);

                        gamesArray.put(game);
                    } catch (PackageManager.NameNotFoundException e) {
                        userGameList.remove(pkg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return gamesArray.toString();
        }

        @JavascriptInterface
        public void openGame(final String packageName) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                        if (intent != null) {
                            startActivity(intent);
                            showToast("🎮 Membuka game...");
                        } else {
                            showToast("❌ Game tidak ditemukan!");
                        }
                    } catch (Exception e) {
                        showToast("❌ Gagal membuka game!");
                    }
                }
            });
        }

        @JavascriptInterface
        public void openFreeFire() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.dts.freefireth");
                        if (intent != null) {
                            startActivity(intent);
                            showToast("🎮 Membuka Free Fire...");
                        } else {
                            Intent intentMax = getPackageManager().getLaunchIntentForPackage("com.dts.freefiremax");
                            if (intentMax != null) {
                                startActivity(intentMax);
                                showToast("🎮 Membuka Free Fire MAX...");
                            } else {
                                Intent playStore = new Intent(Intent.ACTION_VIEW);
                                playStore.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.dts.freefireth"));
                                playStore.setPackage("com.android.vending");
                                startActivity(playStore);
                                showToast("📥 Free Fire tidak terinstall, buka Play Store...");
                            }
                        }
                    } catch (Exception e) {
                        showToast("❌ Gagal membuka Free Fire!");
                    }
                }
            });
        }

        private String drawableToBase64(Drawable drawable) {
            try {
                android.graphics.Bitmap bitmap = null;
                if (drawable instanceof android.graphics.drawable.BitmapDrawable) {
                    bitmap = ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();
                } else {
                    int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 128;
                    int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 128;

                    if (width > 128) {
                        height = (int) (height * (128.0 / width));
                        width = 128;
                    }

                    bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                }

                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 80, baos);
                byte[] imageBytes = baos.toByteArray();
                return android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
            } catch (Exception e) {
                return "";
            }
        }

        @JavascriptInterface
        public void showToast(final String message) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void copyToClipboard(final String text) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", text);
                    clipboard.setPrimaryClip(clip);
                    showToast("✅ Text disalin: " + text);
                }
            });
        }

        @JavascriptInterface
        public String getClipboardText() {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip()) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                return item.getText().toString();
            }
            return "";
        }
    }
}
