package com.android.samplesmartpen.fragments;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.samplesmartpen.KMEActivity;
import com.android.samplesmartpen.MainActivity;
import com.android.samplesmartpen.R;
import com.android.samplesmartpen.adapters.CategoryAdapter;
import com.android.samplesmartpen.adapters.LevelAdapter;
import com.android.samplesmartpen.adapters.SetsAdapter;
import com.android.samplesmartpen.models.Category;
import com.android.samplesmartpen.models.Level;
import com.android.samplesmartpen.models.Set;
import com.android.samplesmartpen.parsers.CategoryHandler;
import com.android.samplesmartpen.parsers.XMLParser;
import com.android.samplesmartpen.services.NeoSampleService;
import com.android.samplesmartpen.utilities.Const;
import com.android.samplesmartpen.utilities.Util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainFragment";
    private static final int REQUEST_ENABLE_BT = 1;

    //
    private GridView mlvCategories;

    private static ArrayList<Category> falCategories;
    private static CategoryAdapter adapter;

    private ArrayList<Level> falLevel;
    private LevelAdapter falLevelAdapter;

    private ArrayList<Set> falSets;
    private SetsAdapter falSetsAdapter;

    private int ListCounter = 0;
    private int pos[] = {0, 0};

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        initViews(view);

        Intent oIntent = new Intent();
        oIntent.setClass(getContext(), NeoSampleService.class);
        getActivity().startService(oIntent);

        ((MainActivity) getActivity()).setOnBackPressed(() -> {
            if (ListCounter == 2) {
                ListCounter--;
                falLevel = falCategories.get(pos[0]).getFalLevels();
                falLevelAdapter = new LevelAdapter(getContext(), falLevel);
                falLevelAdapter.notifyDataSetChanged();
                mlvCategories.setAdapter(falLevelAdapter);
            } else if (ListCounter == 1) {
                ListCounter--;
                adapter = new CategoryAdapter(getContext(), falCategories);
                mlvCategories.setAdapter(adapter);
            } else {
                getActivity().finish();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Intent oIntent = new Intent();
        oIntent.setClass(getContext(), NeoSampleService.class);
        getActivity().stopService(oIntent);

//        if (penClientCtrl != null)
//            penClientCtrl.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect_bluetooth:
                BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

                if (mBtAdapter == null) {
                    Util.showToast(getContext(), "Bluetooth is not supported in this device.");
                } else {
                    if (!mBtAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
//                    else {
//                        Navigation.findNavController(Objects.requireNonNull(getView())).navigate(R.id.toBluetoothFragment);
//                    }
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
//                if (resultCode == Activity.RESULT_OK) {
//                    Navigation.findNavController(Objects.requireNonNull(getView())).navigate(R.id.toBluetoothFragment);
//                }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (ListCounter == 0) {
            ListCounter++;
            pos[0] = position;
            falLevel = falCategories.get(pos[0]).getFalLevels();
            falLevelAdapter = new LevelAdapter(getContext(), falLevel);
            mlvCategories.setAdapter(falLevelAdapter);
        } else if (ListCounter == 1) {
            ListCounter++;
            pos[1] = position;
            falSets = falLevel.get(pos[1]).getFalSets();
            falSetsAdapter = new SetsAdapter(getContext(), falSets);
            mlvCategories.setAdapter(falSetsAdapter);
        } else if (ListCounter == 2) {
            Const.SetID = falSets.get(position).getFiId();
            Const.CategoryID = falSets.get(position).getFiPId();

            Intent intent = new Intent(getActivity(), KMEActivity.class);
            startActivity(intent);
        }
    }

    private void initViews(View view) {
        mlvCategories = view.findViewById(R.id.lvCategories);

        initVariables();
    }

    private void initVariables() {
        falCategories = new ArrayList<>();
        adapter = new CategoryAdapter(getContext(), falCategories);
        mlvCategories.setAdapter(adapter);
        mlvCategories.setOnItemClickListener(this);
        new getCategoriesClass().execute();
    }

    private static class getCategoriesClass extends AsyncTask<Void, Category, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                XMLParser xmlParser = new XMLParser();
                CategoryHandler categoryHandler = new CategoryHandler();
                categoryHandler.setOnCategoryParsed(category -> publishProgress(category));

                xmlParser.ParseXML(new URL(Const.PHP_CATEGORY_LIST), categoryHandler);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Category... values) {
            super.onProgressUpdate(values[0]);
            falCategories.add(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
        }
    }
}
