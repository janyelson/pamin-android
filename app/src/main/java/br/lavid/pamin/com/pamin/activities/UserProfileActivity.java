package br.lavid.pamin.com.pamin.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import br.lavid.pamin.com.pamin.R;
import br.lavid.pamin.com.pamin.models.User;
import br.lavid.pamin.com.pamin.utils.PictureTransformation;

public class UserProfileActivity extends AppCompatActivity {

    private final int RESULT_LOAD_IMAGE_USERPIC = 134;
    private final int RESULT_LOAD_IMAGE_BACKGROUND = 135;

    private User user;

    private Toolbar toolbar;
    private TextView usernameTV, userEmailTV;
    private CircularImageView userPicture;
    private LinearLayout userBackground;

    private Button changePictureBtn, changeBackgroundBtn, logoffBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initToolbar();

        usernameTV = (TextView) findViewById(R.id.userProfileAct_username);
        userEmailTV = (TextView) findViewById(R.id.userProfileAct_email);
        userPicture = (CircularImageView) findViewById(R.id.userProfileAct_imageView);
        changeBackgroundBtn = (Button) findViewById(R.id.userProfileAct_changeBack);
        changePictureBtn = (Button) findViewById(R.id.userProfileAct_changePic);
        logoffBtn = (Button) findViewById(R.id.userProfileAct_logoff);
        userBackground = (LinearLayout) findViewById(R.id.userProfileAct_userBackground);

        user = User.getInstance(this);
        usernameTV.setText(user.getName());
        userEmailTV.setText(user.getEmail());
        if (user.getUserPicture() != null)
            userPicture.setImageBitmap(user.getUserPicture());

        changePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE_USERPIC);
            }
        });
        changeBackgroundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE_BACKGROUND);

            }
        });
        logoffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.logoffUser();
                startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
                finishAffinity();
            }
        });

        setToolbarText();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.userProfileAct_tool_bar);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void setToolbarText() {
        String name = user.getName();
        String[] names = name.split("\\s+");
        try {
            toolbar.setTitle(names[0] + " " + names[1]);
        } catch (ArrayIndexOutOfBoundsException overTheLimit) {
            toolbar.setTitle(name);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE_USERPIC && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap userPic = BitmapFactory.decodeFile(picturePath);
            userPic = PictureTransformation.getCroppedBitmap(userPic);
            userPic = PictureTransformation.scaleDown(userPic, 300);
            userPicture.setImageBitmap(userPic);
            user.setUserPicture(userPic, UserProfileActivity.this);

        }
        if (requestCode == RESULT_LOAD_IMAGE_BACKGROUND && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap back = PictureTransformation.scaleDown(BitmapFactory.decodeFile(picturePath), 320);
            back = PictureTransformation.fastblur(back, 60);

            userBackground.setBackground(new BitmapDrawable(getResources(), back));
            user.setBackground(back, UserProfileActivity.this);
        }
    }
}
