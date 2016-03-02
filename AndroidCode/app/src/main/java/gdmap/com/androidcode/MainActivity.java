package gdmap.com.androidcode;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setListAdapter(new SimpleAdapter(this,getData(),android.R.layout.simple_list_item_1,new String[]{"title"},new int[]{android.R.id.text1}));
        createSliderMenu();
    }

    protected List<Map<String, Object>> getData() {
        List<Map<String, Object>> intentList=new ArrayList<>();
        addItem(intentList,"下拉刷新效果",new Intent(this,PullToReflshActivity.class));
        return intentList;
    }
    protected void addItem(List<Map<String, Object>> list,String title,Intent intent)
    {
        Map<String,Object> map=new HashMap<>();
        map.put("title",title);
        map.put("intent",intent);
        list.add(map);
    }

    //创建滑动菜单
    private void createSliderMenu() {
        SlidingMenu slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);//设置弹出模式
        // 设置触摸屏幕的模式
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        // 设置滑动菜单视图的宽度
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 设置渐入渐出效果的值
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.layout_slider_menu);

        //菜单栏内容设置
        ListView list_city = (ListView) slidingMenu.findViewById(R.id.list_city);
        String[] citys = {"北京", "上海", "南京", "广州", "中山", "珠海", "深圳"};
        ArrayList<String> items = new ArrayList<>();
        items.addAll(Arrays.asList(citys));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);
        list_city.setAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Map<String,Object> map=(Map<String,Object>)l.getItemAtPosition(position);
        Intent intent=(Intent) map.get("intent");
        startActivity(intent);
    }
}
