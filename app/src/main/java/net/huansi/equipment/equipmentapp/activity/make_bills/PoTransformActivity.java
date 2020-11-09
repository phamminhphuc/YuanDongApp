package net.huansi.equipment.equipmentapp.activity.make_bills;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import net.huansi.equipment.equipmentapp.R;
import net.huansi.equipment.equipmentapp.activity.BaseActivity;
import net.huansi.equipment.equipmentapp.activity.LoginActivity;
import net.huansi.equipment.equipmentapp.activity.MainActivity;
import net.huansi.equipment.equipmentapp.activity.check_goods.CheckMainActivity;
import net.huansi.equipment.equipmentapp.adapter.HsArrayAdapter;
import net.huansi.equipment.equipmentapp.adapter.HsBaseAdapter;
import net.huansi.equipment.equipmentapp.entity.HsWebInfo;
import net.huansi.equipment.equipmentapp.entity.LogBillGroup;
import net.huansi.equipment.equipmentapp.entity.PoTransformBaseInfo;
import net.huansi.equipment.equipmentapp.entity.PoTransformDayInfo;
import net.huansi.equipment.equipmentapp.entity.PoTransformMainInfo;
import net.huansi.equipment.equipmentapp.entity.TransformDayInfo;
import net.huansi.equipment.equipmentapp.helpers.LocaleHelper;
import net.huansi.equipment.equipmentapp.listener.WebListener;
import net.huansi.equipment.equipmentapp.util.HexString;
import net.huansi.equipment.equipmentapp.util.NewRxjavaWebUtils;
import net.huansi.equipment.equipmentapp.util.OthersUtil;
import net.huansi.equipment.equipmentapp.util.SPHelper;
import net.huansi.equipment.equipmentapp.util.ViewHolder;
import net.huansi.equipment.equipmentapp.util.VolleyMultipartRequest;
import net.huansi.equipment.equipmentapp.util.VolleySingleton;
import net.huansi.equipment.equipmentapp.widget.LoadProgressDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.OnLongClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

import static net.huansi.equipment.equipmentapp.util.SPHelper.COMPANY_CODE;
import static net.huansi.equipment.equipmentapp.util.SPHelper.ROLE_CODE_KEY;
import static net.huansi.equipment.equipmentapp.util.SPHelper.USER_NO_KEY;

public class PoTransformActivity extends BaseActivity {
    @BindView(R.id.lv_transformSequence)
    ListView lv_Sequence;
    @BindView(R.id.lv_transformProject)
    ListView lv_Project;
    @BindView(R.id.lv_transformKpi_T)
    ListView lv_Ppi_T;
    @BindView(R.id.lv_transformResponsibleMan)
    ListView lv_ResponsibleMan;
    @BindView(R.id.lv_transformAdvanceDay)
    ListView lv_AdvanceDay;
    @BindView(R.id.lv_transformConfirmDate_T)
    ListView lv_ConfirmDate_T;
    @BindView(R.id.lv_transformKpi_A)
    ListView lv_Kpi_A;
    @BindView(R.id.lv_transformConfirmDate_A)
    ListView lv_ConfirmDate_A;
    @BindView(R.id.lv_transformRemark)
    ListView lv_Remark;
    @BindView(R.id.transformPoDate)
    TextView tvDate;
    @BindView(R.id.transformPoNo)
    EditText fepo;
    @BindView(R.id.transformGroupNo)
    TextView group;
    @BindView(R.id.calculatePercentage)
    TextView tvKpi;
    private ArrayAdapter adapter_Sequence;
    private ArrayAdapter adapter_Project;
    private ArrayAdapter adapter_Kpi_T;
    private ArrayAdapter adapter_ResponsibleMan;
    private ArrayAdapter adapter_AdvanceDay;
    private ArrayAdapter adapter_ConfirmDate_T;
    private ArrayAdapter adapter_Kpi_A;
    private ArrayAdapter adapter_ConfirmDate_A;
    private BaseAdapter adapter_Remark;
    private List<String> groupList;

    private List<String> codeList;
    private List<String> sequenceList;
    private List<String> projectList;
    private List<String> kpiList_T;
    private List<String> responsibleManList;
    private List<String> advanceDayList;
    private List<String> confirmDateList_T;
    private List<String> kpiList_A;
    private List<String> confirmDateList_A;
    private List<String> remarkList;


    private String ItemID="";
    private String Factory="";
    private String Area="";
    private String SewLine="";
    private String FEPO="";
    private String TransDate="";



    private List<Integer> monitorLimits=new ArrayList<>();//
    private List<Integer> maintainLimits=new ArrayList<>();//
    private List<Integer> canqianLimits=new ArrayList<>();//
    private List<Integer> improveLimits=new ArrayList<>();//
    private List<Integer> jinshiLimits=new ArrayList<>();//
    private List<Integer> quantityLimits=new ArrayList<>();//

    private LoadProgressDialog dialog;
    private Boolean isBigger=null;
    private List<Boolean> isOk_KPI=new ArrayList<>();
    private List<Boolean> isOk_Date= new ArrayList<>();
    private List<Boolean> isOk_Count=new ArrayList<>();

