package za.co.peruzal.chattr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = ChatCallBack.class.getSimpleName();
    private static final String CHANNEL = "chat";
    private Pubnub pubnub;
    private ChatCallBack chatCallBack;

    @InjectView(R.id.listView) ListView listView;
    @InjectView(R.id.editText) EditText editText;

    private ArrayList<String> messages;
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        pubnub = new Pubnub("pub-c-4aa4909f-37b4-4afd-9d0d-2b0c40796046","sub-c-e25739f4-1339-11e5-825b-02ee2ddab7fe", true);
        messages = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,messages);
        listView.setAdapter(adapter);
        chatCallBack = new ChatCallBack();

    }

    @OnClick(R.id.send) public void onSendChat(View view){
        String message = editText.getText().toString();

        if (!TextUtils.isEmpty(message)){
           pubnub.publish(CHANNEL, message, new Callback() {
               @Override
               public void successCallback(String channel, Object message) {
                   super.successCallback(channel, message);
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           Toast.makeText(MainActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                       }
                   });
               }

               @Override
               public void errorCallback(String channel, PubnubError error) {
                   super.errorCallback(channel, error);
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           Toast.makeText(MainActivity.this, "Unable to send message", Toast.LENGTH_SHORT).show();
                       }
                   });

               }
           });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            pubnub.subscribe(CHANNEL, chatCallBack);
            pubnub.hereNow(CHANNEL, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    super.successCallback(channel, message);
                    Log.d(TAG, "Who's is here : " + message.toString());
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    super.errorCallback(channel, error);
                    Log.e(TAG, error.toString());
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pubnub.unsubscribe(CHANNEL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ChatCallBack extends Callback {
        public ChatCallBack() {
        }

        @Override
        public void successCallback(String channel, Object message) {
            super.successCallback(channel, message);
            Log.d(TAG, "Success " + message.toString());
        }

        @Override
        public void successCallback(String channel, Object message, String timetoken) {
            super.successCallback(channel, message, timetoken);
            Log.d(TAG, message.toString() + ":" + DateUtils.getRelativeTimeSpanString(Long.parseLong(timetoken)));

            String chatObject = null;
            if (message instanceof String){
                chatObject = (String)message;
            }
            if (chatObject != null)
            messages.add(chatObject);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            super.errorCallback(channel, error);
            Log.e(TAG, error.toString());
        }

        @Override
        public void connectCallback(String channel, Object message) {
            super.connectCallback(channel, message);
            Log.d(TAG, message.toString());
        }

        @Override
        public void reconnectCallback(String channel, Object message) {
            super.reconnectCallback(channel, message);
            Log.d(TAG, message.toString());
        }

        @Override
        public void disconnectCallback(String channel, Object message) {
            super.disconnectCallback(channel, message);
            Log.d(TAG, message.toString());
        }
    }
}
