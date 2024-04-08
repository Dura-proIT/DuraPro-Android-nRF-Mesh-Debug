package no.nordicsemi.android.nrfmesh.node.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutVendorModelBottomSheetBinding;
import no.nordicsemi.android.nrfmesh.utils.HexKeyListener;
import no.nordicsemi.android.nrfmesh.utils.Utils;

public class BottomSheetVendorDialogFragment extends BottomSheetDialogFragment {
    private static final String KEY_INDEX = "KEY_INDEX";
    private static final String MODEL_ID = "MODEL_ID";

    private LayoutVendorModelBottomSheetBinding binding;
    private int mModelId;
    private int mKeyIndex;

    public interface BottomSheetVendorModelControlsListener {
        void sendVendorModelMessage(final int modelId, final int keyIndex, final int opCode, final byte[] parameters, final boolean acknowledged);
    }

    public static BottomSheetVendorDialogFragment getInstance(final int modelId, final int appKeyIndex) {
        final BottomSheetVendorDialogFragment fragment = new BottomSheetVendorDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(MODEL_ID, modelId);
        args.putInt(KEY_INDEX, appKeyIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mModelId = getArguments().getInt(MODEL_ID);
            mKeyIndex = getArguments().getInt(KEY_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        binding = LayoutVendorModelBottomSheetBinding.inflate(getLayoutInflater());
        final KeyListener hexKeyListener = new HexKeyListener();

        binding.opCode.setKeyListener(hexKeyListener);
        binding.opCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                binding.opCodeLayout.setError(null);
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        binding.parameters.setKeyListener(hexKeyListener);
        binding.parameters.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                binding.parametersLayout.setError(null);
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        // 手動指令
        binding.actionSend.setOnClickListener(v -> {
            binding.receivedMessageContainer.setVisibility(View.VISIBLE);
            binding.receivedMessage.setText("");
            final String opCode = binding.opCode.getEditableText().toString().trim();
            final String parameters = binding.parameters.getEditableText().toString().trim();

            if (!validateOpcode(opCode, binding.opCodeLayout))
                return;

            if (!validateParameters(parameters, binding.parametersLayout))
                return;

            final byte[] params;
            if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                params = null;
            } else {
                params = MeshParserUtils.toByteArray(parameters);
            }

            Log.e("顯示Log紀錄",getResources().getString(R.string.group_send_cmd) + " Opcode without company id: 0xC0 | 0x" + opCode + "  Parameters: 0x" + parameters);
            ((BottomSheetVendorModelControlsListener) requireActivity())
                    .sendVendorModelMessage(mModelId, mKeyIndex, Integer.parseInt(opCode, 16), params, binding.chkAcknowledged.isChecked());
        });

        // 開燈按鈕
        binding.buttonOn.setOnClickListener(v -> {
            binding.receivedMessageContainer.setVisibility(View.VISIBLE);
            binding.receivedMessage.setText("");

            binding.opCode.setText("09");
            binding.parameters.setText("F00100000000F1F1");

            final String opCode = binding.opCode.getEditableText().toString().trim();
            final String parameters = binding.parameters.getEditableText().toString().trim();

            if (!validateOpcode(opCode, binding.opCodeLayout))
                return;

            if (!validateParameters(parameters, binding.parametersLayout))
                return;

            final byte[] params;
            if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                params = null;
            } else {
                params = MeshParserUtils.toByteArray(parameters);
            }

            Log.e("顯示Log紀錄",getResources().getString(R.string.group_send_cmd) + " Opcode without company id: 0xC0 | 0x" + opCode + "  Parameters: 0x" + parameters);
            ((BottomSheetVendorModelControlsListener) requireActivity())
                    .sendVendorModelMessage(mModelId, mKeyIndex, Integer.parseInt(opCode, 16), params, binding.chkAcknowledged.isChecked());
        });

        // 關燈按鈕
        binding.buttonOff.setOnClickListener(v -> {
            binding.receivedMessageContainer.setVisibility(View.VISIBLE);
            binding.receivedMessage.setText("");

            binding.opCode.setText("09");
            binding.parameters.setText("F00000000000F0F1");

            final String opCode = binding.opCode.getEditableText().toString().trim();
            final String parameters = binding.parameters.getEditableText().toString().trim();

            if (!validateOpcode(opCode, binding.opCodeLayout))
                return;

            if (!validateParameters(parameters, binding.parametersLayout))
                return;

            final byte[] params;
            if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                params = null;
            } else {
                params = MeshParserUtils.toByteArray(parameters);
            }

            Log.e("顯示Log紀錄",getResources().getString(R.string.group_send_cmd) + " Opcode without company id: 0xC0 | 0x" + opCode + "  Parameters: 0x" + parameters);
            ((BottomSheetVendorModelControlsListener) requireActivity())
                    .sendVendorModelMessage(mModelId, mKeyIndex, Integer.parseInt(opCode, 16), params, binding.chkAcknowledged.isChecked());
        });

