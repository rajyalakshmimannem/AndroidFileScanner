package com.filescanner.rmann52.androidfilescanner.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.filescanner.rmann52.androidfilescanner.R;
import com.filescanner.rmann52.androidfilescanner.adapter.ScannerDataAdapter;
import com.filescanner.rmann52.androidfilescanner.databinding.ActivityFileScannerBinding;
import com.filescanner.rmann52.androidfilescanner.model.ScanFileInfo;
import com.filescanner.rmann52.androidfilescanner.model.ScanResults;
import com.filescanner.rmann52.androidfilescanner.util.ScanUtils;

import java.util.Map;

public class SDFileScannerActivity extends AppCompatActivity implements SDFileScannerResultsFragment.OnFileScanListener{

    private static final String TAG = "SDFileScannerActivity";

    private static final int REQUEST_PERMISSIONS_CODE = 100;

    private static final int NOTIFICATION_ID = 1;

    private static final String SCAN_RESULTS_KEY = "scan-results-key";

    private static final String SCAN_RESULTS_FRAGMENT_KEY = "scan-results-fragment";

    private ActivityFileScannerBinding fileScannerBinding;

    private SDFileScannerResultsFragment fileScannerResultsFragment;

    private ScanResults mScanResults;

    private boolean isPermissionDisplayed;

    private RecyclerView scan_list;

 //   private ShareActionProvider mShareActionProvider;

    private MenuItem menuItem;

    //private NotificationManager mNotificationManager;


