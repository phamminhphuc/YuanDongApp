package net.huansi.equipment.equipmentapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import net.huansi.equipment.equipmentapp.R;
import net.huansi.equipment.equipmentapp.entity.HsWebInfo;
import net.huansi.equipment.equipmentapp.entity.LoginUser;
import net.huansi.equipment.equipmentapp.helpers.LocaleHelper;
import net.huansi.equipment.equipmentapp.imp.SimpleHsWeb;
import net.huansi.equipment.equipmentapp.util.OthersUtil;
import net.huansi.equipment.equipmentapp.util.RxjavaWebUtils;
import net.huansi.equipment.equipmentapp.util.SPHelper;
import net.huansi.equipment.equipmentapp.util.VolleySingleton;
import net.huansi.equipment.equipmentapp.widget.LoadProgressDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

import static net.huansi.equipment.equipmentapp.util.SPHelper.ROLE_CODE_KEY;
import static net.huansi.equipment.equipmentapp.util.SPHelper.USER_NO_KEY;
import static net.huansi.equipment.equipmentapp.util.SPHelper.USER_PWS;

/**
 * Created by 单中年 on 2017/2/22.
 */

public class LoginActivity extends BaseActivity {
    @BindView(R.id.etLoginPwd) EditText etLoginPwd;//密码
    @BindView(R.id.etLoginUserNo) EditText etLoginUserNo;//用户名
    @BindView(R.id.btnLogin) Button btnLogin;//登录
    TextView txtUserCode,txtPassword,txtTypeUserCode,txtTypePassword,
            txtFastlogin,txtlogin;



    private LoadProgressDialog dialog;

    @Override
    protected int getLayoutId() {
        return R.layout.login_activity;
    }

    @Override
    public void init() {
        OthersUtil.hideInputFirst(this);
        dialog=new LoadProgressDialog(this);
        etLoginUserNo.setText(SPHelper.getLocalData(getApplicationContext(),USER_NO_KEY,String.class.getName(),"").toString());
        Log.e("TAG","执行了吗");
        initFindViewByID();
        renderViewActivity();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private void initFindViewByID() {
        txtUserCode = (TextView)findViewById(R.id.user_code);
        txtPassword = (TextView)findViewById(R.id.password);
        txtTypeUserCode = (TextView)findViewById(R.id.etLoginUserNo);
        txtTypePassword = (TextView)findViewById(R.id.etLoginPwd);
        txtFastlogin = (TextView)findViewById(R.id.btnFastLogin);
        txtlogin = (TextView)findViewById(R.id.btnLogin);
    }

    private void renderViewActivity(){
        txtUserCode.setText(getResources().getString(R.string.usercode));
        txtPassword.setText(getResources().getString(R.string.password));
        txtTypeUserCode.setHint(getResources().getString(R.string.type_usercode));
        txtTypePassword.setHint(getResources().getString(R.string.type_password));
        txtFastlogin.setText(getResources().getString(R.string.fast_login));
        txtlogin.setText(getResources().getString(R.string.login));
    }

    private Map<String, String> mParams;
    /**
     * 登陆
     */
    @OnClick(R.id.btnLogin)
    void login(){
        final String userNo = etLoginUserNo.getText().toString().trim();
        final String passWord = etLoginPwd.getText().toString().trim();
        if(userNo.isEmpty()){
            OthersUtil.ToastMsg(getApplicationContext(), getResources().getString(R.string.user_code_empty));
            return;
        }

        if(passWord.isEmpty()){
            OthersUtil.ToastMsg(getApplicationContext(), getResources().getString(R.string.password_empty));
            return;
        }
        String urlUserITS = "http://portal.feavn.com.vn:82/Json/api/ITS/Login?usercode=" + userNo + "&password=" + passWord;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, urlUserITS, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            SPHelper.saveLocalData(getApplicationContext(),USER_NO_KEY,response.get("UserCodeID").toString(),String.class.getName());
                            SPHelper.saveLocalData(getApplicationContext(),ROLE_CODE_KEY,response.get("UserGroupID").toString(),String.class.getName());
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        OthersUtil.ToastMsg(getApplicationContext(),getResources().getString(R.string.please_check_usercode_password));
                    }
                });
        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * 叫修快速登陆（无需密码）
     */
    @OnClick(R.id.btnFastLogin)
    void fastLogin(){
        String userNo=etLoginUserNo.getText().toString().trim();
        String password="callRepair";
        if(userNo.isEmpty()){
            OthersUtil.ToastMsg(getApplicationContext(),getResources().getString(R.string.user_code_empty));
            return;
        }
        String substring = userNo.substring(0, 1);
        if ((substring.equalsIgnoreCase("A")||substring.equalsIgnoreCase("B"))&&userNo.length()==6){
            Log.e("TAG",getResources().getString(R.string.successfully));
            OthersUtil.showLoadDialog(dialog);
            RxjavaWebUtils.requestByGetJsonData(this,"spAppEPUserLogin",
                    "sUserNo=" + userNo +
                            ",sPassword=" + password,
                    getApplicationContext(), dialog,
                    LoginUser.class.getName(),
                    true,
                    getResources().getString(R.string.connect_server_error),
                    new SimpleHsWeb() {
                        @Override
                        public int hashCode() {
                            return super.hashCode();
                        }

                        @Override
                        public void error(HsWebInfo hsWebInfo) {
                            super.error(hsWebInfo);
                        }

                        @Override
                        public void success(HsWebInfo hsWebInfo) {
                            OthersUtil.dismissLoadDialog(dialog);
                            LoginUser loginUser= (LoginUser) hsWebInfo.wsData.LISTWSDATA.get(0);
                            SPHelper.saveLocalData(getApplicationContext(),USER_NO_KEY,loginUser.SUSERNO,String.class.getName());
                            SPHelper.saveLocalData(getApplicationContext(),ROLE_CODE_KEY,loginUser.SROLECODE,String.class.getName());
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });
        }else {
            OthersUtil.showTipsDialog(this,getResources().getString(R.string.please_check_usercode_password));
        }

    }
}