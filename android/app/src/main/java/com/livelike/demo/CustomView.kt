package com.livelike.demo

import android.graphics.Color
import android.widget.FrameLayout
import android.widget.TextView
import android.content.Context
import android.widget.Toast

class CustomView(context: Context) : FrameLayout(context) {
    init {
        // set padding and background color
        this.setPadding(16, 16, 16, 16);
        this.setBackgroundColor(Color.parseColor("#5FD3F3"));

        // add default text view
        val text = TextView(context);
        text.setText("Welcome to Android Fragments with React Native.");
        text.setOnClickListener({
            Toast.makeText(context,"Whoaaa",Toast.LENGTH_LONG).show()
        })
        this.addView(text);
    }
}