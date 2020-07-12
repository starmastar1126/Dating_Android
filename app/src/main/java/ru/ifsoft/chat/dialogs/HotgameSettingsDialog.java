package ru.ifsoft.chat.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import ru.ifsoft.chat.R;
import ru.ifsoft.chat.constants.Constants;


public class HotgameSettingsDialog extends DialogFragment implements Constants {

    CheckBox genderMaleCheckBox, genderFemaleCheckBox, likedCheckBox, matchesCheckBox;
    Spinner sexOrientationSpinner;

    private int searchGender, searchSexOrientation, hotgameLiked, hotgameMatches;

    /** Declaring the interface, to invoke a callback function in the implementing activity class */
    AlertPositiveListener alertPositiveListener;

    /** An interface to be implemented in the hosting activity for "OK" button click listener */
    public interface AlertPositiveListener {

        public void onCloseHotgameSettingsDialog(int searchGender, int searchSexOrientation, int liked, int matches);
    }

    /** This is a callback method executed when this fragment is attached to an activity.
     *  This function ensures that, the hosting activity implements the interface AlertPositiveListener
     * */
    public void onAttach(android.app.Activity activity) {

        super.onAttach(activity);

        try {

            alertPositiveListener = (AlertPositiveListener) activity;

        } catch(ClassCastException e){

            // The hosting activity does not implemented the interface AlertPositiveListener
            throw new ClassCastException(activity.toString() + " must implement AlertPositiveListener");
        }
    }

    /** This is the OK button listener for the alert dialog,
     *  which in turn invokes the method onPositiveClick(position)
     *  of the hosting activity which is supposed to implement it
     */
    OnClickListener positiveListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            alertPositiveListener.onCloseHotgameSettingsDialog(searchGender, searchSexOrientation, hotgameLiked, hotgameMatches);
        }
    };

    /** This is the OK button listener for the alert dialog,
     *  which in turn invokes the method onPositiveClick(position)
     *  of the hosting activity which is supposed to implement it
     */
    OnClickListener negativeListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            //
        }
    };

    /** This is a callback method which will be executed
     *  on creating this fragment
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /** Getting the arguments passed to this fragment */
        Bundle bundle = getArguments();

        searchGender = bundle.getInt("hotgameGender");
        searchSexOrientation = bundle.getInt("hotgameSexOrientation");
        hotgameLiked = bundle.getInt("hotgameLiked");
        hotgameMatches = bundle.getInt("hotgameMatches");

        /** Creating a builder for the alert dialog window */
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        /** Setting a title for the window */
        b.setTitle(getText(R.string.label_hotgame_dialog_title));

        LinearLayout view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_hotgame_settings, null);

        b.setView(view);

        genderMaleCheckBox = (CheckBox) view.findViewById(R.id.genderMaleCheckBox);
        genderFemaleCheckBox = (CheckBox) view.findViewById(R.id.genderFemaleCheckBox);

        likedCheckBox = (CheckBox) view.findViewById(R.id.likedCheckBox);
        matchesCheckBox = (CheckBox) view.findViewById(R.id.matchesCheckBox);

        sexOrientationSpinner = (Spinner) view.findViewById(R.id.sexOrientationSpinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item , android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexOrientationSpinner.setAdapter(spinnerAdapter);
        spinnerAdapter.add(getString(R.string.label_sex_orientation_any));
        spinnerAdapter.add(getString(R.string.sex_orientation_1));
        spinnerAdapter.add(getString(R.string.sex_orientation_2));
        spinnerAdapter.add(getString(R.string.sex_orientation_3));
        spinnerAdapter.add(getString(R.string.sex_orientation_4));
        spinnerAdapter.notifyDataSetChanged();

        setGender(searchGender);
        setLiked(hotgameLiked);
        setMatches(hotgameMatches);
        sexOrientationSpinner.setSelection(searchSexOrientation);


        /** Setting a positive button and its listener */

        b.setPositiveButton(getText(R.string.action_ok), positiveListener);

        b.setNegativeButton(getText(R.string.action_cancel), negativeListener);


        b.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    return true;
                }

                return true;
            }
        });

        /** Creating the alert dialog window using the builder class */
        final AlertDialog d = b.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                final DialogInterface dlg = dialog;

                final Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something

                        d.dismiss();
                        alertPositiveListener.onCloseHotgameSettingsDialog(getGender(), sexOrientationSpinner.getSelectedItemPosition(), getLiked(), getMatches());
                    }
                });

                Button p = d.getButton(AlertDialog.BUTTON_NEGATIVE);
                p.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something

                        d.dismiss();
                    }
                });
            }
        });

        d.setCanceledOnTouchOutside(false);
        d.setCancelable(false);

        /** Return the alert dialog window */
        return d;
    }

    public int getLiked() {

        if (likedCheckBox.isChecked()) {

            return 1;
        }

        return 0;
    }

    public void setLiked(int liked) {

        if (liked == 0) {

            likedCheckBox.setChecked(false);

        } else {

            likedCheckBox.setChecked(true);
        }
    }

    public int getMatches() {

        if (matchesCheckBox.isChecked()) {

            return 1;
        }

        return 0;
    }

    public void setMatches(int matches) {

        if (matches == 0) {

            matchesCheckBox.setChecked(false);

        } else {

            matchesCheckBox.setChecked(true);
        }
    }

    public int getGender() {

        if (genderFemaleCheckBox.isChecked() && genderMaleCheckBox.isChecked()) {

            return -1;
        }

        if (genderMaleCheckBox.isChecked()) {

            return 0;
        }

        if (genderFemaleCheckBox.isChecked()) {

            return 1;
        }

        return -1;
    }

    public void setGender(int gender) {

        switch (gender) {

            case 0: {

                genderMaleCheckBox.setChecked(true);
                genderFemaleCheckBox.setChecked(false);

                break;
            }

            case 1: {

                genderMaleCheckBox.setChecked(false);
                genderFemaleCheckBox.setChecked(true);

                break;
            }

            default: {

                genderMaleCheckBox.setChecked(true);
                genderFemaleCheckBox.setChecked(true);

                break;
            }
        }
    }
}