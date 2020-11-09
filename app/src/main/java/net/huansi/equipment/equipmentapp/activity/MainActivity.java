package net.huansi.equipment.equipmentapp.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.GridView;
import android.widget.TextView;

import net.huansi.equipment.equipmentapp.R;
import net.huansi.equipment.equipmentapp.activity.block_material.BlockMaInActivity;
import net.huansi.equipment.equipmentapp.activity.awake_goods.AwakeMainActivity;
import net.huansi.equipment.equipmentapp.activity.bom_data.BomActivity;
import net.huansi.equipment.equipmentapp.activity.call_repair.CallRepairMainActivity;
import net.huansi.equipment.equipmentapp.activity.check_goods.CheckMainActivity;
import net.huansi.equipment.equipmentapp.activity.check_quality.QualityMainActivity;
import net.huansi.equipment.equipmentapp.activity.check_simple.CheckSimpleDepartmentActivity;
import net.huansi.equipment.equipmentapp.activity.cut_parts.CutPartsMainActivity;
import net.huansi.equipment.equipmentapp.activity.inventory.InventoryMainActivity;
import net.huansi.equipment.equipmentapp.activity.logging_bill.LoggingBillActivity;
import net.huansi.equipment.equipmentapp.activity.make_bills.MakeBillsMainActivity;
import net.huansi.equipment.equipmentapp.activity.merge_goods.ScannerActivity;
import net.huansi.equipment.equipmentapp.activity.move_cloth.ClothMoveMainActivity;
import net.huansi.equipment.equipmentapp.activity.repair.RepairMainActivity;
import net.huansi.equipment.equipmentapp.activity.send_card.BindCardMainActivity;
import net.huansi.equipment.equipmentapp.activity.store_goods.StoreGoodsMainActivity;
import net.huansi.equipment.equipmentapp.activity.user_manage.RoleActivity;
import net.huansi.equipment.equipmentapp.activity.version_control.VersionControlMainActivity;
import net.huansi.equipment.equipmentapp.adapter.MainAdapter;
import net.huansi.equipment.equipmentapp.entity.MainItem;
import net.huansi.equipment.equipmentapp.entity.ReaderDevice;
import net.huansi.equipment.equipmentapp.helpers.LocaleHelper;
import net.huansi.equipment.equipmentapp.service.AlwaysRunningService;
import net.huansi.equipment.equipmentapp.util.EPData;
import net.huansi.equipment.equipmentapp.util.OthersUtil;
import net.huansi.equipment.equipmentapp.util.SPHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;

import static net.huansi.equipment.equipmentapp.util.SPHelper.ROLE_CODE_KEY;
import static net.huansi.equipment.equipmentapp.util.SPHelper.USER_NO_KEY;

/**
 * Created by 单中年 on 2017/2/22.
 */

public class MainActivity extends BaseActivity {

    @BindView(R.id.tvMainUserNo) TextView tvMainUserNo;
    @BindView(R.id.gvMain) GridView gvMain;



