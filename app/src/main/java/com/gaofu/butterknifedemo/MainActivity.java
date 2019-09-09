package com.gaofu.butterknifedemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gaofu.annotation.BindView;
import com.gaofu.annotation.OnClick;
import com.gaofu.library.ButterKnife;

/**
 * @author Gaofu
 * Time 2019-09-09 12:10
 */
public class MainActivity extends Activity {

    @BindView(R.id.tv)
    TextView tv;
    @BindView(R.id.btn)
    Button btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn)
    public void click() {
        Toast.makeText(this, btn.getText().toString(), Toast.LENGTH_SHORT).show();
    }

}
