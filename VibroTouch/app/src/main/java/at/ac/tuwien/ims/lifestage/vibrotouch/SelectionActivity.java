package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.ims.lifestage.vibrotouch.Entities.Testcase;
import at.ac.tuwien.ims.lifestage.vibrotouch.Util.SparkManager;
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
            Toast.makeText(SelectionActivity.this, "Please make sure your XML Files are correct.", Toast.LENGTH_SHORT).show();
        }
        connect();
        initializeList();
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
                    Toast.makeText(SelectionActivity.this, "Please connect to SparkCore first.", Toast.LENGTH_SHORT).show();
                    //return; TODO
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
            holder.text.setText(getString(R.string.testcase) + " " + testcase.getId() + " (" + testcase.getObjects().size() + " Object(s), MinIntensity: "+ testcase.getMinIntensity()+ "/MaxIntensity: "+testcase.getMaxIntensity() +")");

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
                    att=" - Precision Test";
                    break;
                case 2:
                    att=" - Recreate Size";
                    break;
                default:
                    att=" - Playground";
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
