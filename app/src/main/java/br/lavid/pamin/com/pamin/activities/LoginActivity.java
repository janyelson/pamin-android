package br.lavid.pamin.com.pamin.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import java.util.regex.Pattern;

import br.lavid.pamin.com.pamin.R;
import br.lavid.pamin.com.pamin.models.User;
import br.lavid.pamin.com.pamin.utils.InternetFeatures;
import br.lavid.pamin.com.pamin.utils.MaterialFeatures;

public class LoginActivity extends AppCompatActivity {

    private final int REGISTER_RETURN = 500;

    private User user;

    private AppCompatEditText emailField, passField;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = User.getInstance(this);

        MaterialFeatures.actionBarColor(this, getResources().getColor(R.color.md_indigo_700));
        MaterialFeatures.navigationBarColor(this, getResources().getColor(R.color.md_indigo_700));

        setContentView(R.layout.activity_login);

        emailField = (AppCompatEditText) findViewById(R.id.login_emailField);
        passField = (AppCompatEditText) findViewById(R.id.login_passwordField);
        loading = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        loading.setMessage("Entrando...");

        emailField.setText(getSmartphoneEmail());

    }

    public void callRegisterActivity(View v) {
        MaterialFeatures.hideKeyboard(this);
        startActivityForResult(new Intent(this, SignUpActivity.class), REGISTER_RETURN);
        overridePendingTransition(0, 0);
    }

    public void enterWithEmail(View v) {
        MaterialFeatures.hideKeyboard(this);

        if (!InternetFeatures.hasInternet(this)) {
            new AlertDialog.Builder(this, R.style.dialog)
                    .setTitle("Acesse a internet")
                    .setMessage("Acesse a internet para fazer login no app do Pamin")
                    .show();
            return;
        }

        loading.show();
        user.loginUser(emailField.getText().toString(), passField.getText().toString(), new User.CompleteCallback() {
            @Override
            public void completeCallback(boolean successful) {
                if (successful) {
                    goToMain();
                } else {
                    Toast.makeText(getApplicationContext(), "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
                }
                loading.dismiss();
            }
        });

    }

    private String getSmartphoneEmail() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
        }
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return "";
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTER_RETURN && resultCode == RESULT_OK && user.hasUser()) {
            emailField.setText(data.getStringExtra("email"));
            passField.setText(data.getStringExtra("pass"));

            Log.v("LoginAct", "EMAIL: " + User.getInstance(this).getEmail());
            Log.v("LoginAct", "TOKEN: " + User.getInstance(this).getToken());

        }

    }
}
