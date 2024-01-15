package no.nordicsemi.android.nrfmesh;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TruckFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_main_truck,container,false);

        // 使用Fragment時，按鈕的觸發必須寫在上面，return必須在最下面
        return view;
    }

}