package com.yyj.stydyroom.study.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;



import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.ui.TFragment;
import com.yyj.stydyroom.study.activity.ChatRoomActivity;
import com.yyj.stydyroom.study.helper.ChatRoomMemberCache;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.yyj.stydyroom.study.session.Container;
import com.yyj.stydyroom.study.session.ModuleProxy;
import com.yyj.stydyroom.study.session.actions.BaseAction;
import com.yyj.stydyroom.study.session.input.InputConfig;
import com.yyj.stydyroom.study.session.input.InputPanel;
import com.yyj.stydyroom.study.util.ChatRoomMsgListPanel;
import com.yyj.stydyroom.study.util.actions.GuessAction;
import com.yyj.stydyroom.views.data.MyCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hzxuwen on 2016/2/29.
 */
public class ChatRoomMessageFragment extends TFragment implements ModuleProxy {

    private static final String TAG = ChatRoomMessageFragment.class.getSimpleName();
    private String roomId;
    private String creator;
    private Context context;

    // modules
    private View rootView;
    private InputPanel inputPanel;
    private ChatRoomMsgListPanel messageListPanel;
    private EditText messageEditText;
    private InputConfig inputConfig = new InputConfig(false, true, true);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.chat_room_message_fragment, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ChatRoomInfo roomInfo = ((ChatRoomActivity) getActivity()).getRoomInfo();
        this.roomId = roomInfo.getRoomId();
        this.creator = roomInfo.getCreator();
        init(getContext(), roomId, creator);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (inputPanel != null) {
            inputPanel.onPause();
        }
        if (messageListPanel != null) {
            messageListPanel.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (messageListPanel != null) {
            messageListPanel.onResume();
        }
    }

    public void init(Context context, String roomId, String creator) {
        this.context = context;
        this.roomId = roomId;
        this.creator = creator;
        registerObservers(true);
        findViews();
    }

    public boolean onBackPressed() {
        if (inputPanel != null && inputPanel.collapse(true)) {
            return true;
        }

        if (messageListPanel != null && messageListPanel.onBackPressed()) {
            return true;
        }
        return false;
    }

    public void onLeave() {
        if (inputPanel != null) {
            inputPanel.collapse(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerObservers(false);

        if (messageListPanel != null) {
            messageListPanel.onDestroy();
        }

        inputPanel = null;
    }

    private void findViews() {
        Container container = new Container((Activity) context, roomId, SessionTypeEnum.ChatRoom, this);
        if (messageListPanel == null) {
            messageListPanel = new ChatRoomMsgListPanel(container, rootView);
        }

        if (inputPanel == null) {
            inputPanel = new InputPanel(container, rootView, getActionList(), inputConfig);
        } else {
            inputPanel.reload(container, inputConfig);
        }

        messageEditText = findView(R.id.editTextMessage);
        messageEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    inputPanel.collapse(true);
                }
                return false;
            }
        });
    }

    private void registerObservers(boolean register) {
        ChatRoomMemberCache.getInstance().registerRoomMsgObserver(roomMsgObserver, register);
    }

    ChatRoomMemberCache.RoomMsgObserver roomMsgObserver = new ChatRoomMemberCache.RoomMsgObserver() {
        @Override
        public void onMsgIncoming(List<ChatRoomMessage> messages) {
            messageListPanel.onIncomingMessage(messages);
        }
    };

    /************************** Module proxy ***************************/

    @Override
    public boolean sendMessage(IMMessage msg) {
        ChatRoomMessage message = (ChatRoomMessage) msg;

        Map<String, Object> ext = new HashMap<>();
        ChatRoomMember chatRoomMember = ChatRoomMemberCache.getInstance().getChatRoomMember(roomId, MyCache.getAccount());
        if (chatRoomMember != null && chatRoomMember.getMemberType() != null) {
            ext.put("type", chatRoomMember.getMemberType().getValue());
            message.setRemoteExtension(ext);
        }

        NIMClient.getService(ChatRoomService.class).sendMessage(message, false)
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == ResponseCode.RES_CHATROOM_MUTED) {
                            Toast.makeText(MyCache.getContext(), "用户被禁言", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyCache.getContext(), "消息发送失败：code:" + code, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onException(Throwable exception) {
                        Toast.makeText(MyCache.getContext(), "消息发送失败！", Toast.LENGTH_SHORT).show();
                    }
                });
        messageListPanel.onMsgSend(msg);
        return true;
    }

    @Override
    public void onInputPanelExpand() {
        messageListPanel.scrollToBottom();
    }

    @Override
    public void shouldCollapseInputPanel() {
        inputPanel.collapse(false);
    }

    @Override
    public boolean isLongClickEnabled() {
        return !inputPanel.isRecording();
    }

    // 操作面板集合
    protected List<BaseAction> getActionList() {
        List<BaseAction> actions = new ArrayList<>();
        actions.add(new GuessAction());
        return actions;
    }

}
