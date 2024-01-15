/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfmesh.node.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.CompanyIdentifiers;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.databinding.LayoutVendorModelControlsBinding;
import no.nordicsemi.android.nrfmesh.databinding.NetworkItemBinding;
import no.nordicsemi.android.nrfmesh.node.VendorModelActivity;
import no.nordicsemi.android.nrfmesh.viewmodels.BaseViewModel;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.ViewHolder>  {

    private final AsyncListDiffer<ProvisionedMeshNode> differ = new AsyncListDiffer<>(this, new NodeDiffCallback());
    private OnItemClickListener mOnItemClickListener;
    private  NetworkItemBinding networkItemBinding;
    private VendorModelActivity VMActivity;
    private BaseViewModel mViewModel;
    private  LayoutVendorModelControlsBinding layoutVendorModelControlsBinding;
    private byte[] parameters ;

    public NodeAdapter(@NonNull final LifecycleOwner owner,
                       @NonNull final LiveData<List<ProvisionedMeshNode>> provisionedNodesLiveData) {

        provisionedNodesLiveData.observe(owner, nodes -> {
            if (nodes != null) {
                differ.submitList(new ArrayList<>(nodes));
            }
        });


    }


    public void setOnItemClickListener(@NonNull final OnItemClickListener listener) {
        mOnItemClickListener = listener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new NodeAdapter.ViewHolder(NetworkItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }


    @SuppressLint({"ResourceAsColor", "SuspiciousIndentation"})
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final ProvisionedMeshNode node = differ.getCurrentList().get(position);
        if (node != null) {
//            holder.nodeControls.setBackgroundColor(Color.DKGRAY);
//          holder.nodeControls.setBackgroundColor(R.color.md_theme_dark_errorContainer);
//            if(node.getNodeName().contains("3032")){
//                holder.icon.setBackgroundResource(R.drawable.ic_sidemarkergyd);
//            }else if(node.getNodeName().contains("3031")){
//                holder.icon.setBackgroundResource(R.drawable.ic_sidemarkersquare);
//            }else if(node.getNodeName().contains("3030")){
//                holder.icon.setBackgroundResource(R.drawable.ic_taillight);
//            }
            holder.name.setText(node.getNodeName());
//            holder.icon.setBackgroundResource(R.drawable.ic_lightbulb_outline_black_24dp);
            holder.unicastAddress.setText(MeshParserUtils.bytesToHex(MeshAddress.addressIntToBytes(node.getUnicastAddress()), false));

            final Map<Integer, Element> elements = node.getElements();
            if (!elements.isEmpty()) {
                holder.nodeInfoContainer.setVisibility(View.VISIBLE);
                if (node.getCompanyIdentifier() != null) {
                    holder.companyIdentifier.setText(CompanyIdentifiers.getCompanyName(node.getCompanyIdentifier().shortValue()));
                } else {
                    holder.companyIdentifier.setText(R.string.unknown);
                }
                holder.elements.setText(String.valueOf(elements.size()));
                holder.models.setText(String.valueOf(getModels(elements)));
            } else {
                holder.companyIdentifier.setText(R.string.unknown);
                holder.elements.setText(String.valueOf(node.getNumberOfElements()));
                holder.models.setText(R.string.unknown);
            }

            holder.test_on.setOnClickListener(v -> {
                MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(v.getContext());
                holder.nodeControls.setBackgroundColor(Color.WHITE);
                alertDialog.setTitle("Device State:");
                VendorModelActivity VMA = new VendorModelActivity();
                String STATE = VMA.getState();
                if (node.getNodeName().contains("C1386F") ||node.getNodeName().contains("C13874")||node.getNodeName().contains("C13870")){
                    alertDialog.setMessage("Check your Device");
                    alertDialog.setIcon(R.drawable.ic_lightbulb_outline_black_24dp);

                }else {
                    alertDialog.setIcon(R.drawable.ic_lamp_green);
                    alertDialog.setMessage(" Device is working");

                }
                    alertDialog.setPositiveButton("Confirm", (dialog, which) -> {
                    });
                alertDialog.setCancelable(false);
                alertDialog.show();
                VendorModelActivity VM = new VendorModelActivity();
                String state = VM.getState();
                Log.i("TAG","Get State:"+state);
                if(node.getNodeName().contains("C1386F") ||node.getNodeName().contains("C13874")||node.getNodeName().contains("C13870")) {
                    holder.unicastAddress.setText("Error");
                    holder.icon.setBackgroundResource(R.drawable.ic_lightbulb_outline_black_24dp);
                }else
                    holder.unicastAddress.setText("Normal");
                    if (holder.unicastAddress.getText().equals("Normal")) {
                        holder.icon.setBackgroundResource(R.drawable.ic_lamp_green);
                        String opCode = "09";
                        final String params = "F00300000000F3F1";
                        parameters = MeshParserUtils.toByteArray(params);
                        Log.i("TAG", "test get parameters" + parameters);
                        sendVendorModelMessage(Integer.parseInt(opCode, 16), parameters);
                        Log.i("TAG", "send successful");

                }
//                    if(state.getState().equals("ON")){
//                        holder.nodeInfoContainer.setBackgroundColor(Color.GREEN);
//                    }else
//                    holder.nodeInfoContainer.setBackgroundColor(Color.DKGRAY);
//                   VendorModelActivity VMA = new VendorModelActivity();
//                   String s= VMA.getState();
//                   if(s.equals("ON")){
//                       holder.icon.setBackgroundResource(R.drawable.ic_lightbulb_level_48dp);
//                   }else if(s.equals("OFF")){
//                       holder.icon.setBackgroundResource(R.drawable.ic_lightbulb_outline_black_24dp);
//                   }

            });
        }

    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public ProvisionedMeshNode getItem(final int position) {
        if (getItemCount() > 0 && position > -1) {
            return differ.getCurrentList().get(position);
        }
        return null;
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    private int getModels(final Map<Integer, Element> elements) {
        int models = 1;
//        for (Element element : elements.values()) {
//            models += element.getMeshModels().size();
//        }
        return models;
    }

    @FunctionalInterface
    public interface OnItemClickListener {
        void onConfigureClicked(final ProvisionedMeshNode node);
    }

    final class ViewHolder extends RemovableViewHolder {
        FrameLayout container;
        TextView name;
        View nodeInfoContainer;
        TextView unicastAddress;
        TextView companyIdentifier;
        TextView elements;
        TextView models;
        ImageView icon;
        Button test_on;
        Button detection;
        View nodeControls;

        private ViewHolder(final @NonNull NetworkItemBinding binding) {
            super(binding.getRoot());
            container = binding.container;
            name = binding.nodeName;
            nodeInfoContainer = binding.configuredNodeInfoContainer;
//            nodeInfoContainer = binding.nodeControlsContainer;
            unicastAddress = binding.unicast;
            companyIdentifier = binding.companyIdentifier;
            elements = binding.elements;
            models = binding.models;
            icon = binding.icon;
            test_on = binding.testOn;
            nodeControls = binding.nodeControlsContainer;

            container.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onConfigureClicked(differ.getCurrentList().get(getAbsoluteAdapterPosition()));
                }
            });
        }
    }
    public void sendVendorModelMessage(int opcode, byte[] parameters){

    }

//    @Override
//    public void updateMeshMessage(MeshMessage message){
//      if(message instanceof VendorModelMessageStatus){
//          final VendorModelMessageStatus status = (VendorModelMessageStatus) message;
//          if (MeshParserUtils.bytesToHex(status.getAccessPayload(),false).contains("F0A000")){
//              Log.i("TAG","test successful");
//          }
//      }
//    }

    }



