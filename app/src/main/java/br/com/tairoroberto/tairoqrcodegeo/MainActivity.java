package br.com.tairoroberto.tairoqrcodegeo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.zxing.client.android.CaptureActivity;

import java.util.ArrayList;
import java.util.List;

import br.com.tairoroberto.tairoqrcodegeo.adapters.RegistroAdapter;
import br.com.tairoroberto.tairoqrcodegeo.database.RegistroDAO;
import br.com.tairoroberto.tairoqrcodegeo.domain.Registro;
import br.com.tairoroberto.tairoqrcodegeo.interfaces.RecyclerViewOnClickListenerHack;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RecyclerViewOnClickListenerHack {

    private static final String TAG = "Script";
    private static final int ENABLED_GPS = 1;
    private static final int BARCODE_SCANNER = 2;
    private GoogleApiClient googleApiClient;
    private TextView text;
    private LocationRequest locationRequest;
    private double latitude;
    private double longitude;

    /**
      keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     * */

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Registro> registros = new ArrayList<>();
    private RegistroDAO registroDAO;
    private RegistroAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState != null){
            registros = savedInstanceState.getParcelableArrayList("registros");
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        /** Atualiza a lista de registros quando puchar o swipe */
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                registroDAO = new RegistroDAO(MainActivity.this);
                registros = registroDAO.getAll();
                adapter = new RegistroAdapter(MainActivity.this, registros, getFragmentManager());
                mRecyclerView.setAdapter(adapter);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        mRecyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(MainActivity.this, mRecyclerView, this));

        LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        /** Busca os dados dos registros */
        registroDAO = new RegistroDAO(MainActivity.this);
        registros = registroDAO.getAll();
        adapter = new RegistroAdapter(MainActivity.this, registros, getFragmentManager());
        mRecyclerView.setAdapter(adapter);

        gpsVerify();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        text = (TextView) findViewById(R.id.text);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (gpsVerify()) {
                        callConnection();
                        Intent intent = new Intent(getApplicationContext(),CaptureActivity.class);
                        intent.setAction("com.google.zxing.client.android.SCAN");
                        intent.putExtra("PROMPT_MESSAGE", "Leia um QRCODE");
                        intent.putExtra("SAVE_HISTORY", false);
                        startActivityForResult(intent, BARCODE_SCANNER);
                    }
                }
            });
        }
    }

    private boolean gpsVerify() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!service.isProviderEnabled(LocationManager.GPS_PROVIDER )) {
            Toast.makeText(MainActivity.this, "Habilite o GPS!", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public void onPause(){
        super.onPause();

        if(googleApiClient != null){
            stopLocationUpdate();
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /* Pega o resultado para a verificação do QRCODE */
        if (requestCode == BARCODE_SCANNER) {
            if (resultCode == RESULT_OK) {
                String content = data.getStringExtra("SCAN_RESULT");

                text.setText(Html.fromHtml("Latitude: " + latitude + " | Longitude: " + longitude + "<br> QRCODE: " + content));

                Registro registro = new Registro();
                RegistroDAO registroDAO = new RegistroDAO(MainActivity.this);

                registro.setContent(content);
                registro.setLatitude(latitude);
                registro.setLongitude(longitude);
                registro.setType("img");
                registroDAO.insert(registro);

                registros.add(registro);
                mRecyclerView.getAdapter().notifyDataSetChanged();
                stopLocationUpdate();

            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "Scanner cancelado");
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

            final Dialog dialog = new Dialog(MainActivity.this); // Context, this, etc.
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.info_alert_dialog);
            dialog.show();

            Button btOK = (Button) dialog.findViewById(R.id.dialog_ok);
            final TextView textView = (TextView) dialog.findViewById(R.id.dialog_info);
            textView.setText(Html.fromHtml("<h1>Tairo Roberto Miguel De Assunção</h1>" +
                    "<br><b>Fone: </b>(11)95297-9157" +
                    "<br><b>Email: </b>tairoroberto@hotmail.com" +
                    "<br><b>Github: </b>https://github.com/tairoroberto "));

            btOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Inicializa os parametros */
    private void initLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    /* Inicia o serviço de atualização de coordenadas */
    private void startLocationUpdate() {
        initLocationRequest();
        /* Verificação de permissão para SDK > 23 */
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    /* Para o serviço de atualização de coordenadas */
    private void stopLocationUpdate(){
        if (googleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    /* Inicializa o googleApiClient */
    public synchronized void callConnection() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        /* Verificação de permissão para SDK > 23 */
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        /* Pega as coordenadas assim que conecta */
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        /* Atualiza as coordenadas */
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Log.i(TAG, "latitude: " + latitude);
        Log.i(TAG, "longitude: " + longitude);
    }


    @Override
    public void onClickListener(View view, int position) {
        //Toast.makeText(MainActivity.this, "Posição: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongPressClickListener(View view, int position) {
        //Toast.makeText(MainActivity.this, "onLongPressClickListener(): " + position, Toast.LENGTH_SHORT).show();
    }



    /** Classe para click em recyclerview */
    private static class RecyclerViewTouchListener implements RecyclerView.OnItemTouchListener {
        private Context mContext;
        private GestureDetector mGestureDetector;
        private RecyclerViewOnClickListenerHack mRecyclerViewOnClickListenerHack;

        public RecyclerViewTouchListener(Context c, final RecyclerView rv, RecyclerViewOnClickListenerHack rvoclh){
            mContext = c;
            mRecyclerViewOnClickListenerHack = rvoclh;

            mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);

                    View cv = rv.findChildViewUnder(e.getX(), e.getY());

                    if(cv != null && mRecyclerViewOnClickListenerHack != null){
                        mRecyclerViewOnClickListenerHack.onLongPressClickListener(cv,
                                rv.getChildAdapterPosition(cv) );
                    }
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    View cv = rv.findChildViewUnder(e.getX(), e.getY());

                    if(cv != null && mRecyclerViewOnClickListenerHack != null){
                        mRecyclerViewOnClickListenerHack.onClickListener(cv,
                                rv.getChildAdapterPosition(cv) );
                    }

                    return(true);
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            mGestureDetector.onTouchEvent(e);
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
