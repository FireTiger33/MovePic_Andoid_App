package com.stacktivity.movepic.movepic.binded_buttons;

import android.view.View;

import androidx.annotation.NonNull;

class ButtonAddViewHolder extends BaseButtonViewHolder{
    public ButtonAddViewHolder(@NonNull View itemView, View.OnClickListener clickListener) {
        super(itemView);

        itemView.setOnClickListener(clickListener);
    }
}
