package br.com.elede.mauro.weme;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.facebook.FacebookSdk;
import com.squareup.picasso.Picasso;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class Feed extends AppCompatActivity {

    //variaveis do facebook
    private CallbackManager callbackManager;
    private com.facebook.Profile profile = null;
    private Uri fotoPerfilFacebook;
    private ImageView imgPerfil, imgPerfilDialg;
    private Resources res;
    private Dialog dialogFB;
    private TextView nameFBDialg;
    private LinearLayout llDialog;
    private DataBaseConnection dbWeme;
    private Usuario usuario;
    private String imgPerfilByte;
    private AccessToken accessToken;
    private  AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    private static final int CAPTURAR_IMAGEM = 1;
    private final int REQUEST_PERMISSIONS = 1010;

    private ImageView ivVazio;
    private LinearLayout layoutFeed;
    private DialogInterface.OnClickListener listener, listenerPermissions;
    private Bitmap imageBitmap;
    private ScrollView sv;
    private SwipeRefreshLayout swr;
    private int scrollTo;
    private TextView pegartamanhoTexto;
    private File file;
    private Uri caminhoImagemMemoria;
    private Display display;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //facebook
        FacebookSdk.sdkInitialize ( getApplicationContext ());

        AppEventsLogger.activateApp ( this );
        res = getResources(); // need this to fetch the drawable

        setContentView(R.layout.activity_feed);

        display = ((WindowManager) getSystemService(this.WINDOW_SERVICE)).getDefaultDisplay();

        //referencia os views da activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //disponibiliza o botao voltar para a tela anterior. QUE ESTÁ SETADA DENTRO DO ANDROID MANIFEST DESTA ACTIVITY
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layoutFeed = (LinearLayout) findViewById(R.id.feed);
        ivVazio = (ImageView) findViewById(R.id.ivVazio);
        imgPerfil = (ImageView) findViewById(R.id.imgperfil);
        sv = (ScrollView) findViewById(R.id.sv);
        pegartamanhoTexto = (TextView) findViewById(R.id.textofeed);
        swr = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_feed);
        swr.setColorSchemeColors(Color.BLUE);

        swr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                layoutFeed.removeAllViews();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        carregaFeed();
                        swr.setRefreshing(false);
                    }
                }, 500);
            }
        });


        iniciaDialog();

        Bitmap temp = BitmapFactory.decodeResource(getResources(), R.drawable.logindesconhecido);
        Bitmap bitmap = getRoundCornerBitmap(temp, 90);
        imgPerfil.setImageBitmap(bitmap);
        imgPerfilDialg.setImageBitmap(bitmap);


        imgPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFB.show();
            }
        });

        //recebe o nome do evento selecionado
        Bundle bundle = getIntent().getExtras();
        toolbar.setTitle(bundle.getString("NomeEvento"));

        /*
         * Para pegar o id do evento usar o método:
         *
         * bundle.getString("idEvento")
         *
         * Depois podemos enviar para o servidor
         *
         */

        setSupportActionBar(toolbar);

        //listener para o Dialg de confirmacao de postagem da foto tirada
        listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {
                if (button == DialogInterface.BUTTON_POSITIVE) {

                    //armazena a foto no Banco de Dados
                    dbWeme.inserirImagem(new ImageFeed(usuario.getNome_fb(), bitMapToString(imageBitmap), 1, 35));

                    //cria um novo layout para a foto
                    layoutFeed.removeView(ivVazio);
                    layoutFeed.setGravity(Gravity.CENTER_HORIZONTAL);

                    //icone do facebook
                    ImageView facebook = new ImageView(getApplicationContext());
                    Bitmap bitmapIconFacebook = BitmapFactory.decodeResource(getResources(), R.drawable.facebook);
                    bitmapIconFacebook = Bitmap.createScaledBitmap(bitmapIconFacebook, 32, 32, true);
                    facebook.setImageBitmap(bitmapIconFacebook);

                    //cria layout para o nome usuário facebook
                    LinearLayout nomeUsuario = new LinearLayout(getApplicationContext());
                    nomeUsuario.setOrientation(LinearLayout.HORIZONTAL);
                    nomeUsuario.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    nomeUsuario.setPadding(10, 10, 10, 0);
                    nomeUsuario.setGravity(Gravity.CENTER_VERTICAL);
                    nomeUsuario.addView(facebook, 0);

                    //nome de usuario facebook
                    TextView nomeFB = new TextView(Feed.this);
                    nomeFB.setText(usuario.getNome_fb());//aeewwww
                    nomeFB.setPadding(10, 0, 0, 0);
                    nomeFB.setTextSize(18);
                    nomeFB.setTextColor(Color.BLACK);

                    //adiciona nome no feed
                    nomeUsuario.addView(nomeFB, 1);
                    layoutFeed.addView(nomeUsuario, 0);

                    //usa a foto tirada pelo usuario
                    //imageBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    ImageView img = new ImageView(Feed.this);
                    img.setLayoutParams(new ViewGroup.LayoutParams(display.getWidth(), display.getWidth()));
                    img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    img.setImageBitmap(imageBitmap);
                    img.setBackgroundColor(Color.rgb(138,43,226));

                    //adiciona a foto tirada ao feed
                    layoutFeed.addView(img, 1);

                    //adiciona icone de curtidas no feed

                    //cria layout para as curtidas das fotos dos usuários
                    LinearLayout layoutCurtidas = new LinearLayout(getApplicationContext());
                    layoutCurtidas.setOrientation(LinearLayout.HORIZONTAL);
                    layoutCurtidas.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    layoutCurtidas.setPadding(10, 10, 10, 50);
                    layoutCurtidas.setGravity(Gravity.CENTER_VERTICAL);

                    ImageView facebookCurtidas = new ImageView(getApplicationContext());
                    Drawable draw2 = res.getDrawable(R.drawable.like);
                    facebookCurtidas.setImageDrawable(draw2);
                    layoutCurtidas.addView(facebookCurtidas, 0);

                    final TextView countlike = new TextView(getApplicationContext());
                    countlike.setText("");
                    countlike.setPadding(10, 0, 0, 0);
                    countlike.setTextSize(18);
                    countlike.setTextColor(Color.BLACK);
                    layoutCurtidas.addView(countlike, 1);

                    //adiciona Curtidas no feed
                    layoutFeed.addView(layoutCurtidas, 2);

                    //ajustar o scroll para mostrar a ultima foto tirada
                    scrollTo = pegartamanhoTexto.getHeight();
                    scrollTo += 50;
                    sv.scrollTo(0, scrollTo);

                    //adiona doubletouch para curtidas
                    img.setOnClickListener(new View.OnClickListener() {
                        boolean liked = false;
                        int i = 0, curtidassss = 1;

                        @Override
                        public void onClick(View v) {
                            i++;
                            Handler handler = new Handler();
                            Runnable r = new Runnable() {

                                @Override
                                public void run() {
                                    i = 0;
                                }
                            };

                            if (i == 1) {
                                //Single click
                                handler.postDelayed(r, 250);
                            } else if (i == 2) {
                                //Double click
                                if (!liked) {
                                    i = 0;
                                    if (curtidassss == 1) {
                                        countlike.setText("" + curtidassss + " curtida");
                                    } else {
                                        countlike.setText("" + curtidassss + " curtidas");
                                    }
                                    curtidassss++;
                                    liked = true;
                                }
                            }
                        }
                    });

                    facebookCurtidas.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /* aqui quando clicado devera descurtir a foto e setar a variavel liked para false
                             * porem no momento nao tem acesso a variavel countlike pois é unica de cada ImageView
                             * esse valor deverá ser resgatado do servidor
                             */
                        }
                    });

                    } else if (button == DialogInterface.BUTTON_NEGATIVE) {
                        mostrarMensagem("Post cancelado!");
                    }
            }
        };

        //botao flutuante que tira a foto
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!nameFBDialg.getText().toString().equalsIgnoreCase("Usuário")){
                    capturarImagem(view);
                } else {
                    mostrarMensagem("Primeiramente faça Login com seu Facebook\nSegundamente Bom dia!");
                }
            }
        });

        dbWeme = new DataBaseConnection(this);
        List<Usuario> usuarioList = dbWeme.Buscar();

        //carrega imagens do feed
        carregaFeed();