    private List<MainItem> mList;
    private MainAdapter mAdapter;



    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void init() {
        tvMainUserNo.setText(SPHelper.getLocalData(getApplicationContext(),USER_NO_KEY,String.class.getName(),"").toString());
        mList=new ArrayList<>();
        String roleCode = SPHelper.getLocalData(getApplicationContext(),ROLE_CODE_KEY,String.class.getName(),"").toString().toUpperCase();
        switch (roleCode){
            case "2"://管理员账号 - Admin All permission
                mList.add(new MainItem(R.drawable.icon_wifi,getResources().getString(R.string.version_app),"versionControl"));
                mList.add(new MainItem(R.drawable.icon_hairpin,getResources().getString(R.string.issue_card),"sendCard"));
                mList.add(new MainItem(R.drawable.icon_user_manage,getResources().getString(R.string.management_app),"manage"));
                mList.add(new MainItem(R.drawable.icon_inventory,getResources().getString(R.string.inventory),"inventory"));
                mList.add(new MainItem(R.drawable.icon_repair,getResources().getString(R.string.repair),"repair"));
                mList.add(new MainItem(R.drawable.icon_check,getResources().getString(R.string.check),"check"));
                mList.add(new MainItem(R.drawable.icon_loose,getResources().getString(R.string.relaxing_fabric),"awake"));
                mList.add(new MainItem(R.drawable.icon_movebox,getResources().getString(R.string.move_material),"merge"));
                mList.add(new MainItem(R.drawable.icon_fly,getResources().getString(R.string.block_material),"BlockMaterial"));
                mList.add(new MainItem(R.drawable.icon_style,getResources().getString(R.string.style),"designDescription"));
                mList.add(new MainItem(R.drawable.icon_zoom,getResources().getString(R.string.check_quality),"checkQuality"));
                mList.add(new MainItem(R.drawable.icon_calling,getResources().getString(R.string.call_repair),"callRepair"));
                mList.add(new MainItem(R.drawable.icon_bird,getResources().getString(R.string.transfer),"clothMoving"));
                mList.add(new MainItem(R.drawable.icon_safari,getResources().getString(R.string.sample_check),"measureSimple"));
                mList.add(new MainItem(R.drawable.icon_barcode,getResources().getString(R.string.finished_goods),"storeGoods"));
                mList.add(new MainItem(R.drawable.icon_browser,getResources().getString(R.string.register),"loggingBill"));
                mList.add(new MainItem(R.drawable.icon_buray,getResources().getString(R.string.cut_parts),"cutParts"));
                mList.add(new MainItem(R.drawable.remind_icon,"Bom","bom"));
                break;
            case "16":
                mList.add(new MainItem(R.drawable.icon_style,getResources().getString(R.string.style),"designDescription"));
                break;
            case "B"://生产主管账号 - Production/Leader Manager
                //mList.add(new MainItem(R.drawable.icon_bird,"流转","clothMoving"));
                mList.add(new MainItem(R.drawable.icon_hairpin,getResources().getString(R.string.issue_card),"sendCard"));
                mList.add(new MainItem(R.drawable.icon_inventory,getResources().getString(R.string.inventory),"inventory"));
                mList.add(new MainItem(R.drawable.icon_repair,getResources().getString(R.string.repair),"repair"));
                break;
            case "C"://盘点员 - Machine Inventory User
                mList.add(new MainItem(R.drawable.icon_inventory,getResources().getString(R.string.inventory),"inventory"));
                mList.add(new MainItem(R.drawable.icon_repair,getResources().getString(R.string.repair),"repair"));
                break;
            case "D"://机修工 - Machine Maintenance
                mList.add(new MainItem(R.drawable.icon_repair,getResources().getString(R.string.repair),"repair"));
                break;
            case "E"://原料仓库、成品仓 - Finished Good and Material
                mList.add(new MainItem(R.drawable.icon_barcode,getResources().getString(R.string.finished_goods),"storeGoods"));
                mList.add(new MainItem(R.drawable.icon_check,getResources().getString(R.string.check),"check"));
                mList.add(new MainItem(R.drawable.icon_movebox,getResources().getString(R.string.move_material),"merge"));
                break;
            case "F"://裁剪 - Cutting User
                mList.add(new MainItem(R.drawable.icon_loose,getResources().getString(R.string.relaxing_fabric),"awake"));
                mList.add(new MainItem(R.drawable.icon_zoom,getResources().getString(R.string.check_quality),"checkQuality"));
                mList.add(new MainItem(R.drawable.icon_style,getResources().getString(R.string.style),"designDescription"));
                mList.add(new MainItem(R.drawable.icon_buray,getResources().getString(R.string.cut_parts),"cutParts"));
                mList.add(new MainItem(R.drawable.icon_fly,getResources().getString(R.string.block_material),"BlockMaterial"));
                break;
            case "G"://看制单,转换款信息填写 - Look PI/Change Over key in User
                mList.add(new MainItem(R.drawable.icon_style,getResources().getString(R.string.style),"designDescription"));
                mList.add(new MainItem(R.drawable.icon_calling,getResources().getString(R.string.call_repair),"callRepair"));
                break;
            case "H"://工人叫修，查制单等资料 - Call machine repair/ Look PI
                mList.add(new MainItem(R.drawable.icon_calling,getResources().getString(R.string.call_repair),"callRepair"));
                mList.add(new MainItem(R.drawable.icon_style,getResources().getString(R.string.style),"designDescription"));
                break;
            case "I"://样衣流转 - Sample garment transfer
                mList.add(new MainItem(R.drawable.icon_bird,getResources().getString(R.string.transfer),"clothMoving"));
                mList.add(new MainItem(R.drawable.icon_style,getResources().getString(R.string.style),"designDescription"));
                break;
            case "J"://样衣检验 - Sample garment check
                mList.add(new MainItem(R.drawable.icon_safari,getResources().getString(R.string.sample_check),"measureSimple"));
                mList.add(new MainItem(R.drawable.icon_style,getResources().getString(R.string.style),"designDescription"));
                break;
            case "K"://app对点单 - App check
                mList.add(new MainItem(R.drawable.icon_browser,getResources().getString(R.string.register),"loggingBill"));
                mList.add(new MainItem(R.drawable.icon_style,getResources().getString(R.string.style),"designDescription"));
                break;
        }
        mList.add(new MainItem(R.drawable.icon_setting,getResources().getString(R.string.setting_app),"setting"));
        mList.add(new MainItem(R.drawable.icon_exit,getResources().getString(R.string.exit_app),"exit"));
        mAdapter=new MainAdapter(mList,getApplicationContext());
        gvMain.setAdapter(mAdapter);
        //checkVersion();
        initDevices();
        Intent intent=new Intent(this, AlwaysRunningService.class);
        startService(intent);

//        Intent pushIntent=new Intent(this, PushService.class);//推送先停掉
//        startService(pushIntent);
//        PollingUtils.startPollingService(this, 5, PushService.class, PushService.ALARM_SERVICE);
    }