        // 讀取狀態按鈕
        binding.buttonDetection.setOnClickListener(v -> {
            binding.receivedMessageContainer.setVisibility(View.VISIBLE);
            binding.receivedMessage.setText("");

            binding.opCode.setText("09");
            binding.parameters.setText("F00300000000F3F1");

            final String opCode = binding.opCode.getEditableText().toString().trim();
            final String parameters = binding.parameters.getEditableText().toString().trim();

            if (!validateOpcode(opCode, binding.opCodeLayout))
                return;

            if (!validateParameters(parameters, binding.parametersLayout))
                return;

            final byte[] params;
            if (TextUtils.isEmpty(parameters) && parameters.length() == 0) {
                params = null;
            } else {
                params = MeshParserUtils.toByteArray(parameters);
            }

            Log.e("顯示Log紀錄",getResources().getString(R.string.group_send_cmd) + " Opcode without company id: 0xC0 | 0x" + opCode + "  Parameters: 0x" + parameters);
            ((BottomSheetVendorModelControlsListener) requireActivity())
                    .sendVendorModelMessage(mModelId, mKeyIndex, Integer.parseInt(opCode, 16), params, binding.chkAcknowledged.isChecked());
        });

        return binding.getRoot();
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

    // 顯示狀態
    @SuppressLint("SetTextI18n")
    public void setReceivedMessage(final byte[] accessPayload) {
        binding.receivedMessageContainer.setVisibility(View.VISIBLE);

//        binding.receivedMessage.setText(MeshParserUtils.bytesToHex(accessPayload, false));
        if (MeshParserUtils.bytesToHex(accessPayload, false).contains("F0A000")) {
            binding.receivedMessage.setText(getResources().getString(R.string.device_control_off) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + getResources().getString(R.string.device_control_off) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if (MeshParserUtils.bytesToHex(accessPayload, false).contains("F0A003000092")) {
            binding.receivedMessage.setText(getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text)  + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if (MeshParserUtils.bytesToHex(accessPayload, false).contains("F0A003000192")) {
            binding.receivedMessage.setText(getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if (MeshParserUtils.bytesToHex(accessPayload, false).contains("F0A001")) {
            binding.receivedMessage.setText(getResources().getString(R.string.device_control_on) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + getResources().getString(R.string.device_control_on) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if (MeshParserUtils.bytesToHex(accessPayload, false).contains("F0B000")) {
            binding.receivedMessage.setText(getResources().getString(R.string.device_control_remove_successful) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + getResources().getString(R.string.device_control_remove_successful) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if (MeshParserUtils.bytesToHex(accessPayload, false).contains("92")) {
            binding.receivedMessage.setText(getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + getResources().getString(R.string.device_control_error) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if (MeshParserUtils.bytesToHex(accessPayload, false).contains("F0A0030000")) {
            binding.receivedMessage.setText("OFF(Low)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + "OFF(Low)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if(MeshParserUtils.bytesToHex(accessPayload, false).contains("F0A0030000")){
            binding.receivedMessage.setText("ON(Low)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + "ON(Low)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if(MeshParserUtils.bytesToHex(accessPayload, false).contains("F0A0030010")){
            binding.receivedMessage.setText("OFF(High)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + "OFF(High)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if(MeshParserUtils.bytesToHex(accessPayload, false).contains("F0A0030011")) {
            binding.receivedMessage.setText("ON(High)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + "ON(High)" + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if(MeshParserUtils.bytesToHex(accessPayload, false).contains("F0A00301")){
            binding.receivedMessage.setText(getResources().getString(R.string.device_control_cmd_fail) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + getResources().getString(R.string.device_control_cmd_fail) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else if(MeshParserUtils.bytesToHex(accessPayload, false).contains("F0A0FC")){
            binding.receivedMessage.setText(getResources().getString(R.string.device_control_remove_device) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + getResources().getString(R.string.device_control_remove_device) + getResources().getString(R.string.all_state_text) + MeshParserUtils.bytesToHex(accessPayload, false));
        } else {
            binding.receivedMessage.setText(MeshParserUtils.bytesToHex(accessPayload, false));
            Log.e("顯示Log紀錄", getResources().getString(R.string.group_received_message) + MeshParserUtils.bytesToHex(accessPayload, false));
        }
    }
}