//        LoginManager.getInstance().registerCallback(callbackManager, (FacebookCallback<LoginResult>) getApplicationContext());

        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        if (usuarioList.isEmpty()){
            loginButton.setVisibility(View.VISIBLE);
            //botao logar no facebook
            callbackManager = CallbackManager.Factory.create();
            loginButton.setReadPermissions(Arrays.asList("email", "public_profile", "user_work_history", "user_education_history", "user_birthday","user_friends"));
//            logarFacebook(loginButton);

            accessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(
                        AccessToken oldAccessToken,
                        AccessToken currentAccessToken) {
                    // Set the access token using
                    // currentAccessToken when it's loaded or set.

                }
            };

            profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    if (oldProfile != null) {
                        //Log.i(getClass().getSimpleName(), "profile oldProfile: " + oldProfile.getFirstName());
                    }
                    if (currentProfile != null) {

                        profile = currentProfile;
                        nameFBDialg.setText(currentProfile.getFirstName() + " " + currentProfile.getLastName());
                        fotoPerfilFacebook = currentProfile.getProfilePictureUri(180, 180);
                        DownloadFoto downloadFoto = new DownloadFoto();
                        downloadFoto.execute();
                        mostrarMensagem("Olá " + currentProfile.getFirstName());

                        loginButton.setVisibility(View.INVISIBLE);

                    }
                }
            };
            profileTracker.startTracking();


            // If the access token is available already assign it.
            accessToken = AccessToken.getCurrentAccessToken();

            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

                @Override
                public void onSuccess(LoginResult loginResult) {

                    accessToken = loginResult.getAccessToken();
                    profile = com.facebook.Profile.getCurrentProfile();

                    if (profile != null){
//                        validaProfile(profile);

                        nameFBDialg.setText(profile.getFirstName() + " " + profile.getLastName());
                        fotoPerfilFacebook = profile.getProfilePictureUri(180, 180);
                        DownloadFoto downloadFoto = new DownloadFoto();
                        downloadFoto.execute();
                        mostrarMensagem("Olá " + profile.getFirstName());

                        loginButton.setVisibility(View.INVISIBLE);

                    }
                }

                @Override
                public void onCancel() {
                    mostrarMensagem("Cancelado!");
                }

                @Override
                public void onError(FacebookException error) {
                    mostrarMensagem("Falha!");
                }
            });

        }else {
            loginButton.setVisibility(View.INVISIBLE);
            usuario = usuarioList.get(0);
            nameFBDialg.setText(usuario.getNome_fb());
            Bitmap temporario = getRoundCornerBitmap(StringToBitMap(usuario.getImagem_fb()), 150);
            imgPerfil.setImageBitmap(temporario);
            imgPerfilDialg.setImageBitmap(temporario);
        }

        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build(); //???????????
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //busca as imagens do feed que estao armazenadas no banco de dados
    private void carregaFeed() {
        List<ImageFeed> imagensFeed = dbWeme.BuscarFeed();
        ImageFeed imageFeed;
        if (!imagensFeed.isEmpty()){
            for (int f = 0; f < imagensFeed.size(); f++){

                imageFeed = imagensFeed.get(f);

                layoutFeed.removeView(ivVazio);
                layoutFeed.setGravity(Gravity.CENTER_HORIZONTAL);

                //icone do facebook
                ImageView facebook = new ImageView(getApplicationContext());
                Bitmap bitmapIconFacebook = BitmapFactory.decodeResource(getResources(), R.drawable.facebook);
                bitmapIconFacebook = Bitmap.createScaledBitmap(bitmapIconFacebook, 32, 32, true);
                facebook.setImageBitmap(bitmapIconFacebook);

                //cria layout para o nome usuário facebook
                LinearLayout nomeUsuario = new LinearLayout(getApplicationContext());
                nomeUsuario.setOrientation(LinearLayout.HORIZONTAL);
                nomeUsuario.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                nomeUsuario.setPadding(10, 10, 10, 0);
                nomeUsuario.setGravity(Gravity.CENTER_VERTICAL);
                nomeUsuario.addView(facebook, 0);

                //nome de usuario facebook
                TextView nomeFB = new TextView(Feed.this);
                nomeFB.setText(imageFeed.getNome_fb());//aeewwww
                nomeFB.setPadding(10, 0, 0, 0);
                nomeFB.setTextSize(18);
                nomeFB.setTextColor(Color.BLACK);

                //adiciona nome no feed
                nomeUsuario.addView(nomeFB, 1);
                layoutFeed.addView(nomeUsuario, 0);

                //usa a foto tirada pelo usuario
                ImageView img = new ImageView(Feed.this);
                img.setLayoutParams(new ViewGroup.LayoutParams(display.getWidth(), display.getWidth()));
                img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                img.setImageBitmap(StringToBitMap(imageFeed.getImagem_feed()));
                img.setBackgroundColor(Color.rgb(138,43,226));

                //adiciona a foto tirada ao feed
                layoutFeed.addView(img, 1);

                /*adiciona icone de curtidas no feed*/

                //cria layout para as curtidas das fotos dos usuários
                LinearLayout layoutCurtidas = new LinearLayout(getApplicationContext());
                layoutCurtidas.setOrientation(LinearLayout.HORIZONTAL);
                layoutCurtidas.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                layoutCurtidas.setPadding(10, 10, 10, 50);
                layoutCurtidas.setGravity(Gravity.CENTER_VERTICAL);

                ImageView facebookCurtidas = new ImageView(getApplicationContext());
                Drawable draw2 = res.getDrawable(R.drawable.like);
                facebookCurtidas.setImageDrawable(draw2);
                layoutCurtidas.addView(facebookCurtidas, 0);

                final TextView countlike = new TextView(getApplicationContext());
                countlike.setText("" + imageFeed.getCurtidas() + " curtidas");
                countlike.setPadding(10, 0, 0, 0);
                countlike.setTextSize(18);
                countlike.setTextColor(Color.BLACK);
                layoutCurtidas.addView(countlike, 1);

                //adiciona Curtidas no feed
                layoutFeed.addView(layoutCurtidas, 2);

                //adiona doubletouch para curtidas
                img.setOnClickListener(new View.OnClickListener() {
                    boolean liked = false;
                    int i = 0, curtidassss = 1;

                    @Override
                    public void onClick(View v) {
                        i++;
                        Handler handler = new Handler();
                        Runnable r = new Runnable() {

                            @Override
                            public void run() {
                                i = 0;
                            }
                        };

                        if (i == 1) {
                            //Single click
                            handler.postDelayed(r, 250);
                        } else if (i == 2) {
                            //Double click
                            if (!liked) {
                                i = 0;
                                if (curtidassss == 1) {
                                    countlike.setText("" + curtidassss + " curtida");
                                } else {
                                    countlike.setText("" + curtidassss + " curtidas");
                                }
                                curtidassss++;
                                liked = true;
                            }
                        }
                    }
                });

                facebookCurtidas.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            /* aqui quando clicado devera descurtir a foto e setar a variavel liked para false
                             * porem no momento nao tem acesso a variavel countlike pois é unica de cada ImageView
                             * esse valor deverá ser resgatado do servidor
                             */
                    }
                });

            }
        }
    }

    //transforma Bitmap em String
    public String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte [] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    //transforma String em Bitmap
    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    //Inicializa o Dialog de perfil de usuario FB
    private void iniciaDialog() {
        dialogFB = new Dialog(Feed.this);
        dialogFB.setContentView(R.layout.layout_perfil_fecebook);
        dialogFB.setTitle("Facebook");

        llDialog = (LinearLayout) dialogFB.findViewById(R.id.llDialog);
        imgPerfilDialg = (ImageView) dialogFB.findViewById(R.id.imgFBDialog);
        llDialog.setPadding(50, 50, 50, 10);
        nameFBDialg = (TextView) dialogFB.findViewById(R.id.nameFBDialog);
    }

    //mostrar mensagem para o usuario em forma de Toasts
    public void mostrarMensagem(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    //instancia a activity da camera
    public void capturarImagem(View v) {

        boolean temCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

        if (temCamera) {
            if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.CAMERA) != PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)){
                    callDialogPermissions("É necessário conceder permissão para o uso da camera e armazenamento em seu dispositivo", new String[]{android.Manifest.permission.CAMERA});
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
                }
            } else {
                chamaCamera();
            }
        }else{
            mostrarMensagem("Seu dispositivo não possui camera!");
        }
    }

    //cria o caminho na memoria e chama a camera
    private void chamaCamera() {

//        File picsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

//        file = new File(picsDir, "weme_" + getDateTime() + ".jpeg");

//        file = new File("storage/emulated/0/Android/data/br.com.elede.weme/Pictures", "weme_" + getDateTime() + ".jpeg");
        file = new File("storage/external_SD/Android/data/br.com.elede.weme/files/Pictures", "weme_" + getDateTime() + ".jpeg");
        caminhoImagemMemoria = Uri.fromFile(file);

        //chama a intent da camera
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, caminhoImagemMemoria);
        startActivityForResult(i, CAPTURAR_IMAGEM);
    }

    //cria Dialog para conceder permissoes android M
    private void callDialogPermissions(String mensagem, final String[] permissions) {
        final AlertDialog.Builder alertDPermissions = new AlertDialog.Builder(this);
        alertDPermissions.setTitle("Permissões");
        alertDPermissions.setMessage(mensagem);
        alertDPermissions.setPositiveButton("Permitir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(Feed.this, permissions, REQUEST_PERMISSIONS);
            }
        });
        alertDPermissions.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDPermissions.show();
    }

    //Recebe o resultado das permissoes Android M ou superior
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSIONS:
                for (int i = 0; i < permissions.length; i++){
                    if (permissions[i].equalsIgnoreCase(Manifest.permission.CAMERA)
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED ){
                        chamaCamera();
                        break;
                    } else if (permissions[i].equalsIgnoreCase(Manifest.permission_group.STORAGE)
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED ){
                        armazenaFoto();
                        break;
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //faz o tratamento do bitmap de retorno da camera e verifica postagem no feed
    private void armazenaFoto() {
        //recebe o bitmap da foto tirada
        int width = layoutFeed.getWidth();
        int height = width;

        /*
         * metodo que deve ser redimencionado manual.
         *
         * metodo 2 retorna tamanho original porem com bordas maiores
         */
        imageBitmap = ImageUtils.getResizedImage2(caminhoImagemMemoria, width, height);

        //confirma se a foto vai ser postada ou nao
        AlertDialog.Builder alertD = new AlertDialog.Builder(this);
        alertD.setTitle("Feed Weme");
        alertD.setMessage("Olá " + usuario.getNome_fb() + ", você deseja postar essa foto?");
        alertD.setPositiveButton("Sim", listener);
        alertD.setNegativeButton("Cancelar", listener);
        alertD.show();

    }

    //captura data e hora do sistema para nome unica de cada foto tirada
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    //activity da camera e do login do facebook que contem os resultados esperados
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURAR_IMAGEM) {
            if (resultCode == RESULT_OK) {

                //Verifica e concede permissons apenas para Android M o superior
                if (ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)){
                        callDialogPermissions("É necessário conceder permissão para o uso da camera e armazenamento em seu dispositivo", new String[]{Manifest.permission_group.STORAGE});
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                    }
                } else {
                   armazenaFoto();
                }

            } else {
                mostrarMensagem("Imagem não capturada!");
            }
        } else {
            //chamada requisitada pelo Facebook
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    //redesenha o bitmap com as bordas desejadas
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
        final Rect clipRect = new Rect(radius, radius, w - radius, h - radius);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        canvas.drawRect(clipRect, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rectF, paint);

        bitmap.recycle();

        return output;
    }

    //classe para fazer download das imagens do facebook
    public class DownloadFoto extends AsyncTask<Void, Void, Void>{

        Bitmap bitmap = null, temp = null;
        @Override
        protected Void doInBackground(Void... params) {

            try{
                temp = Picasso.with(Feed.this).load(fotoPerfilFacebook).get();
                bitmap = getRoundCornerBitmap(temp, 150);
            } catch (Exception e){
                mostrarMensagem("Erro no download da imagem de perfil!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){

            if (bitmap != null){
                imgPerfil.setImageBitmap(bitmap);
                imgPerfilDialg.setImageBitmap(bitmap);

                //transforma o bitmap em string para mandar para dataBase/Servidor
                imgPerfilByte = bitMapToString(bitmap);

                usuario = new Usuario((profile.getFirstName() + " " + profile.getLastName()),
                        imgPerfilByte, 0);
                dbWeme.inserir(usuario);
            }
            super.onPostExecute(aVoid);
        }
    }
}


