package com.capton.chunmengassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.suke.widget.SwitchButton;
import com.turing.androidsdk.HttpRequestListener;
import com.turing.androidsdk.TuringManager;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements HttpRequestListener{

    private RecognizerListener mRecoListener;
    private SpeechRecognizer mIat;
    private LinearLayout chatLayout,searchBarLayout;
    private ScrollView scrollView;
    private ImageView recordBtn;
    private ImageView openSearchBarBtn;
    private ImageView sendBtn;
    private ImageView backBtn;
    private ImageView moreBtn;
    private EditText editText;
    private TextView hintView;
    private SwitchButton switchBtn;
    private RecognizerResult mResults;
    private String finalSpeakWords="";

    private SharedPreferences spf;
    private View settingsView;

    private TuringManager turingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        spf=getSharedPreferences("Settings",MODE_PRIVATE);
        Glide.with(this).load(R.drawable.line).into((ImageView) findViewById(R.id.bg));
        initView();
        setListener();

        /*getAccounts();
        callPhone();
        readPhoneState();
        recordAudio();
        readExternalStorage();
        writeExternalStorage();*/


        PermissionUtils.requestMultiPermissions(this,mPermissionGrant);
    }

    public void initTuring(){
        turingManager=new TuringManager(this,getString(R.string.turing_apikey),getString(R.string.turing_secret));
        turingManager.setHttpRequestListener(this);

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Called when the 'show camera' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void showCamera() {
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_CAMERA, mPermissionGrant);
    }

    public void getAccounts() {
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_GET_ACCOUNTS, mPermissionGrant);
    }

    public void callPhone() {
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_CALL_PHONE, mPermissionGrant);
    }

    public void readPhoneState() {
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_READ_PHONE_STATE, mPermissionGrant);
    }

    public void accessFineLocation() {
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_ACCESS_FINE_LOCATION, mPermissionGrant);
    }

    public void accessCoarseLocation() {
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_ACCESS_COARSE_LOCATION, mPermissionGrant);
    }

    public void readExternalStorage() {
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_READ_EXTERNAL_STORAGE, mPermissionGrant);
    }

    public void writeExternalStorage() {
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE, mPermissionGrant);
    }

    public void recordAudio() {
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_RECORD_AUDIO, mPermissionGrant);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.requestPermissionsResult(this,requestCode,permissions,grantResults,mPermissionGrant);
    }

    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case PermissionUtils.CODE_MULTI_PERMISSION:
                    initTuring();
                    break;
            }
        }
    };


    private void setListener() {
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hintView.setText("语音识别中...");
                hintView.setTextColor(getResources().getColor(R.color.colorHint));
                if(mIat==null){
                    startSpeak();
                }else {
                    mIat.startListening(mRecoListener);
                }
                recordBtn.setEnabled(false);
                recordBtn.setImageResource(R.drawable.speaker2);
                if(mTts!=null){
                    if(mTts.isSpeaking())
                        mTts.stopSpeaking();
                }
            }
        });
        openSearchBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchBarBtn.setVisibility(View.INVISIBLE);
                recordBtn.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.INVISIBLE);
                hintView.setVisibility(View.INVISIBLE);
                searchBarLayout.setVisibility(View.VISIBLE);
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBarLayout.setVisibility(View.INVISIBLE);
                openSearchBarBtn.setVisibility(View.VISIBLE);
                recordBtn.setVisibility(View.VISIBLE);
                hintView.setVisibility(View.VISIBLE);
                moreBtn.setVisibility(View.VISIBLE);
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!"".equals(editText.getText().toString())){
                    TextView textView=new TextView(MainActivity.this);
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(60,60,60,60);
                    textView.setLayoutParams(lp);
                    textView.setText(editText.getText().toString());
                    textView.setTextIsSelectable(true);
                    textView.setBackgroundResource(R.drawable.text_shape);
                    chatLayout.addView(textView);
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                  //  getAnswer(editText.getText().toString());
                    turingManager.requestTuring(editText.getText().toString());

                    editText.setText("");
                }
            }
        });
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupWindow popupWindow=new PopupWindow(MainActivity.this);
                 popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                 popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                 popupWindow.setFocusable(true);
                 popupWindow.setContentView(settingsView);
                popupWindow.showAsDropDown(moreBtn,0,-moreBtn.getHeight()*5);
                popupWindow.setTouchable(true);
            }
        });
        //开关切换事件
        switchBtn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if(isChecked){
                    spf.edit().putBoolean("sound",true).apply();
                    ((TextView)settingsView.findViewById(R.id.sound_title)).setText("语音：开");
                }else {
                    spf.edit().putBoolean("sound",false).apply();
                    ((TextView)settingsView.findViewById(R.id.sound_title)).setText("语音：关");
                    if(mTts!=null){
                        if(mTts.isSpeaking()){
                            mTts.stopSpeaking();
                        }
                    }

                }
            }
        });
    }

    private void initView() {
        recordBtn= (ImageView) findViewById(R.id.record);
        backBtn= (ImageView) findViewById(R.id.back);
        sendBtn= (ImageView) findViewById(R.id.send);
        moreBtn= (ImageView) findViewById(R.id.more);
        editText= (EditText) findViewById(R.id.editText);
        chatLayout= (LinearLayout) findViewById(R.id.chatLayout);
        scrollView= (ScrollView) findViewById(R.id.scrollView);
        hintView= (TextView) findViewById(R.id.hint);
        openSearchBarBtn= (ImageView) findViewById(R.id.openSearchBar);
        searchBarLayout= (LinearLayout) findViewById(R.id.searchBar);
        settingsView= LayoutInflater.from(this).inflate(R.layout.settings,null);

        switchBtn= (SwitchButton) settingsView.findViewById(R.id.soundToggle);

        if(spf.getBoolean("sound",true)){
            ((TextView)settingsView.findViewById(R.id.sound_title)).setText("语音：开");
            switchBtn.setChecked(true);
        }else {
            ((TextView)settingsView.findViewById(R.id.sound_title)).setText("语音：关");
            switchBtn.setChecked(false);
        }
    }

    private void startSpeak(){
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"="+getString(R.string.xf_appid));
        //1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        mIat= SpeechRecognizer.createRecognizer(this, null);
        //2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");

        //听写监听器
        mRecoListener = new RecognizerListener(){
       //听写结果回调接口(返回Json格式结果，用户可参见附录12.1)；
       //一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
       //关于解析Json的代码可参见MscDemo中JsonParser类；
       //isLast等于true时会话结束。
            public void onResult(RecognizerResult results, boolean isLast) {
                Log.d("Result:",results.getResultString ());
                mResults=results;
                if(isLast){
                   finalSpeakWords+=analyseRecognizerResult(mResults.getResultString());
                    appendAskView(finalSpeakWords);
                    turingManager.requestTuring(finalSpeakWords);
                }
            }
            //会话发生错误回调接口
            public void onError(SpeechError error) {
                error.getPlainDescription(true); //获取错误码描述
            }
            //开始录音
            public void onBeginOfSpeech() {
                Log.d("startSpeak:","开始说话呀");

            }
            //音量值0~30
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {

            }

            //结束录音
            public void onEndOfSpeech() {
                if(mResults!=null) {
                    finalSpeakWords = analyseRecognizerResult(mResults.getResultString());
                    Log.d("endSpeak:", "结束说话。。。");
                    recordBtn.setEnabled(true);
                    recordBtn.setImageResource(R.drawable.speaker);
                    hintView.setText("点击录音");
                    hintView.setTextColor(Color.WHITE);
                }else {
                    recordBtn.setEnabled(true);
                    recordBtn.setImageResource(R.drawable.speaker);
                    hintView.setText("点击录音");
                    hintView.setTextColor(Color.WHITE);
                }
            }
            //扩展用接口
            public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
        };

       //3.开始听写
         mIat.startListening(mRecoListener);

    }

    private String analyseRecognizerResult(String results){
        String finalWords="";
         JSONObject jsonObject=JSONObject.parseObject(results);
        Log.i("ws", jsonObject.getJSONArray("ws").toJSONString());
        String wsString=jsonObject.getJSONArray("ws").toJSONString();
         JSONArray wsArray=JSON.parseArray(wsString);
        for (int i = 0; i <wsArray.size(); i++) {
            Log.i("ws son",wsArray.get(i).toString());
            JSONObject WordObject= JSONObject.parseObject(wsArray.get(i).toString());
            String cwString=WordObject.getJSONArray("cw").toJSONString();
            JSONArray cwArray=JSON.parseArray(cwString);
            for (int j = 0; j <cwArray.size(); j++) {
                Log.i("cw", cwArray.get(j).toString());
                JSONObject resultObject=JSONObject.parseObject(cwArray.get(j).toString());
                result mResult= JSONObject.parseObject(resultObject.toJSONString(),result.class);
                finalWords+=mResult.getW();
            }
        }
        return finalWords;
    }

    public static final String APPKEY = "9679f404049995cc";// 你的appkey
    public static final String URL = "http://api.jisuapi.com/iqa/query";
    private String finalAnswer="";
    private String answerJsonString="";
    private OkHttpClient okhttpClient;

    private void getAnswer(String finalSpeakWords) {
        String url = URL + "?appkey=" + APPKEY + "&question=" + finalSpeakWords;
        if(okhttpClient==null)
            okhttpClient=new OkHttpClient();
        final Request request=new Request.Builder().get().url(url).build();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Response response=null;
                try {
                    response=okhttpClient.newCall(request).execute();
                    if(response.isSuccessful()){
                        answerJsonString=response.body().string();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject json = JSONObject.parseObject(answerJsonString);
                    if (json.getIntValue("status") != 0) {
                        System.out.println(json.getString("msg"));
                    } else {
                        JSONObject resultarr = json.getJSONObject("result");
                        String type = resultarr.getString("type");
                        String content = resultarr.getString("content");
                        String relquestion = resultarr.getString("relquestion");
                        System.out.println(type + " " + content + " " + relquestion);
                        finalAnswer=content;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }


            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if(spf.getBoolean("sound",true)) {
                    speakDialog(finalAnswer);
                }

                TextView textView2=new TextView(MainActivity.this);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(60,60,60,60);
                lp.gravity= Gravity.RIGHT;
                textView2.setLayoutParams(lp);
                textView2.setText(finalAnswer);
                textView2.setTextColor(Color.BLACK);
                textView2.setTextIsSelectable(true);
                textView2.setBackgroundResource(R.drawable.text_shape2);
                chatLayout.addView(textView2);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        }.execute();
    }

    private SpeechSynthesizer mTts;
    private SynthesizerListener mSynListener;
    private void speakDialog(String dialog) {
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
//2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "vinn");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
//设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
//保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
//如果不需要保存合成音频，注释该行代码
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");

//合成监听器
        mSynListener = new SynthesizerListener() {
            //会话结束回调接口，没有错误时，error为null
            public void onCompleted(SpeechError error) {
            }

            //缓冲进度回调
            //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
            public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            }

            //开始播放
            public void onSpeakBegin() {
            }

            //暂停播放
            public void onSpeakPaused() {
            }

            //播放进度回调
            //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
            public void onSpeakProgress(int percent, int beginPos, int endPos) {
            }

            //恢复播放回调接口
            public void onSpeakResumed() {
            }

            //会话事件回调接口
            public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            }
        };
            //开始合成
            mTts.startSpeaking(dialog, mSynListener);

    }

    @Override
    public void onSuccess(String s) {
        Log.i("onSuccess", "onSuccess: "+s);
        if(!"".equals(getText(s))&&getText(s)!=null) {
            appendAnswerView(getText(s), false);
            if(spf.getBoolean("sound",true)) {
                speakDialog(getText(s));
            }
        }
        if(!"".equals(getUrl(s))&&getUrl(s)!=null)
        appendAnswerView(getUrl(s),true);

    }

    @Override
    public void onFail(int i, String s) {
        Log.i("onFail", "onFail:"+i+" "+s);
    }

    private String getText(String s){
        JSONObject jsonObject=JSON.parseObject(s);
        return  jsonObject.getString("text");
    }
    private String getUrl(String s){
        JSONObject jsonObject=JSON.parseObject(s);
        return  jsonObject.getString("url");
    }

    private void appendAskView(String ask){
        TextView textView=new TextView(MainActivity.this);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(60,60,60,60);
        textView.setLayoutParams(lp);
        textView.setText(ask);
        textView.setTextIsSelectable(true);
        textView.setBackgroundResource(R.drawable.text_shape);
        chatLayout.addView(textView);
        hintView.setTextColor(Color.WHITE);
        //getAnswer(finalSpeakWords);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void appendAnswerView(final String answer, boolean isUrl){
        TextView textView2=new TextView(MainActivity.this);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(60,60,60,60);
        lp.gravity= Gravity.RIGHT;
        textView2.setLayoutParams(lp);
        textView2.setText(answer);
        textView2.setTextColor(Color.BLACK);
        if(isUrl) {
            textView2.setTextColor(Color.BLUE);
            textView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(answer);
                    intent.setData(content_url);
                    startActivity(intent);
                }
            });
        }
        textView2.setTextIsSelectable(true);
        textView2.setBackgroundResource(R.drawable.text_shape2);
        chatLayout.addView(textView2);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

}
