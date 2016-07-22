package pe.hikaru.stalker.activities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.http.Header;

import pe.hikaru.stalker.tools.ClienteHttp;
import pe.hikaru.stalker.tools.UsuariosSQLiteHelper;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button btnAndroidPost;
    Button btnAndroidGet;
    Button btnAndroidDesfase;
    Button btnAndroidHistorial;
    Button btnAndroidGps;
    TextView lblInfo;
    long webms;
    long androidms;
    boolean escucha = false;
    boolean escuchaidaccion = true;
    String idaccion = "0";
    short tipo = ClienteHttp.JAVA;
    String desfase = "";
    boolean http = false;
    String email_cliente;
    String horaandroid = "";
    String mensaje = "";
    LocationManager locManager;
    LocationListener locListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAndroidPost = (Button) findViewById(R.id.btnAndroidPost);
        btnAndroidGet = (Button) findViewById(R.id.btnAndroidGet);
        btnAndroidDesfase = (Button) findViewById(R.id.btnAndroidDesfase);
        btnAndroidHistorial = (Button) findViewById(R.id.btnAndroidHistorial);
        btnAndroidGps = (Button) findViewById(R.id.btnAndroidGps);
        lblInfo = (TextView) findViewById(R.id.lblInfo);
        email_cliente = obtenerCorreo();

        RequestParams params = new RequestParams();
        params.put("email_cliente", email_cliente);
        http = true;
        ClienteHttp.get(tipo, "drone?acc=androidgetid", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int status, Header[] cabecera, byte[] cuerpo) {
                idaccion = new String(cuerpo);
                http = false;
            }

            @Override
            public void onFailure(int status, Header[] cabecera, byte[] cuerpo, Throwable thr) {
                Toast.makeText(getApplicationContext(), "No se pudo obtener el último id " + status, Toast.LENGTH_SHORT).show();
                http = false;
            }
        });

        btnAndroidDesfase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (http)
                    Toast.makeText(getApplicationContext(), "Petición en proceso.", Toast.LENGTH_SHORT).show();
                else {
                    http = true;
                    ClienteHttp.get(tipo, "drone?acc=androidhora", null, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int status, Header[] cabecera, byte[] cuerpo) {
                            webms = Long.parseLong(new String(cuerpo));
                            androidms = (new Date()).getTime();
                            desfase = ((webms - androidms) / 1000) + "." + ((webms - androidms) % 1000);
                            Toast.makeText(getApplicationContext(), "Desfase calculado: " + desfase, Toast.LENGTH_SHORT).show();
                            http = false;
                        }

                        @Override
                        public void onFailure(int status, Header[] cabecera, byte[] cuerpo, Throwable thr) {
                            Toast.makeText(getApplicationContext(), "No se pudo calcular el desfase. Error " + status, Toast.LENGTH_SHORT).show();
                            http = false;
                        }
                    });
                }
            }
        });

        btnAndroidPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                horaandroid = (new SimpleDateFormat("HH:mm:ss.SSS", Locale.US)).format(new Date());
                if (desfase.equals(""))
                    Toast.makeText(getApplicationContext(), "Calcule primero el desfase", Toast.LENGTH_SHORT).show();
                else {
                    if (http)
                        Toast.makeText(getApplicationContext(), "Petición en proceso.", Toast.LENGTH_SHORT).show();
                    else {
                        RequestParams params = new RequestParams();
                        params.put("email_cliente", email_cliente);
                        params.put("horaandroid", horaandroid);
                        params.put("mensaje", "");
                        params.put("desfase", desfase);
                        http = true;
                        ClienteHttp.post(tipo, "drone?acc=androidpost", params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int status, Header[] cabecera, byte[] cuerpo) {
                                Toast.makeText(getApplicationContext(), "Información enviada", Toast.LENGTH_SHORT).show();
                                http = false;
                            }

                            @Override
                            public void onFailure(int status, Header[] cabecera, byte[] cuerpo, Throwable thr) {
                                System.out.println(new String(cuerpo));
                                Toast.makeText(getApplicationContext(), "No se pudo enviar la petición. Error " + status, Toast.LENGTH_SHORT).show();
                                http = false;
                            }
                        });
                    }
                }
            }
        });

        btnAndroidGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (escucha) {
                    btnAndroidGet.setText("Escuchar órdenes");
                    http = false;
                    escucha = false;
                    escuchaidaccion = true;
                } else if (desfase.equals(""))
                    Toast.makeText(getApplicationContext(), "Calcule primero el desfase", Toast.LENGTH_SHORT).show();
                else if (http)
                    Toast.makeText(getApplicationContext(), "Petición en proceso.", Toast.LENGTH_SHORT).show();
                else {
                    btnAndroidGet.setText("No escuchar");
                    http = true;
                    escucha = true;
                    start();
                }
            }
        });

        btnAndroidHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(getApplicationContext(), "stalker.sqlite", null, 1);
                SQLiteDatabase db = usdbh.getWritableDatabase();
                Cursor c = db.rawQuery("SELECT idenvio, horaandroid, desfase FROM drone_accion WHERE enviado = 0", null);
                String historial = "|";
                if (c.moveToFirst())
                    do {
                        historial += (c.getInt(0) + "," + c.getString(1) + "," + c.getString(2) + "|");
                    } while (c.moveToNext());
                db.close();
                if (!historial.equals("|")) {
                    if (http)
                        Toast.makeText(getApplicationContext(), "Petición en proceso.", Toast.LENGTH_SHORT).show();
                    else {
                        RequestParams params = new RequestParams();
                        params.put("email_cliente", email_cliente);
                        params.put("historial", historial);
                        http = true;
                        ClienteHttp.post(tipo, "drone?acc=androidhistorial", params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int status, Header[] cabecera, byte[] cuerpo) {
                                Toast.makeText(getApplicationContext(), "Historial enviado", Toast.LENGTH_SHORT).show();
                                http = false;
                            }

                            @Override
                            public void onFailure(int status, Header[] cabecera, byte[] cuerpo, Throwable thr) {
                                System.out.println(new String(cuerpo));
                                Toast.makeText(getApplicationContext(), "No se pudo enviar la petición. Error " + status, Toast.LENGTH_SHORT).show();
                                http = false;
                            }
                        });
                    }
                }
            }
        });

        btnAndroidGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mostrarPosicion(loc);

                // Nos registramos para recibir actualizaciones de la posición
                locListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        mostrarPosicion(location);
                    }

                    public void onProviderDisabled(String provider) {
                        System.out.println("Provider OFF");
                    }

                    public void onProviderEnabled(String provider) {
                        System.out.println("Provider ON ");
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {

                        System.out.println("Provider Status: " + status);
                    }
                };

                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locListener);
            }
        });

    }

    private void start() {
        if (escucha) {
            if (escuchaidaccion) {
                ClienteHttp.get(tipo, "drone?acc=androidgetid", null, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] cabecera, byte[] cuerpo) {
                        System.out.println((new String(cuerpo)) + " coco");
                        if (!idaccion.equals(new String(cuerpo))) {
                            idaccion = new String(cuerpo);
                            escuchaidaccion = false;
                        }
                    }

                    @Override
                    public void onFailure(int status, Header[] cabecera, byte[] cuerpo, Throwable thr) {
                        System.out.println("ERROR: " + new String(cuerpo));
                    }

                    @Override
                    public void onFinish() {
                        start();
                        super.onFinish();
                    }
                });
            } else {
                ClienteHttp.get(tipo, "drone?acc=androidget", null, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int status, Header[] cabecera, byte[] cuerpo) {
                        horaandroid = (new SimpleDateFormat("HH:mm:ss.SSS", Locale.US)).format(new Date());
                        registra(Integer.parseInt((new String(cuerpo)).split("\\|")[0]), (new String(cuerpo)).split("\\|")[1], horaandroid);
                        lblInfo.setText("ID: " + (new String(cuerpo)).split("\\|")[0] + "\n" + "Hora servidor: " + (new String(cuerpo)).split("\\|")[1] + "\n" + "Longitud recibida: " + (new String(cuerpo)).split("\\|")[2].length() + "\n" + "Hora dispositivo: " + horaandroid + "\n" + "Desfase calculado: " + desfase);
                        escuchaidaccion = true;
                    }

                    @Override
                    public void onFailure(int status, Header[] cabecera, byte[] cuerpo, Throwable thr) {
                        System.out.println("ERROR: " + new String(cuerpo));
                    }

                    @Override
                    public void onFinish() {
                        start();
                        super.onFinish();
                    }
                });
            }
        }
    }

    private final String obtenerCorreo() {
        try {
            Pattern emailPattern = Patterns.EMAIL_ADDRESS;
            Account[] accounts = AccountManager.get(getBaseContext()).getAccounts();
            for (Account account : accounts)
                if (emailPattern.matcher(account.name).matches())
                    return account.name;
        } catch (Exception e) {

        }
        return "anonimo";
    }

    private final void registra(int idaccion, String horaweb, String horaandroid) {
        UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(this, "stalker.sqlite", null, 1);
        SQLiteDatabase db = usdbh.getWritableDatabase();
        if (db != null) {
            db.execSQL("INSERT INTO drone_accion (idenvio, horaweb, horaandroid, desfase, enviado) VALUES (" + idaccion + ", '" + horaweb + "', '" + horaandroid + "', '" + desfase + "', 0)");
            db.close();
        }
    }

    private void mostrarPosicion(Location loc) {
        if (loc != null) {
            System.out.println("Latitud: " + String.valueOf(loc.getLatitude()));
            System.out.println("Longitud: " + String.valueOf(loc.getLongitude()));
            System.out.println("Precision: " + String.valueOf(loc.getAccuracy()));
            System.out.println(String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));
        } else {
            System.out.println("Latitud: (sin_datos)");
        }
    }

}

/*
 * REQUEST CON VOLLEY Response.Listener<String> listener = new
 * Response.Listener<String>() {
 * 
 * @Override public void onResponse(String response) {
 * System.out.println(response.toString()); http = false;
 * //L.d("Success Response: " + response.toString()); } };
 * Response.ErrorListener errorListener = new Response.ErrorListener() {
 * 
 * @Override public void onErrorResponse(VolleyError error) { if
 * (error.networkResponse != null) { //L.d("Error Response code: " +
 * error.networkResponse.statusCode); } } }; RequestQueue requestQueue =
 * Volley.newRequestQueue(getApplicationContext()); http = true; StringRequest
 * request = new StringRequest(Request.Method.GET,
 * "http://stalker.tegobijava.cloudbees.net/drone?acc=androidgetid", listener,
 * errorListener); requestQueue.add(request);
 */