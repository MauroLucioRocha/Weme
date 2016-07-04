package br.com.elede.mauro.weme;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Mauro on 22/06/2016.
 */
public class ConsultaSituacaoFeed extends AsyncTask<Void, Void, String> {

    private ConsultaSituacaoEventosListener listener;
    private static final String URL_EVENTOS_SERVIDOR = "http://104.236.28.174/agenda/getAgendaApp/"; //http://104.236.28.174/albuns/getobjalbuns
    private static final String URL_IMAGENS_SERVIDOR = "http://104.236.28.174/images/uploads/";
    private String eventos = "";
    private String atracaoEvento = "";
    private String dataEvento = "";
    private String horaEvento = "";
    private String imagemEvento = "";
    private String nomeEvento = "";
    private String temp = "";

    public ConsultaSituacaoFeed(ConsultaSituacaoEventosListener listener){
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... params){
        try {
            String resultado = consultaServidor(URL_EVENTOS_SERVIDOR);

            try {
                temp = interpretaResultado(resultado);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    private String interpretaResultado(String resultado) throws JSONException {

        JSONObject jsonObject = new JSONObject(resultado);

        JSONArray jsonArray = jsonObject.getJSONArray("dados");

        JSONObject jsonObjectEvent;

        for( int x = 0; x < jsonArray.length(); x++){
            jsonObjectEvent = jsonArray.getJSONObject(x);

            nomeEvento = jsonObjectEvent.getString("evento") + "&";
            imagemEvento = URL_IMAGENS_SERVIDOR + jsonObjectEvent.getString("imagem") + "&";
            atracaoEvento = jsonObjectEvent.getString("atracao") + "&";
            dataEvento = jsonObjectEvent.getString("data") + "&";
            horaEvento = jsonObjectEvent.getString("horaInicio") + (x == jsonArray.length() - 1 ? "" : "?");

            eventos += nomeEvento + imagemEvento + atracaoEvento + dataEvento + horaEvento;
        }
        return eventos;
    }

    private String consultaServidor(String urlServidor) throws IOException{
        InputStream is = null;
        try{
            URL url = new URL(urlServidor);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(20000);
            conn.setConnectTimeout(25000);
            conn.setRequestMethod("GET");
            conn.connect();

            conn.getResponseCode();

            is = conn.getInputStream();

            Reader reader = null;
            reader = new InputStreamReader(is);
            char[] buffer = new char[2048];
            reader.read(buffer);
            return new String(buffer);

        } catch (Exception e){

        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result){
        listener.onConsultaConcluida(result);
    }

    public interface ConsultaSituacaoEventosListener{
        void onConsultaConcluida(String situacao);
    }
}
