package pe.hikaru.stalker.tools;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ClienteHttp {

    private static final String BASE_URL_JAVA = "http://stalkerweb.tegobijava.cloudbees.net/";
    private static final String BASE_URL_PHP = "";
    private static final String BASE_URL_GCM = "";
    public static final short JAVA = 1;
    public static final short PHP = 2;
    public static final short GCM = 3;

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(short tipo, String accion, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(tipo, accion), params, responseHandler);
    }

    public static void post(short tipo, String accion, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(tipo, accion), params, responseHandler);
    }

    private static String getAbsoluteUrl(short tipo, String accion) {
        return (tipo == JAVA ? BASE_URL_JAVA : (tipo == PHP ? BASE_URL_PHP : (tipo == GCM ? BASE_URL_GCM : ""))) + accion;
    }

}
