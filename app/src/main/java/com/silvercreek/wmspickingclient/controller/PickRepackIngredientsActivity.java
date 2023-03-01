package com.silvercreek.wmspickingclient.controller;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.silvercreek.wmspickingclient.R;
import com.silvercreek.wmspickingclient.database.WMSDbHelper;
import com.silvercreek.wmspickingclient.model.RepackFG;
import com.silvercreek.wmspickingclient.model.RepackIngredients;
import com.silvercreek.wmspickingclient.model.picktaskPrintlabel;
import com.silvercreek.wmspickingclient.model.picktaskWHIPTL;
import com.silvercreek.wmspickingclient.model.picktaskWHMQTY;
import com.silvercreek.wmspickingclient.model.picktaskdetail;
import com.silvercreek.wmspickingclient.model.picktaskheader;
import com.silvercreek.wmspickingclient.model.picktasklist;
import com.silvercreek.wmspickingclient.util.DataLoader;
import com.silvercreek.wmspickingclient.util.Globals;
import com.silvercreek.wmspickingclient.util.LogfileCreator;
import com.silvercreek.wmspickingclient.util.Supporter;
import com.silvercreek.wmspickingclient.xml.ExportPickTask;

import org.apache.commons.io.FileUtils;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.nio.DoubleBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static com.silvercreek.wmspickingclient.util.Supporter.MyPREFERENCES;
import static com.silvercreek.wmspickingclient.util.Supporter.SUMQTY;

public class PickRepackIngredientsActivity extends AppBaseActivity {

    private ListView transList;
    private EditText edtPallet, edtQty;
    private TextView tvRepacknum;
    /*private TextView tvLotRfId;*/

    private List<picktaskdetail> exportTranList;

    String filename = "PickTaskPalletLabel.pdf";
    private File pdfFile = null;
    String toasttext = "";
    private String mPath;
    private Supporter mSupporter;
    private WMSDbHelper mDbHelper;
    private ToastMessage mToastMessage;
    private Document document;
    private PdfContentByte cb;
    private PdfPTable table;
    private PdfWriter docWriter;
    private PdfPCell cell;
    public String lockStatus = "";

