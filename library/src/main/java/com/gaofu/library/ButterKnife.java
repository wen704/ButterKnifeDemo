package com.gaofu.library;

import android.app.Activity;

/**
 * @author Gaofu
 * Time 2019-09-06 14:50
 */
public class ButterKnife {

    public static void bind(Activity activity) {
        // 拼接一个类名 MainActivity$ViewBinder
        String className = activity.getClass().getName() + "$ViewBinder";
        try {
            // ViewBinder 接口的实现类
            Class clazz = Class.forName(className);
            // 接口 = 接口的实现类
            ViewBinder viewBinder = (ViewBinder) clazz.newInstance();
            // 调用接口的 bind 方法
            viewBinder.bind(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
