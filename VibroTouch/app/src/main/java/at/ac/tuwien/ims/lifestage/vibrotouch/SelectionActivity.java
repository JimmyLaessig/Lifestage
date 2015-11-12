package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.SparkManager;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.UserPreferences;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Activity where you can select testcases.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class SelectionActivity extends BaseActivity {
    private FloatingActionButton fab;
    private NiftyDialogBuilder dialogBuilder;
    private EditText userID;

    private volatile boolean running=true;
    private long time=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(btnListener);
        try {
            connectionManager.setIDandToken(XmlHelper.getIDandToken(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.inputXMLPath));
        } catch (Exception e) {
            Toast.makeText(SelectionActivity.this, getString(R.string.xml_correct), Toast.LENGTH_SHORT).show();
        }
        createIDDialog();
        connect();
        initializeList();
        startConnectionThread();

        if(UserPreferences.getCurrentUserID(this)==null)
            dialogBuilder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_user) {
            dialogBuilder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createIDDialog() {
        View nameView = getLayoutInflater().inflate(R.layout.dialog_userid, null, false);
        userID=(EditText)(nameView.findViewById(R.id.dialog_edittext_field));
        String curr= UserPreferences.getCurrentUserID(this)==null ? "not set yet" : UserPreferences.getCurrentUserID(this);
        userID.setHint("Current: "+curr);

        dialogBuilder= NiftyDialogBuilder.getInstance(SelectionActivity.this);
        dialogBuilder.setCancelable(false);
        dialogBuilder
                .withEffect(Effectstype.Fadein)
                .isCancelableOnTouchOutside(false)
                .setCustomView(nameView, SelectionActivity.this)
                .withTitle(getString(R.string.setUserid))
                .withTitleColor("#000000")
                .withMessageColor("#000000")
                .withDialogColor("#f9f9f9")
                .withButton1Text(getString(R.string.ok))
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (userID.getText().length() != 0) {
                            String id = userID.getText().toString();
                            UserPreferences.saveUserID(SelectionActivity.this, id);
                        }
                        dialogBuilder.dismiss();
                        createIDDialog();
                    }
                })
                .withButton2Text(getString(R.string.cancel))
                .setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.dismiss();
                        createIDDialog();
                    }
                });
    }

    private void startConnectionThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    if (System.currentTimeMillis() - time >= 700) {
                        final boolean connected=connectionManager.getStatus() == SparkManager.CONNECTED;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (connected) {
                                    fab.setImageDrawable(ContextCompat.getDrawable(SelectionActivity.this, R.drawable.wifi_off));
                                } else {
                                    fab.setImageDrawable(ContextCompat.getDrawable(SelectionActivity.this, R.drawable.wifi_on));
                                }
                            }
                        });
                        time = System.currentTimeMillis();
                    }
                }
            }
        });
        thread.start();
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v==fab) {
                if(connectionManager.getStatus()== SparkManager.NOT_CONNECTED) {
                    fab.setImageDrawable(ContextCompat.getDrawable(SelectionActivity.this, R.drawable.wifi_off));
                    connect();
                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(SelectionActivity.this, R.drawable.wifi_on));
                    disconnect();
                }
                return;
            }
        }
    };

    @Override
    protected void onDestroy () {
        super.onDestroy();
        running=false;
        disconnect();
    }

    private MyAdapter myAdapter;
    private void initializeList() {
        StickyListHeadersListView stickyList = (StickyListHeadersListView) findViewById(R.id.list);
        myAdapter=new MyAdapter(testcases);
        stickyList.setAdapter(myAdapter);

        stickyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if(connectionManager.getStatus()!=SparkManager.CONNECTED) {
                    Toast.makeText(SelectionActivity.this, getString(R.string.connectToCore), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent myIntent = new Intent(SelectionActivity.this, ScenarioActivity.class);
                myIntent.putExtra("testcase", ((Testcase) myAdapter.getItem(position)).getId());
                startActivity(myIntent);
            }
        });
    }

    private class MyAdapter extends BaseAdapter implements StickyListHeadersAdapter {
        private List<Testcase> testcases;

        public MyAdapter(List<Testcase> objects) {
            testcases=objects;
        }

        @Override
        public int getCount() {
            return testcases.size();
        }

        @Override
        public Object getItem(int position) {
            return testcases.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Testcase testcase=testcases.get(position);
            holder.text.setText(getString(R.string.testcase) + " " + testcase.getId() /*+ " (" + testcase.getObjects().size() + " Object(s), MinIntensity: "+ testcase.getMinIntensity()+ "/MaxIntensity: "+testcase.getMaxIntensity() +")"*/);

            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder=null;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.list_header, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.text_header);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            String att;
            switch (testcases.get(position).getScenario()) {
                case 1:
                    att=" - "+getString(R.string.scenario1);
                    break;
                case 2:
                    att=" - "+getString(R.string.scenario2);
                    break;
                default:
                    att=" - "+getString(R.string.scenario0);
            }
            String headerText = "Scenario " + testcases.get(position).getScenario() + att;
            holder.text.setText(headerText);

            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            return testcases.get(position).getScenario();
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder {
            TextView text;
        }
    }
}
