package com.filescanner.rmann52.androidfilescanner.ui;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.filescanner.rmann52.androidfilescanner.R;
import com.filescanner.rmann52.androidfilescanner.model.ScanResults;
import com.filescanner.rmann52.androidfilescanner.util.ScanUtils;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SDFileScannerResultsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SDFileScannerResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SDFileScannerResultsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

 //   private OnFragmentInteractionListener mListener;

    private OnFileScanListener mListener;

    private FileScannerTask fileScannerTask;

    private boolean running;

    public SDFileScannerResultsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FileScannerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SDFileScannerResultsFragment newInstance(String param1, String param2) {
        SDFileScannerResultsFragment fragment = new SDFileScannerResultsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_scanner_results, container, false);
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setListener(context);
    }

    private void setListener(Context context){
        if (context instanceof OnFileScanListener) {
            mListener = (OnFileScanListener) context;
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void startFileScan() {
        if(ScanUtils.isExternalStorageReadable()) {
            fileScannerTask = new FileScannerTask();
            fileScannerTask.execute();
            running = true;
        }else {
            if(mListener != null){
                mListener.showError();
            }
        }
    }

    public void cancelFileScan(){
        if(fileScannerTask != null) {
            fileScannerTask.cancel(true);
            fileScannerTask = null;
            running = false;
        }

    }

    public boolean isRunning(){
        return running;
    }

    private class FileScannerTask extends AsyncTask<Uri, Void, Void> {

        private String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();

        private ScanResults mScanResults;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(mListener != null) {
                mListener.onScanning();
            }
            running = true;
        }

        @Override
        protected Void doInBackground(Uri... uri) {
            mScanResults = new ScanResults();
            File root = new File(SDCARD_ROOT);
            getFile(root);
            mScanResults.extractResults();
            return null;
        }

        public void onPostExecute(Void none) {
            super.onPostExecute(none);
            if(mListener != null) {
                mListener.onScanResults(mScanResults);
            }
            running = false;
        }

        @Override
        public void onCancelled(){
            if(mListener != null) {
                mListener.onCancelled();
            }
            running = false;
        }

        private void getFile(File dir) {
            if (dir == null) {
                return;
            }

            File listFile[] = dir.listFiles();
            if (listFile != null && listFile.length > 0) {
                for (File file : listFile) {
                    if (file.isDirectory()) {
                        getFile(file);
                    } else {
                        String ext = ScanUtils.getExtension(file.getName());
                        mScanResults.addScanResult(file.getName(), file.length(), ext);
                    }
                }
            }
        }
    }

    public interface OnFileScanListener {
        void onScanning();
        void onScanResults(ScanResults results);
        void onCancelled();
        void showError();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
