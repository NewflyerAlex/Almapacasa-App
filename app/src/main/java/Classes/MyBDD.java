package Classes;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Alexandre on 02/04/2016.
 */
public class MyBDD {

    private static MyBDD mInstance = null;
    public static final String DB_NAME = "almapacasa";
    public static final String TAG = "almapacasa-db";
    private static Manager myManager = null;
    private static Database myDatabase = null;
    private static View infirmiereView;

    public MyBDD (AndroidContext TheContext)
    {
        try{
            myManager = new Manager(TheContext, Manager.DEFAULT_OPTIONS);
            myDatabase = myManager.getDatabase(DB_NAME);
            android.util.Log.e(TAG, "Database is created");
        }catch (Exception e)
        {
            android.util.Log.e(TAG, "Error getting the database", e);
        }

        infirmiereView = myDatabase.getView("infirmiere");
        infirmiereView.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                if(document.get("type").equals("INFIRMIERE")) {
                    String id = (String) document.get("id");
                    emitter.emit("id",id);
                }
            }
        }, "1");
    }

    public static MyBDD getmInstance(AndroidContext TheContext){
        if(mInstance == null)
        {
            mInstance = new MyBDD(TheContext);
        }
        return mInstance;
    }

    public boolean saveLogin(JSONArray leLogin)
    {
        Document document = myDatabase.createDocument();
        String documentId = document.getId();
        Map<String, Object> map = new HashMap<String, Object>();
        String id, nom, identifiant, mdp;
        try
        {
            JSONObject Infirmière = leLogin.getJSONObject(0);
            id = Infirmière.getString("ID");
            nom = Infirmière.getString("NOM");
            identifiant = Infirmière.getString("LOGIN");
            mdp = Infirmière.getString("MDP");
        }catch(JSONException e)
        {
            e.printStackTrace();
            return false;
        }
        map.put("type", "INFIRMIERE");
        map.put("id", id);
        map.put("nom", nom);
        map.put("identifiant", identifiant);
        map.put("mdp", mdp);
        try {
            //On sauve dans le doc
            document.putProperties(map);
        }catch (CouchbaseLiteException e)
        {
            android.util.Log.e(TAG, "Error putting", e);
            return false;
        }

        return true;

    }

    public boolean autoLogin() {
        Query query = infirmiereView.createQuery();
        //query.setStartKey();

        try {
            QueryEnumerator result = query.run();
            //for (Iterator<QueryRow> it = result; it.hasNext(); ) {
            //    QueryRow row = it.next();
            //    android.util.Log.e(TAG, "yo");
            //    com.couchbase.lite.util.Log.w(TAG, "doc named %s value = %s", row.getKey(), row.getValue());
            //}
            int log = result.getCount();
            Log.e(TAG, String.valueOf(log));
            if(log == 1)
            {
                return true;
            }
        }catch (CouchbaseLiteException e)
        {
            e.printStackTrace();
            android.util.Log.e(TAG, "you");
        }
        return false;
    }

    public void emptyBDD() {
        Query query = infirmiereView.createQuery();
        query.setLimit(1);
        try {
            QueryEnumerator result = query.run();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                    QueryRow row = it.next();
                    android.util.Log.e(TAG, "yo");
                    com.couchbase.lite.util.Log.w(TAG, "doc named %s value = %s", row.getKey(), row.getValue());
                    row.getDocument().delete();
                }
        }catch (CouchbaseLiteException e)
        {
            e.printStackTrace();
        }
    }

}
