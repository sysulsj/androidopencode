package gdmap.com.androidcode.helper;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/3/2.
 * 监听短信内容变化从而获取相应验证码
 */
public class SmsObserver extends ContentObserver{

    private Context mContext;
    private Handler mHandler;
    public SmsObserver(Context context,Handler handler)
    {
        super(handler);
        mContext=context;
        mHandler=handler;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.e("短信信息", uri.toString());
        //这种情况剔除
        if(uri.toString().equals("content://sms/raw"))
            return;
        String code;
        Uri inboxUri=Uri.parse("content://sms/inbox");
        Cursor cursor=mContext.getContentResolver().query(inboxUri,null,null,null,"date desc");
        if(cursor!=null)
        {
            if(cursor.moveToFirst())
            {
                String address=cursor.getString(cursor.getColumnIndex("address"));
                String body=cursor.getString(cursor.getColumnIndex("body"));
                //若指定具体的手机号可以加上这一句
//                if(!address.equals("13417745428"))
//                    return;
                mHandler.obtainMessage(1,body).sendToTarget();//测试，返回监听的短信内容
                Log.e("短信信息","发件人："+address+",发送内容:"+body);
                Pattern pattern=Pattern.compile("(\\d{6})");
                Matcher matcher=pattern.matcher(body);
                if(matcher.find())
                {
                    code=matcher.group(0);
                    mHandler.obtainMessage(1,code).sendToTarget();
                    Log.e("短信信息","收到的短信验证码:"+code);
                }
            }
            cursor.close();
        }

    }
}
