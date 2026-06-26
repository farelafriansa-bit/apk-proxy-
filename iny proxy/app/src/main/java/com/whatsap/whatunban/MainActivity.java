package com.whatsap.whatunban;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private WebView webView;
    private Handler handler = new Handler();

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

    // ========================================
    // WEB APP INTERFACE
    // ========================================
    class WebAppInterface {

        @JavascriptInterface
        public void executeCommand(String menu, String action) {
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

        @JavascriptInterface
        public void openWirelessDebugging() {
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

        @JavascriptInterface
        public void openShizuku() {
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
        public void addGame(String packageName) {
            if (!userGameList.contains(packageName)) {
                userGameList.add(packageName);
                showToast("✅ Game berhasil ditambahkan!");
            } else {
                showToast("⚠️ Game sudah ada di daftar");
            }
        }

        @JavascriptInterface
        public void removeGame(String packageName) {
            if (userGameList.contains(packageName)) {
                userGameList.remove(packageName);
                showToast("🗑️ Game dihapus dari daftar");
            }
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

        @JavascriptInterface
        public void openFreeFire() {
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
        public void showToast(String message) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void copyToClipboard(String text) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", text);
            clipboard.setPrimaryClip(clip);
            showToast("✅ Text disalin: " + text);
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
