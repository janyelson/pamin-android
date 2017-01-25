package br.lavid.pamin.com.pamin.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import br.lavid.pamin.com.pamin.R;
import br.lavid.pamin.com.pamin.activities.MainActivity;
import br.lavid.pamin.com.pamin.adapter.MainListAdapter;


public class MainListFragment extends android.support.v4.app.Fragment {

    private ListView listView;
    private MainListAdapter mainListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_list, container, false);

        listView = (ListView) view.findViewById(R.id.mainListView);

        ViewGroup parentGroup = (ViewGroup) listView.getParent();
        View empty = inflater.inflate(R.layout.loading_screen, parentGroup, false);
        parentGroup.addView(empty);
        listView.setEmptyView(empty);

        mainListAdapter = new MainListAdapter(getActivity());
        listView.setAdapter(mainListAdapter);

        new WaitAsync().execute();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setCategory(String name) {
        mainListAdapter.setCategory(name);
    }
    public void searchEvents(String name) {

        mainListAdapter.searchEvents(name);
    }

    /**
     * Update the list, called on MainActivity
     */
    public void updateList() {
        try {
            mainListAdapter.updateList(MainActivity.getActCulturalRegisters());
        } catch (NullPointerException npe) {
            Log.e("MainListFrag", "Cant update");
        }
    }

    /**
     * This will prevent add new elements on update
     */
    private class WaitAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (MainActivity.getActCulturalRegisters() == null) {
                try {
                    Thread.sleep(1000);
                    Log.v("Loading", "List is loading");
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.v("Complete", "List is loaded!");
            mainListAdapter.loadList(
                    MainActivity.getActCulturalRegisters());
        }
    }
}
