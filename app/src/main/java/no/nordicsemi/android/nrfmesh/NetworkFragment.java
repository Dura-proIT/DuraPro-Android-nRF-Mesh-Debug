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

package no.nordicsemi.android.nrfmesh;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.nrfmesh.adapter.GroupAdapter;
import no.nordicsemi.android.nrfmesh.ble.ScannerActivity;
import no.nordicsemi.android.nrfmesh.databinding.FragmentNetworkBinding;
import no.nordicsemi.android.nrfmesh.databinding.LayoutVendorModelControlsBinding;
import no.nordicsemi.android.nrfmesh.databinding.NetworkItemBinding;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentDeleteNode;
import no.nordicsemi.android.nrfmesh.dialog.DialogFragmentError;
import no.nordicsemi.android.nrfmesh.node.NodeConfigurationActivity;
import no.nordicsemi.android.nrfmesh.node.VendorModelActivity;
import no.nordicsemi.android.nrfmesh.node.adapter.NodeAdapter;
import no.nordicsemi.android.nrfmesh.utils.Utils;
import no.nordicsemi.android.nrfmesh.viewmodels.BaseViewModel;
import no.nordicsemi.android.nrfmesh.viewmodels.SharedViewModel;
import no.nordicsemi.android.nrfmesh.widgets.ItemTouchHelperAdapter;
import no.nordicsemi.android.nrfmesh.widgets.RemovableItemTouchHelperCallback;
import no.nordicsemi.android.nrfmesh.widgets.RemovableViewHolder;

