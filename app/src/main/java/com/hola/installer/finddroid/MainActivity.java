package com.hola.installer.finddroid;

import static android.view.View.GONE;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    private static final String APK_URL =
            "https://finddroid.github.io/finddroid.apk";
    ProgressBar progressBar;
    Button btn;
    TextView appSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
         progressBar = findViewById(R.id.downloadProgress);
         appSize = findViewById(R.id.appSize);

        btn = findViewById(R.id.updateBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadAndInstallFindDroid();
            }
        });
    }


    private void downloadAndInstallFindDroid() {



        new Thread(() -> {

            try {
                URL url = new URL(APK_URL);
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Download failed: "
                            + connection.getResponseCode());
                }

                File apkFile = new File(
                        getCacheDir(),
                        "FindDroid.apk"
                );

                try (InputStream input = connection.getInputStream();
                     FileOutputStream output =
                             new FileOutputStream(apkFile)) {
                    runOnUiThread(()->{
                        progressBar.setVisibility(View.VISIBLE);
                    });


                    int fileLength = connection.getContentLength();
                    long total = 0;

                    byte[] buffer = new byte[8192];
                    int length;

                    while ((length = input.read(buffer)) != -1) {

                        total += length;
                        output.write(buffer, 0, length);

                        if (fileLength > 0) {
                            int progress = (int) (total * 100 / fileLength);

                            runOnUiThread(() -> {
                                btn.setVisibility(GONE);
                                progressBar.setProgress(progress);
                            });
                        }
                    }
                    runOnUiThread(() -> {
                        progressBar.setProgress(100);
                        progressBar.setVisibility(View.GONE);
                        btn.setVisibility(View.VISIBLE);
                        btn.setText("Install Again");
                    });

                    while ((length = input.read(buffer)) != -1) {
                        output.write(buffer, 0, length);
                    }
                }

                connection.disconnect();

                runOnUiThread(() -> installApk(apkFile));

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Download failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }

        }).start();
    }

    private void installApk(File apkFile) {

        Uri apkUri = FileProvider.getUriForFile(
                getApplicationContext(),
                getPackageName() + ".provider",
                apkFile
        );
                Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            if (!getPackageManager().canRequestPackageInstalls()) {
//
//                Intent settingsIntent = new Intent(
//                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
//                        Uri.parse("package:" + getPackageName())
//                );
//
//                startActivity(settingsIntent);
////                return;
//            }
//        }




    }


}