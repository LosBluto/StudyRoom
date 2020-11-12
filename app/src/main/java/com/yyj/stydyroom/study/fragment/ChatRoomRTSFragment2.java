package com.yyj.stydyroom.study.fragment;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;



import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.ui.TFragment;
import com.yyj.stydyroom.base.ui.dialog.EasyAlertDialogHelper;
import com.yyj.stydyroom.study.activity.ChatRoomActivity;
import com.yyj.stydyroom.study.helper.ChatRoomMemberCache;
import com.yyj.stydyroom.study.model.WhiteBoardDoc;
import com.yyj.stydyroom.study.model.WhiteBoardLoginInfo;
import com.yyj.stydyroom.study.model.WhiteBoardMode;
import com.yyj.stydyroom.study.model.WhiteBoardPic;
import com.yyj.stydyroom.study.model.WhiteBoardReplayFile;
import com.yyj.stydyroom.study.model.WhiteBoardReplayInfo;
import com.yyj.stydyroom.study.model.WhiteBoardToolbar;
import com.yyj.stydyroom.study.model.WhiteBoardTools;
import com.yyj.stydyroom.study.util.Preferences;
import com.yyj.stydyroom.study.webview.BaseWebView;
import com.yyj.stydyroom.study.webview.WebViewConfig;
import com.yyj.stydyroom.views.data.AuthPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzsunyj on 2019-05-30.
 */
public class ChatRoomRTSFragment2 extends TFragment implements View.OnClickListener {

    private static final String TAG = "ChatRoomRTSFragment2";

    public BaseWebView webView;

    private TextView switchMode;

    private View loadFail;

    private ChatRoomInfo roomInfo;

    private boolean isCreate;

    public static final int INTERACTION_MODE = 0;

    public static final int PLAYBACK_MODE = 1;

    private int mode = INTERACTION_MODE;

