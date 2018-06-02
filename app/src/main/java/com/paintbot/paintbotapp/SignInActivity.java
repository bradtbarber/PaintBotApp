package com.paintbot.paintbotapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class SignInActivity extends AppCompatActivity {
    private static EditText passwordPrompt;
    private static EditText usernamePrompt;
    private static String username;
    private static String password;
    private static Button signInButton;
    private int attempts = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        SignInButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SignInButton(){
        usernamePrompt = findViewById(R.id.usernamePromptEditText);
        passwordPrompt = findViewById(R.id.passwordPromptEditText);
        signInButton = findViewById(R.id.signInButton);

        usernamePrompt.requestFocus();

        signInButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (usernamePrompt.getText().toString().equals("admin") &&
                                passwordPrompt.getText().toString().equals("admin")){
                            Toast.makeText(SignInActivity.this,
                                    "Username and password are correct.",
                                    Toast.LENGTH_SHORT).show();
                            bluetoothSetup(v);
                        }
                        else {
                            attempts--;
                            Toast.makeText(SignInActivity.this,
                                    "Incorrect username or password.\n"
                                            + attempts + " attempts remaining.",
                                    Toast.LENGTH_SHORT).show();
                            if(attempts == 0)
                                signInButton.setEnabled(false);
                        }
                    }
                }
        );
    }

    public void bluetoothSetup(View view) {
        Intent bluetoothSetupActivity = new Intent(this, BluetoothSetupActivity.class);
        startActivity(bluetoothSetupActivity);
    }
}