    /**
     * 初始化盘点需要的数据以及设备信息
     */
    private void initDevices(){
        EPData.getActiveDeceiveList().clear();
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null||!bluetoothAdapter.isEnabled()){
            //打开蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivity(enableIntent);
            return;
        }
        for(BluetoothDevice bluetoothDevice:bluetoothAdapter.getBondedDevices()){
            if(OthersUtil.isRFIDReader(bluetoothDevice)){
                EPData.getActiveDeceiveList().add(new ReaderDevice(bluetoothDevice,bluetoothDevice.getName(),bluetoothDevice.getAddress(),null,null,false));
            }
        }
    }

    @OnItemClick(R.id.gvMain)
    void toNext(int position){
        Intent intent=new Intent();
        MainItem item=mList.get(position);
        switch (item.code){
            case "sendCard":
//                if(EPData.mConnectedDevice==null){
//                    OthersUtil.showTipsDialog(this,"请在设置中连接RFID扫描设备");
//                    return;
//                }
                intent.setClass(this,BindCardMainActivity.class);
                break;
            case "versionControl":
                intent.setClass(this,VersionControlMainActivity.class);
                break;
            case "manage":
                intent.setClass(this,RoleActivity.class);
                break;
            case "check":
                intent.setClass(this, CheckMainActivity.class);
                break;
            case "inventory":
//                if(EPData.mConnectedDevice==null){
//                    OthersUtil.showTipsDialog(this,"请在设置中连接RFID扫描设备");
//                    return;
//                }
                intent.setClass(this,InventoryMainActivity.class);
                break;
            case "repair":
                intent.setClass(this,RepairMainActivity.class);
                break;
            case "setting":
                intent.setClass(this,DeviceSettingActivity.class);
                break;
            case "awake":
                intent.setClass(this, AwakeMainActivity.class);
                break;
            case "designDescription":
                intent.setClass(this, MakeBillsMainActivity.class);
                break;
            case "measureSimple":
                intent.setClass(this, CheckSimpleDepartmentActivity.class);
                break;
            case "merge":
                intent.setClass(this, ScannerActivity.class);
                break;
            case "checkQuality":
                intent.setClass(this, QualityMainActivity.class);
                break;
            case "callRepair":
                intent.setClass(this, CallRepairMainActivity.class);
                break;
            case "clothMoving":
                intent.setClass(this, ClothMoveMainActivity.class);
                break;
            case "storeGoods":
                intent.setClass(this, StoreGoodsMainActivity.class);
                break;
            case "BlockMaterial":
                intent.setClass(this, BlockMaInActivity.class);
                break;
            case "loggingBill":
                intent.setClass(this, LoggingBillActivity.class);
                break;
            case "bom":
                intent.setClass(this, BomActivity.class);
                break;
            case "cutParts":
                intent.setClass(this, CutPartsMainActivity.class);
                break;
            case "exit":
                intent.setClass(this,IPConfigActivity.class);
                break;
        }
        startActivity(intent);
        if(item.code.equals("exit")){
            SPHelper.saveLocalData(getApplicationContext(),ROLE_CODE_KEY,"",String.class.getName());
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("EquipmentApp", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.commit();
            finish();
        }
    }

    @Override
    public void back() {
        OthersUtil.showChooseDialog(this, getResources().getString(R.string.are_you_sure_exit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                });
    }
}