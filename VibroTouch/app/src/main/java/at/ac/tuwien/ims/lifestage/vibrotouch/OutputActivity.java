package at.ac.tuwien.ims.lifestage.vibrotouch;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import at.ac.tuwien.ims.lifestage.vibrotouch.Util.XmlHelper;

public class OutputActivity extends AppCompatActivity {
    private TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);
        ((TextView)findViewById(R.id.header)).setText(getString(R.string.output_header) + " (" + XmlHelper.outputtXMLPath + ")");
        text=(TextView)findViewById(R.id.xml_text);
    }

    @Override
    public void onResume() {
        super.onResume();

        String txt;
        try {
            txt=XmlHelper.getOutputString(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + XmlHelper.outputtXMLPath);
            txt=txt.replaceAll(">", ">\r\n \r\n");
        } catch (Exception e) {
            txt=getString(R.string.xml_correct);
        }
        text.setText(txt);
    }
}