    TextView txtTransferPOFull,txtGroup,txtTransferPODate
            ,txtQuery,txtOrder,txtProject,txtKPITarget
            ,txtResponsible,txtDayInAdvance,txtDateTargetConfirm
            ,txtKPIActually,txtDateActuallyConfirm,txtRemark
            ,txtSent,txtRateActually,txtCalculate;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_transform_po;
    }

    @Override
    protected void attachBaseContext(Context base){
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void init() {
        setToolBarTitle(getResources().getString(R.string.conversion));
        dialog=new LoadProgressDialog(this);
        initFindActivityID();
        initGroup();
        monitorLimits.add(15);monitorLimits.add(14);monitorLimits.add(26);monitorLimits.add(30);monitorLimits.add(34);
        maintainLimits.add(4); maintainLimits.add(7);
        canqianLimits.add(0);canqianLimits.add(1);canqianLimits.add(2);canqianLimits.add(5);canqianLimits.add(6);canqianLimits.add(8);
        canqianLimits.add(9);canqianLimits.add(17);canqianLimits.add(20);canqianLimits.add(21);canqianLimits.add(24);canqianLimits.add(19);
        improveLimits.add(18);improveLimits.add(22);improveLimits.add(28);improveLimits.add(27);improveLimits.add(33);
        jinshiLimits.add(13);jinshiLimits.add(23);jinshiLimits.add(25);jinshiLimits.add(29);jinshiLimits.add(31);jinshiLimits.add(32);jinshiLimits.add(35);
        quantityLimits.add(3);quantityLimits.add(10);quantityLimits.add(11);quantityLimits.add(12);quantityLimits.add(16);
        String Po = getIntent().getStringExtra("FEPO");
        String SewLine = getIntent().getStringExtra("SEW");
        String TransformDay = getIntent().getStringExtra("DATE");
        this.fepo.setText(Po);
        this.group.setText(SewLine);
        //tvDate.setText(TransformDay);
        if(Po != null && SewLine != null){
            getTransformDate(Po,SewLine);
        }
        renderViewActivity();
    }

    /*
    * Construct base
    * */
    private void initFindActivityID(){
        txtTransferPOFull = (TextView)findViewById(R.id.transformPoNo);
        txtGroup = (TextView)findViewById(R.id.transformGroupNo);
        txtTransferPODate = (TextView)findViewById(R.id.transformPoDate);
        txtQuery = (TextView)findViewById(R.id.transformData_seach);
        txtOrder = (TextView)findViewById(R.id.orderNo);
        txtProject = (TextView)findViewById(R.id.projectName);
        txtKPITarget = (TextView)findViewById(R.id.KPITarget);
        txtResponsible = (TextView)findViewById(R.id.responsibleMan);
        txtDayInAdvance = (TextView)findViewById(R.id.dayInAdvance);
        txtDateTargetConfirm = (TextView)findViewById(R.id.dayTargetConfirm);
        txtKPIActually = (TextView)findViewById(R.id.KPIActually);
        txtDateActuallyConfirm = (TextView)findViewById(R.id.dateActuallyConfirm);
        txtRemark = (TextView)findViewById(R.id.remark);
        txtSent = (TextView)findViewById(R.id.transformData_commit);
        txtRateActually = (TextView)findViewById(R.id.rateActually);
        txtCalculate = (TextView)findViewById(R.id.calculatePercentage);
    }

    private void renderViewActivity(){
        txtTransferPOFull.setHint(getResources().getString(R.string.fepo_code_input));
        txtGroup.setHint(getResources().getString(R.string.choice_a_team));
        txtTransferPODate.setHint(getResources().getString(R.string.transfer_date));
        txtQuery.setText(getResources().getString(R.string.query));
        txtOrder.setText(getResources().getString(R.string.orderNumber));
        txtProject.setText(getResources().getString(R.string.project_title));
        txtKPITarget.setText(getResources().getString(R.string.KPI_target));
        txtResponsible.setText(getResources().getString(R.string.responsible_man));
        txtDayInAdvance.setText(getResources().getString(R.string.date_in_advance));
        txtDateTargetConfirm.setText(getResources().getString(R.string.date_target_confirm));
        txtKPIActually.setText(getResources().getString(R.string.KPI_actually));
        txtDateActuallyConfirm.setText(getResources().getString(R.string.date_actually_confirm));
        txtRemark.setText(getResources().getString(R.string.remark));
        txtSent.setText(getResources().getString(R.string.sent));
        txtRateActually.setText(getResources().getString(R.string.rate_actually));
        txtCalculate.setText(getResources().getString(R.string.calculate_percent));
    }

    @OnClick(R.id.calculatePercentage)
    void calculatePercentage(){
//        if (kpiList_A.contains("")||confirmDateList_A.contains("")){
//            OthersUtil.ToastMsg(this,"全部确认完后方可计算");
//            return;
//        }
        isOk_Count.clear();
        for (int i=0;i<codeList.size();i++){
            if (isOk_KPI.get(i)==true&&isOk_Date.get(i)==true){
                isOk_Count.add(true);
            }
        }
        double div = div(isOk_Count.size(), codeList.size(), 1);


        DecimalFormat df = new DecimalFormat("00.0%");
        tvKpi.setText(df.format(div));
    }
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }



    //判断是否为乱码，如果是返回true
    private boolean isVnMessyCode(String source) {
        boolean flag = false;
        String[] arr = { "\"", "?", "\u0001", "'", "&" ,"�","@"," " };
        for (int i=0;i<arr.length;i++){
          if (source.contains(arr[i])){
              flag = true;
          }
       }
        return flag;
    }


    private void initGroup() {
        String urlGetLineSewERP = "http://10.20.226.96/FEAService/api/ERP";
        groupList=new ArrayList<>();
        VolleyMultipartRequest erpPostRequest = new VolleyMultipartRequest(Request.Method.POST, urlGetLineSewERP, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    JSONArray data = result.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++){
                        JSONObject currentData = (JSONObject) data.get(i);
                        groupList.add(currentData.getString("PostName"));
                    }
                    String status = result.getString("status");
                    String message = result.getString("errMsg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("errMsg");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("key", "ERP");
                params.put("action","getallpostbycompanyforstp");
                params.put("companyCode",SPHelper.getLocalData(getApplicationContext(),COMPANY_CODE,String.class.getName(),"").toString());
                return params;
            }
        };
        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(erpPostRequest);
    }

    @OnClick(R.id.transformGroupNo)
    void selectGroup(){
        if (groupList==null||groupList.isEmpty()){
            return;
        }
        int size = groupList.size();

        final String[] array = groupList.toArray(new String[size]);
        AlertDialog alertDialog = new AlertDialog.Builder(PoTransformActivity.this)
                .setTitle(getResources().getString(R.string.choice_a_team))
                .setIcon(R.drawable.app_icon)
                .setItems(array,  new DialogInterface.OnClickListener() {//添加单选框
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int which) {
                        group.setText(array[which]);

                    }
                }).create();
        alertDialog.show();
    }


    private void initTransformBaseData(final String group, final String po) {
        OthersUtil.showLoadDialog(dialog);
        String urlTransformCheckList = "http://10.20.226.96/FEAService/api/ERP";
        VolleyMultipartRequest erpTransformCheckListRequest = new VolleyMultipartRequest(Request.Method.POST, urlTransformCheckList, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                OthersUtil.dismissLoadDialog(dialog);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    JSONArray data = result.getJSONArray("data");
                    //Construct grid view
                    codeList = new ArrayList<>();
                    sequenceList = new ArrayList<>();
                    projectList = new ArrayList<>();
                    kpiList_T = new ArrayList<>();
                    responsibleManList = new ArrayList<>();
                    advanceDayList = new ArrayList<>();
                    confirmDateList_T = new ArrayList<>();
                    kpiList_A = new ArrayList<>();
                    confirmDateList_A = new ArrayList<>();
                    remarkList = new ArrayList<>();

                    adapter_Sequence=new ArrayAdapter(getApplicationContext(),R.layout.string_item,R.id.text,sequenceList);
                    adapter_Project=new ArrayCoAdapter(getApplicationContext(),R.layout.string_item,R.id.text,projectList);
                    adapter_Kpi_T=new ArrayAdapter(getApplicationContext(),R.layout.string_item,R.id.text,kpiList_T);
                    adapter_ResponsibleMan=new ArrayAdapter(getApplicationContext(),R.layout.string_item,R.id.text,responsibleManList);
                    adapter_AdvanceDay=new ArrayAdapter(getApplicationContext(),R.layout.string_item,R.id.text,advanceDayList);
                    adapter_ConfirmDate_T=new ArrayAdapter(getApplicationContext(),R.layout.string_item,R.id.text,confirmDateList_T);
                    adapter_Kpi_A=new ArrayKpiAdapter(getApplicationContext(),R.layout.string_item,R.id.text,kpiList_A);
                    adapter_ConfirmDate_A=new ArrayDateAdapter(getApplicationContext(),R.layout.string_item,R.id.text,confirmDateList_A);
                    adapter_Remark=new ArrayRemarkAdapter(getApplicationContext(),R.layout.string_item,R.id.text,remarkList);

                    lv_Sequence.setAdapter(adapter_Sequence);
                    lv_Project.setAdapter(adapter_Project);
                    lv_Ppi_T.setAdapter(adapter_Kpi_T);
                    lv_ResponsibleMan.setAdapter(adapter_ResponsibleMan);
                    lv_AdvanceDay.setAdapter(adapter_AdvanceDay);
                    lv_ConfirmDate_T.setAdapter(adapter_ConfirmDate_T);
                    lv_Kpi_A.setAdapter(adapter_Kpi_A);
                    lv_ConfirmDate_A.setAdapter(adapter_ConfirmDate_A);
                    lv_Remark.setAdapter(adapter_Remark);

                    for (int i = 0; i < data.length(); i++){
                        JSONObject currentData = (JSONObject) data.get(i);
                        sequenceList.add((i+1)+"");
                        codeList.add(currentData.getString("Code"));
                        projectList.add(currentData.getString("ProjectName"));
                        kpiList_T.add(currentData.getString("TargetKPI"));
                        responsibleManList.add(currentData.getString("ResponsibleMan"));
                        advanceDayList.add(currentData.getString("AdvanceDay"));
                        confirmDateList_T.add(currentData.getString("TargetConfirmDate"));
                        kpiList_A.add(currentData.getString("ActualKPI"));
                        confirmDateList_A.add(currentData.getString("ActualConfirmDate"));
                        remarkList.add(currentData.getString("Remark"));
                    }

                    for (int i=0; i< data.length(); i++){
                        isOk_Date.add(true);
                        isOk_KPI.add(true);
                    }
                    setListViewHeightBasedOnChildren(lv_Sequence);
                    setListViewHeightBasedOnChildren(lv_Project);
                    setListViewHeightBasedOnChildren(lv_Ppi_T);
                    setListViewHeightBasedOnChildren(lv_ResponsibleMan);
                    setListViewHeightBasedOnChildren(lv_AdvanceDay);
                    setListViewHeightBasedOnChildren(lv_ConfirmDate_T);
                    setListViewHeightBasedOnChildren(lv_Kpi_A);
                    setListViewHeightBasedOnChildren(lv_ConfirmDate_A);
                    setListViewHeightBasedOnChildren(lv_Remark);

                    String date = tvDate.getText().toString();
                    initTransformDay(date);
                    String status = result.getString("status");
                    String message = result.getString("errMsg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                OthersUtil.dismissLoadDialog(dialog);
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("errMsg");
                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                OthersUtil.ToastMsg(PoTransformActivity.this,errorMessage);
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("key", "ERP");
                params.put("action","getpotransformandchecklist");
                params.put("fepoCode", po);
                params.put("sewLine", group);
                params.put("companyCode",SPHelper.getLocalData(getApplicationContext(),COMPANY_CODE,String.class.getName(),"").toString());
                return params;
            }
        };
        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(erpTransformCheckListRequest);
    }



    /*
     * 当ScrollView 与 LiseView 同时滚动计算高度的方法
     * 设置listview 的高度
     * 参数：listivew的findviewbyid
     * */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        try{
            // 获取ListView对应的Adapter
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
                return;
            }
            int totalHeight = 0;
            for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
                // listAdapter.getCount()返回数据项的数目
                View listItem = listAdapter.getView(i, null, listView);
                // 计算子项View 的宽高
                listItem.measure(0, 0);
                // 统计所有子项的总高度
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            // listView.getDividerHeight()获取子项间分隔符占用的高度
            // params.height最后得到整个ListView完整显示需要的高度
            listView.setLayoutParams(params);
        }catch (Exception e){

        }
    }

    @OnClick(R.id.transformPoDate)
    void setDate(){
        String fepo = this.fepo.getText().toString();
        String group = this.group.getText().toString();
        if (fepo.isEmpty()||group.isEmpty()){
           OthersUtil.ToastMsg(this,getResources().getString(R.string.please_type_fepo_or_group));
        }else {
            getTransformDate(fepo,group);
        }

    }

    @OnLongClick(R.id.transformPoDate)
    public boolean changeDate(){
        String user = SPHelper.getLocalData(getApplicationContext(), USER_NO_KEY, String.class.getName(), "").toString();
        if (user.equalsIgnoreCase("A10010")){
            setTransformDate();
        }
        return true;
    }

    private void getTransformDate(final String po, final String group) {
        OthersUtil.showLoadDialog(dialog);
        String urlGetTransformDate = "http://10.20.226.96/FEAService/api/ERP";
        VolleyMultipartRequest erpTransformDateRequest = new VolleyMultipartRequest(Request.Method.POST, urlGetTransformDate, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    OthersUtil.dismissLoadDialog(dialog);
                    JSONObject result = new JSONObject(resultResponse);
                    String onlineDate = result.getString("data");
                    tvDate.setText(onlineDate);
                    OthersUtil.ToastMsg(PoTransformActivity.this,getResources().getString(R.string.input_machine_number_and_group_success));
                    String status = result.getString("status");
                    String message = result.getString("errMsg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                OthersUtil.dismissLoadDialog(dialog);
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("errMsg");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                OthersUtil.ToastMsg(PoTransformActivity.this, errorMessage);
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("key", "ERP");
                params.put("action","gettransformdatebyfepo");
                params.put("fepoCode", po);
                params.put("groupNo", group);
                params.put("companyCode",SPHelper.getLocalData(getApplicationContext(),COMPANY_CODE,String.class.getName(),"").toString());
                return params;
            }
        };
        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(erpTransformDateRequest);
    }

    private void setTransformDate() {

        final Calendar calendar=Calendar.getInstance();
        DatePickerDialog dialog=new DatePickerDialog(PoTransformActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.e("TAG","month="+month);
                String sMonth = null;
                if (month < 9) {
                    month=month+1;
                    sMonth = "0" + month;
                }else {
                    month=month+1;
                    sMonth=""+month;
                }
                String sDay = null;
                if (dayOfMonth < 10) {
                    sDay = "0" + dayOfMonth;
                }else {
                    sDay=""+dayOfMonth;
                }
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                String sHour=null;
                if (hour<10){
                    sHour="0"+hour;
                }else {
                    sHour=""+hour;
                }
                String sMinute=null;
                if (minute<10){
                    sMinute="0"+minute;
                }else {
                    sMinute=""+minute;
                }
                tvDate.setText(year + "-" + sMonth + "-" +sDay);

            }
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }


    @OnClick(R.id.transformData_seach)
    void data_search(){
        String po = fepo.getText().toString().trim();
        String mgroup = group.getText().toString().trim();
        String date = tvDate.getText().toString().trim();
        if (po.isEmpty()||mgroup.isEmpty()||date.isEmpty()){
            OthersUtil.ToastMsg(getApplicationContext(),getResources().getString(R.string.have_space_empty_in_condition));
            return;
        }else {
            initTransformBaseData(mgroup,po);
        }
    }


    private void initTransformDay(final String transformDay) {
        OthersUtil.showLoadDialog(dialog);
        String urlGetTransform15Date = "http://10.20.226.96/FEAService/api/ERP";
        VolleyMultipartRequest erpTransformDate15Request = new VolleyMultipartRequest(Request.Method.POST, urlGetTransform15Date, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                ArrayList dataArraylist = new ArrayList();
                try {
                    OthersUtil.dismissLoadDialog(dialog);
                    JSONObject result = new JSONObject(resultResponse);
                    JSONArray data = result.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++){
                        JSONObject current_data = (JSONObject) data.get(i);
                        dataArraylist.add(current_data.getString("the_date"));
                    }
                    confirmDateList_T.set(0,dataArraylist.get(8).toString());
                    confirmDateList_T.set(1,dataArraylist.get(7).toString());
                    confirmDateList_T.set(2,dataArraylist.get(7).toString());
                    confirmDateList_T.set(3,dataArraylist.get(7).toString());
                    confirmDateList_T.set(4,dataArraylist.get(7).toString());
                    confirmDateList_T.set(5,dataArraylist.get(5).toString());
                    confirmDateList_T.set(6,dataArraylist.get(5).toString());
                    confirmDateList_T.set(7,dataArraylist.get(5).toString());
                    confirmDateList_T.set(8,dataArraylist.get(5).toString());
                    confirmDateList_T.set(9,dataArraylist.get(5).toString());
                    confirmDateList_T.set(10,dataArraylist.get(5).toString());
                    confirmDateList_T.set(11,dataArraylist.get(5).toString());
                    confirmDateList_T.set(12,dataArraylist.get(5).toString());
                    confirmDateList_T.set(13,dataArraylist.get(5).toString());
                    confirmDateList_T.set(14,dataArraylist.get(4).toString());
                    confirmDateList_T.set(15,dataArraylist.get(4).toString());
                    confirmDateList_T.set(16,dataArraylist.get(4).toString());
                    confirmDateList_T.set(17,dataArraylist.get(4).toString());
                    confirmDateList_T.set(18,dataArraylist.get(4).toString());
                    confirmDateList_T.set(19,dataArraylist.get(4).toString());
                    confirmDateList_T.set(20,dataArraylist.get(4).toString());
                    confirmDateList_T.set(21,dataArraylist.get(3).toString());
                    confirmDateList_T.set(22,dataArraylist.get(3).toString());
                    confirmDateList_T.set(23,dataArraylist.get(3).toString());
                    confirmDateList_T.set(24,dataArraylist.get(2).toString());
                    confirmDateList_T.set(25,dataArraylist.get(2).toString());
                    confirmDateList_T.set(26,dataArraylist.get(1).toString());
                    confirmDateList_T.set(27,dataArraylist.get(1).toString());
                    confirmDateList_T.set(28,dataArraylist.get(0).toString());
                    confirmDateList_T.set(29,dataArraylist.get(0).toString());
                    confirmDateList_T.set(30,dataArraylist.get(0).toString());
                    confirmDateList_T.set(31,dataArraylist.get(0).toString());
                    confirmDateList_T.set(32,dataArraylist.get(0).toString());
                    confirmDateList_T.set(33,dataArraylist.get(0).toString());
                    confirmDateList_T.set(34,dataArraylist.get(0).toString());
                    confirmDateList_T.set(35,dataArraylist.get(0).toString());
                    adapter_ConfirmDate_T.notifyDataSetChanged();
                    String status = result.getString("status");
                    String message = result.getString("errMsg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                OthersUtil.dismissLoadDialog(dialog);
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("errMsg");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                OthersUtil.ToastMsg(PoTransformActivity.this, errorMessage);
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("key", "ERP");
                params.put("action","getpotransform15date");
                params.put("transformDay", transformDay);
                params.put("companyCode",SPHelper.getLocalData(getApplicationContext(),COMPANY_CODE,String.class.getName(),"").toString());
                return params;
            }
        };
        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(erpTransformDate15Request);
    }



    @OnItemClick(R.id.lv_transformKpi_A)
    void setLv_Kpi_A(int position){
        Log.e("TAG",position+"m");
        findKPI(position);
    }


    private void findKPI(final int position){
        android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(this);
        View areaDialogView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.area_input_dialog,null);
        final EditText editText= (EditText) areaDialogView.findViewById(R.id.etInventoryAreaDialog);
        final TextView textViewKPIA = (TextView) areaDialogView.findViewById(R.id.tvInventoryAreaTitle);
        editText.setHint(getResources().getString(R.string.please_input_value));
        textViewKPIA.setText(getResources().getString(R.string.KPI_actually));
        editText.setTextColor(Color.BLACK);
        builder.setView(areaDialogView)
                .setPositiveButton(getResources().getString(R.string.complete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        final String area=editText.getText().toString().trim();
                        if(area.isEmpty()){
                            OthersUtil.dialogNotDismissClickOut(dialogInterface);
                            OthersUtil.ToastMsg(getApplicationContext(),getResources().getString(R.string.please_input_value));
                            return;
                        }

                        Float kpi = Float.parseFloat(area);

                        kpiList_A.set(position,Chufa(kpi,1));
//                        Float a = Float.parseFloat(kpiList_A.get(position));
//                        Float t = Float.parseFloat(kpiList_T.get(position));
//                        if (a<t){
//                            lv_Kpi_A.getChildAt(position).setBackgroundColor(Color.RED);
//                        }else {
//                            lv_Kpi_A.getChildAt(position).setBackgroundColor(Color.WHITE);
//                        }
                        adapter_Kpi_A.notifyDataSetChanged();
                        OthersUtil.dialogDismiss(dialogInterface);
                        dialogInterface.dismiss();
                        //ChangePoActivity.this.KPI=area;
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        kpiList_A.set(position,"0.00");
                        lv_Kpi_A.getChildAt(position).setBackgroundColor(Color.WHITE);
                        adapter_Kpi_A.notifyDataSetChanged();
                        OthersUtil.dialogDismiss(dialogInterface);
                        dialogInterface.dismiss();

                    }
                })
                .setCancelable(true)
                .show();
    }


    public static String Chufa(Float a,int b) {
        //“0.00000000”确定精度
        DecimalFormat dF = new DecimalFormat("0.00");
        return dF.format((float)a/b);
    }


    @OnItemClick(R.id.lv_transformConfirmDate_A)
    void setLv_ConfirmDate_A(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(PoTransformActivity.this);
        builder.setTitle(getResources().getString(R.string.confirm)).setMessage(getResources().getString(R.string.set_current_date_as_confirm_date)).setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO:
                String date = findDate();
                confirmDateList_A.set(position, date);
                adapter_ConfirmDate_A.notifyDataSetChanged();
                //OthersUtil.ToastMsg(PoTransformActivity.this, "Confirm");
            }
        }).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO:
                confirmDateList_A.set(position, "");
                lv_ConfirmDate_A.getChildAt(position).setBackgroundColor(Color.WHITE);
                adapter_ConfirmDate_A.notifyDataSetChanged();
                //OthersUtil.ToastMsg(PoTransformActivity.this,"Cancel");
            }
        }).setCancelable(true).show();
