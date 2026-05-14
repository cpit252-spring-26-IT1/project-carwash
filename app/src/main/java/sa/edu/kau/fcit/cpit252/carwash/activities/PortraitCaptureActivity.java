package sa.edu.kau.fcit.cpit252.carwash.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import sa.edu.kau.fcit.cpit252.carwash.R;

public class PortraitCaptureActivity extends AppCompatActivity {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portrait_capture);

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);

        TextView tvBack = findViewById(R.id.tvBackScanner);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    @Override protected void onResume() {
        super.onResume(); capture.onResume();
    }
    @Override protected void onPause() {
        super.onPause(); capture.onPause();
    }
    @Override protected void onDestroy() {
        super.onDestroy(); capture.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}