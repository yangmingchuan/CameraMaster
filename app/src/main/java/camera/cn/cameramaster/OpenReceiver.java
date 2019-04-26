package camera.cn.cameramaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import camera.cn.cameramaster.ui.MainActivity;

/**
 * 开机
 * @packageName: cn.tongue.tonguecamera
 * @fileName: OpenReceiver
 * @date: 2019/1/28  13:09
 * @author: ymc
 * @QQ:745612618
 */

public class OpenReceiver  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("broadCastReceiver","onReceiver...");

        Intent mBootIntent = new Intent(context, MainActivity.class);
        // 必须设置FLAG_ACTIVITY_NEW_TASK
        mBootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mBootIntent);

    }

}