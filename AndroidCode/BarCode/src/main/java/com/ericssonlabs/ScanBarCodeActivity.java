package com.ericssonlabs;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.zxing.activity.CaptureActivity;
import com.zxing.encoding.EncodingHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

//识别和生成二维码功能实现
public class ScanBarCodeActivity extends Activity implements View.OnClickListener {

    private final int CHOOSE_PIC = 0;
    private final int CAMERA_PIC = 1;
    private EditText mTxtBarCode;
    private TextView mTxtScanResult;
    private ImageView mImgBarCode;
    private Bitmap mBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        initView();
    }

    //处理返回结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //获取识别结果
            if (requestCode == CHOOSE_PIC) {
                //获取选择图片的绝对路径
                String[] prj = {MediaStore.Images.Media.DATA};
                String imgPath = null;
                try {
                    Cursor cursor = ScanBarCodeActivity.this.getContentResolver().query(data
                            .getData(), prj, null, null, null);

                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        imgPath = cursor.getString(index);
                    }
                    cursor.close();
                    //获取解析结果
                    Result result = parseQRcodeBitmap(imgPath);
                    mTxtScanResult.setText(result.toString());
                } catch (Exception e) {
                    mTxtScanResult.setText("");
                    show("无法识别图片二维码");
                    e.printStackTrace();
                }

            } else if (requestCode == CAMERA_PIC) {
                String result = data.getExtras().getString("result");
                mTxtScanResult.setText(result);
            }
        }
    }

    //初始化控件
    private void initView() {
        mTxtBarCode = (EditText) findViewById(R.id.txt_barcode);
        mTxtScanResult = (TextView) findViewById(R.id.txt_scan_result);
        mImgBarCode = (ImageView) findViewById(R.id.img_barcode);
        findViewById(R.id.btn_scan_picture).setOnClickListener(this);
        findViewById(R.id.btn_scan_camera).setOnClickListener(this);
        findViewById(R.id.btn_create_code).setOnClickListener(this);
        mImgBarCode.setOnClickListener(this);
    }

    //解析二维码图片,返回结果封装在Result对象中
    private com.google.zxing.Result parseQRcodeBitmap(String bitmapPath) {
        //解析转换类型UTF-8
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        //获取到待解析的图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        //如果我们把inJustDecodeBounds设为true，那么BitmapFactory.decodeFile(String path, Options opt)
        //并不会真的返回一个Bitmap给你，它仅仅会把它的宽，高取回来给你
        options.inJustDecodeBounds = true;
        //此时的bitmap是null，这段代码之后，options.outWidth 和 options.outHeight就是我们想要的宽和高了
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        //我们现在想取出来的图片的边长（二维码图片是正方形的）设置为400像素
        /**
         options.outHeight = 400;
         options.outWidth = 400;
         options.inJustDecodeBounds = false;
         bitmap = BitmapFactory.decodeFile(bitmapPath, options);
         */
        //以上这种做法，虽然把bitmap限定到了我们要的大小，但是并没有节约内存，如果要节约内存，我们还需要使用inSimpleSize这个属性
        options.inSampleSize = options.outHeight / 400;
        if (options.inSampleSize <= 0) {
            options.inSampleSize = 1; //防止其值小于或等于0
        }
        /**
         * 辅助节约内存设置
         *
         * options.inPreferredConfig = Bitmap.Config.ARGB_4444;    // 默认是Bitmap.Config.ARGB_8888
         * options.inPurgeable = true;
         * options.inInputShareable = true;
         */
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        //新建一个RGBLuminanceSource对象，将bitmap图片传给此对象
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(bitmap);
        //将图片转换成二进制图片
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        //初始化解析对象
        QRCodeReader reader = new QRCodeReader();
        //开始解析
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.btn_scan_picture) {
            //选择一张本地图片识别二维码
            chooseOnePicture();
        } else if (id == R.id.btn_scan_camera) {
            //打开摄像头进行扫描识二维码
            Intent intent = new Intent(this, CaptureActivity.class);
            startActivityForResult(intent, CAMERA_PIC);
        } else if (id == R.id.btn_create_code) {
            //生成二维码
            String barcode = mTxtBarCode.getText().toString().trim();
            if (barcode.equals("")) {
                show("输入的二维码文本不能为空");
                return;
            }
            try {
                Bitmap barcodeBitmap = EncodingHandler.createQRCode(barcode, 400);
                //mBitmap=barcodeBitmap;
                mImgBarCode.setImageBitmap(barcodeBitmap);
            } catch (WriterException e) {
                mImgBarCode.setImageResource(-1);
                show("生成二维码失败");
                e.printStackTrace();
            }
        }
        else if(id==R.id.img_barcode)
        {
            SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd HHmmss");
            String name=format.format(new Date());
            String dir=Environment.getExternalStorageDirectory()+"/二维码";
            String imgPath= dir+"/"+name+".png";
            File file=new File(dir);
            if(file.exists()==false)
                file.mkdir();
            BitmapDrawable bitmapDrawable=(BitmapDrawable)mImgBarCode.getDrawable();
//             mImgBarCode.buildDrawingCache();
//             Bitmap bitmap = mImgBarCode.getDrawingCache();
             saveImageToGallery(this, bitmapDrawable.getBitmap(), imgPath);
        }
    }

    /*
    选择一张本地图片
     */
    private void chooseOnePicture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
//        if (android.os.Build.VERSION.SDK_INT < 19) {
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//        } else {
//            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//        }
        intent.setType("image/*");
        Intent selectIntent = Intent.createChooser(intent, "选择二维码图片");
        startActivityForResult(selectIntent, CHOOSE_PIC);
    }

    //提示
    private void show(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //Uri和Bitmap之间的转换
    private Bitmap converToBitmap(Uri uri) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //保存Bitmap
    private Uri saveBitmap(Bitmap bitmap, String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
            return Uri.fromFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //保存图片到图库中
    public  void saveImageToGallery(Context context, Bitmap bmp,String fileName) {
        // 首先保存图片
        File file = new File(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
            // 其次把文件插入到系统图库
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), "二维码",null);
            // 最后通知图库更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + fileName)));
            show("已保存二维码图片到图库");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
