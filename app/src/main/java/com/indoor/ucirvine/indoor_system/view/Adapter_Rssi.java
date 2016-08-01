package com.indoor.ucirvine.indoor_system.view;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.indoor.ucirvine.indoor_system.R;
import com.indoor.ucirvine.indoor_system.rssiData;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-06-29.
 */
public class Adapter_Rssi  extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<rssiData> listView = new ArrayList<rssiData>() ;

    public static ArrayList<rssiData> device_1 = new ArrayList<rssiData>();
    public static ArrayList<rssiData> device_2 = new ArrayList<rssiData>();
    public static ArrayList<rssiData> device_3 = new ArrayList<rssiData>();

    public static ArrayList<rssiData> device_4 = new ArrayList<rssiData>();


    // ListViewAdapter의 생성자
    public Adapter_Rssi() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listView.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_viewlist, parent, false);
        }

        TextView device_name = ViewHolderHelper.get(convertView, R.id.device_name);
        TextView device_address = ViewHolderHelper.get(convertView, R.id.device_address);
        TextView timeStamp = ViewHolderHelper.get(convertView, R.id.timeStamp);
        TextView rssi = ViewHolderHelper.get(convertView, R.id.rssi);

        rssiData item_list_view = listView.get(position);

        device_name.setText(" "+item_list_view.getDeviceName());
        device_address.setText(" "+item_list_view.getDeviceAddress());
        timeStamp.setText(" "+item_list_view.getTimeStamp());
        rssi.setText(" "+item_list_view.getRssi());

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listView.get(position) ;
    }

    public void clear(){
        listView.clear();
    }

    public void addItem(String name, String address, String time, String rssi, String distance) {
        rssiData item = new rssiData();

//        if (address.equals("B8:27:EB:A6:A1:E9") || address.equals("B8:27:EB:26:28:F4") || address.equals("B8:27:EB:25:31:D6")) {

            item.setDeviceName(name);
            item.setDeviceAddress(address);
            item.setTimeStamp(time);
            item.setRssi(rssi);
            item.setDistance(distance);

            if (address.equals("B8:27:EB:A6:A1:E9"))
                device_1.add(item);

            if (address.equals("B8:27:EB:26:28:F4"))
                device_2.add(item);

            if (address.equals("B8:27:EB:25:31:D6"))
                device_3.add(item);

            if (address.equals("B8:27:EB:3A:91:F4"))
                device_4.add(item);


            listView.add(item);
//        }
    }

    public static class ViewHolderHelper{
        public static <T extends View> T get(View convertView, int id) {

            SparseArray<View> viewHolder = (SparseArray<View>) convertView.getTag();

            if (viewHolder == null) {

                viewHolder = new SparseArray<View>();
                convertView.setTag(viewHolder);
            }

            View childView = viewHolder.get(id);

            if (childView == null) {

                childView = convertView.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;
        }
    }


}
