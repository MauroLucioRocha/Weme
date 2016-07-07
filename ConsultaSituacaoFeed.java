package br.com.elede.mauro.weme;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by Mauro on 22/06/2016.
 */
public class ConsultaSituacaoFeed extends AsyncTask<Void, Void, String> {

    HttpURLConnection conn;
    StringBuilder sb = new StringBuilder();

    private ConsultaSituacaoFeedListener listener;
    private static final String URL_EVENTOS_SERVIDOR = "http://104.236.28.174/agenda/getAgendaApp/";
    private static final String URL_IMAGENS_EVENTOS_SERVIDOR = "http://104.236.28.174/images/uploads/";
    private static final String URL_FEED_SERVIDOR = "http://104.236.28.174/feed/posts";

    private String eventos = "";
    private String atracaoEvento = "";
    private String dataEvento = "";
    private String horaEvento = "";
    private String imagemEvento = "";
    private String nomeEvento = "";
    private String temp = "";
    private String idFesta = "";

    public ConsultaSituacaoFeed(ConsultaSituacaoFeedListener listener, String idFesta){
        this.listener = listener;
        this.idFesta = idFesta;
    }

    @Override
    protected String doInBackground(Void... params){
        try {
            String resultado = consultaServidor(idFesta);

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

//        JSONObject jsonObject = new JSONObject(resultado);
//
//        JSONArray jsonArray = jsonObject.getJSONArray("dados");
//
//        JSONObject jsonObjectEvent;
//
//        for( int x = 0; x < jsonArray.length(); x++){
//            jsonObjectEvent = jsonArray.getJSONObject(x);
//
//            nomeEvento = jsonObjectEvent.getString("evento") + "&";
//            imagemEvento = URL_IMAGENS_EVENTOS_SERVIDOR + jsonObjectEvent.getString("imagem") + "&";
//            atracaoEvento = jsonObjectEvent.getString("atracao") + "&";
//            dataEvento = jsonObjectEvent.getString("data") + "&";
//            horaEvento = jsonObjectEvent.getString("horaInicio") + (x == jsonArray.length() - 1 ? "" : "?");
//
//            eventos += nomeEvento + imagemEvento + atracaoEvento + dataEvento + horaEvento;
//        }
//        return eventos;
        return resultado;
    }

    private String consultaServidor(String idFesta) throws IOException{

        try {

//            URL url = new URL(URL_FEED_SERVIDOR);
//
//            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
//            String urlParameters = "fizz=buzz";
//            connection.setRequestMethod("GET");
//
//            connection.setRequestProperty("Accept", "application/json");
//            connection.setRequestProperty("Content-type", "application/json");
//
//            int responseCode = connection.getResponseCode();
//
//            final StringBuilder output = new StringBuilder("Request URL " + url);
//            output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
//            output.append(System.getProperty("line.separator") + "Type " + "GET");
//            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String line = "";
//            sb = new StringBuilder();
//            System.out.println("output===============" + br);
//            while((line = br.readLine()) != null ) {
//                sb.append(line);
//            }
//            br.close();
//
//            output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + sb.toString());
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return sb.toString();

            URL url = new URL(URL_FEED_SERVIDOR);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("Host", URL_FEED_SERVIDOR);
            conn.setDoInput(true);

            //Create JSONObject here
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("idFesta", idFesta);
            OutputStreamWriter out = new   OutputStreamWriter(conn.getOutputStream());
            out.write(jsonParam.toString());
            out.close();

            conn.connect();
            int HttpResult = conn.getResponseCode();
            if(HttpResult == HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(),"utf-8"));
                String line = null;

                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                System.out.println(""+sb.toString());

            }else{
                System.out.println(conn.getResponseMessage());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }finally{
            if(conn != null)
                conn.disconnect();
        }

        return sb.toString();

//        InputStream is = null;
//        try{
//            URL url = new URL(URL_FEED_SERVIDOR);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setReadTimeout(10000);
//            conn.setConnectTimeout(15000);
//            conn.setRequestMethod("GET");
//            conn.connect();
//
//
//            conn.getResponseCode();
//            is = conn.getInputStream();
//
//            Reader reader = null;
//            reader = new InputStreamReader(is);
//            char[] buffer = new char[2048];
//            reader.read(buffer);
//            return new String(buffer);
//
//        } catch (Exception e){
//
//        } finally {
//            if (is != null) {
//                is.close();
//            }
//        }
//        return null;

    }

    @Override
    protected void onPostExecute(String result){
        listener.onConsultaConcluida(result);
    }

    public interface ConsultaSituacaoFeedListener{
        void onConsultaConcluida(String situacao);
    }
}
