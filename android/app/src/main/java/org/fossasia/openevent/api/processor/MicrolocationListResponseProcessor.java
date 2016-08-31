package org.fossasia.openevent.api.processor;

import org.fossasia.openevent.OpenEventApp;
import org.fossasia.openevent.data.Microlocation;
import org.fossasia.openevent.dbutils.DbContract;
import org.fossasia.openevent.dbutils.DbSingleton;
import org.fossasia.openevent.events.MicrolocationDownloadEvent;
import org.fossasia.openevent.utils.CommonTaskLoop;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by MananWason on 27-05-2015.
 */
public class MicrolocationListResponseProcessor implements Callback<List<Microlocation>> {

    ArrayList<String> queries = new ArrayList<>();

    @Override
    public void onResponse(Call<List<Microlocation>> call, final Response<List<Microlocation>> response) {
        if (response.isSuccessful()) {
            CommonTaskLoop.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    for (Microlocation microlocation : response.body())

                    {
                        String query = microlocation.generateSql();
                        queries.add(query);
                        Timber.d(query);
                    }
                    DbSingleton dbSingleton = DbSingleton.getInstance();

                    dbSingleton.clearTable(DbContract.Microlocation.TABLE_NAME);
                    dbSingleton.insertQueries(queries);

                    OpenEventApp.postEventOnUIThread(new MicrolocationDownloadEvent(true));
                }
            });
        } else {
            OpenEventApp.getEventBus().post(new MicrolocationDownloadEvent(false));
        }
    }

    @Override
    public void onFailure(Call<List<Microlocation>> call, Throwable t) {
        OpenEventApp.getEventBus().post(new MicrolocationDownloadEvent(false));
    }
}