package com.icanvass.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.icanvass.R;
import com.icanvass.dummy.DummyContent;
import com.icanvass.helpers.CommonHelpers;
import com.icanvass.helpers.SDDefine;
import com.icanvass.objects.CustomValuesObject;
import com.icanvass.objects.PINObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by romek on 10.05.2014.
 */
public class PinDetailsAdapter extends ArrayAdapter {

    ArrayList<String> mAddressHeaders = new ArrayList<String>();
    private PINObject mPin = null;
    private ArrayList<CustomValuesObject> mQuestions = new ArrayList<CustomValuesObject>();
    private LinkedHashMap<String, JSONObject> mFilteredFields = new LinkedHashMap<String, JSONObject>();
    private boolean udFlag = true;

    public PinDetailsAdapter(Context context, List<DummyContent.DummyItem> objects) {
        super(context, R.layout.pin_list_item, objects);
        mAddressHeaders.add("Status");
        mAddressHeaders.add("Number");
        mAddressHeaders.add("Street");
        mAddressHeaders.add("Unit");
    }

    public void setmFields(LinkedHashMap<String, JSONObject> fields) {
        for (Map.Entry entry : fields.entrySet()) {
            for (CustomValuesObject question : mQuestions){
                String key = question.DefinitionId + "";
                if (key.equals(entry.getKey())) {
                    mFilteredFields.put((String) entry.getKey(), (JSONObject) entry.getValue());
                }
            }
        }

    }

    public void setQuestions(ArrayList<CustomValuesObject> mQuestions) {
        this.mQuestions = mQuestions;
    }

    public void setPin(PINObject mPin) {
        this.mPin = mPin;
    }

    @Override
    public int getCount() {
        return 5 + (mQuestions != null ? mQuestions.size() : 0);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (position == 0) {
            convertView = inflater.inflate(R.layout.details_list_status, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.details_list_item_2, parent, false);
        }
        TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
        final TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
        if (position == 0) {
            text1 = (TextView) convertView.findViewById(R.id.tvAddress);
            text1.setText(mPin.Status);
        } else if (position < 4) {
            text1.setText(mAddressHeaders.get(position));
            if (position == 1) {
                text2.setText(""+mPin.Location.HouseNumber);
            } else if (position == 2) {
                text2.setText(mPin.Location.Street);
            } else if (position == 3) {
                String unit = mPin.Location.Unit;
                if (unit.equals("null")) unit = "";
                text2.setText(unit);
            }
        } else if (position == 4) {
            text1.setText("");
            text2.setText("");
        } else {
            if (mFilteredFields != null && mFilteredFields.size() > 0) {
                // getting field first - to save fields order
                JSONObject field = (JSONObject) getValueFromMapByIndex(position - 5, mFilteredFields);
                CustomValuesObject question = getByValue(Integer.parseInt((String) mFilteredFields.keySet().toArray()[position - 5]), mQuestions);
                if (field != null) {
                    text1.setText(field.optString("Name"));
                }
                if (question == null) return convertView;
                if (!question.DateTimeValue.equalsIgnoreCase("null") && !question.DateTimeValue.equalsIgnoreCase("")) {
                    String time = CommonHelpers.convertStringToAnotherFormat(question.DateTimeValue,
                            SDDefine.simpleServerFormat, SDDefine.localFormat);
                    text2.setText(time);
                    text2.setOnClickListener(null);
                } else {
                    if (!question.DecimalValue.equals("null")) {
                        text2.setText(question.DecimalValue);
                    } else if (!question.StringValue.equals("null")) {
                        text2.setText(question.StringValue);
                    }

                    if (text1.getText().toString().equals("Notes")) {
                        final ImageView icon = (ImageView) convertView.findViewById(R.id.image1);
                        text2.setMaxLines(4);
                        icon.setImageResource(R.drawable.downarrow);
                        text2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i("udFlag", String.valueOf(udFlag));
                                if (udFlag) {
                                    text2.setMaxLines(20);
                                    icon.setImageResource(R.drawable.uparrow);
                                    final ListView lv = (ListView) parent;
                                    lv.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Select the last row so it will scroll into view...
                                            lv.smoothScrollToPosition(position);
                                        }
                                    });
                                    udFlag = false;
                                } else {
                                    text2.setMaxLines(4);
                                    icon.setImageResource(R.drawable.downarrow);
                                    udFlag = true;
                                }
                            }
                        });
//                        text2.post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                int lineCount = text2.getLineCount();
//                                if(lineCount >=4) {
//                                    text2.setMaxLines(4);
//                                    icon.setImageResource(R.drawable.downarrow);
//                                };
//                            }
//                        });
                    }
                }
            }
        }
        return convertView;
    }

    private Object getValueFromMapByIndex(int id, Map map){
        Iterator iterator = map.entrySet().iterator();
        int n = 0;
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            if(n == id){
                return entry.getValue();
            }
            n++;
        }
        return null;
    }

    private CustomValuesObject getByValue(int value, ArrayList<CustomValuesObject> arrayList){
        for (CustomValuesObject obj : arrayList){
            if (obj.DefinitionId == value){
                return obj;
            }
        }
        return null;
    }
}
