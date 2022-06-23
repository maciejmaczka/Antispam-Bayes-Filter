/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package as_bayes;
import java.sql.*;
import java.util.StringTokenizer;
/**
 *
 * @author mac
 */
public class Main_Windows extends javax.swing.JFrame {

    
    Connection bayes_sql_connection = null;
    Statement  bayes_sql_statement = null;
    
    int rev_total = 0;
    int rev_current = 0;
    
    boolean remark = false;
            
    double spam_probability = 0;
    double ham_probability = 0;
    
    double hs_ceternly = 0;
    double word_count = 0;
    double unkwnown_word_count = 0;
    
    String word2 = "";
    String word1 = "";
    
    double h_word2 = 1;
    double h_word1 = 1;
    
    double s_word2 = 1;
    double s_word1 = 1;
    
    double wendung_2 = 1;
    double wendung_1 = 1;
    
    
    Details dt_windows; 
        long spam_temp = 0;
        long ham_temp = 0;
        long cer_temp = 0;
    /**
     * Creates new form Main_Windows
     */
    public Main_Windows() 
    {
        initComponents();
        
        dt_windows = new Details();
        
        init_sql_connection();
        check_sql_connection();
        sql_get_messages_summary();
        
       
     
       
       
    }
    
    public void write_details(String text)
    {
        
        
        dt_windows.add_msg(text + " \n");
        
    }
    
    
    public boolean init_sql_connection()
    {
        try
        {
            Class.forName(("org.sqlite.JDBC"));
            
            bayes_sql_connection = DriverManager.getConnection("jdbc:sqlite:bayes.db");
            
            
            A_Output.append("> Connected to DB" +"\n" );
        }
        catch (Exception e)
        {
            
            A_Output.append("> (ISC) ERROR: +" + e.getMessage() +"\n" );
            
            
        }
        
        
        
       return true; 
    }
    
    
    public boolean check_sql_connection()
    {
        
        try
        {
            
            bayes_sql_statement = bayes_sql_connection.createStatement();
            
            String sql_messages;
            String sql_words;
            
            
            sql_messages = "CREATE TABLE IF NOT EXISTS MESSAGES " + 
                           "(ID        INT PRIMARY KEY  NOT NULL, " +       // ID
                           "HEADER     TEXT             NOT NULL, " +       // 
                           "BODY       TEXT             NOT NULL, " +
                           "HS         TEXT             NOT NULL, " +       // HAM OR SPAM Marked by user
                           "H_VALUE    INT              , " +               // header spam probability
                           "B_VALUE    INT              , " +               // body spam probability
                           "VALUE      INT              ) " ;               // total value   
                    
            
            bayes_sql_statement.execute(sql_messages);
            
            
            sql_words  = "CREATE TABLE IF NOT EXISTS WORDS " +
                         "(ID           INT PRIMARY KEY NOT NULL, " +
                         "WORD          TEXT            NOT NULL, " +
                      //   "HS            TEXT            NOT NULL, " +       // HAM OR SPAM Marked by user
                         "H_COUNT       INT             NOT NULL, " +       // Number in ham msg
                         "S_COUNT       INT             NOT NULL) " ;       // Number in spam msg
                    
            bayes_sql_statement.execute(sql_words);
            
            A_Output.append("> Table OK" +"\n");
            
            bayes_sql_statement.close();
            
        }
        catch (Exception e)
        {
             A_Output.append("> (TC) ERROR " + e.getMessage() +"\n" );        
        }
        
    
        
        
        
        
        
        
        return true;
        
   }
        
        
    public void sql_get_messages_summary()
    {
        Sum_HAM.setText( "HAM:   " + Integer.toString(sql_get_messages("HAM")) ) ;
        Sum_SPAM.setText("SPAM: " + Integer.toString(sql_get_messages("SPAM")) ) ;
        
        Sum_Total.setText("ALL:   " + Integer.toString(sql_get_messages("%")) ) ;
        
        rev_total = sql_get_messages("%");
        
        Rev_Total.setText("Current:  " + rev_current + "/" + rev_total) ;
    }
    
    
    public void word_tokenizer(String message)
    {
        
        StringTokenizer tokens = new StringTokenizer(message.toLowerCase());
        String word_check = "";
        
        while (tokens.hasMoreTokens())
        {
            
            word_check = tokens.nextToken();
            word_check = word_check.replaceAll("[\"<>!#$^*-+.^:''~;,()\\]\\[]", "");
            
            
            if (word_check.length() <=3)
            {
                
                continue;
                
            }
            
            if (word_check_if_registerd(word_check))
            {
                // word is there already
                update_words_count(word_check);

              
                
                
            }
            else
            {
                
              
                // need to add words
                String HS = "";
                
                if (B_HAM.isSelected())
                {
                    
                    HS = "HAM";
                    
                }
                if (B_SPAM.isSelected())
                {
                    HS = "SPAM";
                    
                }
                
                
                word_add(word_check, HS);
                
            }
            
            
        }
        
        
        
    }
    
