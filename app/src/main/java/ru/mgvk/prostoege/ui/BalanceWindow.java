package ru.mgvk.prostoege.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.R;

/**
 * Created by mihail on 08.10.16.
 */
public class BalanceWindow extends Dialog implements View.OnClickListener {

    private static final int DONAT_1 = 0;
    private static final int DONAT_2 = 1;
    private static final int DONAT_3 = 2;
    Context context;

    private LinearLayout price1Btn;
    private TextView     price1Virt;
    private TextView     price1Real;
    private LinearLayout price2Btn;
    private TextView     price2Virt;
    private TextView     price2Real;
    private LinearLayout price3Btn;
    private TextView     price3Virt;
    private TextView     price3Real;
    private TextView     codeBtn;
    private TextView     closeBtn;

    public BalanceWindow(Context context) {
        super(context);
        this.context = context;
    }

    public static void show(Context context) {
        new BalanceWindow(context).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.balance_dialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawableResource(R.drawable.empty_color);
        assignViews();
    }

    //     view.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (v.getTag() != null && ((Integer) v.getTag()) == 1 && (++count) == 20) {
//                        Toast.makeText(context, "Made by MGVK: vk.com/mihailllo",
//                                Toast.LENGTH_SHORT).show();
//                        count = 0;
//                    }
//                }
//            });

    private void assignViews() {
        price1Btn = (LinearLayout) findViewById(R.id.price1_btn);
        price1Virt = (TextView) findViewById(R.id.price1_virt);
        price1Real = (TextView) findViewById(R.id.price1_real);
        price2Btn = (LinearLayout) findViewById(R.id.price2_btn);
        price2Virt = (TextView) findViewById(R.id.price2_virt);
        price2Real = (TextView) findViewById(R.id.price2_real);
        price3Btn = (LinearLayout) findViewById(R.id.price3_btn);
        price3Virt = (TextView) findViewById(R.id.price3_virt);
        price3Real = (TextView) findViewById(R.id.price3_real);
        codeBtn = (TextView) findViewById(R.id.code_btn);
        closeBtn = (TextView) findViewById(R.id.btn_close);

        price1Btn.setOnClickListener(this);
        price2Btn.setOnClickListener(this);
        price3Btn.setOnClickListener(this);
        codeBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {

            if (v == price1Btn) {
                ((MainActivity) context).pays.buyPack(DONAT_1);
            }
            if (v == price2Btn) {
                ((MainActivity) context).pays.buyPack(DONAT_2);
            }
            if (v == price3Btn) {
                ((MainActivity) context).pays.buyPack(DONAT_3);
            }

            if (v == codeBtn) {
                Toast.makeText(context, "Функция в разработке!", Toast.LENGTH_SHORT).show();
                // TODO: 11.02.18  сделать ввод кода
            }

        } catch (Exception e) {
            Toast.makeText(context, "Произошла ошибка!", Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }
}
