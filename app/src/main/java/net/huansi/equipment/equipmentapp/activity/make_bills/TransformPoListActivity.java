package net.huansi.equipment.equipmentapp.activity.make_bills;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import net.huansi.equipment.equipmentapp.R;
import net.huansi.equipment.equipmentapp.activity.BaseActivity;
import net.huansi.equipment.equipmentapp.activity.logging_bill.LoggingBillActivity;
import net.huansi.equipment.equipmentapp.activity.logging_bill.LoggingBillSearchActivity;
import net.huansi.equipment.equipmentapp.adapter.HsBaseAdapter;
import net.huansi.equipment.equipmentapp.entity.HsWebInfo;
import net.huansi.equipment.equipmentapp.entity.LogBillGroup;
import net.huansi.equipment.equipmentapp.entity.TransformPoDetail;
import net.huansi.equipment.equipmentapp.entity.TransformPoLists;
import net.huansi.equipment.equipmentapp.entity.TransformPoRecords;
import net.huansi.equipment.equipmentapp.listener.WebListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static net.huansi.equipment.equipmentapp.util.SPHelper.COMPANY_CODE;
import static net.huansi.equipment.equipmentapp.util.SPHelper.USER_NO_KEY;

public class TransformPoListActivity extends BaseActivity {

    @BindView(R.id.TransformPoList)
    ListView lv_poList;
    private TransformPoDetail poDetail;
    private List<TransformPoDetail> poDetails;
    private LoadProgressDialog dialog;
    private TransformPoAdapter poAdapter;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_transform_polist;
    }

    @Override
    public void init() {
        TextView subTitle = getSubTitle();

        //转换款页面
        String userCode = SPHelper.getLocalData(getApplicationContext(), USER_NO_KEY, String.class.getName(), "").toString().toUpperCase();
//        if (role.equalsIgnoreCase("A10010")){
//            subTitle.setText("建单");
//            subTitle.setVisibility(View.VISIBLE);
//        }else {
//            subTitle.setText("修改");
//            subTitle.setVisibility(View.VISIBLE);
//        }
        subTitle.setText(getResources().getString(R.string.po_list));
        setToolBarTitle(getResources().getString(R.string.process_pending_confirm));
        dialog=new LoadProgressDialog(this);
        poDetails=new ArrayList<>();
        //String role = SPHelper.getLocalData(getApplicationContext(), USER_NO_KEY, String.class.getName(), "").toString().toUpperCase();
        initPoInfo(userCode);
        subTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(TransformPoListActivity.this,PoTransformActivity.class);
                startActivity(intent);
            }
        });
    }



//    @Override
//    protected void onResume() {
//        poDetails=new ArrayList<>();
//        String role = SPHelper.getLocalData(getApplicationContext(), USER_NO_KEY, String.class.getName(), "").toString().toUpperCase();
//        initPoInfo(role);
//        super.onResume();
//    }

    @OnItemClick(R.id.TransformPoList)
    void getTransformInfo(int position){
        finish();
        Intent intent = new Intent(this,PoTransformActivity.class);
        String fepo = poDetails.get(position).FEPO;
        String sewline = poDetails.get(position).SEWLINE;
        String transformday = poDetails.get(position).TRANSFORMDAY;
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("FEPO",fepo);
        intent.putExtra("SEW",sewline);
        intent.putExtra("DATE",transformday);
        startActivity(intent);


    }

    @OnItemLongClick(R.id.TransformPoList)
    boolean deleteTransformInfo(int position){
        inputAreaDialog(position,false);
        return true;
    }

    private void inputAreaDialog(final int position, final boolean isNotDismissByNotInput){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View areaDialogView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.area_input_dialog,null);
        final EditText editText= (EditText) areaDialogView.findViewById(R.id.etInventoryAreaDialog);
        final TextView textView= (TextView) areaDialogView.findViewById(R.id.tvInventoryAreaTitle);
        editText.setHint(getResources().getString(R.string.enter_here));
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        textView.setText(getResources().getString(R.string.to_be_delete_need_confirm));
        editText.setTextColor(Color.BLACK);
        builder.setView(areaDialogView)
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String password=editText.getText().toString().trim();
                        if(password.isEmpty()){
                            OthersUtil.dialogNotDismissClickOut(dialogInterface);
                            OthersUtil.ToastMsg(getApplicationContext(),getResources().getString(R.string.can_not_blank_value));
                            return;
                        }
                        OthersUtil.dialogDismiss(dialogInterface);
                        dialogInterface.dismiss();
                        DeletePoInfo(position,password);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if(isNotDismissByNotInput){
                            OthersUtil.dialogNotDismissClickOut(dialogInterface);
                        }else {
                            OthersUtil.dialogDismiss(dialogInterface);
                            dialogInterface.dismiss();
                        }
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void DeletePoInfo(final int position, String password) {
        String urlDeletePOTransformCheckList = "http://10.20.226.96/FEAService/api/ERP";
        final String fepo = poDetails.get(position).FEPO;
        final String sewline = poDetails.get(position).SEWLINE;
        if (password.equalsIgnoreCase("123")) {
            OthersUtil.showLoadDialog(dialog);
            VolleyMultipartRequest erpDeletePOTransferCheckList = new VolleyMultipartRequest(Request.Method.POST, urlDeletePOTransformCheckList, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    //TODO:
                    OthersUtil.ToastMsg(getApplicationContext(),getResources().getString(R.string.delete_success));
                    OthersUtil.dismissLoadDialog(dialog);
                    poDetails.remove(position);
                    poAdapter.notifyDataSetChanged();
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
                    OthersUtil.ToastMsg(getApplicationContext(),errorMessage);
                    Log.i("Error", errorMessage);
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("key", "ERP");
                    params.put("action","deletepotransformchecklist");
                    params.put("fepoCode", fepo);
                    params.put("sewLine", sewline);
                    params.put("companyCode",SPHelper.getLocalData(getApplicationContext(),COMPANY_CODE,String.class.getName(),"").toString());
                    return params;
                }
            };
            // Access the RequestQueue through your singleton class.
            VolleySingleton.getInstance(this).addToRequestQueue(erpDeletePOTransferCheckList);
//        NewRxjavaWebUtils.getUIThread(NewRxjavaWebUtils.getObservable(TransformPoListActivity.this, "")
//                .map(new Func1<String, HsWebInfo>() {
//                    @Override
//                    public HsWebInfo call(String s) {
//                        return NewRxjavaWebUtils.getJsonData(getApplicationContext(), "spAPP_DeleteTransformPo_list",
//                                "Fepo=" + fepo +
//                                        ",SewLine=" + sewline, String.class.getName(), false, "info获取成功");
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io()), getApplicationContext(), dialog, new WebListener() {
//            @Override
//            public void success(HsWebInfo hsWebInfo) {
//                OthersUtil.ToastMsg(getApplicationContext(),getResources().getString(R.string.delete_success));
//
//                OthersUtil.dismissLoadDialog(dialog);
//                        poDetails.remove(position);
//                        poAdapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void error(HsWebInfo hsWebInfo) {
//                Log.e("TAG", "errorLog2=" + hsWebInfo.json);
//            }
//        });
    }else {
            OthersUtil.ToastMsg(getApplicationContext(),getResources().getString(R.string.type_value_not_correct));
        }
    }


    private void initPoInfo(final String userCode) {
        String urlGetPOTransferPending = "http://10.20.226.96/FEAService/api/ERP";
        VolleyMultipartRequest erpPOTransferPending = new VolleyMultipartRequest(Request.Method.POST, urlGetPOTransferPending, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    JSONArray data = result.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++){
                        JSONObject currentData = (JSONObject) data.get(i);
                        poDetail=new TransformPoDetail();
                        poDetail.FEPO = currentData.getString("fepoCode");
                        poDetail.SEWLINE = currentData.getString("sewLine");
                        poDetail.TRANSFORMDAY = currentData.getString("transformDate");
                        poDetail.CUSTOMERNAME = currentData.getString("customerName");
                        poDetails.add(poDetail);
                    }
                    poAdapter=new TransformPoAdapter(poDetails,getApplicationContext());
                    lv_poList.setAdapter(poAdapter);
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
                OthersUtil.ToastMsg(getApplicationContext(),errorMessage);
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("key", "ERP");
                params.put("action","getpotransformchecklistpending");
                params.put("userCode", userCode);
                params.put("companyCode",SPHelper.getLocalData(getApplicationContext(),COMPANY_CODE,String.class.getName(),"").toString());
                return params;
            }
        };
        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(erpPOTransferPending);