    public void update_words_count(String word)
    {
        
         int ham  = get_word_count_ham(word);
         int spam = get_word_count_spam(word);
                
        
         if(B_HAM.isSelected())
         {
             
             ham++;
             
             if (remark == true)
             {
                 
                 spam--;
                 
                 if (spam <= 0)
                 {
                     
                     spam = 0;
                     
                 }
             }
             
         }
         if(B_SPAM.isSelected())
         {
             
             spam++;
          
             
             if (remark == true)
             {
             
                 ham--;
                 
                 if(ham <= 0)
                 {
                     
                     ham = 0;
                     
                     
                 }
                 
             }
             
             
         }
         
        try
        {
           

            bayes_sql_statement = bayes_sql_connection.createStatement();
            String update_word;
       
   
            
            update_word =  "UPDATE WORDS " +
                                  "SET H_COUNT='" + ham + "' , " +
                                  "S_COUNT='" + spam +  "'  " +
                                  "WHERE WORD ='" + word + "';";
            
            bayes_sql_statement.execute(update_word);
            
            

            bayes_sql_statement.close();
            
            write_details("Update word: " + word + " Ham: " + ham + " Spam: " + spam );
            
        }
        catch (Exception e)
        {
             A_Output.append("> (WUP) ERROR " + e.getMessage() +"\n" );    
             
            
        }
        
        
  
        
    }
    
    
    public void word_add(String word, String HS)
    {
        
        try
        {
           

            bayes_sql_statement = bayes_sql_connection.createStatement();
            String insert_message;

            int h_count = 0;
            int s_count = 0;
            
            if (HS.compareTo("SPAM") == 0)
            {
                
                s_count++;
                
            }
            if (HS.compareTo("HAM") == 0)
            {
                
                h_count++;
                
            }
            
             
            
            insert_message = "INSERT INTO WORDS " + 
                                  " (ID, WORD, H_COUNT , S_COUNT) VALUES "  +
                                  " ( " +  (sql_get_primary_key("WORDS") + 1) + " , '" + 
                                        word + "' , " +
                                       
                                        " " + h_count + " , " +
                                        " " + s_count  + " );";
                    
                    
            
            bayes_sql_statement.execute(insert_message);
      
            

            bayes_sql_statement.close();
            
            
            write_details("New word: " + word + " Ham: " + h_count + " Spam: " + s_count );
            
        }
        catch (Exception e)
        {
             A_Output.append("> (WAD) ERROR " + e.getMessage() +"\n" );    
             
            
        }
        
        
    }
    