    public static Font FONT_TABLE_CONTANT = new Font(Font.FontFamily.TIMES_ROMAN, 30, Font.BOLD);
    public static Font FONT_TABLE_BODY = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.NORMAL);
    public static Font FONT_BODY = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);

    private List<RepackIngredients> repackFGList;
    private List<RepackIngredients> saveList;
    private List<RepackIngredients> ingredientsList;
    private List wLotNoList;
    public Double IngredientsList = 0.0;

    public static String NAMESPACE = "";
    public static String URL_PROTOCOL = "";
    public static String URL_SERVICE_NAME = "";
    public static String APPLICATION_NAME = "";
    public static String URL_SERVER_PATH = "";
    public static String SOFT_KEYBOARD = "";
    public static final String METHOD_EXPORT_DATA = "PickTask_SaveMain";
    private SharedPreferences sharedpreferences;
    private int mTimeout;
    private RepackIngredientsAdapter adapter;
    private List<String> mPalletList;
    private ArrayList<picktaskWHIPTL> mPalletMast;
    private List<String> mLotList;
    private ArrayList<picktaskWHMQTY> mLotMast;
    private String StrFlag = "Y";
    private Boolean isTaskCompleted;
    private Boolean isTaskOnHold;
    private Boolean isItemAvailable;
    private List<picktaskPrintlabel> PrintLabelList;
    private String stop, trailer, route, dock, deldate, order, task, custid, custname, picker, palno;
    private String mPalno = "";
    private String PalletNumber = "", enteredQty, allocQtyForSave;
    //private TextView,tvDesc txtWeight;
    private Button btnCancel, btnClose, btnSave;
    private Boolean isSameItem = false;
    private Boolean isProceed = true;
    private String uom = "", doctype = "", docno = "", doclineno = "", docstat = "", strweight = "", stkumid = "",
            orgdoclineno = "", volume = "", decnum = "", orgTranlineno = "", Lbshp = "", umfact = "", Tshipped = "", Trkshiped = "", LineSplit = "";
    private String ItemNo, strDesc;
    private String strTQty, strorgTQty;
    private String strTrkQty, strorgTrkQty;
    private String strCatchwt, strSlot, strLot, strwLotno;
    private String strTranlineNo;
    private Double dQty;
    private double dAvailQty;
    private Integer pickDuration;
    private double dSoQty = 0.0, dLotQty = 0.0;
    private boolean isMoreQty = true;
    public static final String METHOD_GET_RAW_DATA = "Repack_PickRawItem";
    public static final String METHOD_SAVE_RAW_DATA = "Repack_TempAlloc";
    public static final String LOGOUTREQUEST = "LogoutRequest";
    private String mSessionId, mCompany,mUsername,mDeviceId="";

    private String mLoctid = "";

    private File mImpOutputFile;
    private String Getmsg = "";
    private String selectedItem = "";
    private String[] itemArray = {};
    private int SubTranlineCount = 0;
    private String subTranNo = "";
    private String stagingSlot = "";
    private String taskStatus = "";
    private String editQty = "";
    private Boolean scanResult = true;
    private ArrayList<picktaskdetail> editPickDetail;
    private String repackNum = "";
    private boolean isLocalData = false;
    private boolean ISSUMQTY = false;
    private double firstQty = 0;
    private Double totalQty = 0.0;
    private Double whQty = 0.0;
    private Double icQty = 0.0;
    private Double qtyUsed = 0.0;
    private Double totalQtY = 0.0;
    private Double toTalQtY = 0.0;
    private String tempAlloc2 = "";
    private boolean isLocked=false;
    private boolean isdeviceSideLock=false;
    private String lockUserName = "";


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_repack_ingredients);

        mDbHelper = new WMSDbHelper(this);
        mSupporter = new Supporter(this, mDbHelper);
        mToastMessage = new ToastMessage();

        edtPallet = (EditText) findViewById(R.id.edtPallet);
        edtQty = (EditText) findViewById(R.id.edtQty);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnClose = (Button) findViewById(R.id.btn_close);
        tvRepacknum = (TextView) findViewById(R.id.tv_repacknum);
        /* tvLotRfId = (TextView) findViewById(R.id.tvLotrfId);*/
        String customerId;
        String _currentJson;


        transList = (ListView) findViewById(R.id.lst_TransItems);


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        NAMESPACE = sharedpreferences.getString("Namespace", "");
        URL_PROTOCOL = sharedpreferences.getString("Protocol", "");
        URL_SERVICE_NAME = sharedpreferences.getString("Servicename", "");
        URL_SERVER_PATH = sharedpreferences.getString("Serverpath", "");
        APPLICATION_NAME = sharedpreferences.getString("AppName", "");
        mTimeout = Integer.valueOf(sharedpreferences.getString("Timeout", "0"));
        SOFT_KEYBOARD = sharedpreferences.getString("SoftKey", "");
        NAMESPACE = NAMESPACE + "/";
        Globals.gNamespace = NAMESPACE;
        Globals.gProtocol = URL_PROTOCOL;
        Globals.gServicename = URL_SERVICE_NAME;
        Globals.gAppName = APPLICATION_NAME;
        Globals.gTimeout = sharedpreferences.getString("Timeout", "");
        Globals.isNewWlotno = false;
        picktasklist tpicktasklist = new picktasklist();
        mDbHelper.openReadableDatabase();
        mSessionId = mDbHelper.mGetSessionId();
        mDbHelper.closeDatabase();
        mCompany = Globals.gCompanyDatabase;
        mLoctid = Globals.gLoctid;
        mUsername = Globals.gUsercode;
        mDeviceId = Globals.gDeviceId;


        edtPallet.requestFocus();
        if(edtPallet.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        if (SOFT_KEYBOARD.equals("CHECKED")){
            edtPallet.setShowSoftInputOnFocus(false);
            edtQty.setShowSoftInputOnFocus(false);
        }else {
            edtPallet.setShowSoftInputOnFocus(true);
            edtQty.setShowSoftInputOnFocus(true);
        }

        repackNum = this.getIntent().getExtras().getString("repacknum");
        isLocked = this.getIntent().getExtras().getBoolean("lockstatus");
        isdeviceSideLock = this.getIntent().getExtras().getBoolean("isdeviceSidelock");
        lockUserName = this.getIntent().getExtras().getString("lockUser");

        mDbHelper.openReadableDatabase();
        lockStatus = mDbHelper.getLockStatus(repackNum.trim());
        mDbHelper.closeDatabase();
        //lockStatus="L";
        /*if (lockStatus == null) {
            lockStatus = "O";
        } else if (lockStatus.equals("L")) {
            edtPallet.setEnabled(false);
            edtQty.setEnabled(false);
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
            StatusLockAlert();
        }*/
      //  if(isLocked){
        if(isLocked){
            edtPallet.setEnabled(false);
            edtQty.setEnabled(false);
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
           // StatusLockAlert();
        }else if (isdeviceSideLock){
            edtPallet.setEnabled(false);
            edtQty.setEnabled(false);
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
            //StatusLockAlertDivice();
        }


        tvRepacknum.setText("Repack # " + repackNum.trim());

        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);
        edtQty.setEnabled(false);
        edtPallet.requestFocus();

       /* mDbHelper.openReadableDatabase();
        picktaskdetail = mDbHelper.getPickTaskDetail();
        mDbHelper.closeDatabase();

        mDbHelper.openReadableDatabase();
        picktaskheader = mDbHelper.getPickTaskHeader();
        mDbHelper.closeDatabase();

        mDbHelper.openWritableDatabase();
        mDbHelper.UpdateTaskStatus(tpicktasklist, Globals.gTaskNo);
        mDbHelper.closeDatabase();


        adapter = new PickTaskDetailAdapter(PickRepackIngredientsActivity.this, picktaskdetail);
        transList.setAdapter(adapter);*/

        mDbHelper.openReadableDatabase();
        repackFGList = mDbHelper.getRepackIngredients();
        wLotNoList = mDbHelper.getWlotNoList();
        mDbHelper.closeDatabase();


        adapter = new RepackIngredientsAdapter(PickRepackIngredientsActivity.this, repackFGList);
        transList.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        edtPallet.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:

                            PalletNumber = edtPallet.getText().toString();
                            //PalletNumber = fixedLengthString(PalletNumber);


                            if (PalletNumber.equalsIgnoreCase("")) {
                                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                                        "Please Enter or Scan the Pallet");
                                scanResult = true;

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        edtPallet.requestFocus();
                                    }
                                }, 150);

                            } else if (wLotNoList.contains(PalletNumber)) {
                                isLocalData = true;
                                new GetRepackRawData(mUsername).execute();
                                mDbHelper.openReadableDatabase();
                                ingredientsList = mDbHelper.getWlotRepackIngredients(PalletNumber.trim());
                                mDbHelper.closeDatabase();

                            } else {
                                Supporter.SUMQTY = false;
                                new GetRepackRawData(mUsername).execute();


                            }
                        default:
                            break;
                    }
                }
                return false;
               // return true;
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mSupporter.simpleNavigateTo(PickRepackActivity.class);
                Intent theIntent = new Intent(PickRepackIngredientsActivity.this, PickRepackActivity.class);
                theIntent.putExtra("repacknum", repackNum);
                theIntent.putExtra("lockstatus",isLocked);
                theIntent.putExtra("isdeviceSidelock",isdeviceSideLock);
                theIntent.putExtra("rpNum",repackNum);
                theIntent.putExtra("lockUser",lockUserName);

                //lockUser
                startActivity(theIntent);

                //cancelAlert();
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                edtPallet.setText("");
                edtQty.setText("");
                /*  tvLotRfId.setText("");*/
                edtPallet.setEnabled(true);
                edtPallet.requestFocus();
                btnSave.setEnabled(false);


            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String allocQty = "";
                enteredQty = edtQty.getText().toString();
                mDbHelper.openReadableDatabase();
                saveList = mDbHelper.getWlotRepackIngredients(PalletNumber.trim());
                mDbHelper.closeDatabase();
                if (saveList.size()>0 && ingredientsList.size()>0){  //SCS CIRK 2022/07/25 CT69649C:
                    firstQty = Double.parseDouble(saveList.get(0).getRIT_REMARKS()) + Double.parseDouble(saveList.get(0).getRIT_QTYUSED());

                    totalQty = Double.valueOf(ingredientsList.get(0).getRIT_QTYUSED()) - Double.valueOf(ingredientsList.get(0).getRIT_RPALLOCQTY());
                    whQty = Double.valueOf(ingredientsList.get(0).getRIT_WHQTY());
                    icQty = Double.valueOf(ingredientsList.get(0).getRIT_ICQTY());
                    qtyUsed = Double.valueOf(ingredientsList.get(0).getRIT_QTYUSED());
                }else {
                    mToastMessage.showToast(PickRepackIngredientsActivity.this,"Ingredients data not available");
                    LogfileCreator.mAppendLog("No data available in saveList,ingredientsList(PickRepackIngredientsActivity)");
                }



                if (enteredQty.equalsIgnoreCase("")) {


                    mToastMessage.showToast(PickRepackIngredientsActivity.this,
                            "Please Enter the Qty");
                    edtQty.requestFocus();
                    /*  } else if (Double.parseDouble(enteredQty) > Double.parseDouble(saveList.get(0).getRIT_TRKQTYPK())) {*/

                }/*else if (qtyUsed<=0){
                    mToastMessage.showToast(PickRepackIngredientsActivity.this,
                            "No Qty available on this pallet");
                }
               *//* else if (totalQty<=0){
                    mToastMessage.showToast(PickRepackIngredientsActivity.this,
                            "Qty less allocation insufficient on this pallet");
                }*//*
                else if (whQty<totalQty){
                    mToastMessage.showToast(PickRepackIngredientsActivity.this,
                            "Insufficient quantity in WH Quantity table");
                }
                else if (icQty<totalQty){
                    mToastMessage.showToast(PickRepackIngredientsActivity.this,
                            "Insufficient quantity in IC Quantity table");
                }else if (Double.parseDouble(enteredQty) > firstQty) {
                    mToastMessage.showToast(PickRepackIngredientsActivity.this,
                            "Qty entered is more than available Qty");
                    edtQty.requestFocus();
                } */ else if (wLotNoList.contains(PalletNumber)) {
                    //Double.parseDouble(saveList.get(0).getRIT_TRKQTYPK())<Double.parseDouble(enteredQty);
                    /*mDbHelper.openReadableDatabase();
                    saveList = mDbHelper.getWlotRepackIngredients(PalletNumber);
                    mDbHelper.closeDatabase();*/
                    String UpdFlag = "", AddFlag = "";
                      if (saveList.size()>0){              //SCS CIRK 2022/07/25 CT69649C:
                          if (!enteredQty.equalsIgnoreCase(saveList.get(0).getRIT_QTYUSED()) || (saveList.get(0).getRIT_TRANLINENO() != null && !saveList.get(0).getRIT_TRANLINENO().contains("-"))) {
                              allocQty = String.valueOf(Double.parseDouble(enteredQty) - Double.parseDouble(saveList.get(0).getRIT_QTYUSED()));
                          } else {
                              allocQty = saveList.get(0).getRIT_QTYUSED();
                          }
                          allocQtyForSave = allocQty;

                          Double temp = Double.parseDouble(saveList.get(0).getRIT_TRKQTYPK());
                          Double temp2 = Double.parseDouble(enteredQty);

                  //  if ((Double.parseDouble(saveList.get(0).getRIT_TRKQTYPK()) <= Double.parseDouble(enteredQty)) && (saveList.get(0).getRIT_TRANLINENO() != null && !saveList.get(0).getRIT_TRANLINENO().contains("-"))) {
                    if ((Double.parseDouble(saveList.get(0).getRIT_TRKQTYPK()) <= Double.parseDouble(enteredQty)) && (saveList.get(0).getRIT_TRANLINENO() != null && !saveList.get(0).getRIT_TRANLINENO().contains("-")) && saveList.size()>0) {      //SCS CIRK 2022/07/25 CT69649C:



                        new SaveRepackRawData(mUsername, allocQtyForSave).execute();
                        /*if (saveList.get(0).getRIT_TRANLINENO().contains("-")) {
                            UpdFlag = "0";
                            AddFlag = "1";
                        } else {
                            UpdFlag = "1";
                            AddFlag = "0";
                        }
                        //String allocQty2 = String.valueOf(Double.parseDouble(enteredQty) - Double.parseDouble(saveList.get(0).getRIT_TRKQTYPK()));
                        String tempAlloc = String.valueOf(Double.parseDouble(enteredQty) - Double.parseDouble(saveList.get(0).getRIT_QTYUSED()));
*/
                       /* mDbHelper.openWritableDatabase();
                        mDbHelper.updateAllocQty(saveList.get(0).getRIT_ITEM(),saveList,allocQtyForSave,enteredQty,UpdFlag,AddFlag,tempAlloc);
                        mDbHelper.closeDatabase();

                        mDbHelper.openReadableDatabase();
                        repackFGList = mDbHelper.getRepackIngredients();
                        mDbHelper.closeDatabase();

                        adapter = new RepackIngredientsAdapter(PickRepackIngredientsActivity.this, repackFGList);
                        transList.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        edtPallet.setText("");
                        edtQty.setText("");
                        tvLotRfId.setText("");
                        edtPallet.requestFocus();
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);*/
                    } else if ((Double.parseDouble(saveList.get(0).getRIT_TRKQTYPK()) >= Double.parseDouble(enteredQty)) && (saveList.get(0).getRIT_TRANLINENO() != null && !saveList.get(0).getRIT_TRANLINENO().contains("-")) && saveList.size()>0) {   //SCS CIRK 2022/07/25 CT69649C:

                        if (saveList.get(0).getRIT_TRANLINENO().contains("-")) {
                            UpdFlag = "0";
                            AddFlag = "1";
                        } else {
                            UpdFlag = "1";
                            AddFlag = "0";
                        }


                        String allocQty2 = String.valueOf(Double.parseDouble(enteredQty) - Double.parseDouble(saveList.get(0).getRIT_TRKQTYPK()));
                        tempAlloc2 = "-" + saveList.get(0).getRIT_TEMPALLOC();

                        mDbHelper.openWritableDatabase();
                        mDbHelper.updateAllocQty(saveList.get(0).getRIT_ITEM(), saveList, allocQty2, enteredQty, UpdFlag, AddFlag, tempAlloc2);
                        mDbHelper.closeDatabase();
                        mDbHelper.openReadableDatabase();
                        repackFGList = mDbHelper.getRepackIngredients();
                        mDbHelper.closeDatabase();

                        adapter = new RepackIngredientsAdapter(PickRepackIngredientsActivity.this, repackFGList);
                        transList.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        edtPallet.setText("");
                        edtQty.setText("");
                        /* tvLotRfId.setText("");*/
                        edtPallet.requestFocus();
//               4         finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);

                    } else {

                        new SaveRepackRawData(mUsername, allocQtyForSave).execute();
                    }
                }else {
                    mToastMessage.showToast(PickRepackIngredientsActivity.this," Data not available to save");
                    LogfileCreator.mAppendLog("No data available in saveList,ingredientsList(PickRepackIngredientsActivity)");
                }
                } else {
                    /*mDbHelper.openReadableDatabase();
                    saveList = mDbHelper.getWlotRepackIngredients(PalletNumber);
                    mDbHelper.closeDatabase();*/

                    allocQtyForSave = enteredQty;
                    if(saveList.size()>0) {
                        new SaveRepackRawData(mUsername, allocQtyForSave).execute();
                    }else {
                        mToastMessage.showToast(PickRepackIngredientsActivity.this," Data not available to save");
                        LogfileCreator.mAppendLog("No data available in saveList,ingredientsList(PickRepackIngredientsActivity)");
                    }
                }

            }
        });

    }

    public boolean validateDate() {

        String allocQty = "";
        boolean result = true;
        enteredQty = edtQty.getText().toString();
        mDbHelper.openReadableDatabase();
        saveList = mDbHelper.getWlotRepackIngredients(PalletNumber.trim());
        mDbHelper.closeDatabase();
        if(saveList.size()>0 && ingredientsList.size()>0){
            firstQty = Double.parseDouble(saveList.get(0).getRIT_REMARKS()) + Double.parseDouble(saveList.get(0).getRIT_QTYUSED());

            totalQty = Double.valueOf(ingredientsList.get(0).getRIT_QTYUSED()) - Double.valueOf(ingredientsList.get(0).getRIT_RPALLOCQTY());
            whQty = Double.valueOf(ingredientsList.get(0).getRIT_WHQTY());
            icQty = Double.valueOf(ingredientsList.get(0).getRIT_ICQTY());
            qtyUsed = Double.valueOf(ingredientsList.get(0).getRIT_QTYUSED());
        } else if (qtyUsed <= 0&&!isLocalData) {
            mToastMessage.showToast(PickRepackIngredientsActivity.this,
                    "No Qty available on the " + PalletNumber + " pallet");
            result = false;
        } else if (totalQty <= 0&&!isLocalData) {
            mToastMessage.showToast(PickRepackIngredientsActivity.this,
                    "Qty less allocation insufficient on the " + PalletNumber + "  pallet");
            result = false;
        } else if (whQty < totalQty) {
            mToastMessage.showToast(PickRepackIngredientsActivity.this,
                    "Insufficient quantity in WH Quantity table for the Pallet " + PalletNumber);
            result = false;
        } else if (icQty < totalQty) {
            mToastMessage.showToast(PickRepackIngredientsActivity.this,
                    "Insufficient quantity in IC Quantity table for the Pallet " + PalletNumber);
            result = false;
        } else if (Double.parseDouble(enteredQty) > firstQty) {
            mToastMessage.showToast(PickRepackIngredientsActivity.this,
                    "Qty entered is more than available Qty for the Pallet " + PalletNumber);
            edtQty.requestFocus();
            result = false;
        }
        return result;
    }

    public void SetData() {

        mDbHelper.openReadableDatabase();
        ingredientsList = mDbHelper.getWlotRepackIngredients(PalletNumber.trim());
        mDbHelper.closeDatabase();
        Supporter.SUMQTY = false;
        isLocalData = true;
        edtPallet.setText(PalletNumber.trim());

        if(ingredientsList.size()>0){
            totalQtY = Double.valueOf(ingredientsList.get(0).getRIT_QTYUSED()) - Double.valueOf(ingredientsList.get(0).getRIT_RPALLOCQTY());
            //totalQtY = Double.valueOf(ingredientsList.get(0).getRIT_QTYUSED());
       /*if(String.valueOf(totalQtY).contains("-")){
           edtQty.setText(String.valueOf(Math.round(Double.valueOf(ingredientsList.get(0).getRIT_QTYUSED()))));
       }else {
           edtQty.setText(String.valueOf(Math.round(totalQtY)));
       }*/
            edtQty.setText(String.valueOf(Math.round(Double.valueOf(ingredientsList.get(0).getRIT_QTYUSED()))));
            // edtQty.setText(String.valueOf(Math.round(totalQtY)));
            edtQty.requestFocus();
            edtQty.setSelectAllOnFocus(true);
            edtQty.selectAll();
            edtPallet.setEnabled(false);
            btnSave.setEnabled(true);
            btnCancel.setEnabled(true);
            edtQty.setEnabled(true);
            edtQty.setSelectAllOnFocus(true);
            //    isLocalData = true;
        }else{
            mToastMessage.showToast(PickRepackIngredientsActivity.this,"Ingredient Data not available");
            LogfileCreator.mAppendLog("No data available in ingredientsList(PickRepackIngredientsActivity)");
        }


    }

    @Override
    public void onBackPressed() {
      //  cancelAlert();
    }


    public void cancelAlert() {
        AlertDialog.Builder alertUser = new AlertDialog.Builder(this);
        alertUser.setTitle("Confirmation");
        alertUser.setIcon(R.drawable.warning);
        alertUser.setCancelable(false);
        alertUser.setMessage("Are you sure you want to cancel?");
        alertUser.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       // mSupporter.simpleNavigateTo(PickRepackActivity.class);
                        Intent theIntent = new Intent(PickRepackIngredientsActivity.this, PickRepackActivity.class);
                        theIntent.putExtra("lockstatus",isLocked);
                        theIntent.putExtra("isdeviceSidelock",isdeviceSideLock);
                        startActivity(theIntent);
                    }
                });

        alertUser.setNegativeButton("No",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertUser.show();
    }

