package com.gson8.floatingballwindow;

/*
 * FloatingBallWindow making by Syusuke/琴声悠扬 on 2016/6/1
 * E-Mail: Zyj7810@126.com
 * Package: com.gson8.floatingballwindow.PopupService
 * Description: null
 */

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PopupService extends Service implements View.OnClickListener {

    /**
     * 控件window
     */
    private FloatingView mFloatingWindow;

    /**
     * 两个状态的View
     */
    private View mFloatView;
    private View mPopupView;


    /**
     * 相关控件
     */
    private TextView mTvShowShotTime;

    private ImageView mIvShotGif;
    private ImageView mIvShotVideo;
    private ImageView mIvShotFolder;
    private ImageView mIvShotSettings;


    @Override
    public IBinder onBind(Intent intent) {
        return new PopupBinder();
    }

    public class PopupBinder extends Binder {
        public PopupService getService() {
            return PopupService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initFloatingWindow();

    }

    private void initFloatingWindow() {
        mFloatView = LayoutInflater.from(this).inflate(R.layout.folating_view, null);
        mPopupView = LayoutInflater.from(this).inflate(R.layout.popup_view, null);

        mTvShowShotTime = (TextView) mFloatView.findViewById(R.id.id_show_shot_time);

        mIvShotGif = (ImageView) mPopupView.findViewById(R.id.id_pop_shot_gif);
        mIvShotVideo = (ImageView) mPopupView.findViewById(R.id.id_pop_shot_video);
        mIvShotFolder = (ImageView) mPopupView.findViewById(R.id.id_pop_shot_folder);
        mIvShotSettings = (ImageView) mPopupView.findViewById(R.id.id_pop_shot_settings);

        mIvShotGif.setOnClickListener(this);
        mIvShotVideo.setOnClickListener(this);
        mIvShotFolder.setOnClickListener(this);
        mIvShotSettings.setOnClickListener(this);


        mFloatingWindow = new FloatingView(this);
        mFloatingWindow.setFloatingView(mFloatView);
        mFloatingWindow.setPopupView(mPopupView);
    }

    public void show() {
        if(null != mFloatingWindow)
            mFloatingWindow.show();
    }

    public void dimiss() {
        if(null != mFloatingWindow)
            mFloatingWindow.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.id_pop_shot_gif:
                Toast.makeText(this, "gif", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_pop_shot_video:
                Toast.makeText(this, "video", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_pop_shot_folder:
                Toast.makeText(this, "folder", Toast.LENGTH_SHORT).show();
                break;
            case R.id.id_pop_shot_settings:
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        mFloatingWindow.turnMini();
    }


}
