package br.lavid.pamin.com.pamin.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Collections;
import java.util.LinkedList;

import br.lavid.pamin.com.pamin.data.CulturalRegisterDAO;
import br.lavid.pamin.com.pamin.models.CulturalRegister;
import br.lavid.pamin.com.pamin.utils.PaminAPI;

/**
 * Created by araujojordan on 02/07/15.
 */
public class SyncPamimData {

    private CulturalRegisterDAO localDB;
    private PaminAPI paminAPI;
    private Context ctx;

    public SyncPamimData(Context ctx) {
        this.ctx = ctx;
        localDB = new CulturalRegisterDAO(ctx);
        paminAPI = new PaminAPI();
    }

    public LinkedList<CulturalRegister> getOffList() {
        LinkedList<CulturalRegister> list = localDB.getAll();
        Collections.reverse(list);
        return list;
    }

    public LinkedList<CulturalRegister> getAll(UpdateList updateList) {

        LinkedList<CulturalRegister> list = localDB.getAll();
        Collections.reverse(list);
        new GetAllAsync(list, updateList).execute();
        return list;
    }

    public interface UpdateList {
        void updateList();
    }

    private class GetAllAsync extends AsyncTask<Void, Void, Void> {

        private LinkedList<CulturalRegister> offlineList;
        private UpdateList updateList;

        public GetAllAsync(LinkedList<CulturalRegister> offlineList, UpdateList updateList) {
            this.offlineList = offlineList;
            this.updateList = updateList;
        }

        @Override
        protected Void doInBackground(Void... params) {

            //ADD PAMINAPI NEW ELEMENTS
            Log.v("Sync", "Add new Elements");
            paminAPI.getAll(new PaminAPI.GetAllCallback() {
                @Override
                public void JsonReturn(LinkedList<CulturalRegister> onlineList) {
                    Next_Online:
                    for (CulturalRegister onlineCultReg : onlineList) {
                        for (CulturalRegister offlineCultReg : offlineList) {
                            if (onlineCultReg.isTheSame(offlineCultReg))
                                continue Next_Online;
                        }

                        try {
                            localDB.save(onlineCultReg);
                        } catch (Exception e) {
                            Log.e("DB_ERROR", e.getMessage());
                        }
                    }
                    updateList.updateList();

                    //DELETE OLD ELEMENTS
                    Log.v("Sync", "Delete old elements");
                    Next_Offline:
                    for (CulturalRegister offlineCultReg : offlineList) {
                        for (CulturalRegister onlineCultReg : onlineList) {
                            if (offlineCultReg.isTheSame(onlineCultReg))
                                continue Next_Offline;
                        }
                        localDB.delete(offlineCultReg);
                    }
                    updateList.updateList();

                }
            }, ctx);


            return null;
        }
    }
}
