namespace XMLShuffler
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.button1 = new System.Windows.Forms.Button();
            this.openFileDialog1 = new System.Windows.Forms.OpenFileDialog();
            this.OutputConsole = new System.Windows.Forms.Label();
            this.button2 = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.button3 = new System.Windows.Forms.Button();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.Calibration_numSteps = new System.Windows.Forms.TextBox();
            this.Obj_min = new System.Windows.Forms.TextBox();
            this.Obj_max = new System.Windows.Forms.TextBox();
            this.SuspendLayout();
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(110, 488);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(75, 23);
            this.button1.TabIndex = 0;
            this.button1.Text = "StartShuffle";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // openFileDialog1
            // 
            this.openFileDialog1.FileName = "openFileDialog1";
            this.openFileDialog1.FileOk += new System.ComponentModel.CancelEventHandler(this.openFileDialog1_FileOk);
            // 
            // OutputConsole
            // 
            this.OutputConsole.BackColor = System.Drawing.SystemColors.ActiveCaptionText;
            this.OutputConsole.ForeColor = System.Drawing.SystemColors.Control;
            this.OutputConsole.Location = new System.Drawing.Point(3, 29);
            this.OutputConsole.Name = "OutputConsole";
            this.OutputConsole.Size = new System.Drawing.Size(386, 274);
            this.OutputConsole.TabIndex = 1;
            this.OutputConsole.Text = "Output";
            // 
            // button2
            // 
            this.button2.Location = new System.Drawing.Point(29, 488);
            this.button2.Name = "button2";
            this.button2.Size = new System.Drawing.Size(75, 23);
            this.button2.TabIndex = 2;
            this.button2.Text = "Generate";
            this.button2.UseVisualStyleBackColor = true;
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 333);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(56, 13);
            this.label1.TabIndex = 3;
            this.label1.Text = "Calibration";
            this.label1.Click += new System.EventHandler(this.label1_Click);
            // 
            // button3
            // 
            this.button3.Location = new System.Drawing.Point(220, 488);
            this.button3.Name = "button3";
            this.button3.Size = new System.Drawing.Size(158, 23);
            this.button3.TabIndex = 4;
            this.button3.Text = "Generate Testcases";
            this.button3.UseVisualStyleBackColor = true;
            this.button3.Click += new System.EventHandler(this.button3_Click);
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(12, 408);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(28, 13);
            this.label2.TabIndex = 5;
            this.label2.Text = "Test";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(13, 431);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(40, 13);
            this.label3.TabIndex = 6;
            this.label3.Text = "ObjMin";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(69, 431);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(43, 13);
            this.label4.TabIndex = 7;
            this.label4.Text = "ObjMax";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(16, 350);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(54, 13);
            this.label5.TabIndex = 8;
            this.label5.Text = "numSteps";
            // 
            // Calibration_numSteps
            // 
            this.Calibration_numSteps.Location = new System.Drawing.Point(19, 366);
            this.Calibration_numSteps.Name = "Calibration_numSteps";
            this.Calibration_numSteps.Size = new System.Drawing.Size(100, 20);
            this.Calibration_numSteps.TabIndex = 9;
            this.Calibration_numSteps.Text = "20";
            // 
            // Obj_min
            // 
            this.Obj_min.Location = new System.Drawing.Point(13, 448);
            this.Obj_min.Name = "Obj_min";
            this.Obj_min.Size = new System.Drawing.Size(38, 20);
            this.Obj_min.TabIndex = 10;
            this.Obj_min.Text = "2";
            // 
            // Obj_max
            // 
            this.Obj_max.Location = new System.Drawing.Point(72, 448);
            this.Obj_max.Name = "Obj_max";
            this.Obj_max.Size = new System.Drawing.Size(47, 20);
            this.Obj_max.TabIndex = 11;
            this.Obj_max.Text = "4";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(444, 659);
            this.Controls.Add(this.Obj_max);
            this.Controls.Add(this.Obj_min);
            this.Controls.Add(this.Calibration_numSteps);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.button3);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.OutputConsole);
            this.Name = "Form1";
            this.Text = "Form1";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.OpenFileDialog openFileDialog1;
        private System.Windows.Forms.Label OutputConsole;
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button button3;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.TextBox Calibration_numSteps;
        private System.Windows.Forms.TextBox Obj_min;
        private System.Windows.Forms.TextBox Obj_max;
    }
}

