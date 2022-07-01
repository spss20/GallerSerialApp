package com.ssoftwares.newgaller.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ssoftwares.newgaller.R;
import com.ssoftwares.newgaller.modals.InputObject;

import java.util.ArrayList;

public class InputsAdapter extends RecyclerView.Adapter<InputsAdapter.InputViewHolder> {

    private Context mContext;
    private ArrayList<InputObject> inputConfigs;
    private boolean isInputStates;

    public InputsAdapter(Context context, ArrayList<InputObject> inputConfigs, boolean isInputStates) {
        mContext = context;
        this.inputConfigs = inputConfigs;
        this.isInputStates = isInputStates;
    }

    @NonNull
    @Override
    public InputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (isInputStates) {
            view = LayoutInflater.from(mContext).inflate(R.layout.input_states_item, parent, false);
        } else
            view = LayoutInflater.from(mContext).inflate(R.layout.input_configure_item, parent, false);
        return new InputViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InputViewHolder holder, int position) {
        InputObject input = inputConfigs.get(position);
        holder.inputId.setText("Input ID: " + input.getId());
        if (input.isValue()){
            holder.group.check(R.id.button2);
        } else holder.group.check(R.id.button1);
        holder.group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.button1){
                    input.setValue(false);
                } else input.setValue(true);
            }
        });
//        holder.inputSwitch.setChecked(input.isValue());
//        holder.inputSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                input.setValue(b);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return inputConfigs.size();
    }

    class InputViewHolder extends RecyclerView.ViewHolder {
        TextView inputId;
//        Switch inputSwitch;
        RadioGroup group;
        InputViewHolder(@NonNull View itemView) {
            super(itemView);
            inputId = itemView.findViewById(R.id.input_id);
            group = itemView.findViewById(R.id.input_group);
//            inputSwitch = itemView.findViewById(R.id.on_off_switch);
        }
    }

    public ArrayList<InputObject> getList() {
        return inputConfigs;
    }

    public void updateData(ArrayList<InputObject> inputConfigs) {
        this.inputConfigs = inputConfigs;
        notifyDataSetChanged();
    }
}
