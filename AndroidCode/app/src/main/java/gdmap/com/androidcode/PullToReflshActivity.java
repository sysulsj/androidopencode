package gdmap.com.androidcode;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Arrays;


public class PullToReflshActivity extends Activity {


    private  PullToRefreshListView mPullToRefreshList;
    private ArrayList<String> mCityList;
    private ArrayAdapter<String> mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_to_reflsh);
        mPullToRefreshList=(PullToRefreshListView)findViewById(R.id.list_pull_reflesh);
        mPullToRefreshList.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        //刷新操作触发
        mPullToRefreshList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                //在这里获取刷新数据，一般通过异步获取数据来实现
                new GetDataTask().execute();
            }
        });
        String[] citys={"北京","上海","南京","广州","中山","珠海","深圳"};
        mCityList=new ArrayList<>();
        mCityList.addAll(Arrays.asList(citys));
        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,mCityList);
        mPullToRefreshList.setAdapter(mAdapter);
    }

    //创建一个异步任务来模拟数据刷新过程
    private class GetDataTask extends AsyncTask<Void,Void,String>
    {

        @Override
        protected String doInBackground(Void... params) {
            //执行获取数据等耗时操作
            String newStr="新增城市";
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return newStr;
        }

        @Override
        protected void onPostExecute(String s) {
            //更新UI
            mCityList.add(s);
            mAdapter.notifyDataSetChanged();
            //需手动设置完成更新操作
            mPullToRefreshList.onRefreshComplete();
            super.onPostExecute(s);
        }
    }
}
