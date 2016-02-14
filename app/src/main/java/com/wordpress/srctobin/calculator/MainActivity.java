package com.wordpress.srctobin.calculator;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView txtView;
    private RelativeLayout layout;
    private TextView txtRes;
    private boolean clearForDecPoint=true;
    //private boolean equalButtonClicked;
    private final static String TAG="suvu";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = (RelativeLayout) findViewById(R.id.relativeLayout);
        txtView = (TextView) findViewById(R.id.txtView);
        txtRes=(TextView)findViewById(R.id.txtRes);

        //to make result scrollable
        //see the xml file
        //Horizontal Layout
        txtRes.setSelected(true);

        //this is to dynamically set text view's size
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = layout.getMeasuredHeight();
                txtView.setHeight((int) (height * 0.3));
                ArrayList<Button> buttons = new ArrayList<Button>(15);
                for (int i = 0; i < 10; i++) {
                    int id = getResources().getIdentifier("btn"+i, "id", getPackageName());
                    buttons.add((Button) findViewById(id));
                }
                buttons.add((Button)findViewById(R.id.btnDot));
                buttons.add((Button)findViewById(R.id.btnEq));
                buttons.add((Button)findViewById(R.id.btnLp));
                buttons.add((Button)findViewById(R.id.btnRp));
                buttons.add((Button)findViewById(R.id.btnPow));
                buttons.add((Button)findViewById(R.id.btnAdd));
                buttons.add((Button)findViewById(R.id.btnSub));
                buttons.add((Button)findViewById(R.id.btnMul));
                buttons.add((Button)findViewById(R.id.btnDiv));
                for(Button btn:buttons){
                    btn.setHeight((int)(height * 0.12));
                    btn.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
                }
            }
        });

        //set numpad button listeners
        addNumButtonListeners();
        addOpButtonListeners();

        //character limit of txtView is 20
        txtView.addTextChangedListener(new TextWatcher() {
            CharSequence befr = null;
            CharSequence resBefr = null;

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                befr = s;
                resBefr = txtRes.getText();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 20) {
                    txtView.setText(befr);
                    final Toast toast = Toast.makeText(MainActivity.this, "Maximum 20 characters allowed",
                            Toast.LENGTH_SHORT);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                toast.show();
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                            } finally {
                                toast.cancel();
                            }
                        }
                    }).start();
                    //txtView.setTextSize(txtView.getTextSize()-5);

                } else {
                    //change the result accordingly
                    //using UI thread to show result even before Equals button pressed
                    if (txtView.getText().toString().matches(".*[0-9]+[\\*\\+\\-\\*/\\(\\)\\^]+[0-9]+.*")) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                String result = "";
                                try {

                                    result = String.format("%.2f", eval(txtView.getText().toString()));
                                    if (result.endsWith(".00")) {
                                        //Log.d(TAG,result);
                                        result = result.substring(0, result.indexOf('.'));

                                    }
                                } catch (Exception e) {

                                    if(e.getMessage().equalsIgnoreCase("exp")){
                                        result="Exponent too big!";
                                    }
                                    else result = resBefr.toString();
                                }
                                txtRes.setText(result);
                            }
                        });

                    }

                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Log.d(TAG, "About");
            //Toast.makeText(MainActivity.this, "This is a calculator app.", Toast.LENGTH_SHORT).show();
            LayoutInflater layoutInflater
                    = (LayoutInflater)getBaseContext()
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.about_popup, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);

            Button btnDismiss = (Button)popupView.findViewById(R.id.okay);
            btnDismiss.setOnClickListener(new Button.OnClickListener(){

                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }});

            Button email=(Button) popupView.findViewById(R.id.mail);
            email.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "suvankarforandroid@gmail.com", null));
                    startActivity(Intent.createChooser(intent,"Choose an Email client :"));
                }
            });
            popupWindow.showAsDropDown(txtView, 0, 0);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addOpButtonListeners() {
        Button btnAdd=(Button)findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("0");
                    txtRes.setText("Error");
                }
                txtView.setText(String.format("%s%c", txtView.getText(), '+'));
                clearForDecPoint=true;
            }
        });
        Button btnSub=(Button)findViewById(R.id.btnSub);
        btnSub.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("0");
                    txtRes.setText("Error");
                }
                if(txtView.getText().toString().equals("0")){
                    txtView.setText(String.format("%c", '-'));      //negative decimal
                }
                else
                    txtView.setText(String.format("%s%c", txtView.getText(), '-'));
                clearForDecPoint=true;
            }
        });
        Button btnMul=(Button)findViewById(R.id.btnMul);
        btnMul.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("0");
                    txtRes.setText("Error");
                }
                txtView.setText(String.format("%s%c", txtView.getText(), '*'));
                clearForDecPoint=true;
            }
        });
        Button btnDiv=(Button)findViewById(R.id.btnDiv);
        btnDiv.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("0");
                    txtRes.setText("Error");
                }
                txtView.setText(String.format("%s%c", txtView.getText(), '/'));
                clearForDecPoint=true;
            }
        });
        Button btnPow=(Button)findViewById(R.id.btnPow);
        btnPow.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("0");
                    txtRes.setText("Error");
                }
                txtView.setText(String.format("%s%c", txtView.getText(), '^'));
                clearForDecPoint=true;
            }
        });
        Button btnLp=(Button)findViewById(R.id.btnLp);
        btnLp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("0");
                    txtRes.setText("Error");
                }
                txtView.setText(String.format("%s%c", txtView.getText(), '('));
                clearForDecPoint=true;
            }
        });
        Button btnRp=(Button)findViewById(R.id.btnRp);
        btnRp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("0");
                    txtRes.setText("Error");
                }
                txtView.setText(String.format("%s%c", txtView.getText(), ')'));
                clearForDecPoint=true;
            }
        });
        //delete last char
        Button btnDel=(Button)findViewById(R.id.btnDel);
        btnDel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!txtView.getText().toString().equals("0")) {
                    txtView.setText(String.format("%s", txtView.getText().subSequence(0, txtView.getText().length() - 1)));
                }
                if (txtView.getText().length() == 0) {
                    txtView.setText(String.format("%d", 0));
                }
                clearForDecPoint=true;
            }
        });

        //clear everything
        btnDel.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                txtView.setText(String.format("%d", 0));
                txtRes.setText("");
                clearForDecPoint=true;
                return true;
            }
        });

        Button btnEq=(Button) findViewById(R.id.btnEq);
        btnEq.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //equalButtonClicked = true;
                String result="";
                try {
                    result = String.format("%.2f", eval(txtView.getText().toString()));
                    if(result.endsWith(".00")){
                        //Log.d(TAG,result);
                        result = result.substring(0,result.indexOf('.'));

                    }
                }catch(Exception e){
                    result="Error";
                    Log.d(TAG,e.toString());
                }
                CharSequence eq=txtView.getText();
                txtView.setText(result);
                txtRes.setText(eq);
                clearForDecPoint=true;
            }
        });
    }

    /**
     * to evaluate expressions
     * @param str
     * @return
     */
    public static BigDecimal eval(final String str) throws Exception{
        class Parser {
            int pos = -1, c;

            void eatChar() {
                c = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            void eatSpace() {
                while (Character.isWhitespace(c)) eatChar();
            }

            BigDecimal parse() throws Exception{
                eatChar();
                BigDecimal v = parseExpression();
                if (c != -1) throw new RuntimeException("Unexpected: " + (char)c);
                return v;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor | term brackets
            // factor = brackets | number | factor `^` factor
            // brackets = `(` expression `)`

            BigDecimal parseExpression() throws Exception{
                BigDecimal v = parseTerm();
                for (;;) {
                    eatSpace();
                    if (c == '+') { // addition
                        eatChar();
                        v = v.add(parseTerm());
                    } else if (c == '-') { // subtraction
                        eatChar();
                        v = v.subtract(parseTerm());
                    } else {
                        return v;
                    }
                }
            }

            BigDecimal parseTerm() throws Exception{
                BigDecimal v = parseFactor();
                for (;;) {
                    eatSpace();
                    if (c == '/') { // division
                        eatChar();
                        v = v.divide(parseFactor(), 2, RoundingMode.HALF_UP);
                    } else if (c == '*' || c == '(') { // multiplication
                        if (c == '*') eatChar();
                        v = v.multiply(parseFactor());
                    } else {
                        return v;
                    }
                }
            }

            BigDecimal parseFactor() throws Exception{
                BigDecimal v;
                boolean negate = false;
                eatSpace();
                if (c == '+' || c == '-') { // unary plus & minus
                    negate = c == '-';
                    eatChar();
                    eatSpace();
                }
                if (c == '(') { // brackets
                    eatChar();
                    v = parseExpression();
                    if (c == ')') eatChar();
                } else { // numbers
                    StringBuilder sb = new StringBuilder();
                    while ((c >= '0' && c <= '9') || c == '.') {
                        sb.append((char)c);
                        eatChar();
                    }
                    if (sb.length() == 0) throw new RuntimeException("Unexpected: " + (char)c);
                    v = new BigDecimal(sb.toString());
                }
                eatSpace();
                if (c == '^') { // exponentiation
                    eatChar();
                    int exp=parseFactor().intValue();
                    if(exp>100){
                        throw new Exception("exp");

                    }
                    v = v.pow(exp);
                }
                if (negate) v = v.multiply(new BigDecimal("-1")); // unary minus is applied after exponentiation; e.g. -3^2=-9
                return v;
            }
        }
        return new Parser().parse();
    }

    private void addNumButtonListeners() {
        Button btn1=(Button)findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equals("0")
                        ||txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("");
                }
                txtView.setText(String.format("%s%d", txtView.getText(), 1));
            }
        });
        Button btn2=(Button)findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                if(txtView.getText().toString().trim().equals("0")
                        ||txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("");
                }
                txtView.setText(String.format("%s%d", txtView.getText(), 2));
            }
        });
        Button btn3=(Button)findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equals("0")
                        ||txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("");
                }
                txtView.setText(String.format("%s%d", txtView.getText(), 3));
            }
        });
        Button btn4=(Button)findViewById(R.id.btn4);
        btn4.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equals("0")
                        ||txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("");
                }
                txtView.setText(String.format("%s%d", txtView.getText(), 4));
            }
        });
        Button btn5=(Button)findViewById(R.id.btn5);
        btn5.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equals("0")
                        ||txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("");
                }
                txtView.setText(String.format("%s%d", txtView.getText(), 5));
            }
        });
        Button btn6=(Button)findViewById(R.id.btn6);
        btn6.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equals("0")
                        ||txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("");
                }
                txtView.setText(String.format("%s%d", txtView.getText(), 6));
            }
        });
        Button btn7=(Button)findViewById(R.id.btn7);
        btn7.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equals("0")
                        ||txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("");
                }
                txtView.setText(String.format("%s%d", txtView.getText(), 7));
            }
        });
        Button btn8=(Button)findViewById(R.id.btn8);
        btn8.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equals("0")
                        ||txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("");
                }
                txtView.setText(String.format("%s%d", txtView.getText(), 8));
            }
        });
        Button btn9=(Button)findViewById(R.id.btn9);
        btn9.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equals("0")
                        ||txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("");
                }
                txtView.setText(String.format("%s%d", txtView.getText(), 9));
            }
        });
        Button btn0=(Button)findViewById(R.id.btn0);
        btn0.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equals("0")
                        ||txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("");
                }
                txtView.setText(String.format("%s%d", txtView.getText(), 0));
            }
        });
        Button btnDot=(Button)findViewById(R.id.btnDot);
        btnDot.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(txtView.getText().toString().trim().equalsIgnoreCase("error")){
                    txtView.setText("0");
                }
                if(!txtView.getText().toString().endsWith(".") && clearForDecPoint) {
                    txtView.setText(String.format("%s%s", txtView.getText(), "."));
                    clearForDecPoint = false;
                }
            }
        });
    }
}
