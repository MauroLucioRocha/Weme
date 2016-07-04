package br.com.elede.mauro.weme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Festas extends AppCompatActivity implements ConsultaSituacaoEventos.ConsultaSituacaoEventosListener  {

    private String[] listaImagensEvento;
    private LinearLayout linearLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog = null;
    private TextView textFeed;

    private DialogInterface.OnClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_festas);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHome);
//        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.ic_launcher));
        toolbar.setTitle("Weme");
        final Display display = ((WindowManager) getSystemService(this.WINDOW_SERVICE)).getDefaultDisplay();

        progressDialog = ProgressDialog.show(this, "Aguarde!", "Carregando eventos...");

        textFeed = (TextView) findViewById(R.id.textofeed);
        linearLayout = (LinearLayout) findViewById(R.id.listaeventos);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(Color.BLUE);

        //Verifica se o dispositivo possiu conexão com a INTERNET
        if(verificaConexao(this)){
            new ConsultaSituacaoEventos(this).execute();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    linearLayout.removeAllViews();
                    TextView textView = new TextView(Festas.this);
                    textView.setText("Sem conexão com a Internet\n:(");
                    textView.setTypeface(null, Typeface.BOLD_ITALIC);
                    textView.setTextSize(18);
                    textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setPadding(0, (display.getHeight() - textView.getHeight()) / 4, 0, 0);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    linearLayout.addView(textView);
                    Toast.makeText(getApplicationContext(), "Verifique sua Conexão com a Internet!", Toast.LENGTH_SHORT).show();
                    progressDialog.cancel();
                }
            }, 3000);
        }

        //Listener para recarregar a pagina
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(verificaConexao(Festas.this)){
                    linearLayout.removeAllViews();
                    linearLayout.setClickable(false);
                    new ConsultaSituacaoEventos(Festas.this).execute();

                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            linearLayout.removeAllViews();
                            TextView textView = new TextView(Festas.this);
                            textView.setText("Sem conexão com a Internet\n:(");
                            textView.setTypeface(null, Typeface.BOLD_ITALIC);
                            textView.setTextSize(18);
                            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            textView.setPadding(0, (display.getHeight() - textView.getHeight()) / 4, 0, 0);
                            textView.setGravity(Gravity.CENTER_HORIZONTAL);
                            linearLayout.addView(textView);

                            Toast.makeText(getApplicationContext(), "Verifique sua Conexão com a Internet!", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 3000);

                }
            }
        });

        //listener para o Dialg de confirmacao de postagem da foto tirada
        listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {
                if (button == DialogInterface.BUTTON_POSITIVE) {
                    finish();
                } else if (button == DialogInterface.BUTTON_NEGATIVE) {

                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        // code here to show dialog
        AlertDialog.Builder alertD = new AlertDialog.Builder(this);
        alertD.setIcon(getResources().getDrawable(R.mipmap.ic_launcher));
        alertD.setTitle("Weme");
        alertD.setMessage("Deseja sair do Weme?");
        alertD.setPositiveButton("Sim", listener);
        alertD.setNegativeButton("Cancelar", listener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alertD.setIcon(getDrawable(R.drawable.marcaweme));
        }
        alertD.show();
    }

    @Override
    public void onConsultaConcluida(String situacao) {

        //separa cada evento com suas informacoes
        String[] listaEventos = situacao.split (Pattern.quote ("?"));
        ArrayList<String> eventos = new ArrayList<String>();
        for(int x = 0; x < listaEventos.length; x++){
            eventos.add(listaEventos[x]);
        }

        //prepara o array de imagens
        listaImagensEvento = new String[listaEventos.length];
        int indexOfImages = 0;
        //recebe cada informacao de um evento de cada vez e monta o View
        for(String eventoTemp : eventos){
            String[] temp = eventoTemp.split (Pattern.quote ("&"));

            //cria o layout para cada evento
            final LinearLayout layoutEvento = new LinearLayout(getApplicationContext());
            layoutEvento.setOrientation(LinearLayout.HORIZONTAL);
            layoutEvento.setGravity(Gravity.CENTER_VERTICAL);
            layoutEvento.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutEvento.setPadding(15, 10, 10, 10);

            //cria o layout para Informacoes do evento
            LinearLayout layoutInformacoesEvento = new LinearLayout(getApplicationContext());
            layoutInformacoesEvento.setOrientation(LinearLayout.VERTICAL);
            layoutInformacoesEvento.setGravity(Gravity.LEFT);
            layoutInformacoesEvento.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutInformacoesEvento.setPadding(10, 10, 10, 10);

            //instancia novos views para o evento atual
            final TextView txtEvento = new TextView(getApplicationContext());
            String idEvento = "";
            TextView txtAtracao = new TextView(getApplicationContext());
            TextView txtData = new TextView(getApplicationContext());
            TextView txtHora = new TextView(getApplicationContext());

            //recebe os as informacoes do servidor
            txtEvento.setText(temp[0]);
            txtEvento.setTextColor(Color.rgb(90,0,0));
            txtEvento.setTypeface(null, Typeface.BOLD);
            txtEvento.setTextSize(18);
            layoutInformacoesEvento.addView(txtEvento);

            //id do Evento atual
            idEvento = temp[1];
            listaImagensEvento[indexOfImages] = temp[2];

            //Atração/organizacao do evento
            txtAtracao.setText(temp[3]);
            txtAtracao.setTextColor(Color.rgb(128,128,128));
            layoutInformacoesEvento.addView(txtAtracao);

            //data evento
            txtData.setText(temp[4]);
            txtData.setTextColor(Color.rgb(128,128,128));
            layoutInformacoesEvento.addView(txtData);

            //hora do evento
            txtHora.setText(temp[5]);
            txtHora.setTextColor(Color.rgb(128,128,128));
            layoutInformacoesEvento.addView(txtHora);

            //imagem do evento
            ImageView imageViewEvento = new ImageView(this);
            imageViewEvento.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
            imageViewEvento.setId(indexOfImages++);
            //cantos arredondados
//            imageViewEvento.setBackgroundResource(R.drawable.roundcorner);
            imageViewEvento.setBackgroundResource(R.drawable.animation);
            AnimationDrawable animation = (AnimationDrawable) imageViewEvento.getBackground();
            animation.start();

            //adiciona evento no Layout Principal
            layoutEvento.addView(imageViewEvento);
            layoutEvento.addView(layoutInformacoesEvento);
            linearLayout.addView(layoutEvento);

            //Implementa o click para selecionar evento
            final String finalIdEvento = idEvento;
            layoutEvento.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Festas.this, Feed.class);
                    i.putExtra("NomeEvento", txtEvento.getText().toString());
                    i.putExtra("idEvento", finalIdEvento);
                    startActivity(i);
                }
            });
        }

        if (progressDialog != null || swipeRefreshLayout.isRefreshing()){
            progressDialog.cancel();
            swipeRefreshLayout.setRefreshing(false);
        }

        //Faz o download da imagem do evento
        DownloadFoto downloadFoto = new DownloadFoto();
        downloadFoto.execute();
        linearLayout.setClickable(true);
    }

    //classe para fazer download das imagens
    public class DownloadFoto extends AsyncTask<Void, Void, Bitmap[]> {

        Bitmap bitmap = null, temp = null;
        Bitmap[] imagens = new Bitmap[listaImagensEvento.length];
        ImageView imageView = null;

        @Override
        protected Bitmap[] doInBackground(Void... params) {

            //armazena os bitmaps temporarios das imagenso dos eventos
            for(int i = 0; i < listaImagensEvento.length; i++){
                try{
                    //download imagem
                    temp = Picasso.with(Festas.this).load(listaImagensEvento[i]).resize(150, 150).get();

                    //cantos arredondados
                    bitmap = getRoundCornerBitmap(temp, 20);

                    //armazena em um array temporario
                    imagens[i] = bitmap;
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap[] aVoid){

            //seta na view as imagens dos eventos
            for(int i = 0; i < listaImagensEvento.length; i++){
                try{
                    //encontra view pelo id setado anteriormente
                    imageView = (ImageView) findViewById(i);

                    //atribui bitmap ao imageView correspondente
                    imageView.setImageBitmap(imagens[i]);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            super.onPostExecute(aVoid);
        }
    }

    public static Bitmap getRoundCornerBitmap(Bitmap bitmap, int radius) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF rectF = new RectF(0, 0, w, h);

        canvas.drawRoundRect(rectF, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rectF, paint);

        /**
         * here to define your corners, this is for left bottom and right bottom corners
         */
        final Rect clipRect = new Rect(0 + radius, 0 + radius, w - radius, h - radius);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        canvas.drawRect(clipRect, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rectF, paint);

        bitmap.recycle();

        return output;
    }

    public boolean verificaConexao(Context contexto){
        ConnectivityManager cm = (ConnectivityManager) contexto.getSystemService(Context.CONNECTIVITY_SERVICE);//Pego a conectividade do contexto o qual o metodo foi chamado
        NetworkInfo netInfo = cm.getActiveNetworkInfo();//Crio o objeto netInfo que recebe as informacoes da NEtwork
        if ( (netInfo != null) && (netInfo.isConnectedOrConnecting()) && (netInfo.isAvailable()) ) //Se o objeto for nulo ou nao tem conectividade retorna false
            return true;
        else
            return false;
    }
}