@AndroidEntryPoint
public class NetworkFragment extends Fragment implements
        NodeAdapter.OnItemClickListener,
        ItemTouchHelperAdapter,
        DialogFragmentDeleteNode.DialogFragmentDeleteNodeListener,
        GroupCallbacks {
    private FragmentNetworkBinding binding;
    private SharedViewModel mViewModel;
    private BaseViewModel bViewModel;
    private NodeAdapter mNodeAdapter;
    private GroupAdapter groupAdapter;
    private LayoutVendorModelControlsBinding layoutVendorModelControlsBinding;
    private MeshModel meshModel;
    private String state;
    private NetworkItemBinding networkItemBinding;
    private final ActivityResultLauncher<Intent> provisioner = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleActivityResult);

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup viewGroup, @Nullable final Bundle savedInstanceState) {
        binding = FragmentNetworkBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        networkItemBinding = NetworkItemBinding.inflate(getLayoutInflater());
        final ExtendedFloatingActionButton fab = binding.fabAddNode;
        final RecyclerView mRecyclerViewNodes = binding.recyclerViewProvisionedNodes;
        final View noNetworksConfiguredView = binding.noNetworksConfigured.getRoot();

        final ExtendedFloatingActionButton detection = binding.testDetcetion;

//        final MaterialCardView mCardView = binding.truckMain.card;




        // 顯示一開始卡車頁面
//        noNetworksConfiguredView.setVisibility(View.INVISIBLE);
//        mRecyclerViewNodes.setVisibility(View.INVISIBLE);
//        fab.setVisibility(View.INVISIBLE);

        // 當按下前往觀看後，將原本的卡車資訊隱藏，顯示裝置頁面
//        final Button get = binding.truckMain.truckGetView;
//        get.setOnClickListener(view1 -> {
//            mCardView.setVisibility(View.INVISIBLE);
//            noNetworksConfiguredView.setVisibility(View.VISIBLE);
//            mRecyclerViewNodes.setVisibility(View.VISIBLE);
//            fab.setVisibility(View.VISIBLE);


            // Create view model containing utility methods for scanning (此處如果偵測到沒有裝置則顯示畫面上無裝置提示)
            mViewModel.getNodes().observe(getViewLifecycleOwner(), nodes -> {
                if (nodes != null && !nodes.isEmpty()) {
                    noNetworksConfiguredView.setVisibility(View.GONE);
                } else {
                    noNetworksConfiguredView.setVisibility(View.VISIBLE);
                }
                requireActivity().invalidateOptionsMenu();

            });
//        });


        // Configure the recycler view
        mNodeAdapter = new NodeAdapter(this, mViewModel.getNodes());
        mNodeAdapter.setOnItemClickListener(this);
        mRecyclerViewNodes.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerViewNodes.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        final ItemTouchHelper.Callback itemTouchHelperCallback = new RemovableItemTouchHelperCallback(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerViewNodes);
        mRecyclerViewNodes.setAdapter(mNodeAdapter);




        mViewModel.isConnectedToProxy().observe(getViewLifecycleOwner(), isConnected -> {
            if (isConnected != null) {
                requireActivity().invalidateOptionsMenu();
            }

        });



        mRecyclerViewNodes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final LinearLayoutManager m = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (m != null) {
                    if (m.findFirstCompletelyVisibleItemPosition() == 0) {
                        fab.extend();
                    } else {
                        fab.shrink();
                    }
                }
            }
        });




        fab.setOnClickListener(v -> {
            final Intent intent = new Intent(requireContext(), ScannerActivity.class);
            intent.putExtra(Utils.EXTRA_DATA_PROVISIONING_SERVICE, true);
            provisioner.launch(intent);

        });

        detection.setOnClickListener(v -> {
            MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(requireContext());
            alertDialog.setTitle("Device State:");
            VendorModelActivity VMA = new VendorModelActivity();
            String STATE = VMA.getState();
            if (STATE.contains("Error")){
                alertDialog.setMessage("Check your Device");
                alertDialog.setIcon(R.drawable.ic_lightbulb_outline_black_24dp);
            }else {
                alertDialog.setIcon(R.drawable.ic_lamp_green);
                alertDialog.setMessage(" Device is working");
            }
            alertDialog.setPositiveButton("confirm", (dialog, which) -> {

            });
            alertDialog.setCancelable(false);
            alertDialog.show();
        });

        return binding.getRoot();

    }


    @Override
    public void onConfigureClicked(final ProvisionedMeshNode node) {
        mViewModel.setSelectedMeshNode(node);
        final Intent meshConfigurationIntent = new Intent(requireActivity(), NodeConfigurationActivity.class);
        requireActivity().startActivity(meshConfigurationIntent);

    }

    @Override
    public void onItemDismiss(final RemovableViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        if (!mNodeAdapter.isEmpty()) {
            final DialogFragmentDeleteNode fragmentDeleteNode = DialogFragmentDeleteNode.newInstance(position);
            fragmentDeleteNode.show(getChildFragmentManager(), null);
        }
    }

    @Override
    public void onItemDismissFailed(final RemovableViewHolder viewHolder) {
        //Do nothing
    }

    @Override
    public void onNodeDeleteConfirmed(final int position) {
        final ProvisionedMeshNode node = mNodeAdapter.getItem(position);
        if (mViewModel.getNetworkLiveData().getMeshNetwork().deleteNode(node)) {
            mViewModel.displaySnackBar(requireActivity(), binding.container, getString(R.string.node_deleted), Snackbar.LENGTH_LONG);
        }
    }

    @Override
    public void onNodeDeleteCancelled(final int position) {
        mNodeAdapter.notifyItemChanged(position);
    }

    private void handleActivityResult(final ActivityResult result) {
        final Intent data = result.getData();
        if (result.getResultCode() == RESULT_OK && data != null) {
            final boolean provisioningSuccess = data.getBooleanExtra(Utils.PROVISIONING_COMPLETED, false);
            final DialogFragmentError fragmentConfigError;
            if (provisioningSuccess) {
                final boolean provisionerUnassigned = data.getBooleanExtra(Utils.PROVISIONER_UNASSIGNED, false);
                if (provisionerUnassigned) {
                    fragmentConfigError =
                            DialogFragmentError.newInstance(getString(R.string.title_init_config_error)
                                    , getString(R.string.provisioner_unassigned_msg));
                    fragmentConfigError.show(getChildFragmentManager(), null);
                } else {
                    final boolean compositionDataReceived = data.getBooleanExtra(Utils.COMPOSITION_DATA_COMPLETED, false);
                    final boolean defaultTtlGetCompleted = data.getBooleanExtra(Utils.DEFAULT_GET_COMPLETED, false);
                    final boolean appKeyAddCompleted = data.getBooleanExtra(Utils.APP_KEY_ADD_COMPLETED, false);
                    final boolean networkRetransmitSetCompleted = data.getBooleanExtra(Utils.NETWORK_TRANSMIT_SET_COMPLETED, false);
                    final String title = getString(R.string.title_init_config_error);
                    final String message;
                    if (compositionDataReceived) {
                        if (defaultTtlGetCompleted) {
                            if (appKeyAddCompleted) {
                                if (!networkRetransmitSetCompleted) {
                                    message = getString(R.string.init_config_error_app_key_msg);
                                    showErrorDialog(title, message);
                                }
                            } else {
                                message = getString(R.string.init_config_error_app_key_msg);
                                showErrorDialog(title, message);
                            }
                        } else {
                            message = getString(R.string.init_config_error_default_ttl_get_msg);
                            showErrorDialog(title, message);
                        }
                    } else {
                        message = getString(R.string.init_config_error_all);
                        showErrorDialog(title, message);
                    }
                }
            }
            requireActivity().invalidateOptionsMenu();
        }
    }

    private void showErrorDialog(@NonNull final String title, @NonNull final String message) {
        final DialogFragmentError dialogFragmentError = DialogFragmentError.newInstance(title, message);
        dialogFragmentError.show(getChildFragmentManager(), null);
    }

    public void onAttach(VendorModelActivity activity){
        super.onAttach(activity);
        state =((VendorModelActivity)activity).getState();
        Log.i("TAG","networkfragment get state:" +state);

    }



}
