package com.harrysoft.androidbluetoothserial.demoapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.harrysoft.androidbluetoothserial.demoapp.databinding.ActivityCommunicateBinding;

public class CommunicateActivity extends AppCompatActivity {

    private ActivityCommunicateBinding mBinding;

    private CommunicateViewModel viewModel;

    // PIN 13 ON/OFF 상태값 저장
    private boolean mValuePin13 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup our activity
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_communicate);

        // Enable the back button in the action bar if possible
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup our ViewModel
        viewModel = ViewModelProviders.of(this).get(CommunicateViewModel.class);

        // This method return false if there is an error, so if it does, we should close.
        if (!viewModel.setupViewModel(getIntent().getStringExtra("device_name"), getIntent().getStringExtra("device_mac"))) {
            finish();
            return;
        }

        // Start observing the data sent to us by the ViewModel
        viewModel.getConnectionStatus().observe(this, this::onConnectionStatus);
        viewModel.getDeviceName().observe(this, name -> setTitle(getString(R.string.device_name_format, name)));
        viewModel.getMessages().observe(this, message -> {
            if (TextUtils.isEmpty(message)) {
                message = getString(R.string.no_messages);
            }
            mBinding.communicateMessages.setText(message);
        });
        viewModel.getMessage().observe(this, message -> {
            // Only update the message if the ViewModel is trying to reset it
            if (TextUtils.isEmpty(message)) {
                mBinding.communicateMessage.setText(message);
            }
        });

        // Setup the send button click action
        mBinding.communicateSend.setOnClickListener(v -> viewModel.sendMessage(mBinding.communicateMessage.getText().toString()));

        mBinding.btnPin13.setOnClickListener(v -> {
            mValuePin13 = !mValuePin13;
            viewModel.sendMessage("^13:" + (mValuePin13 ? "H" : "L")  + "$");
        });
    }

    // Called when the ViewModel updates us of our connectivity status
    private void onConnectionStatus(CommunicateViewModel.ConnectionStatus connectionStatus) {
        switch (connectionStatus) {
            case CONNECTED:

                mBinding.communicateConnectionText.setText(R.string.status_connected);
                mBinding.communicateMessage.setEnabled(true);
                mBinding.communicateSend.setEnabled(true);
                mBinding.communicateConnect.setEnabled(true);
                mBinding.communicateConnect.setText(R.string.disconnect);
                mBinding.communicateConnect.setOnClickListener(v -> viewModel.disconnect());
                break;

            case CONNECTING:
                mBinding.communicateConnectionText.setText(R.string.status_connecting);
                mBinding.communicateMessage.setEnabled(false);
                mBinding.communicateSend.setEnabled(false);
                mBinding.communicateConnect.setEnabled(false);
                mBinding.communicateConnect.setText(R.string.connect);
                break;

            case DISCONNECTED:
                mBinding.communicateConnectionText.setText(R.string.status_disconnected);
                mBinding.communicateMessage.setEnabled(false);
                mBinding.communicateSend.setEnabled(false);
                mBinding.communicateConnect.setEnabled(true);
                mBinding.communicateConnect.setText(R.string.connect);
                mBinding.communicateConnect.setOnClickListener(v -> viewModel.connect());
                break;
        }
    }

    // Called when a button in the action bar is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                // If the back button was pressed, handle it the normal way
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Called when the user presses the back button
    @Override
    public void onBackPressed() {
        // Close the activity
        finish();
    }
}