//        NewRxjavaWebUtils.getUIThread(NewRxjavaWebUtils.getObservable(TransformPoListActivity.this,"")
//                .map(new Func1<String, HsWebInfo>() {
//                    @Override
//                    public HsWebInfo call(String s) {
//                        return NewRxjavaWebUtils.getJsonData(getApplicationContext(),"spAPP_GetTransformPo_list",
//                                "Type="+userCode,String.class.getName(),false,"info获取成功");
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io()), getApplicationContext(), dialog, new WebListener() {
//            @Override
//            public void success(HsWebInfo hsWebInfo) {
//                String json = hsWebInfo.json;
//                Log.e("TAGG","Json111="+json);
//                TransformPoLists PoLists = JSON.parseObject(json, TransformPoLists.class);
//                List<TransformPoLists.DATABean> data = PoLists.getDATA();
//                for (int i=0;i<data.size();i++){
//                    poDetail=new TransformPoDetail();
//                    poDetail.FEPO=data.get(i).getFEPO();
//                    poDetail.SEWLINE=data.get(i).getSEWLINE();
//                    poDetail.TRANSFORMDAY=data.get(i).getTRANSFORMDAY();
//                    poDetail.CUSTOMERNAME=data.get(i).getCUSTOMERNAME();
//                    poDetails.add(poDetail);
//                }
//                poAdapter=new TransformPoAdapter(poDetails,getApplicationContext());
//                lv_poList.setAdapter(poAdapter);
//
//            }
//            @Override
//            public void error(HsWebInfo hsWebInfo) {
//                Log.e("TAG","errorLog2="+hsWebInfo.json);
//            }
//        });
    }

    private class TransformPoAdapter extends HsBaseAdapter<TransformPoDetail>{
        public TransformPoAdapter(List<TransformPoDetail> list, Context context) {
            super(list, context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (convertView==null)  convertView=mInflater.inflate(R.layout.activity_transform_polist_item,viewGroup,false);
            TransformPoDetail transformPoDetail = mList.get(position);
            TextView fepo = ViewHolder.get(convertView, R.id.transformFEPO);
            TextView sew = ViewHolder.get(convertView, R.id.transformSewLine);
            TextView date = ViewHolder.get(convertView, R.id.transformPoDate);
            TextView brand = ViewHolder.get(convertView, R.id.transformPoBrand);
            fepo.setText(transformPoDetail.FEPO);
            sew.setText(transformPoDetail.SEWLINE);
            date.setText(transformPoDetail.TRANSFORMDAY);
            brand.setText(transformPoDetail.CUSTOMERNAME);
            return convertView;
        }
    }




}
