package no.nordicsemi.android.nrfmesh.notifications;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import no.nordicsemi.android.nrfmesh.databinding.ActivityNotificationsBinding;

public class NotificationsActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityNotificationsBinding binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 顯示返回按鈕
        setSupportActionBar(binding.notifications);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }
}