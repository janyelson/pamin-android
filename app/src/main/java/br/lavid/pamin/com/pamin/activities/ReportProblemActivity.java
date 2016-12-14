package br.lavid.pamin.com.pamin.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Date;

import br.lavid.pamin.com.pamin.R;
import br.lavid.pamin.com.pamin.models.User;
import br.lavid.pamin.com.pamin.utils.InternetFeatures;

public class ReportProblemActivity extends AppCompatActivity {

    public static final String RESULT_REPORT = "Report";

    private EditText etDesc;
    private RadioGroup radioGroupType, radioGroupDevice;
    private RadioButton radioBtnType, radioBtnDevice;
    private int radioGroupTypeBtnId, radioGroupDeviceBtnId;

    private String desc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);

        initToolbar();
    }

    private boolean checkInput() {

        radioGroupType = (RadioGroup) findViewById(R.id.radioGroupType);
        radioGroupDevice = (RadioGroup) findViewById(R.id.radioGroupDevice);

        radioGroupTypeBtnId = radioGroupType.getCheckedRadioButtonId();
        radioGroupDeviceBtnId = radioGroupDevice.getCheckedRadioButtonId();

        radioBtnType = (RadioButton) findViewById(radioGroupTypeBtnId);
        radioBtnDevice = (RadioButton) findViewById(radioGroupDeviceBtnId);


        etDesc = (EditText) findViewById(R.id.reportProblem_descField);
        desc = etDesc.getText().toString();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Faltou algo!");
        alert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        if (radioBtnType == null) {
            alert.setMessage("Selecione o tipo do erro!");
            alert.show();

            return false;
        }
        if (desc.trim().isEmpty()) {
            alert.setMessage("Digite pelo menos a descrição!");
            alert.show();

            return false;
        }
        if (radioBtnDevice == null) {
            alert.setMessage("Selecione o dispositivo!");
            alert.show();

            return false;
        } else {
            return true;
        }


    }

    private void initToolbar() {
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException npe) {
            try {
                getActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (NullPointerException npe2) {
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Intent intent = this.getIntent();
        this.setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void sendReport(View view) {
        String name;

        if (checkInput()) {

            if (!InternetFeatures.hasInternet(this)) {
                new AlertDialog.Builder(this, R.style.dialog)
                        .setTitle("Acesse a internet")
                        .setMessage("Acesse a internet para enviar sua opnião para a equipe Pamin")
                        .show();
                return;
            }

            String subject = "ERRO/SUGESTÃO: " + radioBtnType.getText().toString() + "\nDISPOSITIVO: " + radioBtnDevice.getText().toString() + " " + getDeviceName();
            if (User.getInstance(this).getName() != null)
                name = User.getInstance(this).getName();
            else
                name = "[Anônimo]";


            String uriText =
                    "mailto:developerspamin@lavid.ufpb.br" +
                            "?subject=" + Uri.encode("[SUPORTE APP PAMIN]") +
                            "&body=" + Uri.encode(subject + "\n\nEnviado por : "
                            + name +
                            "\n\n\n" + desc +
                            "\n\n\nData EPOCH: " + new Date().getTime());

            Uri uri = Uri.parse(uriText);

            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.setData(uri);
            startActivity(Intent.createChooser(sendIntent, "Enviar com"));

            Intent intent = this.getIntent();
            intent.putExtra(RESULT_REPORT, "'" + radioBtnType.getText().toString() + "' enviado(a) com sucesso! \n" +
                    " A equipe Pamin agradece sua ajuda! ");
            this.setResult(RESULT_OK, intent);
            finish();
        }
    }

    /**
     * Returns the consumer friendly device name
     */
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        if (manufacturer.equalsIgnoreCase("HTC")) {
            // make sure "HTC" is fully capitalized.
            return "HTC " + model;
        }
        return capitalize(manufacturer) + " " + model;
    }

    private String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }
}
