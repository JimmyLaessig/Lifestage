package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;

public class OutputActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);
        TextView t=(TextView)findViewById(R.id.header);
        t.setText(getString(R.string.output_header) + " (" + XmlHelper.outputtXMLPath + ")");

        TextView t2=(TextView)findViewById(R.id.xml_text);
        try {
            t2.setText(XmlHelper.getOutputString(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.outputtXMLPath));
        } catch (Exception e) {
            Toast.makeText(OutputActivity.this, getString(R.string.xml_correct), Toast.LENGTH_SHORT).show();
        }
    }
}
