package no.nordicsemi.android.nrfmesh;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import no.nordicsemi.android.mesh.models.VendorModel;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.VendorModelMessageStatus;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.ble.adapter.DevicesAdapter;
import no.nordicsemi.android.nrfmesh.databinding.ActivityTruckcontrolBinding;
import no.nordicsemi.android.nrfmesh.databinding.LayoutVendorModelControlsBinding;
import no.nordicsemi.android.nrfmesh.node.ModelConfigurationActivity;
import no.nordicsemi.android.nrfmesh.node.adapter.NodeAdapter;
import no.nordicsemi.android.nrfmesh.viewmodels.BaseActivity;
import no.nordicsemi.android.nrfmesh.viewmodels.ScannerStateLiveData;
import no.nordicsemi.android.nrfmesh.viewmodels.ScannerViewModel;
import no.nordicsemi.android.nrfmesh.viewmodels.SharedViewModel;

public class TruckControlActivity extends BaseActivity {
    private ScannerViewModel scannerViewModel;
    private ActivityTruckcontrolBinding binding;
    private LayoutVendorModelControlsBinding layoutVendorModelControlsBinding;
    private NodeAdapter mNodeAdapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final MeshModel model = mViewModel.getSelectedModel().getValue();
        setTheme(R.style.AppTheme);
        if(model instanceof VendorModel){
            layoutVendorModelControlsBinding = LayoutVendorModelControlsBinding.inflate(getLayoutInflater(),binding.cordinatorLayout,false);
            layoutVendorModelControlsBinding.opCode.setVisibility(View.GONE);
            layoutVendorModelControlsBinding.parameters.setVisibility(View.GONE);
            layoutVendorModelControlsBinding.opCode.setText("09");
            layoutVendorModelControlsBinding.parameters.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    layoutVendorModelControlsBinding.parametersLayout.setError(null);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        binding = ActivityTruckcontrolBinding.inflate(getLayoutInflater());
        mNodeAdapter = new NodeAdapter(this,mViewModel.getNodes());
        }


    @Override
    protected void updateClickableViews() {

    }

    @Override
    protected void showProgressBar() {

    }

    @Override
    protected void hideProgressBar() {

    }

    @Override
    protected void enableClickableViews() {

    }

    @Override
    protected void disableClickableViews() {

    }

    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage){
        if (meshMessage instanceof VendorModelMessageStatus) {
            final VendorModelMessageStatus status = (VendorModelMessageStatus) meshMessage;
            layoutVendorModelControlsBinding.receivedMessageContainer.setVisibility(View.VISIBLE);
            if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A000")){
                layoutVendorModelControlsBinding.receivedMessage.setText("OFF");
                layoutVendorModelControlsBinding.opCode.setText("09");
            }

        }
    }
}
