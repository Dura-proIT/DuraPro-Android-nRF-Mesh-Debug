package no.nordicsemi.android.nrfmesh.node;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.models.VendorModel;
import no.nordicsemi.android.mesh.transport.ConfigVendorModelAppList;
import no.nordicsemi.android.mesh.transport.ConfigVendorModelSubscriptionList;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.VendorModelMessageAcked;
import no.nordicsemi.android.mesh.transport.VendorModelMessageStatus;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.LogcatActivity;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutVendorModelControlsBinding;
import no.nordicsemi.android.nrfmesh.utils.HexKeyListener;
import no.nordicsemi.android.nrfmesh.utils.Utils;


@AndroidEntryPoint
public class VendorModelActivity extends ModelConfigurationActivity {

    private LayoutVendorModelControlsBinding layoutVendorModelControlsBinding;
    private static String State = "";
//    Button  testButton = findViewById(R.id.button_on);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipe.setOnRefreshListener(this);
        final MeshModel model = mViewModel.getSelectedModel().getValue();

        // 顯示返回按鈕
        final Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_vendor_model_controls);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (model instanceof VendorModel) {
            layoutVendorModelControlsBinding = LayoutVendorModelControlsBinding.inflate(getLayoutInflater(), binding.nodeControlsContainer, true);
            final KeyListener hexKeyListener = new HexKeyListener();
            layoutVendorModelControlsBinding.opCode.setKeyListener(hexKeyListener);
            layoutVendorModelControlsBinding.opCode.setVisibility(View.VISIBLE);
            layoutVendorModelControlsBinding.parameters.setVisibility(View.VISIBLE);
            layoutVendorModelControlsBinding.chkAcknowledged.setVisibility(View.VISIBLE);
            layoutVendorModelControlsBinding.textView.setVisibility(View.VISIBLE);
            layoutVendorModelControlsBinding.opCode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                }

                @Override
                public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                    layoutVendorModelControlsBinding.opCodeLayout.setError(null);
                }

                @Override
                public void afterTextChanged(final Editable s) {
                }
            });

            layoutVendorModelControlsBinding.parameters.setKeyListener(hexKeyListener);
            layoutVendorModelControlsBinding.parameters.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                }

                @Override
                public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                    layoutVendorModelControlsBinding.parametersLayout.setError(null);
                }

                @Override
                public void afterTextChanged(final Editable s) {

                }
            });

            // 手動指令
            layoutVendorModelControlsBinding.buttonActionSend.setOnClickListener(v -> {
                layoutVendorModelControlsBinding.receivedMessageContainer.setVisibility(View.VISIBLE);

                final String opCode = layoutVendorModelControlsBinding.opCode.getEditableText().toString().trim();
                final String parameters = layoutVendorModelControlsBinding.parameters.getEditableText().toString().trim();


                if (!validateOpcode(opCode, layoutVendorModelControlsBinding.opCodeLayout))
                    return;

                if (!validateParameters(parameters, layoutVendorModelControlsBinding.parametersLayout))
                    return;

                if (model.getBoundAppKeyIndexes().isEmpty()) {
                    Log.e("顯示Log紀錄", getResources().getString(R.string.no_app_keys_bound));
                    Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
                    return;
                }

                final byte[] params;
                if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                    params = null;
                } else {
                    params = MeshParserUtils.toByteArray(parameters);
                }

                Log.e("顯示Log紀錄",getResources().getString(R.string.send_cmd) + " Opcode without company id: 0xC0 | 0x" + layoutVendorModelControlsBinding.opCode.getText() + "  Parameters: 0x" + layoutVendorModelControlsBinding.parameters.getText());
                sendVendorModelMessage(Integer.parseInt(opCode, 16), params, layoutVendorModelControlsBinding.chkAcknowledged.isChecked());
            });

            // 移除節點按鈕
            layoutVendorModelControlsBinding.actionSend.setOnClickListener(v -> {

                // 設定對話框，標題、內容、icon設定，以及碰觸對話框外不會被關閉對話框
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(VendorModelActivity.this);
                builder.setMessage(R.string.remove_dialog_message)
                        .setTitle(R.string.remove_dialog_title)
                        .setCancelable(false)
                        .setIcon(R.drawable.ic_baseline_warning_24);

                // 使用者選擇按鈕偵測對話框內的行為
                builder.setPositiveButton(R.string.confirm, (dialog, id) -> {
                    layoutVendorModelControlsBinding.receivedMessageContainer.setVisibility(View.GONE);
                    layoutVendorModelControlsBinding.receivedMessage.setText("");
                    layoutVendorModelControlsBinding.opCode.setText("09");
                    layoutVendorModelControlsBinding.parameters.setText("F0FC00000000ECF1");

                    final String opCode = layoutVendorModelControlsBinding.opCode.getEditableText().toString().trim();
                    final String parameters = layoutVendorModelControlsBinding.parameters.getEditableText().toString().trim();
                    if (!validateOpcode(opCode, layoutVendorModelControlsBinding.opCodeLayout))
                        return;

                    if (!validateParameters(parameters, layoutVendorModelControlsBinding.parametersLayout))
                        return;

                    if (model.getBoundAppKeyIndexes().isEmpty()) {
                        Log.e("顯示Log紀錄", getResources().getString(R.string.no_app_keys_bound));
                        Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
                        return;
                    }

                    final byte[] params;
                    if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                        params = null;
                    } else {
                        params = MeshParserUtils.toByteArray(parameters);
                    }

                    Log.e("顯示Log紀錄",getResources().getString(R.string.send_cmd) + " Opcode without company id: 0xC0 | 0x" + layoutVendorModelControlsBinding.opCode.getText() + "  Parameters: 0x" + layoutVendorModelControlsBinding.parameters.getText());
                    sendVendorModelMessage(Integer.parseInt(opCode, 16), params, layoutVendorModelControlsBinding.chkAcknowledged.isChecked());
                });
                builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
                });

                // 啟動對話框
                builder.create().show();

            });

            // 開燈按鈕
            layoutVendorModelControlsBinding.buttonOn.setOnClickListener(v -> {
                layoutVendorModelControlsBinding.receivedMessageContainer.setVisibility(View.VISIBLE);

                layoutVendorModelControlsBinding.opCode.setText("09");
                layoutVendorModelControlsBinding.parameters.setText("F00100000000F1F1");

                final String opCode = layoutVendorModelControlsBinding.opCode.getEditableText().toString().trim();
                final String parameters = layoutVendorModelControlsBinding.parameters.getEditableText().toString().trim();


                if (!validateOpcode(opCode, layoutVendorModelControlsBinding.opCodeLayout))
                    return;

                if (!validateParameters(parameters, layoutVendorModelControlsBinding.parametersLayout))
                    return;

                if (model.getBoundAppKeyIndexes().isEmpty()) {
                    Log.e("顯示Log紀錄", getResources().getString(R.string.no_app_keys_bound));
                    Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
                    return;
                }

                final byte[] params;
                if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                    params = null;
                } else {
                    params = MeshParserUtils.toByteArray(parameters);
                }

                Log.e("顯示Log紀錄",getResources().getString(R.string.send_cmd) + " Opcode without company id: 0xC0 | 0x" + layoutVendorModelControlsBinding.opCode.getText() + "  Parameters: 0x" + layoutVendorModelControlsBinding.parameters.getText());
                sendVendorModelMessage(Integer.parseInt(opCode, 16), params, layoutVendorModelControlsBinding.chkAcknowledged.isChecked());
            });


            // 關燈按鈕
            layoutVendorModelControlsBinding.buttonOff.setOnClickListener(v -> {
                layoutVendorModelControlsBinding.receivedMessageContainer.setVisibility(View.VISIBLE);

                layoutVendorModelControlsBinding.opCode.setText("09");
                layoutVendorModelControlsBinding.parameters.setText("F00000000000F0F1");



                final String opCode = layoutVendorModelControlsBinding.opCode.getEditableText().toString().trim();
                final String parameters = layoutVendorModelControlsBinding.parameters.getEditableText().toString().trim();


                if (!validateOpcode(opCode, layoutVendorModelControlsBinding.opCodeLayout))
                    return;

                if (!validateParameters(parameters, layoutVendorModelControlsBinding.parametersLayout))
                    return;

                if (model.getBoundAppKeyIndexes().isEmpty()) {
                    Log.e("顯示Log紀錄", getResources().getString(R.string.no_app_keys_bound));
                    Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
                    return;
                }

                final byte[] params;
                if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                    params = null;
                } else {
                    params = MeshParserUtils.toByteArray(parameters);
                }

                Log.e("顯示Log紀錄",getResources().getString(R.string.send_cmd) + " Opcode without company id: 0xC0 | 0x" + layoutVendorModelControlsBinding.opCode.getText() + "  Parameters: 0x" + layoutVendorModelControlsBinding.parameters.getText());
                sendVendorModelMessage(Integer.parseInt(opCode, 16), params, layoutVendorModelControlsBinding.chkAcknowledged.isChecked());
            });


            // 偵測裝置狀態按鈕
                layoutVendorModelControlsBinding.buttonDetection.setOnClickListener(v -> {
                    layoutVendorModelControlsBinding.receivedMessageContainer.setVisibility(View.VISIBLE);
                    layoutVendorModelControlsBinding.opCode.setText("09");
                    layoutVendorModelControlsBinding.parameters.setText("F00300000000F3F1");

                    final String opCode = layoutVendorModelControlsBinding.opCode.getEditableText().toString().trim();
                    final String parameters = layoutVendorModelControlsBinding.parameters.getEditableText().toString().trim();


                    if (!validateOpcode(opCode, layoutVendorModelControlsBinding.opCodeLayout))
                        return;

                    if (!validateParameters(parameters, layoutVendorModelControlsBinding.parametersLayout))
                        return;

                    if (model.getBoundAppKeyIndexes().isEmpty()) {
                        Log.e("顯示Log紀錄", getResources().getString(R.string.no_app_keys_bound));
                        Toast.makeText(this, R.string.no_app_keys_bound, Toast.LENGTH_LONG).show();
                        return;
                    }

                    final byte[] params;
                    if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                        params = null;
                    } else {
                        params = MeshParserUtils.toByteArray(parameters);
                    }

                    Log.e("顯示Log紀錄",getResources().getString(R.string.send_cmd) + " Opcode without company id: 0xC0 | 0x" + layoutVendorModelControlsBinding.opCode.getText() + "  Parameters: 0x" + layoutVendorModelControlsBinding.parameters.getText());
                    sendVendorModelMessage(Integer.parseInt(opCode, 16), params, layoutVendorModelControlsBinding.chkAcknowledged.isChecked());
                });



            mViewModel.getSelectedModel().observe(this, meshModel -> {
                if (meshModel != null) {
                    updateAppStatusUi(meshModel);
                    updatePublicationUi(meshModel);
                    updateSubscriptionUi(meshModel);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.logcat,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.logcat) {
            Intent intent = new Intent(VendorModelActivity.this, LogcatActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
    }

    @SuppressLint({"SetTextI18n", "SuspiciousIndentation"})
    @Override
    protected void updateMeshMessage(final MeshMessage meshMessage) {
        super.updateMeshMessage(meshMessage);
        if (meshMessage instanceof VendorModelMessageStatus) {
            final VendorModelMessageStatus status = (VendorModelMessageStatus) meshMessage;
            layoutVendorModelControlsBinding.receivedMessageContainer.setVisibility(View.VISIBLE);

            if (MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A000")){
                layoutVendorModelControlsBinding.receivedMessage.setText(getResources().getString(R.string.device_control_off) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + getResources().getString(R.string.device_control_off) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.DKGRAY);
                State = "OFF";
                mViewModel.setState("OFF");
            }
            else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A003000092")){
                layoutVendorModelControlsBinding.receivedMessage.setText(getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text)  + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.RED);
                State="Error";
                mViewModel.setState("Error");
            }
            else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A003000192")){
                layoutVendorModelControlsBinding.receivedMessage.setText(getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.RED);
                State="Error";
                mViewModel.setState("Error");
            }
            else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A001")){
                layoutVendorModelControlsBinding.receivedMessage.setText(getResources().getString(R.string.device_control_on) + getResources().getString(R.string.all_state_text)+ MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + getResources().getString(R.string.device_control_on) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.YELLOW);
                State = "ON";
                mViewModel.setState("ON");
            }
            else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0B000")){
                layoutVendorModelControlsBinding.receivedMessage.setText(getResources().getString(R.string.device_control_remove_successful) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + getResources().getString(R.string.device_control_remove_successful) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.LTGRAY);
            }
            else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("92")){
                layoutVendorModelControlsBinding.receivedMessage.setText(getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.RED);
                State="Error";
                mViewModel.setState("Error");
            }
            else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A0030000")){
                layoutVendorModelControlsBinding.receivedMessage.setText("OFF(Low)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + "OFF(Low)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.DKGRAY);
                State="OFF";
                mViewModel.setState("OFF");
            }
            else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A0030001")){
                layoutVendorModelControlsBinding.receivedMessage.setText("ON(Low)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + "ON(Low)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.YELLOW);
                State="ON";
                mViewModel.setState("ON");

            }
            else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A0030010")){
                layoutVendorModelControlsBinding.receivedMessage.setText("OFF(High)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + "OFF(High)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.DKGRAY);
                State="OFF";
                mViewModel.setState("OFF");
            }

            else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A0030011")) {
                layoutVendorModelControlsBinding.receivedMessage.setText("ON(High)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + "ON(High)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.YELLOW);
                State = "ON";
                mViewModel.setState("ON");

            }else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A00301")){
                layoutVendorModelControlsBinding.receivedMessage.setText(getResources().getString(R.string.device_control_cmd_fail) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + getResources().getString(R.string.device_control_cmd_fail) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.LTGRAY);
                State = "CMD Fail";
            }
            else if(MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A0FC")){
                layoutVendorModelControlsBinding.receivedMessage.setText(getResources().getString(R.string.device_control_remove_device) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + getResources().getString(R.string.device_control_remove_device) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
                layoutVendorModelControlsBinding.lamp.setColorFilter(Color.LTGRAY);
                State= "Remove";
            } else
                layoutVendorModelControlsBinding.receivedMessage.setText(MeshParserUtils.bytesToHex(status.getAccessPayload(), false));
                Log.e("顯示Log紀錄", getResources().getString(R.string.received_message) + MeshParserUtils.bytesToHex(status.getAccessPayload(),false));
//            else if (MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A003000")){layoutVendorModelControlsBinding.receivedMessage.setText("OFF");}
//            layoutVendorModelControlsBinding.receivedMessage.setText(MeshParserUtils.bytesToHex(status.getAccessPayload(), false));
//            final String parameters = layoutVendorModelControlsBinding.parameters.getEditableText().toString().trim();
//            if(parameters.contains("F000")){
//                layoutVendorModelControlsBinding.receivedMessage.setText("OFF");
//            }else if(parameters.contains("F001")){
//                layoutVendorModelControlsBinding.receivedMessage.setText("ON");
//            }

        } else if (meshMessage instanceof ConfigVendorModelAppList) {
            final ConfigVendorModelAppList status = (ConfigVendorModelAppList) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                if (handleStatuses()) return;
            } else {
                displayStatusDialogFragment(getString(R.string.title_vendor_model_app_list), status.getStatusCodeName());
            }
        } else if (meshMessage instanceof ConfigVendorModelSubscriptionList) {
            final ConfigVendorModelSubscriptionList status = (ConfigVendorModelSubscriptionList) meshMessage;
            mViewModel.removeMessage();
            if (status.isSuccessful()) {
                if (handleStatuses()) return;
            } else {
                displayStatusDialogFragment(getString(R.string.title_vendor_model_subscription_list), status.getStatusCodeName());
            }
        }
        hideProgressBar();
    }

    /**
     * Validate opcode
     *
     * @param opCode       opcode
     * @param opCodeLayout op c0de view
     * @return true if success or false otherwise
     */
    private boolean validateOpcode(final String opCode, final TextInputLayout opCodeLayout) {
        try {
            if (TextUtils.isEmpty(opCode)) {
                opCodeLayout.setError(getString(R.string.error_empty_value));
                return false;
            }

            if (opCode.length() % 2 != 0 || !opCode.matches(Utils.HEX_PATTERN)) {
                opCodeLayout.setError(getString(R.string.invalid_hex_value));
                return false;
            }
            if (MeshParserUtils.isValidOpcode(Integer.valueOf(opCode, 16))) {
                return true;
            }
        } catch (NumberFormatException ex) {
            opCodeLayout.setError(getString(R.string.invalid_value));
            return false;
        } catch (IllegalArgumentException ex) {
            opCodeLayout.setError(ex.getMessage());
            return false;
        } catch (Exception ex) {
            opCodeLayout.setError(ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Validate parameters
     *
     * @param parameters       parameters
     * @param parametersLayout parameter view
     * @return true if success or false otherwise
     */
    private boolean validateParameters(final String parameters, final TextInputLayout parametersLayout) {
        try {
            if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                return true;
            }

            if (parameters.length() % 2 != 0 || !parameters.matches(Utils.HEX_PATTERN)) {
                parametersLayout.setError(getString(R.string.invalid_hex_value));
                return false;
            }

            if (MeshParserUtils.isValidParameters(MeshParserUtils.toByteArray(parameters))) {
                return true;
            }
        } catch (NumberFormatException ex) {
            parametersLayout.setError(getString(R.string.invalid_value));
            return false;
        } catch (IllegalArgumentException ex) {
            parametersLayout.setError(ex.getMessage());
            return false;
        } catch (Exception ex) {
            parametersLayout.setError(ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Send vendor model acknowledged message
     *
     * @param opcode     opcode of the message
     * @param parameters parameters of the message
     */
    public void sendVendorModelMessage(final int opcode, final byte[] parameters, final boolean acknowledged) {
        final Element element = mViewModel.getSelectedElement().getValue();
        if (element != null) {
            final VendorModel model = (VendorModel) mViewModel.getSelectedModel().getValue();
            if (model != null) {
                final int appKeyIndex = model.getBoundAppKeyIndexes().get(0);
                final ApplicationKey appKey = mViewModel.getNetworkLiveData().getMeshNetwork().getAppKey(appKeyIndex);
                if (acknowledged) {
                    sendAcknowledgedMessage(element.getElementAddress() ,
                            new VendorModelMessageAcked(appKey, model.getModelId(), model.getCompanyIdentifier(), opcode, parameters));
                } else {
                    sendUnacknowledgedMessage(element.getElementAddress() ,
                            new VendorModelMessageAcked(appKey, model.getModelId(), model.getCompanyIdentifier(), opcode, parameters));
                }
            }
        }
    }

    // 依據使用者點選的上層Activity回到對應的Activity
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public String getState(){
       String s2 = State;
        return s2;
    }

//    public void setOn(){
//        testButton.performClick();
//        State ="ON";
//    }
//    public String getOn(){
//        return State;
//    }
}