//        OthersUtil.showDoubleChooseDialog(this, getResources().getString(R.string.set_current_date_as_confirm_date), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //取消
//                confirmDateList_A.set(position, "");
//                lv_ConfirmDate_A.getChildAt(position).setBackgroundColor(Color.WHITE);
//                adapter_ConfirmDate_A.notifyDataSetChanged();
//            }
//        }, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //设置数据
//                String date = findDate();
//                confirmDateList_A.set(position, date);
//                        //????
//                //设置颜色
////                boolean b = compareDate(confirmDateList_A.get(position), confirmDateList_T.get(position));
////                if (b){
////                    lv_ConfirmDate_A.getChildAt(position).gett
////                }else {
////                    lv_ConfirmDate_A.getChildAt(position).setBackgroundColor(Color.WHITE);
////                }
//                adapter_ConfirmDate_A.notifyDataSetChanged();
//            }
//        });

    }
    private String findDate() {
        String s = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
        return s;
    }


    @OnItemClick(R.id.lv_transformRemark)
    void setLv_Remark(int position){
        inputRemark(position);
    }

    private void inputRemark(final int position) {
        android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(this);
        View areaDialogView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.area_input_dialog,null);
        final EditText editText= (EditText) areaDialogView.findViewById(R.id.etInventoryAreaDialog);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        String s = remarkList.get(position);
        editText.setText(s);
        final TextView textView= (TextView) areaDialogView.findViewById(R.id.tvInventoryAreaTitle);
        editText.setHint(getResources().getString(R.string.please_type_a_description));
        textView.setText(getResources().getString(R.string.remark));
        editText.setTextColor(Color.BLACK);
        builder.setView(areaDialogView)
                .setPositiveButton(getResources().getString(R.string.complete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {

                        final String area=editText.getText().toString().trim();
                        if(area.isEmpty()){
                            OthersUtil.dialogNotDismissClickOut(dialogInterface);
                            OthersUtil.ToastMsg(getApplicationContext(),getResources().getString(R.string.please_type_a_content));
                            return;
                        }
                        if (area.contains(",")){
                            area.replace(",",";");
                        }
                        remarkList.set(position,area);
                        adapter_Remark.notifyDataSetChanged();
                        OthersUtil.dialogDismiss(dialogInterface);
                        dialogInterface.dismiss();
                        //ChangePoActivity.this.KPI=area;
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        OthersUtil.dialogDismiss(dialogInterface);
                        dialogInterface.dismiss();

                    }
                })
                .setCancelable(true)
                .show();
    }

    @OnClick(R.id.transformData_commit)
    void commitData(){
        String urlgetPOTransformAndCheckMain = "http://10.20.226.96/FEAService/api/ERP";
        final String po = fepo.getText().toString().trim();
        final String group = this.group.getText().toString().trim();
        final String transDate = this.tvDate.getText().toString().trim();

        if (po.isEmpty()||transDate.isEmpty()){
            OthersUtil.ToastMsg(this,getResources().getString(R.string.can_not_sent_while_fepo_or_date_empty));
            return;
        }

        OthersUtil.showLoadDialog(dialog);
        VolleyMultipartRequest erpTransformMainRequest = new VolleyMultipartRequest(Request.Method.POST, urlgetPOTransformAndCheckMain, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    OthersUtil.dismissLoadDialog(dialog);
                    JSONObject result = new JSONObject(resultResponse);
                    JSONArray data = result.getJSONArray("data");
                    Log.e("TAG", data.toString());
                    JSONObject current_data = (JSONObject) data.get(0);
                    ItemID = current_data.getString("ItemID");
                    Factory = current_data.getString("Factory");
                    Area = current_data.getString("ProducingArea");
                    SewLine = current_data.getString("SewLine");
                    FEPO = current_data.getString("Fepo").toUpperCase();
                    TransDate = current_data.getString("TransformDay");
                    String status = result.getString("status");
                    String message = result.getString("errMsg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                OthersUtil.dismissLoadDialog(dialog);
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("errMsg");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                OthersUtil.ToastMsg(PoTransformActivity.this, errorMessage);
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("key", "ERP");
                params.put("action","getpotransformchecklistmain");
                params.put("sewLine", group);
                params.put("fepo", po);
                params.put("companyCode",SPHelper.getLocalData(getApplicationContext(),COMPANY_CODE,String.class.getName(),"").toString());
                return params;
            }
        };
        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(erpTransformMainRequest);
        OthersUtil.showDoubleChooseDialog(this, getResources().getString(R.string.sent_confirm), null, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                int size = codeList.size();
                String user = SPHelper.getLocalData(getApplicationContext(), USER_NO_KEY, String.class.getName(), "").toString();
                String date = findDate();
                String uuid = UUID.randomUUID().toString();
                OthersUtil.showLoadDialog(dialog);
                submitTransformMainData(user, date,"Main");
                for (int i=0; i<size; i++){
                    submitTransformData(i, user, date,"Detail");
                }
                OthersUtil.dismissLoadDialog(dialog);
                finish();
                OthersUtil.ToastMsg(getApplicationContext(),getResources().getString(R.string.sent_success));
            }
        });
    }

    private void submitTransformData(final int position, final String user, final String date,  final String type) {
        String urlInsertPOTransformAndCheckBillDetail = "http://10.20.226.96/FEAService/api/ERP/InsertPOTransformCheckListDetail";
        StringRequest erpInsertPOTransformDetailRequest = new StringRequest(Request.Method.POST, urlInsertPOTransformAndCheckBillDetail, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //TODO:
                //Log.e("TAG", "onResponse: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e("TAG", "onErrorResponse: " + error.getMessage());
                OthersUtil.ToastMsg(getApplicationContext(),error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("billItemID", ItemID);
                params.put("inforType", type);
                params.put("factory", Factory);
                params.put("productArea", Area);
                params.put("sewLine", SewLine);
                params.put("fepo", FEPO);
                params.put("transformDay",date);
                params.put("createUser",user);
                params.put("createDate", date);
                params.put("lastUpdateUser", user);
                params.put("lastUpdateDate", date);
                params.put("code", codeList.get(position));
                params.put("projectName", projectList.get(position));
                params.put("targetKPI", kpiList_T.get(position));
                params.put("actualKPI", kpiList_A.get(position).replace(",",".").isEmpty() ? "0.00" : kpiList_A.get(position).replace(",","."));
                params.put("targetConfirmDate",confirmDateList_T.get(position));
                params.put("actualConfirmDate",confirmDateList_A.get(position).isEmpty() ? "1900-01-01" : confirmDateList_A.get(position));
                params.put("advanceDay", advanceDayList.get(position));
                params.put("responsibleMan",responsibleManList.get(position));
                params.put("remark", remarkList.get(position).isEmpty() ? "Remark..." : remarkList.get(position));
                params.put("companyCode",SPHelper.getLocalData(getApplicationContext(),COMPANY_CODE,String.class.getName(),"").toString());
                return params;
            }
        };
//        VolleyMultipartRequest erpInsertPOTransformDetailRequest = new VolleyMultipartRequest(Request.Method.POST, urlInsertPOTransformAndCheckBillDetail, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(NetworkResponse response) {
//                String resultResponse = new String(response.data);
//                try {
//                    OthersUtil.dismissLoadDialog(dialog);
//                    JSONObject result = new JSONObject(resultResponse);
//                    String status = result.getString("status");
//                    String message = result.getString("errMsg");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                NetworkResponse networkResponse = error.networkResponse;
//                String errorMessage = "Unknown error";
//                OthersUtil.dismissLoadDialog(dialog);
//                if (networkResponse == null) {
//                    if (error.getClass().equals(TimeoutError.class)) {
//                        errorMessage = "Request timeout";
//                    } else if (error.getClass().equals(NoConnectionError.class)) {
//                        errorMessage = "Failed to connect server";
//                    }
//                } else {
//                    String result = new String(networkResponse.data);
//                    try {
//                        JSONObject response = new JSONObject(result);
//                        String status = response.getString("status");
//                        String message = response.getString("errMsg");
//
//                        Log.e("Error Status", status);
//                        Log.e("Error Message", message);
//
//                        if (networkResponse.statusCode == 404) {
//                            errorMessage = "Resource not found";
//                        } else if (networkResponse.statusCode == 401) {
//                            errorMessage = message+" Please login again";
//                        } else if (networkResponse.statusCode == 400) {
//                            errorMessage = message+ " Check your inputs";
//                        } else if (networkResponse.statusCode == 500) {
//                            errorMessage = message+" Something is getting wrong";
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//                OthersUtil.ToastMsg(PoTransformActivity.this, errorMessage);
//                Log.i("Error", errorMessage);
//                error.printStackTrace();
//            }
//        }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
////                params.put("key", "ERP");
////                params.put("action","insertpotransformchecklistdetail");
//                params.put("billItemID", ItemID);
//                params.put("inforType", type);
//                params.put("factory", Factory);
//                params.put("productArea", Area);
//                params.put("sewLine", SewLine);
//                params.put("fepo", FEPO);
//                params.put("transformDay", TransDate);
//                params.put("createUser",user);
//                params.put("createDate", date);
//                params.put("lastUpdateUser", user);
//                params.put("lastUpdateDate", date);
//                params.put("code", codeList.get(position));
//                params.put("projectName", projectList.get(position));
//                params.put("targetKPI", kpiList_T.get(position));
//                params.put("actualKPI", kpiList_A.get(position).replace(",","."));
//                params.put("targetConfirmDate",confirmDateList_T.get(position));
//                params.put("actualConfirmDate",confirmDateList_A.get(position));
//                params.put("advanceDay", advanceDayList.get(position));
//                params.put("responsibleMan",responsibleManList.get(position));
//                params.put("remark", remarkList.get(position));
//                params.put("companyCode",SPHelper.getLocalData(getApplicationContext(),COMPANY_CODE,String.class.getName(),"").toString());
//                return params;
//            }
//        };
        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(erpInsertPOTransformDetailRequest);
//        NewRxjavaWebUtils.getUIThread(NewRxjavaWebUtils.getObservable(PoTransformActivity.this, "")
//                        .map(new Func1<String, HsWebInfo>() {
//                            @Override
//                            public HsWebInfo call(String s) {
//                                return NewRxjavaWebUtils.getJsonData(getApplicationContext(),
//                                        "spAPP_SubmitPOTransformAndCheck",
//                                        "BillItemID="+ ItemID+
//                                                ",InfoType="+type+
//                                                ",M_Factory="+Factory+
//                                                ",M_ProducingArea="+Area+
//                                                ",M_SewLine="+SewLine+
//                                                ",M_Fepo="+FEPO+
//                                                ",M_TransformDay="+TransDate+
//                                                ",M_CreateUser="+""+
//                                                ",M_CreateDate="+""+
//                                                ",M_LastUpdateUser="+user+
//                                                ",M_LastUpdateDate="+date+
//                                                ",D_Code="+codeList.get(position)+
//                                                ",D_ProjectName="+projectList.get(position)+
//                                                ",D_TargetKPI="+Float.parseFloat(kpiList_T.get(position))+
//                                                ",D_ActualKPI="+Float.parseFloat(kpiList_A.get(position))+
//                                                ",D_TargetConfirmDate="+confirmDateList_T.get(position)+
//                                                ",D_ActualConfirmDate="+confirmDateList_A.get(position)+
//                                                ",D_AdvanceDay="+Integer.parseInt(advanceDayList.get(position))+
//                                                ",D_ResponsibleMan="+responsibleManList.get(position)+
//                                                ",D_Remark="+remarkList.get(position),
//                                        String.class.getName(),
//                                        false,
//                                        "helloWorld");
//                            }
//                        })
//                ,PoTransformActivity.this, dialog, new WebListener() {
//                    @Override
//                    public void success(HsWebInfo hsWebInfo) {
//                        String json = hsWebInfo.json;
//                        Log.e("TAG", "isSuccess="+hsWebInfo.json);
//
//                    }
//
//                    @Override
//                    public void error(HsWebInfo hsWebInfo) {
//                        Log.e("TAG", "errorMeeting="+hsWebInfo.json);
//                        //OthersUtil.ToastMsg(PoTransformActivity.this,"提交有误!!!");
//                    }
//                });
    }

    private void submitTransformMainData( final String user, final String date,  final String type) {
        String urlInsertPOTransformAndCheckBillMain = "http://10.20.226.96/FEAService/api/ERP";
        VolleyMultipartRequest erpInsertPoTransformMainRequest = new VolleyMultipartRequest(Request.Method.POST, urlInsertPOTransformAndCheckBillMain, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    OthersUtil.dismissLoadDialog(dialog);
                    JSONObject result = new JSONObject(resultResponse);
                    String status = result.getString("status");
                    String message = result.getString("errMsg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                OthersUtil.dismissLoadDialog(dialog);
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("errMsg");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                OthersUtil.ToastMsg(PoTransformActivity.this, errorMessage);
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("key", "ERP");
                params.put("action","insertpotransformchecklistmain");
                params.put("billItemID", ItemID);
                params.put("inforType", type);
                params.put("factory", Factory);
                params.put("productArea", Area);
                params.put("sewLine", SewLine);
                params.put("fepo", FEPO);
                params.put("transformDay", tvDate.getText().toString());
                params.put("createUser",user);
                params.put("createDate", date);
                params.put("lastUpdateUser", user);
                params.put("lastUpdateDate", date);
                params.put("code", "");
                params.put("projectName", "");
                params.put("targetKPI", "0.00");
                params.put("actualKPI", "0.00");
                params.put("targetConfirmDate","");
                params.put("actualConfirmDate","");
                params.put("advanceDay", "");
                params.put("responsibleMan","");
                params.put("remark", "");
                params.put("companyCode",SPHelper.getLocalData(getApplicationContext(),COMPANY_CODE,String.class.getName(),"").toString());
                return params;
            }
        };
        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(erpInsertPoTransformMainRequest);
//        NewRxjavaWebUtils.getUIThread(NewRxjavaWebUtils.getObservable(PoTransformActivity.this, "")
//                        .map(new Func1<String, HsWebInfo>() {
//                            @Override
//                            public HsWebInfo call(String s) {
//                                return NewRxjavaWebUtils.getJsonData(getApplicationContext(),
//                                        "spAPP_SubmitPOTransformAndCheck",
//                                        "BillItemID="+ ItemID+
//                                                ",InfoType="+type+
//                                                ",M_Factory="+Factory+
//                                                ",M_ProducingArea="+Area+
//                                                ",M_SewLine="+SewLine+
//                                                ",M_Fepo="+FEPO.toUpperCase().trim()+
//                                                ",M_TransformDay="+tvDate.getText().toString()+
//                                                ",M_CreateUser="+user+
//                                                ",M_CreateDate="+date+
//                                                ",M_LastUpdateUser="+user+
//                                                ",M_LastUpdateDate="+date+
//                                                ",D_Code="+""+
//                                                ",D_ProjectName="+""+
//                                                ",D_TargetKPI="+0.0+
//                                                ",D_ActualKPI="+0.0+
//                                                ",D_TargetConfirmDate="+""+
//                                                ",D_ActualConfirmDate="+""+
//                                                ",D_AdvanceDay="+0+
//                                                ",D_ResponsibleMan="+""+
//                                                ",D_Remark="+"",
//                                        String.class.getName(),
//                                        false,
//                                        "helloWorld");
//                            }
//                        })
//                ,PoTransformActivity.this, dialog, new WebListener() {
//                    @Override
//                    public void success(HsWebInfo hsWebInfo) {
//                        String json = hsWebInfo.json;
//                        Log.e("TAG", "isSuccess="+hsWebInfo.json);
//
//                    }
//
//                    @Override
//                    public void error(HsWebInfo hsWebInfo) {
//                        Log.e("TAG", "errorMeeting="+hsWebInfo.json);
//                        //OthersUtil.ToastMsg(PoTransformActivity.this,"提交有误!!!");
//                    }
//                });
    }







    private class ArrayRemarkAdapter extends ArrayAdapter {


        public ArrayRemarkAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public boolean isEnabled(int position) {
            String user = SPHelper.getLocalData(getApplicationContext(), USER_NO_KEY, String.class.getName(), "").toString();
            switch (user){
                case "A10086"://班长
                    if (!monitorLimits.contains(position)){
                        return false;
                    }
                    break;
                case "A10000"://保全
                    if (!maintainLimits.contains(position)){
                        return false;
                    }
                    break;
                case "A10010"://产前
                    if (!canqianLimits.contains(position)){
                        return false;
                    }
                    break;
                case "B10086"://改善小组
                    if (!improveLimits.contains(position)){
                        return false;
                    }
                    break;
                case "B10000"://精实
                    if (!jinshiLimits.contains(position)){
                        return false;
                    }
                    break;
                case "B10010"://品管
                    if (!quantityLimits.contains(position)){
                        return false;
                    }
                    break;
            }
            return super.isEnabled(position);
        }

//        @NonNull
//        @Override
//        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup viewGroup) {
//            if (convertView==null) convertView=LayoutInflater.from(getApplicationContext()).inflate(R.layout.string_item,viewGroup,false);
//            TextView textView = (TextView) convertView.findViewById(R.id.text);
//            String s = projectList.get(position);
//            textView.setText(s);
//
//            return convertView;
//        }



    }


    private class ArrayKpiAdapter extends ArrayAdapter {


        public ArrayKpiAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public boolean isEnabled(int position) {
            String user = SPHelper.getLocalData(getApplicationContext(), USER_NO_KEY, String.class.getName(), "").toString();
            switch (user){
                case "A10086"://班长
                    if (!monitorLimits.contains(position)){
                        return false;
                    }
                    break;
                case "A10000"://保全
                    if (!maintainLimits.contains(position)){
                        return false;
                    }
                    break;
                case "A10010"://产前
                    if (!canqianLimits.contains(position)){
                        return false;
                    }
                    break;
                case "B10086"://改善小组
                    if (!improveLimits.contains(position)){
                        return false;
                    }
                    break;
                case "B10000"://精实
                    if (!jinshiLimits.contains(position)){
                        return false;
                    }
                    break;
                case "B10010"://品管
                    if (!quantityLimits.contains(position)){
                        return false;
                    }
                    break;
            }
            return super.isEnabled(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                //We must create a View:
                convertView = LayoutInflater.from(PoTransformActivity.this).inflate(R.layout.string_item, parent, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            String s = kpiList_A.get(position);
            String s1 = kpiList_T.get(position);
            textView.setText(s);
            NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());

            float a = 0;
            try {
                a = nf.parse(s).floatValue();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Float a = Float.parseFloat(s.replace(",","."));
            Float t = Float.parseFloat(s1);
            if (a<t){
                textView.setTextColor(Color.RED);
                isOk_KPI.set(position,false);
            }else {
                textView.setTextColor(Color.BLACK);
                isOk_KPI.set(position,true);
            }
            //Here we can do changes to the convertView, such as set a text on a TextView
            //or an image on an ImageView.
            return convertView;
//            if (convertView==null) convertView=LayoutInflater.from(getApplicationContext()).inflate(R.layout.string_item,viewGroup,false);
//            TextView textView = (TextView) convertView.findViewById(R.id.text);
//
//                String s = kpiList_A.get(position);
//                textView.setText(s);
//            //设置颜色
//                Float a = Float.parseFloat(s);
//                Float t = Float.parseFloat(kpiList_T.get(position));
//                if (a<t){
//                    textView.setTextColor(Color.RED);
//                    isOk_KPI.set(position,false);
//                }else {
//                    textView.setTextColor(Color.BLACK);
//                    //isOk_KPI.add(position,true);
//                }
//            //adapter_Kpi_A.notifyDataSetChanged();
//            return convertView;
        }



    }

    private class ArrayDateAdapter extends ArrayAdapter {


        public ArrayDateAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public boolean isEnabled(int position) {
            String user = SPHelper.getLocalData(getApplicationContext(), USER_NO_KEY, String.class.getName(), "").toString();
            switch (user){
                case "A10086"://班长
                    if (!monitorLimits.contains(position)){
                        return false;
                    }
                    break;
                case "A10000"://保全
                    if (!maintainLimits.contains(position)){
                        return false;
                    }
                    break;
                case "A10010"://产前
                    if (!canqianLimits.contains(position)){
                        return false;
                    }
                    break;
                case "B10086"://改善小组
                    if (!improveLimits.contains(position)){
                        return false;
                    }
                    break;
                case "B10000"://精实
                    if (!jinshiLimits.contains(position)){
                        return false;
                    }
                    break;
                case "B10010"://品管
                    if (!quantityLimits.contains(position)){
                        return false;
                    }
                    break;
            }
            return super.isEnabled(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup viewGroup) {
            if (convertView==null) convertView=LayoutInflater.from(getApplicationContext()).inflate(R.layout.string_item,viewGroup,false);
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            String s = confirmDateList_A.get(position);
            textView.setText(s);

            if (!s.isEmpty()){
                //设置颜色
                boolean b = compareDate(s, confirmDateList_T.get(position));
                if (b){
                    textView.setTextColor(Color.RED);
                    isOk_Date.set(position,false);
                }else {
                    textView.setTextColor(Color.BLACK);
                    isOk_Date.add(position,true);
                }
            }

            return convertView;
        }



    }







    private class ArrayCoAdapter extends ArrayAdapter<String> {


        public ArrayCoAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List objects) {
            super(context, resource, textViewResourceId, objects);
        }



        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup viewGroup) {
            if (convertView==null) convertView=LayoutInflater.from(getApplicationContext()).inflate(R.layout.string_item,viewGroup,false);
            TextView textView = (TextView) convertView.findViewById(R.id.text);
            String s = projectList.get(position);
            textView.setText(s);
            String user = SPHelper.getLocalData(getApplicationContext(), USER_NO_KEY, String.class.getName(), "").toString();
            switch (user){
                case "A10086"://班长
                    if (monitorLimits.contains(position)){
                        textView.setTextColor(Color.BLUE);
                    }
                    break;
                case "A10000"://保全
                    if (maintainLimits.contains(position)){
                        textView.setTextColor(Color.BLUE);
                    }
                    break;
                case "A10010"://产前
                    if (canqianLimits.contains(position)){
                        textView.setTextColor(Color.BLUE);
                    }
                    break;
                case "B10086"://改善小组
                    if (improveLimits.contains(position)){
                        textView.setTextColor(Color.BLUE);
                    }
                    break;
                case "B10000"://精实
                    if (jinshiLimits.contains(position)){
                        textView.setTextColor(Color.BLUE);
                    }
                    break;
                case "B10010"://品管
                    if (quantityLimits.contains(position)){
                        textView.setTextColor(Color.BLUE);
                    }
                    break;
            }
            return convertView;
        }
    }



    private  boolean compareDate(String startTime, String endTime) {//true不符合要求要显示红色
        if (!startTime.isEmpty()&&!endTime.isEmpty()) {
            String replace = startTime.replace("-", "");
            String replace1 = endTime.replace("-", "");
            int start = Integer.parseInt(replace);
            int end = Integer.parseInt(replace1);
            if (start>end){
                isBigger=true;
            }else {
                isBigger=false;
            }
        }
        return isBigger;
    }
}
