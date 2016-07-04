package br.com.elede.mauro.weme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Mauro on 14/05/2016.
 */
public class Inicio extends Activity {

    private static int SPLASH_TIME_OUT = 2000;
    private ImageView imageView;
    private Bitmap bitmap;

    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.inicio1);

        Display display = ((WindowManager) getSystemService(this.WINDOW_SERVICE)).getDefaultDisplay();
        imageView = (ImageView) findViewById(R.id.imageView2);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.weme);
        Bitmap temp = Bitmap.createScaledBitmap(bitmap, display.getWidth(), display.getHeight(), true);

        imageView.setImageBitmap(temp);

        //Chamda da próxima tela após o tempo determinado
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Inicio.this, Inicio2.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