    private WebChromeClient webChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }
    };

    private WebViewClient webViewClient = new WebViewClient() {

        private boolean isError;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            isError = false;
            syncView();
            Log.i(TAG, "onPageStarted");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            syncView();
            Log.i(TAG, "onWebViewPageFinished");
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            loadFail.setVisibility(View.VISIBLE);
            isError = true;
            handler.proceed();
            Log.i(TAG, "onReceivedSslError");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            //view.stopLoading();
            isError = true;
            syncView();
            Log.i(TAG, "onReceivedError");
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            //loadFail.setVisibility(View.VISIBLE);
            Log.i(TAG, "onReceivedHttpError");
        }

        private void syncView() {
            loadFail.setVisibility(isError ? View.VISIBLE : View.GONE);
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        roomInfo = ((ChatRoomActivity) getActivity()).getRoomInfo();
        if (roomInfo == null) {
            showToast(getString(R.string.fetch_data_error));
            return;
        }
        findViews();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setViewsListener();
        initView();
        initWebView();
        loadUrl();
    }

    private void findViews() {
        webView = getView().findViewById(R.id.web_view);
        loadFail = getView().findViewById(R.id.load_fail);
        switchMode = getView().findViewById(R.id.switch_mode);
    }

    private void setViewsListener() {
        loadFail.setOnClickListener(this);
        switchMode.setOnClickListener(this);
    }

    private void initView() {
        isCreate = isCreate();
    }

    private void initWebView() {
        WebViewConfig.setWebSettings(getContext(), webView.getSettings());
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        webView.setJsCallback(jsCallback);
    }

    private void loadUrl() {
        //调用demo的url
//        webView.loadUrl(DemoServers.getWBAddress());
        webView.resumeTimers();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_fail:
                webView.reload();
                break;
            case R.id.switch_mode:
                //                mode = 1 - mode;
                //                switchMode.setText(getString(mode == INTERACTION_MODE ? R.string.playback : R.string.white_board));
                //                webView.reload();// switch 回放
                break;
        }
    }

    private void clearFileObj() {
        EasyAlertDialogHelper.createOkCancelDialog(getActivity(), null, getString(R.string.sure_clear_doc),
                                                   getString(R.string.ok), getString(R.string.cancel), true,
                                                   new EasyAlertDialogHelper.OnDialogActionListener() {

                                                       @Override
                                                       public void doCancelAction() {
                                                       }

                                                       @Override
                                                       public void doOkAction() {
                                                           clearFile();
                                                       }
                                                   }).show();

    }
    private void clearFile() {
        webView.native2Web("clearFile", new JSONObject());
    }

    BaseWebView.JsCallback jsCallback = message -> {
        if (ChatRoomRTSFragment2.this.isDestroyed()) {
            return;
        }
        String method = message.getMethod();
        JSONObject data = message.getData();
        if (data == null) {
            data = new JSONObject();
        }
        Log.i("JSBridge", "method=" + method + " data=" + data.toString());
        switch (method) {
            case "webPageLoaded":
                onWebPageLoaded();
                break;
            case "webLoginSucceed":
                onWebLoginSucceed();
                break;
            case "webLoginIMFailed":
                onWebLoginIMFailed(data);
                break;
            case "webCreateWBFailed":
                onWebCreateWBFailed(data);
                break;
            case "webJoinWBSucceed":
                onWebJoinWBSucceed(data);
                break;
            case "webJoinWBFailed":
                onWebJoinWBFailed(data);
                break;
            case "webWBWorkerInited":
                onWebWBWorkerInited();
                break;
            case "webToolbarCustomEvent":
                onWebToolbarCustomEvent(data);
                break;
            case "webReconnect":
                onWebReconnect();
                break;
            case "webError":
                onWebError(data);
                break;
            case "webReplayEvent":
                onWebReplayEvent(data);
                break;
        }
    };

    private void onWebReplayEvent(JSONObject data) {
        String eventName;
        try {
            eventName = data.getString("eventName");
            if (TextUtils.equals("ready", eventName)) {
                long duration = data.getLong("duration");
                showToast("Player ready duration=" + duration);
                webReplayDoPlay();// player
            } else if (TextUtils.equals("play", eventName)) {
                showToast("Player play");
            } else if (TextUtils.equals("pause", eventName)) {
                showToast("Player pause");
            } else if (TextUtils.equals("tick", eventName)) {
                long time = data.getLong("time");
                showToast("Player tick time=" + time);
            } else if (TextUtils.equals("finished", eventName)) {
                showToast("Player finished");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始播放
     */
    private void webReplayDoPlay() {
        webView.native2Web("webReplayDoPlay", new JSONObject());
    }

    /**
     * toolbar上自定义的按钮可以点击： param={ eventName: "selectDoc" }
     *
     * @param data
     */
    private void onWebToolbarCustomEvent(JSONObject data) {
        try {
            String eventName = data.getString("eventName");
            if (TextUtils.equals(eventName, "selectDoc")) {
                setFileObj();
            } else if (TextUtils.equals(eventName, "closeDoc")) {
                clearFileObj();
            } else if (TextUtils.equals(eventName, "clear")) {
                clearWhiteBoard();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void clearWhiteBoard() {
        EasyAlertDialogHelper.createOkCancelDialog(getActivity(), null, getString(R.string.sure_clear_white_board),
                                                   getString(R.string.ok), getString(R.string.cancel), true,
                                                   new EasyAlertDialogHelper.OnDialogActionListener() {

                                                       @Override
                                                       public void doCancelAction() {
                                                       }

                                                       @Override
                                                       public void doOkAction() {
                                                           clearCanvas();
                                                       }
                                                   }).show();
    }

    private void clearCanvas() {
        webView.native2Web("clearCanvas", new JSONObject());
    }

    private void onWebWBWorkerInited() {
        if (isCreate) {
            enableDraw(true);
        }
    }

    private void onWebJoinWBFailed(JSONObject data) {
        int code = 0;
        String error = null;
        try {
            error = data.getString("msg");
            code = data.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(error)) {
            error = getString(R.string.join_room_error);
        }
        showToast(error + " code=" + code + " 重新加入");// may be show dialog,
        reJoinNewWBF();
    }

    /**
     * 重新加入弹窗提示？
     */
    private void reJoinNewWBF() {
        EasyAlertDialogHelper.createOkCancelDialog(getActivity(), null, getString(R.string.join_room_again),
                                                   getString(R.string.join), getString(R.string.cancel), true,
                                                   new EasyAlertDialogHelper.OnDialogActionListener() {

                                                       @Override
                                                       public void doCancelAction() {
                                                       }

                                                       @Override
                                                       public void doOkAction() {
                                                           webJoinNewWBF();
                                                       }
                                                   }).show();
    }

    private void webJoinNewWBF() {
        JSONObject params = new JSONObject();
        try {
            params.put("channelName", roomInfo != null ? roomInfo.getRoomId() : "");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "webJoinNewWBF json error");
        }
        webView.native2Web("webJoinNewWBF", params);
    }

    private void setFileObj() {
        WhiteBoardDoc doc = new WhiteBoardDoc();
        doc.setDocId("8bae75fb-2a88-42c6-8395-8756b8b91506");
        doc.setDocName("Qt界面开发.pptx");
        doc.setUrlPrefix("http://nim.nosdn.127.net/f30507ee-0844-4329-9441-7339815d6764");
        doc.setDocSize(1046599);
        doc.setDocType("PPTX");
        doc.setTransStat("Completed");
        doc.setTransType("PNG");
        doc.setTransSize(37204212);
        doc.setPageNum(13);
        List<WhiteBoardPic> pics = new ArrayList<>();
        WhiteBoardPic pic1 = new WhiteBoardPic();
        pic1.setDuration(142171);
        pic1.setHeight(1080);
        pic1.setWidth(0);
        pic1.setType(WhiteBoardPic.QUALITY_HIGH);
        pic1.setSize(21749827);
        pics.add(pic1);
        WhiteBoardPic pic2 = new WhiteBoardPic();
        pic2.setDuration(142171);
        pic2.setHeight(720);
        pic2.setWidth(0);
        pic2.setType(WhiteBoardPic.QUALITY_MEDIUM);
        pic2.setSize(10507133);
        pics.add(pic2);
        WhiteBoardPic pic3 = new WhiteBoardPic();
        pic3.setDuration(142171);
        pic3.setHeight(480);
        pic3.setWidth(0);
        pic3.setType(WhiteBoardPic.QUALITY_LOW);
        pic3.setSize(4947252);
        pics.add(pic3);
        doc.setPicList(pics);
        webView.native2Web("setFileObj", doc.toJson());
    }

    private void onWebCreateWBFailed(JSONObject data) {
        int code = 0;
        String error = null;
        try {
            error = data.getString("msg");
            code = data.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(error)) {
            error = getString(R.string.wb_room_crate_fail);
        }
        showToast(error + " code=" + code);
    }

    private void onWebJoinWBSucceed(JSONObject data) {
        showToast(getString(R.string.join_success));
    }

    private boolean isCreate() {
        String creator = roomInfo != null ? roomInfo.getCreator() : "";
        String account = AuthPreferences.getUserAccount();
        return TextUtils.equals(creator, account);
    }

    private JSONObject buildSingleParam(String key, Object value) {
        JSONObject data = new JSONObject();
        try {
            data.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "enableDraw error");
        }
        return data;
    }

    /**
     * 设置白板可用/不可用
     *
     * @param enable
     */
    private void enableDraw(boolean enable) {
        webView.native2Web("enableDraw", buildSingleParam("enable", enable));
        if (enable) {
            if (isCreate) {
                setColor("#000000");
            } else {
                setColor("#35cbff");
            }
        }
    }

    /**
     * 设置白板字体大小
     *
     * @param size
     */
    private void setFontSize(int size) {
        webView.native2Web("setFontsize", buildSingleParam("fontsize", size));
    }

    /**
     * 设置白板画笔大小
     *
     * @param size
     */
    private void setPaintSize(int size) {
        webView.native2Web("setSize", buildSingleParam("size", size));
    }

    /**
     * 设置白板颜色 { color: "#234234" }
     *
     * @param color
     */
    private void setColor(String color) {
        webView.native2Web("setColor", buildSingleParam("color", color));
    }

    /**
     * 设置白板使用的工具
     * 激光笔 flag
     * 画笔 free
     * 线 line
     * 圆、实心圆 circle solidCircle
     * 矩形、实心矩形 rect solidRect
     * 圆角矩形、实心圆角矩形 roundRect solidRoundRect
     * 文字 text
     * 橡皮擦 erase
     * 油漆桶 fill
     * 撤销 undo
     * 重做 redo
     * 清屏 clear
     *
     * @param tool param={ tool: "free" }
     */
    private void setTool(String tool) {
        webView.native2Web("setTool", buildSingleParam("tool", tool));
    }

    /**
     * 退出登录
     */
    private void webLogout() {
        JSONObject params = new JSONObject();
        webView.native2Web("webLogout", params);
    }

    /**
     * 异常情况
     *
     * @param data
     */
    private void onWebError(JSONObject data) {
        int code = 0;
        String msg = null;
        try {
            msg = data.getString("msg");
            code = data.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (code == ResponseCode.RES_EUIDPASS) {
            showToast(getString(R.string.pwd_or_account_error));
        } else {
            msg = TextUtils.isEmpty(msg) ? getString(R.string.unknown_error) : msg;
            showToast(msg);
        }
    }

    private void onWebReconnect() {
        showToast(getString(R.string.current_net_not_stable));
        if (isCreate) {
            enableDraw(true);
        }
    }

    private void showToast(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }

    private void onWebLoginIMFailed(JSONObject data) {
        int code = 0;
        String error = null;
        try {
            error = data.getString("msg");
            code = data.getInt("code");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(error) && code == ResponseCode.RES_EUIDPASS) {
            error = getString(R.string.pwd_or_account_error);
        }
        if (TextUtils.isEmpty(error)) {
            error = getString(R.string.im_login_fail);
        }
        showToast(error);
    }

    private void onWebLoginSucceed() {
        showToast(getString(R.string.im_login_success));
    }

    /**
     * page load ok
     */
    private void onWebPageLoaded() {
        if (mode == INTERACTION_MODE) {
            webLoginIM();
        } else {
            webReplayInit();
        }
    }

    private void webReplayInit() {
        WhiteBoardReplayInfo replayInfo = new WhiteBoardReplayInfo();
        replayInfo.setMode(WhiteBoardMode.PLAYBACK);
        replayInfo.setDebug(true);
        replayInfo.setAccount(AuthPreferences.getUserAccount());
        replayInfo.setIdentity(isCreate ? "owner" : "normal");
        replayInfo.setOwnerAccount(roomInfo != null ? roomInfo.getCreator() : "");
        List<WhiteBoardReplayFile> files = new ArrayList<>();
        WhiteBoardReplayFile file1 = new WhiteBoardReplayFile();
        file1.setAccount("cs1");
        file1.setUrl("https://apptest.netease.im/webdemo/education/51435961-51609352701904-1574992454435-0.mp4");
        file1.setUid("51435961");
        file1.setCid("51609352701904");
        file1.setTimestamp(1574992454435L);
        file1.setChunk(0);
        file1.setMixed(false);
        file1.setType("mp4");
        files.add(file1);
        WhiteBoardReplayFile file2 = new WhiteBoardReplayFile();
        file2.setAccount("cs1");
        file2.setUrl("https://apptest.netease.im/webdemo/education/51435961-201599034016129-1574992453452-0.gz");
        file2.setUid("51435961");
        file2.setCid("201599034016129");
        file2.setTimestamp(1574992453452L);
        file2.setChunk(0);
        file2.setMixed(false);
        file2.setType("gz");
        files.add(file2);
        WhiteBoardReplayFile file3 = new WhiteBoardReplayFile();
        file3.setAccount("cs2");
        file3.setUrl("https://apptest.netease.im/webdemo/education/77304002-51609352701904-1574992626509-0.mp4");
        file3.setUid("77304002");
        file3.setCid("51609352701904");
        file3.setTimestamp(1574992626509L);
        file3.setChunk(0);
        file3.setMixed(false);
        file3.setType("mp4");
        files.add(file3);
        WhiteBoardReplayFile file4 = new WhiteBoardReplayFile();
        file4.setAccount("cs2");
        file4.setUrl("https://apptest.netease.im/webdemo/education/77304002-201599034016129-1574992453452-0.gz");
        file4.setUid("77304002");
        file4.setCid("201599034016129");
        file4.setTimestamp(1574992453452L);
        file4.setChunk(0);
        file4.setMixed(false);
        file4.setType("gz");
        files.add(file4);
        replayInfo.setFileList(files);
        webView.native2Web("webReplayInit", replayInfo.toJson());
    }

    private void webLoginIM() {
        WhiteBoardLoginInfo info = new WhiteBoardLoginInfo();
        info.setMode(WhiteBoardMode.INTERACTION);
        info.setChannelName(roomInfo != null ? roomInfo.getRoomId() : "");
        info.setDebug(true);
        info.setAppKey(readAppKey());
        info.setAccount(AuthPreferences.getUserAccount());
        info.setToken(AuthPreferences.getUserToken());
        info.setRecord(Preferences.getRTSRecord());
        info.setIdentity(isCreate ? "owner" : "normal");
        info.setOwnerAccount(roomInfo != null ? roomInfo.getCreator() : "");
        info.setCustom(new JSONObject());
        info.setPlayer(new JSONObject());// 当前为空，占位
        WhiteBoardTools tools = new WhiteBoardTools();
        tools.setCustomClear(true);
        JSONArray toolbar = new JSONArray();
        try {
            toolbar.put(WhiteBoardToolbar.FREE).put(WhiteBoardToolbar.TEXT);
            JSONObject shapes = new JSONObject();
            shapes.put("type", "shapes");
            shapes.put("items", new JSONArray().put(WhiteBoardToolbar.LINE).put(WhiteBoardToolbar.RECT)
                                               .put(WhiteBoardToolbar.CIRCLE));
            toolbar.put(shapes);
            toolbar.put(WhiteBoardToolbar.ERASE).put(WhiteBoardToolbar.UNDO).put(WhiteBoardToolbar.CLEAR);
            if (isCreate) {
                JSONObject ctxt = new JSONObject();
                ctxt.put("type", "customTXT");
                ctxt.put("label", "文档库");
                ctxt.put("eventName", "selectDoc");
                toolbar.put(ctxt);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tools.setToolbar(toolbar);
        info.setTools(tools);
        webView.native2Web("webLoginIM", info.toJson());
    }

    private String readAppKey() {
        String appKey = null;
        try {
            ApplicationInfo appInfo = getActivity().getPackageManager().getApplicationInfo(
                    getActivity().getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                appKey = appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "read app key error");
        }
        return appKey;
    }

    public void onAcceptConfirm() {
        enableDraw(true);
    }

    public void onReject() {
        enableDraw(false);
    }

    public void onCurrent() {
    }

    @Override
    public void onDestroy() {
        webLogout();
        if (webView != null) {
            webView.pauseTimers();
            webView.clearCache(true);
            webView.clearHistory();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    public void onAVChatData(AVChatData data) {
    }

    public void onStatusNotify() {
        if (!isCreate) {
            // 重连如果是学生如果还在互动则打开工具条， 如果没有互动则关闭
            if (ChatRoomMemberCache.getInstance().hasPermission(roomInfo != null ? roomInfo.getRoomId() : null,
                                                                AuthPreferences.getUserAccount())) {
                enableDraw(true);
            } else {
                enableDraw(false);
            }
        }
    }
}
