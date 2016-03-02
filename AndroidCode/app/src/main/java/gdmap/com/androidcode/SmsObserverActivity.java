package gdmap.com.androidcode;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;


import gdmap.com.androidcode.helper.SmsObserver;


public class SmsObserverActivity extends Activity {
    SmsObserver mSmsObserver;
    EditText mTxtCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_observer);
        mTxtCode=(EditText)findViewById(R.id.txt_code);
        mSmsObserver=new SmsObserver(this,mHandler);
        Uri uri=Uri.parse("content://sms");
        getContentResolver().registerContentObserver(uri, true, mSmsObserver);
    }
    private Handler mHandler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1)
            {
                String code=msg.obj.toString();
                mTxtCode.setText(code);
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mSmsObserver);
    }

}
