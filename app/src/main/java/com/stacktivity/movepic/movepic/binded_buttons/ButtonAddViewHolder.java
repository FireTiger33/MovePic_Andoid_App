package com.stacktivity.movepic.movepic.binded_buttons;

import android.support.annotation.NonNull;
import android.view.View;

class ButtonAddViewHolder extends BaseButtonViewHolder{
    public ButtonAddViewHolder(@NonNull View itemView, View.OnClickListener clickListener) {
        super(itemView);

        itemView.setOnClickListener(clickListener);
    }
}