    private void initScanResults() {
        fileScannerBinding.emptyMessage.setVisibility(View.GONE);
        fileScannerBinding.scanResults.setLayoutManager(new LinearLayoutManager(this));
        fileScannerBinding.scanResults.setAdapter(new ScannerDataAdapter());

        fileScannerResultsFragment = (SDFileScannerResultsFragment) getSupportFragmentManager().findFragmentByTag(SCAN_RESULTS_FRAGMENT_KEY);
        if (fileScannerResultsFragment == null) {
            fileScannerResultsFragment = new SDFileScannerResultsFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(fileScannerResultsFragment, SCAN_RESULTS_FRAGMENT_KEY).commit();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileScannerBinding = DataBindingUtil.setContentView(this, R.layout.activity_file_scanner);
        scan_list = (RecyclerView) fileScannerBinding.scanResults;
        fileScannerBinding.setScannerVM(this);
        initScanResults();
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgress(fileScannerResultsFragment.isRunning());
        if (ScanUtils.isPermissionRequired(this)) {
            if (!isPermissionDisplayed) {
                showPermissionDialog();
            }
        } else {
            fileScannerBinding.emptyMessage.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null
                && !fileScannerResultsFragment.isRunning()
                && !ScanUtils.isPermissionRequired(this)) {
            mScanResults = savedInstanceState.getParcelable(SCAN_RESULTS_KEY);
            updateScanResults();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mScanResults != null) {
            outState.putParcelable(SCAN_RESULTS_KEY, mScanResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scanner_menu, menu);

    /*    menuItem = menu.findItem(R.id.share_menu);
        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);*/

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.share_menu).setVisible((mScanResults != null) && (!mScanResults.isEmpty()));
        return true;
    }

    @Override
    public void onBackPressed() {
        fileScannerResultsFragment.cancelFileScan();
        removeNotification();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                isPermissionDisplayed = true;
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showOpenPermissionMessage();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onStartScanClick() {
        fileScannerBinding.emptyMessage.setVisibility(View.GONE);
        if (ScanUtils.isPermissionRequired(this)) {
            showOpenPermissionMessage();
        } else {
            startFileScan();
        }
    }

    public void onStopScanClick() {
        fileScannerResultsFragment.cancelFileScan();
        fileScannerBinding.actionStop.setEnabled(false);
        fileScannerBinding.progressTitle.setText(getString(R.string.preparing_to_cancel));
    }

    public void onShareClicked(MenuItem item) {
        Log.d(TAG, "onShareClicked :: ");
        if (mScanResults != null) {
            ScanUtils.shareScanResults(SDFileScannerActivity.this,
                    getString(R.string.share_file_title),
                    getString(R.string.share_file_subject),
                    mScanResults.getResultsAsText()
            );
        }
    }

    @Override
    public void onScanning() {
        showProgress(true);
        scan_list.invalidate();
        fileScannerBinding.scanResults.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onScanResults(ScanResults results) {
        mScanResults = results;
        updateScanResults();
  //      showProgress(false);
    }

    @Override
    public void onCancelled() {
        showProgress(false);
        Log.v("cancelled", "cancelled");
        showMessage(getString(R.string.scan_cancelled));
        fileScannerBinding.progressTitle.setText(getString(R.string.scanning_in_progress));
    }

    @Override
    public void showError() {
        showProgress(false);
        showMessage(getString(R.string.unable_to_read_external_storage));
    }

    private void startFileScan() {
        if (mScanResults != null) {
            mScanResults.reset();
        }
        fileScannerResultsFragment.startFileScan();
    }

    private void showPermissionDialog() {
        if (ScanUtils.isRationaleRequired(this)) { // user already denied
            showExplanationDialog();
        } else {
            displaySystemPermissionDialog();
        }
    }

    private void displaySystemPermissionDialog() {
        ActivityCompat.requestPermissions(this, ScanUtils.PERMISSIONS, REQUEST_PERMISSIONS_CODE);
    }

    private void updateScanResults() {
        if (mScanResults != null && !mScanResults.isEmpty()) {
            mScanResults.setResultsAsText(getString(R.string.share_results_message,
                    mScanResults.getTotalFilesScanned(),
                    mScanResults.getAvgFileSize(),
                    mScanResults.getLargestFilesInfo(),
                    mScanResults.getFrequentExtensionsInfo()));
            updateResultsAdapter();
            showProgress(false);
            fileScannerBinding.scanResults.setVisibility(View.VISIBLE);
        } else {
            showMessage(getString(R.string.no_resullts));
        }
        invalidateOptionsMenu();
    }

    private void updateResultsAdapter() {
        ScannerDataAdapter adapter = (ScannerDataAdapter) fileScannerBinding.scanResults.getAdapter();
        adapter.reset();
        scan_list.invalidate();
        adapter.addHeader(getString(R.string.scan_details));
        adapter.addItem(getString(R.string.files_scanned), mScanResults.getTotalFilesScanned());
        adapter.addItem(getString(R.string.average_file_size), mScanResults.getAvgFileSize());
        addLargestFilesToAdapter(adapter);
        addFrequenciesToAdapter(adapter);
        fileScannerBinding.scanResults.setVisibility(View.VISIBLE);
    }

    private void addLargestFilesToAdapter(ScannerDataAdapter adapter) {
        adapter.addHeader(getString(R.string.top_10_largest_files));
        for (ScanFileInfo file : mScanResults.getLargestFiles()) {
            adapter.addItem(file.getName(), file.getSize());
        }
       adapter.notifyDataSetChanged();
    }

    private void addFrequenciesToAdapter(ScannerDataAdapter adapter) {
        adapter.addHeader(getString(R.string.top_5_frequent_extns));
        for (Map.Entry<String, Integer> entry : mScanResults.getFrequencies()) {
            adapter.addItem(entry.getKey(), String.valueOf(entry.getValue()));
        }
        adapter.notifyDataSetChanged();
    }

    private void showExplanationDialog() {
        AlertDialog dlg = (new AlertDialog.Builder(this))
                .setCancelable(false)
                .setMessage(R.string.enable_permission)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        displaySystemPermissionDialog();
                    }
                }).create();
        dlg.show();
    }

    private void showProgress(boolean showProgress) {
        fileScannerBinding.progressContainer.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        fileScannerBinding.actionStart.setEnabled(!showProgress);
        fileScannerBinding.actionStop.setEnabled(showProgress);
        if (showProgress) {
            showOnGoingNotification();
        } else {
            removeNotification();
        }
    }

    private void showEmptyMessage() {
        fileScannerBinding.scanResults.setVisibility(View.GONE);
        fileScannerBinding.emptyMessage.setVisibility(View.VISIBLE);
    }

    private void showMessage(String message) {
        Snackbar.make(fileScannerBinding.mainContainer, message, Snackbar.LENGTH_LONG).show();
    }

    private void showOpenPermissionMessage() {
        showEmptyMessage();
        Snackbar snackbar = Snackbar.make(fileScannerBinding.mainContainer, R.string.storage_permission_required, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.open, new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ScanUtils.launchApplicationSettings(SDFileScannerActivity.this);
            }
        });
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        snackbar.show();
    }

    /**
     * Displays a non-pending intent Notification.
     */
    private void showOnGoingNotification() {
        Notification notification = (new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.scanner_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.scanning_in_progress)))
                .build();


        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);

    }

    private void removeNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

}
