package br.lavid.pamin.com.pamin.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import br.lavid.pamin.com.pamin.R;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressDialog loadingDialog;
    private AlertDialog EULADialog, privacityDialog;

    private boolean waitingForEULA, waitingForPrivacityPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        new SyncTermsLoading().execute();

        waitingForEULA = false;
        waitingForPrivacityPolicy = false;

        initToolbar();

        Button aboutPaminBtn = (Button) findViewById(R.id.aboutPaminBtn);
        Button aboutDev = (Button) findViewById(R.id.aboutDevBtn);
        Button aboutUserTerms = (Button) findViewById(R.id.about_userTermsBtn);
        Button privacityBtn = (Button) findViewById(R.id.about_privacityPolicyBtn);

        aboutPaminBtn.setOnClickListener(this);
        aboutDev.setOnClickListener(this);
        privacityBtn.setOnClickListener(this);
        aboutUserTerms.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException npe) {
            try {
                getActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (NullPointerException npe2) {
            }
        }
    }

    private AlertDialog.Builder termsDialog(String title, String content) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.scrollable_dialogtext, null);

        TextView textview = (TextView) view.findViewById(R.id.scrollabledialog_text);
        textview.setText(content);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setView(view);
        alertDialog.setNeutralButton("OK", null);
        return alertDialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aboutPaminBtn: {
                startActivity(new Intent(AboutActivity.this, AboutPaminActivity.class));
                return;
            }
            case R.id.aboutDevBtn: {
                startActivity(new Intent(AboutActivity.this, AboutDevActivity.class));
                return;
            }
            case R.id.about_userTermsBtn: {
                if (EULADialog == null) {
                    waitingForEULA = true;
                    loadingDialog = ProgressDialog.show(AboutActivity.this, "Carregando",
                            "Espere enquanto os Termos de usos são carregados...", true);
                } else
                    EULADialog.show();
                return;
            }
            case R.id.about_privacityPolicyBtn: {
                if (privacityDialog == null) {
                    waitingForPrivacityPolicy = true;
                    loadingDialog = ProgressDialog.show(AboutActivity.this, "Carregando",
                            "Espere enquanto a Política de Privacidade é carregada...", true);
                } else {
                    privacityDialog.show();
                }

                return;
            }
        }
    }

    /**
     * Carries the texts of the terms in the background to not hinder the app
     */
    private class SyncTermsLoading extends AsyncTask<Void, Void, Void> {

        AlertDialog.Builder EULADialogBuilder;
        AlertDialog.Builder privacityDialogBuilder;

        @Override
        protected Void doInBackground(Void... params) {
            Log.v("TermsLoading", "Loading texts...");

            EULADialogBuilder = termsDialog("Termos de Uso", getResources().getString(R.string.user_terms));
            publishProgress();
            privacityDialogBuilder = termsDialog("Política de Privacidade", getResources().getString(R.string.privacity_policy));
            publishProgress();

            Log.v("TermsLoading", "Terms loading finish");
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            if (EULADialog == null)
                EULADialog = EULADialogBuilder.create();
            else
                privacityDialog = privacityDialogBuilder.create();

            if (waitingForEULA) {
                loadingDialog.dismiss();
                EULADialog.show();
            }
            if (waitingForPrivacityPolicy) {
                loadingDialog.dismiss();
                privacityDialog.show();
            }
            Log.v("TermsLoading", "Term loaded");
        }
    }
}
