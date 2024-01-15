package no.nordicsemi.android.nrfmesh;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import no.nordicsemi.android.nrfmesh.databinding.ActivityLogcatBinding;

public class LogcatActivity extends AppCompatActivity   {

    private ActivityLogcatBinding binding;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogcatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 顯示返回按鈕
        final Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.logcat_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

//        Log.e("顯示Log紀錄","進入Log紀錄");

        new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec("logcat -d");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                StringBuilder log = new StringBuilder();
                String line;

                // 顯示全部
//                while ((line = bufferedReader.readLine()) != null) {
//                    log.append(line).append("\n");
//                }

                // 顯示特定紀錄
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("顯示Log紀錄")) {
                        // 提取日期和時間部分
                        String dateTime = line.substring(0, 18);
                        // 提取日誌消息，從 "顯示Log紀錄" 之後開始
                        String message = line.substring(line.indexOf("顯示Log紀錄") + "顯示Log紀錄".length()).trim();
                        log.append(dateTime).append(message).append("\n");
                    }
                }

                // 在UI更新顯示
                TextView textView = binding.logcat;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> textView.setText(log.toString()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.remove,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.remove) {

            MaterialAlertDialogBuilder exitApp = new MaterialAlertDialogBuilder(LogcatActivity.this);
            exitApp.setIcon(R.drawable.ic_help_outline_24dp);
            exitApp.setTitle(getResources().getString(R.string.logcat_clear_title))
                    .setMessage(getResources().getString(R.string.logcat_clear_message))
                    .setPositiveButton(R.string.confirm, (dialog_1, id_) -> {
                        new Thread(() -> {
                            try {
                                Runtime.getRuntime().exec("logcat -c");

                                runOnUiThread(() -> {
                                    // 更新 UI
                                    TextView textView = binding.logcat;
                                    textView.setText("");
                                    Snackbar.make(findViewById(android.R.id.content), R.string.device_control_remove_successful, Snackbar.LENGTH_SHORT).show();
                                });

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), (dialog_1, which_1) -> {
                    }).show();

            return true;
        }
        return false;
    }

    // 依據使用者點選的上層Activity回到對應的Activity
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}