/*    public void StatusLockAlert() {
        final AlertDialog.Builder alertUser = new AlertDialog.Builder(this);
        alertUser.setTitle("Alert");
        alertUser.setIcon(R.drawable.warning);
        alertUser.setCancelable(false);
        alertUser.setMessage("This repack has been locked by VP user"+lockUserName+".you can only View");
        alertUser.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


        alertUser.show();
    }*/
    public void StatusLockAlertDivice() {
        final AlertDialog.Builder alertUser = new AlertDialog.Builder(this);
        alertUser.setTitle("Alert");
        alertUser.setIcon(R.drawable.warning);
        alertUser.setCancelable(false);
        alertUser.setMessage("This repack has been locked by scanner user"+lockUserName+".you can only View");
        alertUser.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //  isdeviceSideLock = false;
                    }
                });


        alertUser.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.print_pick_task_item, menu);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
       /*     case R.id.print_pick_task_item:

                if(mSupporter.isNetworkAvailable(PickTaskActivity.this)){
                    mDbHelper.openWritableDatabase();
                    mDbHelper.UpdateFromPickTaskpallet();
                    mDbHelper.closeDatabase();
                    mDbHelper.openWritableDatabase();
                    mDbHelper.UpdateFromPickTaskdetail();
                    mDbHelper.closeDatabase();
                    mDbHelper.openWritableDatabase();
                    mDbHelper.UpdateFromPickTaskWHITRLS();
                    mDbHelper.closeDatabase();
                    PDFCreate();

                } else {
                    mToastMessage.showToast(PickTaskActivity.this,
                            "Unable to connect with Server. Please Check your internet connection");
                }
                break;*/
        }
        return true;
    }

    // to get Pallet List
    public List<String> getPalletList() {
        List<String> PalletList = new ArrayList<String>();

        mDbHelper.openReadableDatabase();
        mPalletMast = mDbHelper.getPalletList();
        mDbHelper.closeDatabase();
        for (int i = 0; i < mPalletMast.size(); i++) {
            PalletList.add(mPalletMast.get(i).getPalno());
        }
        return PalletList;
    }

    // to get Lot List
    public List<String> getLotList() {
        List<String> LotList = new ArrayList<String>();

        mDbHelper.openReadableDatabase();
        //mLotMast = mDbHelper.getLotList();
        mLotMast = mDbHelper.getWHMQTYLotList();
        mDbHelper.closeDatabase();
        for (int i = 0; i < mLotMast.size(); i++) {
            LotList.add(mLotMast.get(i).getWlotno());
        }
        return LotList;
    }

    public String getLotItemList(String lotno) {
        String lotItem = "";

        mDbHelper.openReadableDatabase();
        lotItem = mDbHelper.getLotItemList(lotno);
        mDbHelper.closeDatabase();
        return lotItem;
    }

    private void PDFCreate() {

        mDbHelper.openReadableDatabase();
        PrintLabelList = mDbHelper.getPickTaskPrintLabel();
        mDbHelper.closeDatabase();
        new PrinterConnectOperation().execute();
    }

    public boolean DataToPrint() {
        boolean resultSent = false;
        try {
            createfile();
            document = new Document();
            document.setMargins(13, 3, 1, 1);
            docWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
            cb = docWriter.getDirectContent();
            if (PrintLabelList.size()>0){
                picktaskPrintlabel printLabel = PrintLabelList.get(0);
                palno = printLabel.getPalno();
                Barcode128 barcode128 = new Barcode128();
                barcode128.setCode(palno);
                barcode128.setFont(null);
                barcode128.setCodeType(Barcode.CODE128);
                Image code128Image = barcode128.createImageWithBarcode(cb, null, null);
                code128Image.setAlignment(Image.ALIGN_CENTER);
                code128Image.setWidthPercentage(50);
                document.add(code128Image);
            }else{
                mToastMessage.showToast(PickRepackIngredientsActivity.this,"No data available to Print");
                LogfileCreator.mAppendLog("No data available in PrintLabelList(PickRepackIngredientsActivity)");
            }


            Paragraph reportDetail1 = new Paragraph();
            reportDetail1.setFont(FONT_TABLE_CONTANT); //
            writeDetail1(reportDetail1);
            document.add(reportDetail1);

            Paragraph reportDetail2 = new Paragraph();
            reportDetail2.setFont(FONT_BODY); //
            writeDetail2(reportDetail2);
            document.add(reportDetail2);

            Paragraph reportDetail3 = new Paragraph();
            reportDetail3.setFont(FONT_TABLE_CONTANT); //
            writeDetail3(reportDetail3);
            document.add(reportDetail3);
            document.close();
            resultSent = true;

        } catch (Exception e) {
            resultSent = false;
        } finally {
            return resultSent;
        }
    }

    private void createfile() {
        try {

            File root_path = new File(Environment.getExternalStorageDirectory() + "/Android/WMS/PrintReport/");

            if (!root_path.exists()) {
                root_path.mkdirs();
            }

            mPath = (Environment.getExternalStorageDirectory() + "/Android/WMS/PrintReport/" + filename);

            pdfFile = new File(mPath);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeDetail1(Paragraph reportBody) {
        float[] columnWidths = {5f, 4f};
        table = new PdfPTable(columnWidths);
        // set table width a percentage of the page width
        table.setWidthPercentage(100);
        try {

            if(PrintLabelList.size()>0) {
                picktaskPrintlabel printLabel = PrintLabelList.get(0);
                stop = printLabel.getStop();
                if (stop == null) {
                    stop = "";
                }
                stop = "Stop " + stop;

                trailer = printLabel.getTrailer();
                if (trailer == null) {
                    trailer = "";
                }
                trailer = "Trailer " + trailer;

                cell = new PdfPCell(new Phrase(stop, FONT_TABLE_CONTANT));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);


                cell = new PdfPCell(new Phrase(trailer, FONT_TABLE_CONTANT));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);

                reportBody.add(table);
            }else{
                mToastMessage.showToast(PickRepackIngredientsActivity.this,"No data available to Print");
                LogfileCreator.mAppendLog("No data available in PrintLabelList(PickRepackIngredientsActivity)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeDetail2(Paragraph reportBody) {
        float[] columnWidths = {5f, 4f};
        table = new PdfPTable(columnWidths);
        // set table width a percentage of the page width
        table.setWidthPercentage(100);
        try {

            if(PrintLabelList.size()>0){
            picktaskPrintlabel printLabel = PrintLabelList.get(0);
            route = printLabel.getRoute();
            if (route == null) {
                route = "";
            }
            route = "Route " + route;

            dock = printLabel.getDock();
            if (dock == null) {
                dock = "";
            }
            dock = "Dock " + dock;

            deldate = printLabel.getDeldate();
            if (deldate == null) {
                deldate = "";
            }
            deldate = "Del Date " + deldate;

            order = printLabel.getOrderno();
            if (order == null) {
                order = "";
            }
            order = "Order # " + order;

            task = printLabel.getTaskno();
            if (task == null) {
                task = "";
            }
            task = "Task #" + task;

            custid = printLabel.getCustid();
            if (custid == null) {
                custid = "";
            }
            custid = "Cust #" + custid;

            picker = printLabel.getPicker();
            if (picker == null) {
                picker = "";
            }
            picker = "Picker " + picker;

            cell = new PdfPCell(new Phrase(route, FONT_TABLE_BODY));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);


            cell = new PdfPCell(new Phrase(dock, FONT_TABLE_BODY));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(deldate, FONT_TABLE_BODY));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);


            cell = new PdfPCell(new Phrase("01 of 01", FONT_TABLE_BODY));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(order, FONT_TABLE_BODY));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);


            cell = new PdfPCell(new Phrase(task, FONT_TABLE_BODY));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(custid, FONT_TABLE_BODY));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);


            cell = new PdfPCell(new Phrase(picker, FONT_TABLE_BODY));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            reportBody.add(table);
            }else{
                mToastMessage.showToast(PickRepackIngredientsActivity.this,"No data available to Print");
                LogfileCreator.mAppendLog("No data available in PrintLabelList(PickRepackIngredientsActivity)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeDetail3(Paragraph reportBody) {
        try {
            if(PrintLabelList.size()>0){
            picktaskPrintlabel printLabel = PrintLabelList.get(0);
            custname = printLabel.getCustname();
            if (custname == null) {
                custname = "";
            }

            Paragraph childParagraph = new Paragraph(custname, FONT_TABLE_CONTANT);
            childParagraph.setAlignment(Element.ALIGN_LEFT);
            reportBody.add(childParagraph);
            }else{
                mToastMessage.showToast(PickRepackIngredientsActivity.this,"No data available to Print");
                LogfileCreator.mAppendLog("No data available in PrintLabelList(PickRepackIngredientsActivity)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private class PrinterConnectOperation extends
            AsyncTask<String, String, String> {

        private ProgressDialog dialog;

        public PrinterConnectOperation() {
            dialog = new ProgressDialog(PickRepackIngredientsActivity.this);
            dialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "failed";
            try {
                boolean isDataSentSuccess;
                isDataSentSuccess = DataToPrint();
                if (isDataSentSuccess) {
                    result = "success";
                }
                return result;

            } catch (Exception e) {

                Log.e("tag", "error", e);
                LogfileCreator.mAppendLog("In doInBackground method: "
                        + e.getMessage());
                result = "error";
                toasttext = "File Createion Failed";
            }

            return result;
        }


        @Override
        protected void onPostExecute(final String result) {
            if (dialog != null) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
            if (result.equals("success")) {
                toasttext = "Print PDF creation Success ";
                boolean isDataSentSuccess = true;
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        toasttext);
                try {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                String storage = mPath;
                                File file = new File(storage);
                                Intent target = new Intent(Intent.ACTION_VIEW);
                                target.setDataAndType(Uri.fromFile(file), "application/pdf");
                                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                                Intent intent = Intent.createChooser(target, "Open File");
                                startActivity(intent);

                            } catch (Exception e) {
                                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                                        "No Application match to Open Print File");
                            }
                        }
                    }, 1000);

                } catch (Exception e) {
                    mToastMessage.showToast(PickRepackIngredientsActivity.this, "Not Software Match to Open Print File");
                    mSupporter.simpleNavigateTo(PickRepackIngredientsActivity.class);
                }


            } else {
                toasttext = "Print PDF creation Failed";
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        toasttext);
                mSupporter.simpleNavigateTo(PickRepackIngredientsActivity.class);

            }
        } // end of PostExecute method...

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Creating PDF...");
            this.dialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            this.dialog.setMessage(values[0]);
        }
    }

    public void SplitDetailLine(picktaskdetail tpicktaskdetail, double Qty) {

        mDbHelper.openReadableDatabase();
        int TranlineCount = mDbHelper.mTranlineCount();
        mDbHelper.closeDatabase();
        mDbHelper.openReadableDatabase();
        int DoclineCount = mDbHelper.mDoclineCount();
        mDbHelper.closeDatabase();
        mDbHelper.openReadableDatabase();
        int rowNo = mDbHelper.mRowNoCount();
        mDbHelper.closeDatabase();

        mDbHelper.openWritableDatabase();
        String sQty = String.valueOf(Qty);
        String cQty = mDbHelper.DecimalFractionConversion(sQty, decnum);
        Globals.gTqty = Double.valueOf(cQty);

        Globals.gTranlineno = TranlineCount + 1;
        /*if(String.valueOf(TranlineCount).contains("99")){
            Globals.gTranlineno = TranlineCount+1;
        }else {
            Globals.gTranlineno = 9901;
        }*/
        //strTranlineNo = String.valueOf(Globals.gTranlineno);
        Globals.gDoclineno = DoclineCount + 1;
        Globals.gPTDetailRowCount = rowNo + 1;
        mDbHelper.SplitNewLine(tpicktaskdetail, Globals.gTranlineno, Globals.gDoclineno, Globals.gPTDetailRowCount, Globals.gTqty);
        mDbHelper.closeDatabase();
    }

    //Repack list load the page
    class GetRepackRawData extends AsyncTask<String, String, String> {
        private ProgressDialog dialog;
        private String uCode;

        public GetRepackRawData(String user) {
            this.uCode = user;
            dialog = new ProgressDialog(PickRepackIngredientsActivity.this);
            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Please wait...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            String result = "";

            try {
                SoapObject request = new SoapObject(NAMESPACE, METHOD_GET_RAW_DATA);
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                PropertyInfo info = new PropertyInfo();
                info.setName("pSessionId");
                info.setValue(mSessionId);
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pCompany");
                info.setValue(mCompany);
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pWLotno");
                info.setValue(PalletNumber);
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pLoctid");
                info.setValue(mLoctid);
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pUserName");
                info.setValue(uCode);
                info.setType(String.class);
                request.addProperty(info);

                envelope.dotNet = true;// to handle .net services asmx/aspx
                envelope.setOutputSoapObject(request);
                HttpTransportSE ht = new HttpTransportSE(URL_PROTOCOL + URL_SERVER_PATH + "/" + APPLICATION_NAME + "/" + URL_SERVICE_NAME);
                ht.debug = true;
                String soap_action = NAMESPACE + METHOD_GET_RAW_DATA;
                ht.call(soap_action, envelope);
                SoapPrimitive resultString = (SoapPrimitive) envelope.getResponse();
                File mImpOutputFile = Supporter.getImpOutputFilePathByCompany(uCode, "01", "RepackDataIngr" + ".xml");
                if (!mImpOutputFile.exists()) {
                    Supporter.createFile(mImpOutputFile);
                } else {
                    mImpOutputFile.delete(); // to refresh the file
                    Supporter.createFile(mImpOutputFile);
                }

                BufferedWriter buf = new BufferedWriter(new FileWriter(
                        mImpOutputFile, true));

                buf.append(resultString.toString());

                if (resultString.toString().contains("No Data Found")) {
                    // result = resultString.toString();
                    result = "No Data Found";

                } else if (resultString.toString().contains("false")) {
                    result = "Failed";
                } else {
                    result = "success";
                }
                buf.close();

            } catch (SocketTimeoutException e) {
                result = "time out error";
                e.printStackTrace();
            } catch (IOException e) {
                result = "input error";
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                result = "error";
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("tag", "error", e);
                result = "error";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub


            if (result.equals("success")) {
                new LoadRepackData().execute();

                btnSave.setEnabled(true);
                btnCancel.setEnabled(true);
                edtQty.setEnabled(true);

            } else if (result.equals("Failed")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "Invalid Pallet Number");
                edtPallet.setText("");
                edtQty.setText("");
                btnSave.setEnabled(false);
                edtPallet.requestFocus();
            } else if (result.equals("No Data Found")) {
                if (!isLocalData) {
                    mToastMessage.showToast(PickRepackIngredientsActivity.this, "Invalid Pallet Number.");
                    edtPallet.setText("");
                    edtQty.setText("");
                    btnSave.setEnabled(false);
                    edtPallet.setEnabled(true);
                    edtPallet.requestFocus();
                }
                isLocalData = false;

               /* edtPallet.setText("");
                edtQty.setText("");
                btnSave.setEnabled(false);
                edtPallet.setEnabled(true);
                edtPallet.requestFocus();*/
            } else if (result.equalsIgnoreCase("time out error")) {
                new GetRepackRawData(mUsername).execute();
            } else {
                edtPallet.setText("");
                edtQty.setText("");
                btnSave.setEnabled(false);
                edtPallet.requestFocus();
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        result);
            }
            dialog.cancel();
        }
    }

    private class LoadRepackData extends
            AsyncTask<String, String, String> {

        private ProgressDialog dialog;

        public LoadRepackData() {
            dialog = new ProgressDialog(PickRepackIngredientsActivity.this);
            dialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... params) {

            String errMsg = "";

            try {

                String result = "";

                DataLoader fileLoader = new DataLoader(PickRepackIngredientsActivity.this, mDbHelper, Globals.gUsercode);

                List<String> importFileList = mSupporter.loadImportFileList();

                int totImpFile = importFileList.size();

                File salPer_folder_path = Supporter.getImportFolderPath(Globals.gUsercode);

                List<String> compList = mSupporter.getFolderNames(salPer_folder_path);

                int compSize = compList.size();
                /*mDbHelper.openWritableDatabase();
                mDbHelper.deleteTaskList();
                mDbHelper.closeDatabase();*/
                if (compSize != 0) {

                    startDBTransaction("db data loading"); // to start db
                    // transaction

                    for (int c = 0; c < compList.size(); c++) {

                        for (int i = 0; i < totImpFile; i++) {

                            String fileName = "Acknowledgement";

                            if ((c > 0) && (fileName.equals("Acknowledgement"))) {
                                continue; // to continue for other files
                            }

                            mImpOutputFile = Supporter.getImpOutputFilePathByCompany(Globals.gUsercode, "01", "RepackDataIngr" + ".xml");
                            System.out.println("Name" + mImpOutputFile);
                            if (mImpOutputFile.exists()) {
                                InputStream inputStream;
                                inputStream = new FileInputStream(mImpOutputFile);
                                String[] resultArray = fileLoader.parseDocument(inputStream);
                                result = resultArray[0];
                                errMsg = resultArray[1];

                                if (!result.equals("success")) {
                                    mDbHelper.mEndTransaction();
                                    break;
                                }

                            } else {
                                result = "File not available";
                                mDbHelper.mEndTransaction();
                                break;
                            }
                        }

                        if (!result.equals("success")) {

                            break;
                        }
                    }
                    endDBTransaction(); // to end db transaction

                } else {
                    result = "File not available";
                }

                return result;
            } catch (Exception exe) {
                exe.printStackTrace();
                String errorCode = "Err-CLS-2";
                LogfileCreator.mAppendLog(errorCode + " : " + exe.getMessage()
                        + "\n" + errMsg);
                String result = "error";
                return result;
            }
        }

        @Override
        protected void onPostExecute(final String result) {

            if (dialog != null) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }

            if (result.equals("success")) {
                mDbHelper.openReadableDatabase();
                ingredientsList = mDbHelper.getWlotRepackIngredients(PalletNumber.trim());
                mDbHelper.closeDatabase();
                if (!isLocalData&&ingredientsList.size()>0) {

                    toTalQtY = Double.valueOf(ingredientsList.get(0).getRIT_QTYUSED()) - Double.valueOf(ingredientsList.get(0).getRIT_RPALLOCQTY());
                    edtQty.setText(String.valueOf(Math.round(Double.parseDouble(ingredientsList.get(0).getRIT_QTYUSED()))));
                } else {
                    SetData();
                }

                boolean validationResult = validateDate();
                if (validationResult) {

                    edtPallet.setText(PalletNumber.trim());
                    isLocalData = false;
                    edtQty.requestFocus();
                    edtQty.setSelectAllOnFocus(true);
                    edtQty.selectAll();
                    edtPallet.setEnabled(false);
                } else {
                    edtPallet.setText("");
                    edtQty.setText("");
                    btnSave.setEnabled(false);
                    edtPallet.requestFocus();
                }

                // PickTaskActivity.this.notifyAll();

               /* mDbHelper.openReadableDatabase();
                repackFGList = mDbHelper.getRepackIngredients();
                mDbHelper.closeDatabase();

                adapter = new RepackIngredientsAdapter(PickRepackIngredientsActivity.this, repackFGList);
                transList.setAdapter(adapter);
                adapter.notifyDataSetChanged();*/
                /*mDbHelper.openReadableDatabase();
                ingredientsList = mDbHelper.getWlotRepackIngredients(PalletNumber.trim());
                mDbHelper.closeDatabase();
                edtPallet.setText(PalletNumber.trim());
                *//*tvLotRfId.setText(ingredientsList.get(0).getRIT_LOTREFID());*//*
                if (!isLocalData) {
                  //  IngredientsList = Double.parseDouble(ingredientsList.get(0).getRIT_QTYUSED());
                    toTalQtY = Double.valueOf(ingredientsList.get(0).getRIT_QTYUSED())-Double.valueOf(ingredientsList.get(0).getRIT_RPALLOCQTY());

                    //edtQty.setText(String.valueOf(Math.round(toTalQtY)));
                    edtQty.setText(String.valueOf(Math.round(Double.parseDouble(ingredientsList.get(0).getRIT_QTYUSED()))));
                }else {
                    SetData();
                }
                isLocalData = false;
                edtQty.requestFocus();
                edtQty.setSelectAllOnFocus(true);
                edtQty.selectAll();
                edtPallet.setEnabled(false);*/
                //btnIngredients.setEnabled(true);
                //setViewsData();

                //smSupporter.simpleNavigateTo(PickTaskMenuActivity.class);
            } else if (result.equals("nosd")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this, "Sd card required");
            } else if (result.equals("parsing error")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this, "Error during parsing the data");
            } else if (result.equals("File not available")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this, "File not available");
            } else {
                mToastMessage.showToast(PickRepackIngredientsActivity.this, "Error");
            }
        }

        @Override
        protected void onPreExecute() {
        //    this.dialog.setMessage("Please wait until the Item data is loaded");
            this.dialog.setMessage("Please wait...");
            this.dialog.show();
        }
    }

    class SaveRepackRawData extends AsyncTask<String, String, String> {
        private ProgressDialog dialog;
        private String uCode;
        private String qty;

        public SaveRepackRawData(String user, String qty) {
            this.uCode = user;
            this.qty = qty;
            dialog = new ProgressDialog(PickRepackIngredientsActivity.this);
            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Please wait...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            String result = "";

            try {
                SoapObject request = new SoapObject(NAMESPACE, METHOD_SAVE_RAW_DATA);
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                PropertyInfo info = new PropertyInfo();
                info.setName("pSessionId");
                info.setValue(mSessionId);
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pCompany");
                info.setValue(mCompany);
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pLoctid");
                info.setValue(mLoctid);
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pWlotno");
                info.setValue(saveList.get(0).getRIT_WLOTNO());
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pLotno");
                info.setValue(saveList.get(0).getRIT_LOTNO());
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pPalno");
                info.setValue(saveList.get(0).getRIT_LOTREFID());
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pSlot");
                info.setValue(saveList.get(0).getRIT_SLOT());
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("Item");
                info.setValue(saveList.get(0).getRIT_ITEM());
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pAllocQty");
                info.setValue(qty);
                info.setType(DecimalFormat.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pUserName");
                info.setValue(uCode);
                info.setType(String.class);
                request.addProperty(info);

                envelope.dotNet = true;// to handle .net services asmx/aspx
                envelope.setOutputSoapObject(request);
                HttpTransportSE ht = new HttpTransportSE(URL_PROTOCOL + URL_SERVER_PATH + "/" + APPLICATION_NAME + "/" + URL_SERVICE_NAME);
                ht.debug = true;
                String soap_action = NAMESPACE + METHOD_SAVE_RAW_DATA;
                ht.call(soap_action, envelope);
                SoapPrimitive resultString = (SoapPrimitive) envelope.getResponse();
                File mImpOutputFile = Supporter.getImpOutputFilePathByCompany(uCode, "01", "RepackData" + ".xml");
                if (!mImpOutputFile.exists()) {
                    Supporter.createFile(mImpOutputFile);
                } else {
                    mImpOutputFile.delete(); // to refresh the file
                    Supporter.createFile(mImpOutputFile);
                }

                BufferedWriter buf = new BufferedWriter(new FileWriter(
                        mImpOutputFile, true));

                buf.append(resultString.toString());

                if (resultString.toString().contains("Does not match")) {
                    result = "Does not match";
                } else if (resultString.toString().contains("Insufficient Qty")) {
                    result = "Insufficient Qty";
                } else if (resultString.toString().contains("false")) {
                    result = "Failed";
                } else {
                    result = "success";
                }
                buf.close();

            } catch (SocketTimeoutException e) {
                result = "time out error";
                e.printStackTrace();
            } catch (IOException e) {
                result = "input error";
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                result = "error";
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("tag", "error", e);
                result = "error";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub


            if (result.equals("success")) {
                isLocalData = false;
                //new LoadRepackData(adapter).execute();
                int tempTran = 0, tempTran2 = 0;
                String tranNum = saveList.get(0).getRIT_TRANLINENO();
               /* mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "Qty Allocated");*/


                if (tranNum == null) {
                    mDbHelper.openReadableDatabase();
                    tempTran = mDbHelper.getMaxTranNum();
                    mDbHelper.closeDatabase();
                    tempTran2 = tempTran + (-1);
                    saveList.get(0).setRIT_TRANLINENO(Integer.toString(tempTran2));
                    saveList.get(0).setRIT_QTYUSED(enteredQty);
                    saveList.get(0).setRIT_TEMPALLOC(enteredQty);
                    saveList.get(0).setRIT_PANO(repackNum.trim());
                    saveList.get(0).setRIT_LOTEXPL("1");
                    saveList.get(0).setRIT_LINESPLIT("0");

                    mDbHelper.openWritableDatabase();
                    mDbHelper.updateFlagData(saveList.get(0).getRIT_ITEM(), saveList, saveList.get(0).getRIT_WLOTNO(), "1", "0");
                    mDbHelper.closeDatabase();
                } else {
                    String UpdFlag = "", AddFlag = "";
                    if (saveList.get(0).getRIT_TRANLINENO() != null && saveList.get(0).getRIT_TRANLINENO().contains("-")) {
                        UpdFlag = "0";
                        AddFlag = "1";
                    } else {
                        UpdFlag = "1";
                        AddFlag = "0";
                    }
                    if (Double.parseDouble(saveList.get(0).getRIT_TRKQTYPK()) < Double.parseDouble(enteredQty)) {

                    } else {

                    }
                    String allocQty = String.valueOf(Double.parseDouble(enteredQty) - Double.parseDouble(saveList.get(0).getRIT_TRKQTYPK()));
                    String tempAlloc = String.valueOf(Double.parseDouble(enteredQty) - Double.parseDouble(saveList.get(0).getRIT_QTYUSED()));
                    mDbHelper.openWritableDatabase();
                    mDbHelper.updateRawItemQty(saveList.get(0).getRIT_ITEM(), saveList, enteredQty, allocQty, UpdFlag, AddFlag, tempAlloc);
                    mDbHelper.closeDatabase();
                }

                mDbHelper.openReadableDatabase();
                repackFGList = mDbHelper.getRepackIngredients();
                mDbHelper.closeDatabase();

                adapter = new RepackIngredientsAdapter(PickRepackIngredientsActivity.this, repackFGList);
                transList.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                edtPallet.setText("");
                edtQty.setText("");
                /*tvLotRfId.setText("");*/
                edtPallet.requestFocus();
             //   finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);

                //adapter.notifyDataSetChanged();

            } else if (result.equals("Failed")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "Invalid Pallet Number");
            } else if (result.equalsIgnoreCase("time out error")) {
                if(saveList.size()>0){
                new SaveRepackRawData(mUsername, allocQtyForSave).execute();
                }else {
                    mToastMessage.showToast(PickRepackIngredientsActivity.this," Data not available to save");
                    LogfileCreator.mAppendLog("No data available in saveList,ingredientsList(PickRepackIngredientsActivity)");
                }
            } else if (result.equalsIgnoreCase("Does not match")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this, "Does not match with IC quantity table");
            } else if (result.equalsIgnoreCase("Insufficient Qty")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this, "Insufficient Qty in IC Pallet table");
            } else {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        result);
            }
            dialog.cancel();
        }
    }

    private String GetErrorMessage() {

        String GetErrMsg = "";
        try {
            //creating a constructor of file class and parsing an XML file
            mImpOutputFile = Supporter.getImpOutputFilePathByCompany(Globals.gUsercode, "01", "SubPickTaskList" + ".xml");
            //an instance of factory that gives a document builder
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //an instance of builder to parse the specified xml file
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(mImpOutputFile);
            doc.getDocumentElement().normalize();
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("Acknowledgement");
            // nodeList is not iterable, so we are using for loop
            for (int itr = 0; itr < nodeList.getLength(); itr++) {
                Node node = nodeList.item(itr);
                System.out.println("\nNode Name :" + node.getNodeName());
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element eElement = (org.w3c.dom.Element) node;
                    GetErrMsg = eElement.getElementsByTagName("ErrorMessage").item(0).getTextContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorCode = "Err501";
            LogfileCreator.mAppendLog(errorCode + " : " + e.getMessage());
            String result = "Invalid File";
            return result;
        }
        return GetErrMsg;
    }

    private void startDBTransaction(String action) {
        // transaction is started here..
        mDbHelper.getWritableDatabase();
        Log.i("Writable DB Open", "Writable Database Opened.");
        mDbHelper.mBeginTransaction();
        Log.i("Transaction started", "Transaction successfully started for "
                + action);
    }

    private void endDBTransaction() {
        mDbHelper.mSetTransactionSuccess(); // setting the transaction

        Log.i("Transaction success", "Transaction success.");
        mDbHelper.mEndTransaction();
        Log.i("Transaction success", "Transaction end.");
        mDbHelper.closeDatabase();
        Log.i("DB closed", "Database closed successfully.");
    }

    private void listViewAlert() {
        List<String> itemList = new ArrayList<String>();
        mDbHelper.openReadableDatabase();
        itemList = mDbHelper.getItemList();
        mDbHelper.closeDatabase();
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(PickRepackIngredientsActivity.this);
        builderSingle.setTitle("Select SO item to Substitute");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(PickRepackIngredientsActivity.this, android.R.layout.select_dialog_singlechoice);
        for (int i = 0; i < itemList.size(); i++) {
            arrayAdapter.add(itemList.get(i));
        }

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                edtPallet.setText("");
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                selectedItem = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(PickRepackIngredientsActivity.this);

                //new LoadRepackData(adapter).execute();

            }
        });
        builderSingle.show();
    }

    class ExportTranData extends AsyncTask<String, String, String> {
        private ProgressDialog dialog;
        private String pUname, pSessionId, pCompId;
        String result = "";

        public ExportTranData(String Session, String Uname, String Compid) {
            this.pSessionId = Session;
            this.pUname = Uname;
            this.pCompId = Compid;
            dialog = new ProgressDialog(PickRepackIngredientsActivity.this);
            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Updating the server..");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            String result = "";

            try {

                SoapObject request = new SoapObject(NAMESPACE, METHOD_EXPORT_DATA);
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                File xmlData = Supporter.getImportFolderPath(pUname
                        + "/FinalExoprt/PickTask.xml");
                String pXmldata = FileUtils.readFileToString(xmlData);
                PropertyInfo info = new PropertyInfo();
                info.setName("pSessionId");
                info.setValue(pSessionId);
                info.setType(String.class);
                request.addProperty(info);
                info = new PropertyInfo();
                info.setName("pCompany");
                info.setValue(pCompId);
                info.setType(String.class);
                request.addProperty(info);
                info = new PropertyInfo();
                info.setName("pUserName");
                info.setValue(pUname);
                info.setType(String.class);
                request.addProperty(info);
                info = new PropertyInfo();
                info.setName("pXmlData");
                info.setValue(pXmldata);
                info.setType(String.class);
                request.addProperty(info);
                envelope.dotNet = true;// to handle .net services asmx/aspx
                envelope.setOutputSoapObject(request);
                HttpTransportSE ht = new HttpTransportSE(URL_PROTOCOL + URL_SERVER_PATH + "/" + APPLICATION_NAME + "/" + URL_SERVICE_NAME);
                ht.debug = true;
                String soap_action = NAMESPACE + METHOD_EXPORT_DATA;
                ht.call(soap_action, envelope);
                SoapPrimitive resultString = (SoapPrimitive) envelope.getResponse();
                File mImpOutputFile = Supporter.getImpOutputFilePathByCompany(pUname, "Result", "PickTask" + ".xml");
                if (!mImpOutputFile.exists()) {
                    Supporter.createFile(mImpOutputFile);
                } else {
                    mImpOutputFile.delete(); // to refresh the file
                    Supporter.createFile(mImpOutputFile);
                }

                BufferedWriter buf = new BufferedWriter(new FileWriter(
                        mImpOutputFile, true));

                buf.append(resultString.toString());

                if (resultString.toString().equalsIgnoreCase("Export failed.")) {
                    result = "Unable to Export.";
                } else if (resultString.toString().equalsIgnoreCase(
                        "Failed to post, refer log file.")) {
                    result = "server failed";
                } else if (resultString.toString().contains(
                        "Unexpected end of file has occurred")) {
                    result = "Unexpected";
                } else if (resultString.toString().contains(
                        "Data at the root level is invalid")) {
                    result = "Invalid";
                } else if (resultString.toString().contains(
                        "PO Updation failed.")) {
                    result = "PO Updation failed.";
                } else if (resultString.toString().contains("<Result>true</Result>")) {
                    result = "success";
                } else {
                    result = "error";
                }
                buf.close();

            } catch (SocketTimeoutException e) {
                result = "time out error";
                e.printStackTrace();
            } catch (IOException e) {
                result = "input error";
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                result = "error";
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("tag", "error", e);
                result = "error";
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub

            if (result.equals("success")) {
               /* mDbHelper.openWritableDatabase();
                mDbHelper.deletePickTaskDetail();
                mDbHelper.closeDatabase();*/
               /* mToastMessage.showToast(PickTaskActivity.this,
                        "Data exported to Server successfully");*/
                /*mDbHelper.openWritableDatabase();
                mDbHelper.updateFlagPickTask(ItemNo,strTranlineNo);
                mDbHelper.closeDatabase();*/
                mSupporter.simpleNavigateTo(PickRepackIngredientsActivity.class);
                //edtRepackNum.requestFocus();

            } else if (result.equals("server failed")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "Failed to post, refer log file.");
            } else if (result.equals("PO Updation failed.")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "PO Updation failed");
            } else if (result.equalsIgnoreCase("time out error")) {
                new ExportTranData(mSessionId, Globals.gUsercode, Globals.gCompanyId).execute();
                //mToastMessage.showToast(PickTaskActivity.this, "Time out.");
            } else if (result.equalsIgnoreCase("error")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "Unable to update Server");
            } else {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        result.toString());
            }

            dialog.cancel();
        }
    }

    public void ExportData() {
        exportTranList = new ArrayList<picktaskdetail>();

        mDbHelper.openReadableDatabase();
        /*exportTranList = mDbHelper.getPickTaskDetail();*/
        exportTranList = mDbHelper.getExportPickTaskDetail(ItemNo, strTranlineNo);
        mDbHelper.closeDatabase();

        if (exportTranList.size() != 0) {
            String exportXml = getRecordXmlExportPO(exportTranList);
            uploadDataToServiceExportItm ex = (uploadDataToServiceExportItm) new uploadDataToServiceExportItm()
                    .execute(new String[]{exportXml});
            String response = null;
            try {
                response = ex.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (exportTranList.size() != 0) {
                new ExportTranData(mSessionId, Globals.gUsercode, Globals.gCompanyId).execute();
            } else {
                ExportError();
            }
        } else {
            mToastMessage.showToast(PickRepackIngredientsActivity.this,
                    "Unable to update Server");
        }
    }

    private void ExportError() {
        AlertDialog.Builder alertExit = new AlertDialog.Builder(PickRepackIngredientsActivity.this);
        alertExit.setTitle("No Data");
        alertExit.setIcon(R.drawable.warning);
        alertExit.setCancelable(false);
        alertExit.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertExit.setMessage("No Data to Export");
        alertExit.show();
    }

    // Async task to upload the created XML to the Web Service
    public class uploadDataToServiceExportItm extends AsyncTask<String, String, String> {
        private ProgressDialog mDialog;

        public uploadDataToServiceExportItm() {
            mDialog = new ProgressDialog(PickRepackIngredientsActivity.this);
            mDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... records) {
            String result = "result";
            File exportFile = mSupporter.getImpOutputFilePathByCompany(Globals.gUsercode,
                    "FinalExoprt", "PickTask" + ".xml");
            if (!exportFile.exists()) {
                Supporter.createFile(exportFile);
            } else {
                exportFile.delete(); // to refresh the file
                Supporter.createFile(exportFile);
            }

            try {
                FileOutputStream fos = new FileOutputStream(exportFile);
                fos.write(records[0].getBytes());
                result = "success";
                System.out.println("Export success");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = "nodata";
            } catch (IOException e) {
                e.printStackTrace();
                result = "input error";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mDialog != null) {
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }

            if (result.equals("success")) {

            } else if (result.equals("connection timeout")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "Data not posted connection timeout");
                System.out.println("Data not posted connection timeout");
            } else if (result.equals("nodata")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "Error in exporting, no response from server");
                System.out
                        .println("Error in exporting, no response from server");
            } else {
                mToastMessage.showToast(PickRepackIngredientsActivity.this, "Error in exporting");
                System.out.println("Error in exporting");
            }
        }
    }

    // Method that returns the XML to be exported
    public String getRecordXmlExportPO(List<picktaskdetail> dList) {
        String exportPODataXml = "";
        try {
            ExportPickTask exportData = new ExportPickTask();

            StringBuffer sb = new StringBuffer();
            sb.append("<" + "PickTaskData" + ">");
            for (int i = 0; i < dList.size(); i++) {
                exportData.writeXml(dList.get(i), sb, PickRepackIngredientsActivity.this, mDbHelper);
            }
            sb.append("</" + "PickTaskData" + ">");
            exportPODataXml = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            String errorCode = "Err-CLS-2";
            String errMsg = "pick List detail export failed";
            LogfileCreator.mAppendLog(errorCode + " : " + e.getMessage()
                    + "\n" + errMsg);
            String result = "error";
            return result;
        }
        return exportPODataXml;
    }

    private void editAlert() {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(2, 2, 2, 2);

        TextView tv = new TextView(this);
        tv.setText("Item : " + Globals.editItemNum);
        tv.setPadding(80, 40, 40, 20);
        tv.setGravity(Gravity.LEFT);
        tv.setTextSize(16);

        TextView tv2 = new TextView(this);
        tv2.setText("Please Enter Qty between " + Globals.editQty1 + " to " + Globals.editQty2);
        tv2.setPadding(00, 40, 40, 00);
        tv2.setGravity(Gravity.LEFT);
        tv2.setTextSize(10);

        final EditText et = new EditText(this);
        et.setHint("Please Enter the Qty");
        et.setTextSize(11);
        TextView tv1 = new TextView(this);

        LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv1Params.bottomMargin = 5;
        layout.addView(tv2, tv1Params);
        layout.addView(et, new LinearLayout.LayoutParams(250, 50));
        layout.setPadding(80, 0, 0, 0);

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setCustomTitle(tv);
        alertDialogBuilder.setIcon(R.drawable.warning);
        alertDialogBuilder.setCancelable(false);

        // Setting Negative "Cancel" Button
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        // Setting Negative "Positive" Button
        alertDialogBuilder.setPositiveButton("OK", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        Button postiveBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        postiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editQty = et.getText().toString();
                double Temp = 0.0;
                double Temp2 = 0.0;
                double EPS = 0.00001;
                if (!editQty.equalsIgnoreCase("")) {
                    Temp = Double.parseDouble(editQty);
                }

                if (editQty.equalsIgnoreCase("")) {
                    mToastMessage.showToast(PickRepackIngredientsActivity.this, "Please enter the Qty");
                } else if (Temp > Globals.editQty2) {
                    mToastMessage.showToast(PickRepackIngredientsActivity.this, "Entered Qty is Greater than Ordered Qty");
                    et.setText("");
                    et.setHint("Entered Qty is Greater than Tqty");
                } else if (Temp < Globals.editQty1) {
                    mToastMessage.showToast(PickRepackIngredientsActivity.this, "Entered Qty is Lesser than Ordered Qty");
                } else {
                    //Globals.editQty2 = Double.parseDouble(editQty);
                    Temp2 = Globals.editQty2 - Temp;
                    Globals.editQty2 = Temp;
                   /* if(Globals.editQty2!=Temp){
                        Temp2 = Globals.editQty2 - Temp;
                    }*/
                    mDbHelper.getWritableDatabase();
                    mDbHelper.updateEditQty(Globals.editItemNum, Globals.editTranNum, editQty, String.valueOf(Temp2));
                    mDbHelper.closeDatabase();

                    ItemNo = Globals.editItemNum;
                    strTranlineNo = Globals.editTranNum;
                    ExportData();
                }
            }
        });

    }


    class LogoutRequest extends AsyncTask<String, String, String> {
        private ProgressDialog dialog;
        private String pUsername, pSessionId, pCompId,pDeviceId ;


        String result = "";



        public LogoutRequest(String mDeviceId, String mUsername, String mSessionId, String mCompId ) {
            this.pSessionId = mSessionId;
            this.pDeviceId = mDeviceId;
            this.pUsername = mUsername;
            this.pCompId = mCompId;


            dialog = new ProgressDialog(PickRepackIngredientsActivity.this);
            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Loading..");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            String result = "";

            try {

                SoapObject request = new SoapObject(NAMESPACE, LOGOUTREQUEST);
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            /*    File xmlData = Supporter.getImportFolderPath(mUsername
                        + "/Result/RepackPickList.xml");
                String pXmldata = FileUtils.readFileToString(xmlData);*/
                PropertyInfo info = new PropertyInfo();

                info.setName("pDeviceId");
                info.setValue(pDeviceId);
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pUserName");
                info.setValue(pUsername);
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pSessionId");
                info.setValue(pSessionId);
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pUserType");
                info.setValue("WMSUSR");
                info.setType(String.class);
                request.addProperty(info);

                info = new PropertyInfo();
                info.setName("pCompany");
                info.setValue(pCompId);
                info.setType(String.class);
                request.addProperty(info);

                envelope.dotNet = true;// to handle .net services asmx/aspx
                envelope.setOutputSoapObject(request);
                HttpTransportSE ht = new HttpTransportSE(URL_PROTOCOL + URL_SERVER_PATH +"/"+ APPLICATION_NAME +"/"+ URL_SERVICE_NAME);
                ht.debug = true;
                String soap_action = NAMESPACE + LOGOUTREQUEST;
                ht.call(soap_action, envelope);
                SoapPrimitive resultString = (SoapPrimitive) envelope.getResponse();
                File mImpOutputFile = Supporter.getImpOutputFilePathByCompany(mUsername, "Result", "LogoutRequest" + ".xml");
                if (!mImpOutputFile.exists()) {
                    Supporter.createFile(mImpOutputFile);
                } else {
                    mImpOutputFile.delete(); // to refresh the file
                    Supporter.createFile(mImpOutputFile);
                }

                BufferedWriter buf = new BufferedWriter(new FileWriter(
                        mImpOutputFile, true));

                buf.append(resultString.toString());

                if (resultString.toString().equalsIgnoreCase("Export failed.")) {
                    result = "Unable to Export.";
                } else if (resultString.toString().equalsIgnoreCase(
                        "Failed to post, refer log file.")) {
                    result = "server failed";
                } else if (resultString.toString().contains(
                        "Unexpected end of file has occurred")) {
                    result = "Unexpected";
                } else if (resultString.toString().contains(
                        "Data at the root level is invalid")) {
                    result = "Invalid";
                } else if (resultString.toString().contains(
                        "PO Updation failed.")) {
                    result = "PO Updation failed.";
                }  else {
                    result ="success";

                }
                buf.close();

            } catch (SocketTimeoutException e) {
                result = "time out error";
                e.printStackTrace();
            } catch (IOException e) {
                result = "input error";
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                result = "error";
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("tag", "error", e);
                result = "error";
            }

            return result;
        }

        @SuppressLint("ResourceType")
        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub

            if (result.equals("success")) {

//                mToastMessage.showToast(SelectCompanyActivity.this,
//                        "success");


            } else if (result.equals("server failed")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "Failed to post, refer log file.");
            }else if (result.equalsIgnoreCase("time out error")){

                //mToastMessage.showToast(PickTaskActivity.this, "Time out.");
            } else if (result.equalsIgnoreCase("error")) {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "Unable to update Server");
            } else {
                mToastMessage.showToast(PickRepackIngredientsActivity.this,
                        "Unable to update Server. Please Save again");
            }

            dialog.cancel();
        }
    }


    @Override
    protected void onDestroy() {
       // new LogoutRequest(mDeviceId, mUsername, mSessionId, mCompany).execute();
        super.onDestroy();
        Log.d("ranjith", "desroiiy");
    }

}



