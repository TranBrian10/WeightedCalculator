package com.syfenworks.weightedcalculator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import java.text.DecimalFormat;
import java.util.ArrayList;

//TODO delay to edit on focus
//TODO about activity

public class MainActivity extends ActionBarActivity {
    private final ArrayList<View> capsuleList = new ArrayList<>();
    private Toast error_toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Default two capsules
        AddCapsule();
        AddCapsule();

        //When the layout is touched, remove focus and keyboard
        final RelativeLayout noFocus = (RelativeLayout) findViewById(R.id.noFocus);
        noFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View current = getCurrentFocus();
                if (current!=null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(current.getWindowToken(), 0);

                    current.clearFocus();
                }
            }
        });

        //Calculate button listener
        Button btn_Calculate = (Button) findViewById(R.id.btnCalculate);
        btn_Calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calculate();
            }
        });

        //Make the global toast
        error_toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
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
        switch (item.getItemId()) {
            case R.id.action_add:
                AddCapsule();
                return true;
            case R.id.action_reset:
                Reset();
                return true;
            case R.id.action_about:
                About();
                return true;
            case R.id.action_exit:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    void Reset() {
        ResetDialog resetDialog = new ResetDialog();
        resetDialog.show(getSupportFragmentManager(), "reset");
    }

    public static class ResetDialog extends android.support.v4.app.DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //Use a builder to create an alert dialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.reset_title)
                   .setMessage(R.string.reset_message)
                   .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           //Reset
                           Intent intent = getActivity().getIntent();
                           getActivity().finish();
                           startActivity(intent);
                       }
                   })
                   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           //Cancel
                       }
                   });

            return builder.create();
        }
    }

    ///////////////////////////////////////////////////////////////////

    void About() {
        final View popup_view = getLayoutInflater().inflate(R.layout.about_popup, new LinearLayout(getApplicationContext()), true);
        final PopupWindow about_popup = new PopupWindow(popup_view,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        about_popup.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.accent)));
        about_popup.setFocusable(true);
        about_popup.showAtLocation(findViewById(R.id.noFocus), Gravity.CENTER, 0, 0);

        Button btn_close = (Button)popup_view.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_popup.dismiss();
            }
        });
    }

    public void goToWebsite(View view) {
        goToUrl ("https://syfenworks.blogspot.com");
    }

    public void goToTwitter(View view) {
        goToUrl ("https://twitter.com/SyfenWorks");
    }

    public void goToRate(View view) {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToRate = new Intent(Intent.ACTION_VIEW, uri);
        goToRate.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToRate);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id="
                            + getApplicationContext().getPackageName())));
        }
    }

    private void goToUrl(String url) {
        Uri link = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, link);
        startActivity(launchBrowser);
    }

    ///////////////////////////////////////////////////////////////////

    void AddCapsule() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View capsuleLayout = inflater.inflate(R.layout.capsule_layout, new LinearLayout(getApplicationContext()), true);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.marks_list);
        linearLayout.addView(capsuleLayout);

        capsuleList.add(capsuleLayout);
        TextView textView = (TextView)capsuleLayout.findViewById(R.id.capsule_number);
        textView.setText(String.valueOf(capsuleList.size()));

        //Scroll to bottom
        ScrollView scrollView = (ScrollView)findViewById(R.id.marks_scroll);
        scrollView.scrollTo(0, scrollView.getBottom());
        capsuleList.get(capsuleList.size()-1).requestFocus();
    }

    public void Remove(View view) {
        //Get the capsule and list
        View parent_view = (View) view.getParent().getParent().getParent();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.marks_list);

        //Loop through following capsules and decrement number
        TextView textView = (TextView)parent_view.findViewById(R.id.capsule_number);
        int loop_start_index = (Integer.parseInt(textView.getText().toString()));
        for (int i = loop_start_index; i < capsuleList.size(); i++) {
            LinearLayout temp_layout = (LinearLayout) linearLayout.getChildAt(i);
            TextView temp_textView = (TextView)temp_layout.findViewById(R.id.capsule_number);
            temp_textView.setText(Integer.toString(i));
        }

        //Remove from capsule list
        int index = (Integer.parseInt(textView.getText().toString()) - 1);
        capsuleList.remove(index);

        //Remove from view
        linearLayout.removeView(parent_view);
    }

    void Calculate() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.marks_list);
        EditText final_editText = (EditText)findViewById(R.id.finalMark);
        double final_mark = 0;
        double total_weight = 0;
        boolean reverse_calculation = false;
        double reverse_final = 0;
        double reverse_total = 100;
        double reverse_weighting = 0;
        EditText reverse_mark_editText = null;
        EditText reverse_mark_editTotal = null;

        //Loop through each capsule
        //Get total weight
        for (int i = 0; i < capsuleList.size(); i++) {
            LinearLayout temp_layout = (LinearLayout) linearLayout.getChildAt(i);
            EditText temp_weighting = (EditText)temp_layout.findViewById(R.id.weight);

            if (temp_weighting.getText().toString().length() > 0
                    && !temp_weighting.getText().toString().equals(".")) {
                double weighting_value = Double.parseDouble(String.valueOf(temp_weighting.getText()));
                total_weight += weighting_value;
            }
            else {
                error_toast.setText(R.string.weighting_alert_toast);
                error_toast.show();

                return;
            }
        }
        //mark * (weight / total weight), add together
        for (int i = 0; i < capsuleList.size(); i++) {
            LinearLayout temp_layout = (LinearLayout) linearLayout.getChildAt(i);
            EditText temp_weighting = (EditText)temp_layout.findViewById(R.id.weight);
            EditText temp_mark = (EditText)temp_layout.findViewById(R.id.mark);

            //Check if mark is filled in
            if (temp_mark.getText().toString().length() > 0
                    && !temp_mark.getText().toString().equals(".")) {
                EditText temp_total = (EditText)temp_layout.findViewById(R.id.total);

                //Check if total is filled in
                if (temp_total.getText().toString().length() > 0
                        && !temp_total.getText().toString().equals(".")) {
                    double weighting_value = Double.parseDouble(temp_weighting.getText().toString());
                    double mark_value = Double.parseDouble(temp_mark.getText().toString());
                    double total_value = Double.parseDouble(temp_total.getText().toString());

                    if (total_value > 0) {
                        final_mark += (weighting_value / total_weight) * (mark_value / total_value);
                    }
                    else {
                        error_toast.setText(R.string.total_zero_toast);
                        error_toast.show();

                        return;
                    }
                }
                else {
                    error_toast.setText(R.string.total_alert_toast);
                    error_toast.show();

                    return;
                }
            }
            else {
                //Could be a case of reverse calculation
                //Check if final is filled
                if (final_editText.getText().toString().length() > 0
                        && !final_editText.getText().toString().equals(".")
                        && !reverse_calculation) {
                    reverse_calculation = true;
                    reverse_final = Double.parseDouble(final_editText.getText().toString());
                    reverse_mark_editText = temp_mark;

                    EditText temp_total = (EditText)temp_layout.findViewById(R.id.total);
                    reverse_mark_editTotal = temp_total;

                    //Check if total is filled in
                    if (temp_total.getText().toString().length() > 0
                            && !temp_total.getText().toString().equals(".")) {
                        //Change reverse total from default and record the weighting
                        double total_value = Double.parseDouble(temp_total.getText().toString());


                        if (total_value > 0) {
                            reverse_total = total_value;
                        }
                        else {
                            error_toast.setText(R.string.total_zero_toast);
                            error_toast.show();

                            return;
                        }
                    }

                    double weighting_value = Double.parseDouble(temp_weighting.getText().toString());
                    reverse_weighting = weighting_value;
                }
                else {
                    error_toast.setText(R.string.mark_final_alert_toast);
                    error_toast.show();

                    return;
                }
            }
        }

        //Display rounded final mark or reverse calculation mark
        if (reverse_calculation) {
            double reverse_mark = (reverse_final / 100) - (final_mark);
            reverse_mark = (reverse_mark / (reverse_weighting / total_weight)) * reverse_total;
            reverse_mark = roundTwoDecimals(reverse_mark);

            reverse_mark_editText.setText(Double.toString(reverse_mark));
            reverse_mark_editTotal.setText(Double.toString(reverse_total));
            reverse_mark_editText.requestFocus();
        }
        else {
            final_mark = final_mark * 100;
            final_mark = roundTwoDecimals(final_mark);
            final_editText.setText(Double.toString(final_mark));
        }
    }

    double roundTwoDecimals(double mark) {
        //Rounds to two decimals
        DecimalFormat twoDecimals = new DecimalFormat("#.##");
        return Double.valueOf(twoDecimals.format(mark));
    }
}