    public boolean word_check_if_registerd(String word)
    {
        
        
        try
        {
           

            bayes_sql_statement = bayes_sql_connection.createStatement();
            String select_word;
            ResultSet rs;
            int count = 0;
            
            select_word = "SELECT COUNT(*) FROM WORDS WHERE WORD ='" + word + "';";
                    
            
            rs = bayes_sql_statement.executeQuery(select_word);
            count = rs.getInt(1);
            

            bayes_sql_statement.close();
            
            if (count == 0)
            {
                
                return false;
                
            }
            else
            {
                
                return true;
                
            }
            
        }
        catch (Exception e)
        {
             A_Output.append("> (WEX) ERROR " + e.getMessage() +"\n" );    
             
            
        }
        
        
        return false;
    }
    
    
    public int get_word_count_ham(String Word)
    {
        try
        {
           

            bayes_sql_statement = bayes_sql_connection.createStatement();
            String select_word;
            ResultSet rs;
            int count = 0;
            
            select_word = "SELECT H_COUNT FROM WORDS WHERE WORD ='" + Word + "';";
                    
            
            rs = bayes_sql_statement.executeQuery(select_word);
            

            count = rs.getInt(1);
            

            bayes_sql_statement.close();
            
            return count;
            
        }
        catch (Exception e)
        {
            
           //  A_Output.append("> (HCK) ERROR " + e.getMessage() +"\n" );    
             
            
        }
        
        return -1;
    }
    
    
    
    
    public int get_word_count_spam(String Word)
    {
        
        try
        {
           

            bayes_sql_statement = bayes_sql_connection.createStatement();
            String select_word;
            ResultSet rs;
            int count = 0;
            
            select_word = "SELECT S_COUNT FROM WORDS WHERE WORD ='" + Word + "';";
                    
            
            rs = bayes_sql_statement.executeQuery(select_word);
            
            count = rs.getInt(1);
            

            bayes_sql_statement.close();
            
            return count;
            
        }
        catch (Exception e)
        {
           //  A_Output.append("> (SCK) ERROR " + e.getMessage() +"\n" );    
             
            
        }
        
        
        return -1;
    }
    
        
    public void messages_revision(int ID)
    {
        
        try
        {
            
            bayes_sql_statement = bayes_sql_connection.createStatement();
            ResultSet rs;            
            String sql_messages;

            
            
            sql_messages = "SELECT ID, HEADER , BODY , HS FROM MESSAGES WHERE ID = " + ID + " ;" ;               // total value   
                    
            
            rs = bayes_sql_statement.executeQuery(sql_messages);
           
            rs.getInt(1);
            
            MSG_Title.setText(rs.getString("HEADER"));
            MSG_Body.setText(rs.getString("BODY"));

            String HS = rs.getString("HS");
            
            if (HS.compareTo("SPAM") == 0)
            {
                
                B_SPAM.setSelected(true);
                
            }
            if (HS.compareTo("HAM") == 0)
            {
                
                B_HAM.setSelected(true);
                
            }
                        
            bayes_sql_statement.close();
         
            
            
        }
        catch (Exception e)
        {
             A_Output.append("> (SRM) ERROR " + e.getMessage() +"\n" );        
        }
        
    
        
        
        
        
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        HAM_SPAM = new javax.swing.ButtonGroup();
        MSG_Analyze1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        MSG_Body = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        MSG_Title = new javax.swing.JTextField();
        B_SPAM = new javax.swing.JRadioButton();
        B_HAM = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        A_Output = new javax.swing.JTextArea();
        A_Mark = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        Sum_SPAM = new javax.swing.JLabel();
        Sum_HAM = new javax.swing.JLabel();
        Sum_Total = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        Rev_PRV = new javax.swing.JButton();
        PRV_Next = new javax.swing.JButton();
        Rev_Total = new javax.swing.JLabel();
        Remark = new javax.swing.JButton();
        Rev_First = new javax.swing.JButton();
        PRV_Last = new javax.swing.JButton();
        MSG_Clear = new javax.swing.JButton();
        MSG_Analyze = new javax.swing.JButton();
        SPAM_PROC = new javax.swing.JLabel();
        HAM_PROC = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        Bay_Cer = new javax.swing.JLabel();
        Details = new javax.swing.JButton();
        Label_summary = new javax.swing.JLabel();

        MSG_Analyze1.setText("Analyze");
        MSG_Analyze1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MSG_Analyze1ActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        MSG_Body.setColumns(20);
        MSG_Body.setRows(5);
        jScrollPane1.setViewportView(MSG_Body);

        jLabel1.setText("Header");

        jLabel2.setText("Body");

        HAM_SPAM.add(B_SPAM);
        B_SPAM.setText("SPAM");

        HAM_SPAM.add(B_HAM);
        B_HAM.setSelected(true);
        B_HAM.setText("HAM");

        jLabel3.setText("Output");

        A_Output.setEditable(false);
        A_Output.setColumns(20);
        A_Output.setRows(5);
        jScrollPane2.setViewportView(A_Output);

        A_Mark.setText("Mark as new");
        A_Mark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                A_MarkActionPerformed(evt);
            }
        });

        jLabel4.setText("Database:");

        Sum_SPAM.setText("SPAM: -1");

        Sum_HAM.setText("HAM:   -1");

        Sum_Total.setText("ALL:   -1");

        jLabel5.setText("Learning:");

        jLabel6.setText("Working:");

        jLabel7.setText("SPAM %:");

        jLabel8.setText("HAM %:");

        jLabel9.setText("Review");

        Rev_PRV.setText("<");
        Rev_PRV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Rev_PRVActionPerformed(evt);
            }
        });

        PRV_Next.setText(">");
        PRV_Next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PRV_NextActionPerformed(evt);
            }
        });

        Rev_Total.setText("Current: -1/-1");

        Remark.setText("ReMark");
        Remark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemarkActionPerformed(evt);
            }
        });

        Rev_First.setText("<<");
        Rev_First.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Rev_FirstActionPerformed(evt);
            }
        });

        PRV_Last.setText(">>");
        PRV_Last.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PRV_LastActionPerformed(evt);
            }
        });

        MSG_Clear.setText("Clear");
        MSG_Clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MSG_ClearActionPerformed(evt);
            }
        });

        MSG_Analyze.setText("Analyze");
        MSG_Analyze.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MSG_AnalyzeActionPerformed(evt);
            }
        });

        SPAM_PROC.setText("0");

        HAM_PROC.setText("0");

        jLabel10.setText("Cert %:");

        Bay_Cer.setText("0");

        Details.setText("Details");
        Details.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DetailsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(MSG_Title, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(B_SPAM)
                                        .addComponent(B_HAM)
                                        .addComponent(A_Mark, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel4))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(Sum_HAM, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(Sum_SPAM, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(Sum_Total, javax.swing.GroupLayout.Alignment.TRAILING)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(41, 41, 41))
                                    .addComponent(jLabel6))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(Remark)
                                        .addGap(0, 48, Short.MAX_VALUE))
                                    .addComponent(MSG_Clear, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(HAM_PROC, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                                    .addComponent(SPAM_PROC, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                                    .addComponent(Bay_Cer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(45, 45, 45))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Rev_Total)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(20, 20, 20)
                                .addComponent(Rev_First)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Rev_PRV)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(PRV_Next)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(PRV_Last))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(MSG_Analyze)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Details))
                            .addComponent(Label_summary, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MSG_Title, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(B_SPAM))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(B_HAM))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(A_Mark)
                            .addComponent(Remark))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(SPAM_PROC))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(HAM_PROC))
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(Bay_Cer))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(MSG_Analyze)
                            .addComponent(Details))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Label_summary, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(Rev_PRV)
                            .addComponent(PRV_Next)
                            .addComponent(Rev_First)
                            .addComponent(PRV_Last))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Rev_Total)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Sum_SPAM)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Sum_HAM)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Sum_Total))
                            .addComponent(MSG_Clear, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(22, 22, 22))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void A_MarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_A_MarkActionPerformed
       
        
        remark = false;
        
        sql_get_primary_key("MESSAGES");
        //sql_get_primary_key("WORDS");      
        
        String HS = "";
         String insert_message = "";
         
        if (B_HAM.isSelected())
        {
            
            HS = "HAM";
            
                       
        }
        if (B_SPAM.isSelected())
        {
            
            HS = "SPAM";
            
        }
        
        try
        {
           

            bayes_sql_statement = bayes_sql_connection.createStatement();
           
        
            
            insert_message = "INSERT INTO MESSAGES " + 
                                  " (ID, HEADER, BODY, HS, H_VALUE, B_VALUE, VALUE) VALUES "  +
                                  " ( " +  (sql_get_primary_key("MESSAGES") + 1) + " , '" + 
                                        MSG_Title.getText().replaceAll("'", "") + "' , '" +
                                        MSG_Body.getText().replaceAll("'", "")  + "' , '" +
                                        HS + "' , " +
                                        " 0 , " +
                                        " 0 , " +
                                        " 0 );";
                    
            
            bayes_sql_statement.execute(insert_message);
           
           

            bayes_sql_statement.close();
            
               write_details("New message added: " + HS + " ");
            
        }
        catch (Exception e)
        {
             A_Output.append("> (IAM) ERROR " + e.getMessage() +"\n" );    
              A_Output.append("> " + insert_message +"\n" );
            
        }
        
        sql_get_messages_summary();
        
        word_tokenizer(MSG_Title.getText());
        word_tokenizer(MSG_Body.getText());
        
    }//GEN-LAST:event_A_MarkActionPerformed

    
    public void sql_remark_msg(int ID)
    {
        
        try
        {
             String HS = "";

            if (B_HAM.isSelected())
            {

                HS = "HAM";

            }
            if (B_SPAM.isSelected())
            {

                HS = "SPAM";

            }

            bayes_sql_statement = bayes_sql_connection.createStatement();
            String sql_remark_msg;
           
            
            
            sql_remark_msg = "UPDATE MESSAGES " +
                                  "SET HEADER='" + MSG_Title.getText() + "' , " +
                                  "BODY='" + MSG_Body.getText() +  "' , " +
                                  "HS = '" + HS + "' " +
                                  "WHERE ID=" + ID + ";";
                    
          
            bayes_sql_statement.executeUpdate(sql_remark_msg);
            
            bayes_sql_statement.close();
            
           write_details("Changed message status: " + HS + " " );
       
        }
        catch (Exception e)
        {
             A_Output.append("> (URM) ERROR " + e.getMessage() +"\n" );    
             
         
        }
        
        
         word_tokenizer(MSG_Title.getText());
         word_tokenizer(MSG_Body.getText());
    }
    
    
    
    private void RemarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemarkActionPerformed
       
        remark = true;
        
        sql_remark_msg(rev_current);
        sql_get_messages_summary();
        
    }//GEN-LAST:event_RemarkActionPerformed

    private void Rev_PRVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Rev_PRVActionPerformed
        
        
         rev_current = rev_current -1;
         
         if (rev_current <= 0)
         {
             
             rev_current = 1;
             
         }
             
        
         messages_revision(rev_current);
        
         sql_get_messages_summary();
         
    }//GEN-LAST:event_Rev_PRVActionPerformed

    private void PRV_NextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PRV_NextActionPerformed

        rev_current = rev_current + 1;
        
        if (rev_current >= rev_total)
        {
            
            rev_current = rev_total;
            
            
        }
        
        messages_revision(rev_current);
        
        sql_get_messages_summary();
        
    }//GEN-LAST:event_PRV_NextActionPerformed

    private void Rev_FirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Rev_FirstActionPerformed

        rev_current = 1;
        
        messages_revision(rev_current);
        
        sql_get_messages_summary();
        
    }//GEN-LAST:event_Rev_FirstActionPerformed

    private void PRV_LastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PRV_LastActionPerformed

        rev_current = rev_total;
       
        messages_revision(rev_current);
        
        sql_get_messages_summary();
        
    }//GEN-LAST:event_PRV_LastActionPerformed

    private void MSG_ClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MSG_ClearActionPerformed

        MSG_Body.setText("");
        MSG_Title.setText("");
        
    }//GEN-LAST:event_MSG_ClearActionPerformed

    private void MSG_AnalyzeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MSG_AnalyzeActionPerformed

      //  msg_analyze_bayes(MSG_Title.getText());
        write_details(" " );
        write_details(" ----  Starting ... ----" );
        write_details(" " );
        //   write_details("New word: " + word + " Ham: " + h_count + " Spam: " + s_count );
        msg_analyze_bayes( MSG_Title.getText() + "\n" +   MSG_Body.getText() );
        
     //         spam_temp = 0;
      //  ham_temp = 0;
      //  cer_temp = 0;
        
        
      //      double spam_probability = 0;
    //double ham_probability = 0;
    
   // double hs_ceternly = 0;
        if (cer_temp >= 55)
        {
            
            Label_summary.setText("To review");
            
            if (spam_temp >= 55)
            {
                
                Label_summary.setText("SPAM");
                
            }
            if (ham_temp >= 55)
            {
                
                Label_summary.setText("HAM");
                
            }
            
            
            
        }
        
        if ((cer_temp < 55) && (cer_temp >= 30))
        {
            
            Label_summary.setText("To review");
            
            if (spam_temp >= 70)
            {
                
                Label_summary.setText("PROBABLY SPAM");
                
            }
            if (ham_temp >= 70)
            {
                
                Label_summary.setText("PROBABLY HAM");
                
            }
            
            
            
        }
        if (cer_temp < 30)
        {
            
         //   Label_summary.setText("No data - to review");
        
        }
        
    }//GEN-LAST:event_MSG_AnalyzeActionPerformed

    private void MSG_Analyze1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MSG_Analyze1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MSG_Analyze1ActionPerformed

    private void DetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DetailsActionPerformed
       
        dt_windows.setVisible(true);
        
    }//GEN-LAST:event_DetailsActionPerformed

    
    
    public void msg_analyze_bayes(String message)
    {
        
        
        
        
        spam_probability = 0;
        ham_probability = 0;
         
        unkwnown_word_count = 0;
        word_count = 0;
        
        spam_temp = 0;
        ham_temp = 0;
        cer_temp = 0;
        
        
        StringTokenizer tokens = new StringTokenizer(message.toLowerCase());
        String word_check = "";
        
        while (tokens.hasMoreTokens())
        {
            
            word_check = tokens.nextToken();
            word_check = word_check.replaceAll("[\"<>!#$^*-+.^:''~;,()\\]\\[]", "");
            
            
            if (word_check.length() <=3)
            {
                
                write_details(word_check + " skipping");
                continue;
                
            }
            
            bayes_calculate(word_check);
            
        }
        
       //   A_Output.append("SPAM SCORE " + spam_probability + "\n");
       //   A_Output.append("HAM  SCORE " + ham_probability + "\n");
        

        
         spam_temp = Math.round((spam_probability / (ham_probability + spam_probability)) * 100);
          ham_temp = Math.round((ham_probability / (ham_probability + spam_probability)) * 100);
        
         cer_temp = Math.round(((word_count - unkwnown_word_count)/ word_count) * 100);
        
        write_details(" ");
        write_details(" ---- Finish: ----");
        write_details("Words overall: " + word_count );
        write_details("Words known:" + (word_count - unkwnown_word_count));
        write_details("Words unknown:" + unkwnown_word_count);
        write_details("Certainty % " + cer_temp );
        write_details("SPAM %: " + spam_temp );
        write_details("HAM  %: " + ham_temp);
        write_details("SPAM Score: " + spam_probability );
        write_details("HAM Score: " + ham_probability);
        
        
        write_details(" ");
        
    //    A_Output.append(unkwnown_word_count + " | " + word_count + "\n" );
                 
          SPAM_PROC.setText(Long.toString(spam_temp));
          HAM_PROC.setText(Long.toString(ham_temp));
          Bay_Cer.setText(Long.toString(cer_temp));
        
     //     double abc = 1.25;
      //    int i = (int ) abc;
    }
    
    public void bayes_calculate(String word)
    {
        
        try
        {
            ResultSet rs;
            
            word_count++;
            
            bayes_sql_statement = bayes_sql_connection.createStatement();
            String select_messages;
           
            
            select_messages = "SELECT COUNT(*) FROM WORDS WHERE WORD= '" + word + "' ;";
            rs = bayes_sql_statement.executeQuery(select_messages);
            
            if (rs.getInt(1) == 0)
            {
                
                write_details(word + " no data: H + 1");
                ham_temp++;
                unkwnown_word_count++;
               return; 
                
            }
            
            double spam_temp = 0;
            double ham_temp = 0;
            
            select_messages = " SELECT H_COUNT, S_COUNT FROM WORDS WHERE WORD = '" + word + "';";
                    
            
            rs = bayes_sql_statement.executeQuery(select_messages);
            
            ham_temp = rs.getDouble("H_COUNT");
            spam_temp = rs.getDouble("S_COUNT");
            
            int multiple = 1;
            
            write_details(word + " H: " + ham_temp + " S: " + spam_temp);
            
            if (Math.abs(ham_temp - spam_temp ) <= 10)
            {
                // mala roznica
                
                // nic nie rob 
              //  A_Output.append("1:" + word + " H: " + ham_temp +  " S:" + spam_temp  + "\n");
                
            }
            if ((Math.abs(ham_temp - spam_temp ) <= 20) && (Math.abs(ham_temp - spam_temp ) > 10))
            {
                // srednia roznica
                
                write_details(word + " score multiple x2");
                
                multiple =2;
 
                
                //A_Output.append("2:" + word + " H: " + ham_temp +  " S:" + spam_temp  + "\n");
                
                
            }
            if (Math.abs(ham_temp - spam_temp ) > 20)
            {
                // duza roznica
                write_details(word + " score multiple x4");
                multiple = 4;
                //A_Output.append("4:" + word + " H: " + ham_temp +  " S:" + spam_temp  + "\n");
                
            }
            
             
            if (multiple > 1)
            {
                if (ham_temp > spam_temp)
                {
                    
                    ham_temp = ham_temp * multiple;
                    
                }
                if (spam_temp > ham_temp)
                {
                    
                    spam_temp = spam_temp * multiple;
                    
                }
            }
            
            if (multiple > 1)
            {
            
                write_details(word + " H: " + ham_temp + " S: " + spam_temp);
            
            }
            if (multiple * wendung_2 * wendung_1 >= 8)
            {
                
                
                
                
                if ((s_word2 * s_word1 * spam_temp) > (ham_temp * h_word2 * h_word1))
                {
                        // spam probabiliyt
                    
                        spam_temp = spam_temp * 2;
                       // A_Output.append("> High spam probability:  " + word2 + " " + word1 + " " + word +   " \n");
                        
                        write_details("SPAM Mark: " + word2 + " " + word1 + " " + word );
                        
                }
                else
                {
                        
                      // ham probability
                       ham_temp = ham_temp * 2;
                       write_details("HAM Mark: " + word2 + " " + word1 + " " + word );
                      //  A_Output.append("> High ham probability:  " + word2 + " " + word1 + " " + word +   " \n");
                }
                
            }
            
            
            
            ham_probability +=   ( ham_temp / (ham_temp + spam_temp) * 100);
            spam_probability +=  (spam_temp / (ham_temp + spam_temp) * 100);
            
           
            
            
          //  A_Output.append("HS" + ham_probability + "\n");
           // A_Output.append("SS" + spam_probability + "\n");
            
            bayes_sql_statement.close();
            
           
            word2 = word1;
            word1 = word;
            
            wendung_2 = wendung_1;
            wendung_1 = multiple;
            
            h_word2 = h_word1;
            h_word1 = ham_temp;
            
            s_word2 = s_word1;
            s_word1 = spam_temp;
            
            
        }
        catch (Exception e)
        {
             A_Output.append("> (BC) ERROR " + e.getMessage() +"\n" );    
             
            
        }
        
        
        
        
        
        
    }
    
    
    public int sql_get_messages(String Type)
    {
        
        
        try
        {
            ResultSet rs;
            int count;
            
            bayes_sql_statement = bayes_sql_connection.createStatement();
            String select_messages;
           
            
            
            select_messages = " SELECT COUNT(*) FROM MESSAGES WHERE HS LIKE '" + Type + "';";
                    
            
            rs = bayes_sql_statement.executeQuery(select_messages);
            
            count = rs.getInt(1);
            
            bayes_sql_statement.close();
            
            return count;
            
            
        }
        catch (Exception e)
        {
             A_Output.append("> (MC) ERROR " + e.getMessage() +"\n" );    
             return -1;
            
        }
        
        
        
    }
    
    
    public int sql_get_primary_key(String table)
    {
        ResultSet pk_count;  
        int count = -1;
        
        try
        {
           

            bayes_sql_statement = bayes_sql_connection.createStatement();
            String sql_get_primary_key;
           
            
            
            sql_get_primary_key = "SELECT COUNT(*) FROM " + table +  ";" ;              
                    
            
            pk_count = bayes_sql_statement.executeQuery(sql_get_primary_key);
            count =  pk_count.getInt(1);
           

            bayes_sql_statement.close();
            
            return count;
        }
        catch (Exception e)
        {
             A_Output.append("> (GPK) ERROR " + e.getMessage() +"\n" );    
             
             return -1;
        }
        
    
        
        
        
       
    }        
   
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main_Windows.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main_Windows.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main_Windows.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main_Windows.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main_Windows().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton A_Mark;
    private javax.swing.JTextArea A_Output;
    private javax.swing.JRadioButton B_HAM;
    private javax.swing.JRadioButton B_SPAM;
    private javax.swing.JLabel Bay_Cer;
    private javax.swing.JButton Details;
    private javax.swing.JLabel HAM_PROC;
    private javax.swing.ButtonGroup HAM_SPAM;
    private javax.swing.JLabel Label_summary;
    private javax.swing.JButton MSG_Analyze;
    private javax.swing.JButton MSG_Analyze1;
    private javax.swing.JTextArea MSG_Body;
    private javax.swing.JButton MSG_Clear;
    private javax.swing.JTextField MSG_Title;
    private javax.swing.JButton PRV_Last;
    private javax.swing.JButton PRV_Next;
    private javax.swing.JButton Remark;
    private javax.swing.JButton Rev_First;
    private javax.swing.JButton Rev_PRV;
    private javax.swing.JLabel Rev_Total;
    private javax.swing.JLabel SPAM_PROC;
    private javax.swing.JLabel Sum_HAM;
    private javax.swing.JLabel Sum_SPAM;
    private javax.swing.JLabel Sum_Total;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
