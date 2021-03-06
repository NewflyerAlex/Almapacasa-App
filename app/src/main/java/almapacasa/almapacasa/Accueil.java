package almapacasa.almapacasa;

import android.content.Context;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.android.AndroidContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import Classes.Async;
import Classes.ExpandableListAdapter;
import Classes.MyBDD;
import almapacasa.ClassesMetier.Visite;

public class Accueil extends AppCompatActivity implements Async.AsyncResponse {

    ExpandableListView expListView;
    ExpandableListAdapter listAdapter;
    List<Visite> listVisites;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    MyBDD BDD;
    public static TextView lblDay;
    public static String rep;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);
        expListView = (ExpandableListView) findViewById(R.id.expandableListView);
        BDD = MyBDD.getmInstance(new AndroidContext(this));
        prepareDataVisite(getCurrentDayOfWeek());
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);
        lblDay = (TextView) findViewById(R.id.lblDayOfWeek);
        lblDay.setText(toFirstLetterUpperCase(getCurrentDayOfWeek()));
        lblDay.setPaintFlags(lblDay.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        Visite v = BDD.getUneVisite("15");

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                Visite visiteCliqued = listVisites.get(groupPosition);
                String idVisite = visiteCliqued.getId();
                return false;
                //TODO declarer l'intent de l'activity de modif de visite
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONArray lul = BDD.makeJSON();

                    ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo ntwrkInfo = connMgr.getActiveNetworkInfo();
                    if(ntwrkInfo != null && ntwrkInfo.isConnected())
                    {

                        Async asyncTask = (Async) new Async(new Async.AsyncResponse() {
                            @Override
                            public void processFinish(JSONArray output) {
                                rep = null;
                                if(output != null && output.length() > 0) {
                                    Log.d("UPLOAD", "Upload réussi");
                                    rep = "Upload réussi";
                                }
                                else
                                {
                                    Log.d("UPLOAD", "Upload raté");
                                    rep = "Upload raté";
                                }
                            }
                        }).execute("upload","a","a", lul.toString());
                    }else
                    {
                        rep = "Pas d'accès internet";
                    }
                }catch (CouchbaseLiteException e)
                {
                    e.printStackTrace();
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                Snackbar.make(view, rep, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public String getCurrentDayOfWeek()
    {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat df = new SimpleDateFormat("EEEE",Locale.FRANCE);
        String retour = df.format(date).toString();
        return retour;
    }

    public void changeDay(View V)
    {
        String value = V.getTag().toString();
        prepareDataVisite(value);
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);
        lblDay.setText(toFirstLetterUpperCase(V.getTag().toString()));
    }

    @NonNull
    private String toFirstLetterUpperCase(String day) {
        day = day.substring(0,1).toUpperCase() + day.substring(1);
        return day;
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("Top 250");
        listDataHeader.add("Now Showing");
        listDataHeader.add("Coming Soon..");

        // Adding child data
        List<String> top250 = new ArrayList<String>();
        top250.add("The Shawshank Redemption");
        top250.add("The Godfather");
        top250.add("The Godfather: Part II");
        top250.add("Pulp Fiction");
        top250.add("The Good, the Bad and the Ugly");
        top250.add("The Dark Knight");
        top250.add("12 Angry Men");

        List<String> nowShowing = new ArrayList<String>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");
        nowShowing.add("Turbo");
        nowShowing.add("Grown Ups 2");
        nowShowing.add("Red 2");
        nowShowing.add("The Wolverine");

        List<String> comingSoon = new ArrayList<String>();
        comingSoon.add("2 Guns");
        comingSoon.add("The Smurfs 2");
        comingSoon.add("The Spectacular Now");
        comingSoon.add("The Canyons");
        comingSoon.add("Europa Report");

        listDataChild.put(listDataHeader.get(0), top250); // Header, Child data
        listDataChild.put(listDataHeader.get(1), nowShowing);
        listDataChild.put(listDataHeader.get(2), comingSoon);
    }

    private void prepareDataVisite(String dayOfWeek){
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        listVisites = BDD.getVisites(dayOfWeek);
        sortVisites();
        SimpleDateFormat formatHeure = new SimpleDateFormat("kk:mm");
        for (Visite item: listVisites) {
            StringBuilder sb = new StringBuilder();
            sb.append("Patient: ").append(item.getPersonne().getNom()).append(" ").append(item.getPersonne().getPrenom()).append("\n");
            sb.append("Horaire: ").append(formatHeure.format(item.getHeureDebut())).append("   ").append(formatHeure.format(item.getHeureFin()));
            listDataHeader.add(sb.toString());
            int i = listDataHeader.indexOf(sb.toString());
            List<String> subList = new ArrayList<String>();
            sb = new StringBuilder();
            sb.append("Adresse: ").append(item.getPersonne().getAdresse()).append("\n");
            sb.append("Telephone: ").append(item.getPersonne().getNumero()).append("\n");
            subList.add(sb.toString());
            listDataChild.put(listDataHeader.get(i), subList);
        }
    }

    private void sortVisites() {
        Collections.sort(listVisites, new Comparator<Visite>() {
            @Override
            public int compare(Visite o1, Visite o2) {
                return o1.getHeureDebut().compareTo(o2.getHeureDebut());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accueil, menu);
        //lul
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void processFinish(JSONArray output) {

    }



}
