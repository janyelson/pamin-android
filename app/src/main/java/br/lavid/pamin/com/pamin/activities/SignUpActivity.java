package br.lavid.pamin.com.pamin.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import java.util.regex.Pattern;

import br.lavid.pamin.com.pamin.R;
import br.lavid.pamin.com.pamin.models.User;
import br.lavid.pamin.com.pamin.utils.InternetFeatures;
import br.lavid.pamin.com.pamin.utils.MaterialFeatures;

public class SignUpActivity extends AppCompatActivity {

    private ProgressDialog loading;
    private AppCompatEditText nameField, passField, confPassField, emailField;
    private User user;
    private AppCompatButton cancelBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        MaterialFeatures.actionBarColor(this, getResources().getColor(R.color.md_indigo_700));
        MaterialFeatures.navigationBarColor(this, getResources().getColor(R.color.md_indigo_700));

        nameField = (AppCompatEditText) findViewById(R.id.signup_completeNameField);
        passField = (AppCompatEditText) findViewById(R.id.signup_passwordField);
        confPassField = (AppCompatEditText) findViewById(R.id.signup_passwordConfField);
        emailField = (AppCompatEditText) findViewById(R.id.signup_emailField);

        cancelBtn = (AppCompatButton) findViewById(R.id.signup_cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelSignUp();
            }
        });

        emailField.setText(getSmartphoneEmail());
    }

    public void signUp(View view) {

        if (!InternetFeatures.hasInternet(this)) {
            new AlertDialog.Builder(this, R.style.dialog)
                    .setTitle("Acesse a internet")
                    .setMessage("Acesse a internet para fazer cadastro no app do Pamin")
                    .show();
            return;
        }

        user = User.getInstance(this);
        MaterialFeatures.hideKeyboard(this);

        try {
            if (passField.getText().toString().equals(confPassField.getText().toString())
                    && isValidEmail(emailField.getText()) && !(nameField.getText().toString().trim().equals("")) ) {

                loading = ProgressDialog.show(this, "", "Criando seu usuário...", true);

                user.registerUser(
                        nameField.getText().toString(),
                        passField.getText().toString(),
                        emailField.getText().toString(),
                        new User.CompleteCallback() {
                            @Override
                            public void completeCallback(boolean successful) {
                                loading.dismiss();
                                if (successful) {
                                    successfulSignUp();
                                } else {
                                    Toast.makeText(SignUpActivity.this, "Erro ao efetuar cadastro", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
            else {
                String message;
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Algo deu errado!");
                alert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                if ((nameField.getText().toString().trim().equals(""))) {
                    message = "Informe um nome.";
                    alert.setMessage(message);
                }
                else if (!isValidEmail(emailField.getText())) {
                    message = "Verifique o endereço de e-mail.";
                    alert.setMessage(message);
                }
                else if(!passField.getText().toString().equals(confPassField.getText().toString())) {
                    message = "As senhas não estão iguais.";
                    alert.setMessage(message);
                }
                alert.show();
            }
        } catch (IllegalArgumentException error) {
            loading.dismiss();
            Toast.makeText(SignUpActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void cancel(View view) {
        cancelSignUp();
    }

    private void cancelSignUp() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void successfulSignUp() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("email", emailField.getText());
        returnIntent.putExtra("pass", passField.getText());
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        cancelSignUp();
    }

    public String getSmartphoneEmail() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return "";
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
