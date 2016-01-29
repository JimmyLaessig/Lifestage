package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.SparkManager;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.UserPreferences;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;

/**
 * Activity where you can select testcases.
 * <p/>
 * Application: VibroTouch
 * Created by Florian Schuster (e1025700@student.tuwien.ac.at).
 */
public class SelectionActivity extends BaseActivity {
    private FloatingActionButton fab;
    private AlertDialog.Builder nextBuilder, idBuilder, endBuilder;
    private EditText userID;
    private Menu menu;
    private TextView subtitle;
    private long time=0;
    private boolean running=false;

    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO ordering of testcases
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        subtitle=(TextView)toolbar.findViewById(R.id.toolbar_subtitle);
        String id=UserPreferences.getCurrentUserID(this);
        if(id==null) {
            id=0+"";
            UserPreferences.setUserID(this, id);
        }
        subtitle.setText(getString(R.string.curr) + " " + id);

        createIDDialog();
        createEndDialog();
        createNextDialog();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(btnListener);
        initializeList();
    }

    @Override
    public void onResume() {
        super.onResume();
        startIconThread();

        int nextID = UserPreferences.getCurrentTestcaseID(this);
        if (nextID >= 0) {
            boolean testcaseDone = UserPreferences.getJustFinishedTestcase(this);
            if (testcaseDone) {
                if (nextID < testcases.size() - 1) {
                    nextID++;
                    nextBuilder.setMessage(getString(R.string.next_testcase) + " " + nextID + ".");
                    nextBuilder.show();
                } else {
                    nextID=-3;
                    endBuilder.show();
                }
                UserPreferences.setCurrentTestcaseID(this, nextID);
                UserPreferences.setJustFinishedTestcase(this, false);
            }
            mAdapter.setSelection(nextID);
            mLayoutManager.scrollToPosition(nextID);
        }
        Log.d(getClass().getName(), "Next ID: " + nextID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        running=false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_user) {
            idBuilder.show();
            return true;
        }

        if (item.getItemId() == R.id.action_reload) {
            updateTestcases(true);
            mAdapter.update(testcases);

            mAdapter.setSelection(-1);
            return true;
        }

        if (item.getItemId() == R.id.action_wifi || item.getItemId()==R.id.action_wifi_text) {
            if(verifyStoragePermissions(this)) {
                try {
                    connectionManager.setIDandToken(XmlHelper.getIDandToken(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.inputXMLPath));
                } catch (Exception e) {
                    Toast.makeText(SelectionActivity.this, getString(R.string.xml_correct), Toast.LENGTH_SHORT).show();
                }

                if(connectionManager.getStatus()!=SparkManager.CONNECTED)
                    connect();
                else
                    disconnect();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startIconThread() {
        running=true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    if (System.currentTimeMillis() - time >= 150) {
                        final boolean connected=connectionManager.getStatus() == SparkManager.CONNECTED;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(menu!=null)
                                    if (connected) {
                                        menu.findItem(R.id.action_wifi).setIcon(ContextCompat.getDrawable(SelectionActivity.this, R.drawable.wifi_on));
                                    } else {
                                        menu.findItem(R.id.action_wifi).setIcon(ContextCompat.getDrawable(SelectionActivity.this, R.drawable.wifi_off));
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

    private void createIDDialog() {
        View nameView = getLayoutInflater().inflate(R.layout.dialog_userid, null, false);
        userID=(EditText)(nameView.findViewById(R.id.dialog_edittext_field));
        String curr= UserPreferences.getCurrentUserID(this)==null ? "not set yet" : UserPreferences.getCurrentUserID(this);
        userID.setHint("Current: "+curr);

        idBuilder = new AlertDialog.Builder(SelectionActivity.this);
        idBuilder.setView(nameView)
                .setMessage(getString(R.string.setUserid))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (userID.getText().length() != 0) {
                            String user_id = userID.getText().toString();
                            UserPreferences.setUserID(SelectionActivity.this, user_id);
                            subtitle.setText(getString(R.string.curr) + " " + user_id);
                        }
                        mAdapter.setSelection(-1);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
                .setCancelable(false)
                .create();
    }

    private void createNextDialog() {
        nextBuilder = new AlertDialog.Builder(SelectionActivity.this);
        nextBuilder.setMessage(getString(R.string.next_testcase))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (connectionManager.getStatus() != SparkManager.CONNECTED) {
                            Toast.makeText(SelectionActivity.this, getString(R.string.connectToCore), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent myIntent = new Intent(SelectionActivity.this, ScenarioActivity.class);
                        myIntent.putExtra("testcase", UserPreferences.getCurrentTestcaseID(SelectionActivity.this));
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
                .setCancelable(false)
                .create();
    }

    private void createEndDialog() {
        endBuilder = new AlertDialog.Builder(SelectionActivity.this);
        endBuilder.setMessage(getString(R.string.end_testcases))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
                .setCancelable(false)
                .create();
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v==fab) {
                if(connectionManager.getStatus()!=SparkManager.CONNECTED) {
                    Toast.makeText(SelectionActivity.this, getString(R.string.connectToCore), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(UserPreferences.getCurrentTestcaseID(SelectionActivity.this)>=0) {
                    nextBuilder.setMessage(getString(R.string.next_testcase) + " " + UserPreferences.getCurrentTestcaseID(SelectionActivity.this) + ".");
                    nextBuilder.show();
                } else {
                    UserPreferences.setCurrentTestcaseID(SelectionActivity.this, 0);
                    nextBuilder.setMessage(getString(R.string.next_testcase) + " " + UserPreferences.getCurrentTestcaseID(SelectionActivity.this) + ".");
                    nextBuilder.show();
                }
            }
        }
    };

    private void initializeList() {
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(testcases);
        mRecyclerView.setAdapter(mAdapter);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<Testcase> mDataset;
        private int selection =-1;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ViewHolder(View v) {
                super(v);
                mTextView = (TextView) v.findViewById(R.id.text);
            }
        }

        public MyAdapter(ArrayList<Testcase> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mTextView.setText(getString(R.string.testcase) + " " + mDataset.get(position).getId() + " (" + getString(R.string.scenario) + " " + mDataset.get(position).getScenario() + ")");
            holder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id=mDataset.get(position).getId();
                    if (connectionManager.getStatus() != SparkManager.CONNECTED) {
                        Toast.makeText(SelectionActivity.this, getString(R.string.connectToCore), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent myIntent = new Intent(SelectionActivity.this, ScenarioActivity.class);
                    myIntent.putExtra("testcase", id);
                    startActivity(myIntent);
                }
            });

            if(selection!= -1 && position == selection) {
                holder.mTextView.setBackgroundColor(getColor(R.color.colorAccent));
                holder.mTextView.setTextColor(Color.WHITE);
            } else {
                holder.mTextView.setTextColor(Color.BLACK);
                holder.mTextView.setBackgroundColor(Color.WHITE);
            }
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public void setSelection(int selection) {
            this.selection=selection;
        }

        public void update(ArrayList<Testcase> mDataset) {
            this.mDataset=mDataset;
            notifyDataSetChanged();
        }
    }
}