package jp.hanatoya.training.coach;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;

import org.parceler.Parcels;

import jp.hanatoya.training.R;
import jp.hanatoya.training.json.RunnerData;
import jp.hanatoya.training.util.BusProvider;

/**
 * Created by Martin on 2015/05/24.
 */
public class DialogPickRunner  extends DialogFragment {

    private AQuery a;

    public static DialogPickRunner newInstance(RunnerData runnerData){
        Bundle bundle = new Bundle();
        bundle.putParcelable("data", Parcels.wrap(runnerData));
        DialogPickRunner dialogPickRunner = new DialogPickRunner();
        dialogPickRunner.setArguments(bundle);
        return  dialogPickRunner;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.confirm_runner);
        return dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_pick_runner, container);
        a = new AQuery(rootView);
        final RunnerData runnerData = Parcels.unwrap(getArguments().getParcelable("data"));
        a.id(R.id.text).text(runnerData.getRunnerName() + " : " + runnerData.getDistance() + getString(R.string.m));

//        a.id(android.R.id.edit).getEditText().addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                if (editable.toString().length() > 1){
//                    a.id(R.id.ok).enabled(true).getTextView().setTextColor(getResources().getColor(R.color.grey_dk2));
//                }else{
//                    a.id(R.id.ok).enabled(false).getTextView().setTextColor(getResources().getColor(R.color.grey_lt1));
//                }
//            }
//        });

        a.id(R.id.cancel).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        a.id(R.id.ok).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BusProvider.getInstance().post(new RunnerConfirmedEvent(runnerData));
                dismiss();
            }
        });
        return rootView;
    }

    public static class RunnerConfirmedEvent{
        public RunnerData runnerData;

        public RunnerConfirmedEvent(RunnerData runnerData) {
            this.runnerData = runnerData;
        }
    }

}
