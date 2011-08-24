package com.smspammer;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainActivity extends Activity 
{
	EditText mobilenoText;
	Button startBtn;
	volatile boolean stop=false;
	Thread smsThread = null;
	String msg;
	int intervalSel=0;
	int contactClickCount=0;
	Spinner intervalSpin;
	String[] name;
	String[] number;
	Handler handler;
	private static final int PICK_CONTACT = 3;
   /** Called when the activity is first created. */
   @Override   
   public void onCreate(Bundle savedInstanceState)
   {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
       
       handler = new Handler(Looper.getMainLooper());
     
       mobilenoText=(EditText) findViewById(R.id.Mobile);     
       final Button contactBtn=(Button) findViewById(R.id.Contact);
       final EditText textnoTxt=(EditText) findViewById(R.id.MsgCount);
       
       intervalSpin=(Spinner) findViewById(R.id.Interval);
       ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
               this, R.array.IntervalArray, android.R.layout.simple_spinner_item);
       adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       intervalSpin.setAdapter(adapter);
       
       final CheckBox ownMsg=(CheckBox) findViewById(R.id.OwnMessage);
       startBtn=(Button) findViewById(R.id.Send);
       final EditText msgTxt=(EditText) findViewById(R.id.MsgText);
       final Button stopBtn=(Button) findViewById(R.id.Stop);
       final Button exitBtn=(Button) findViewById(R.id.Exit);
       
       mobilenoText.setHint("Mobile Number");
       contactBtn.setHint("Choose a Contact");
       contactBtn.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
			
		}
	});
       textnoTxt.setHint("No. of Messages");
       
       ownMsg.setText("Own Message?");
       ownMsg.setOnCheckedChangeListener(new OnCheckedChangeListener()
       {
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean checked)
		{
			msgTxt.setEnabled(checked);
		}    	   
       });
       
       msgTxt.setEnabled(false);
       msgTxt.setHint("Message");
       msgTxt.setHeight(100);
       startBtn.setHint("Send");
       
       startBtn.setOnClickListener(new View.OnClickListener()
       {
    	   public void onClick(View v)
    	   {
    		   if(smsThread==null || !smsThread.isAlive()) {
	    
	    		   if(stop==true)
	    		   {
	    			   stop=false;
	    		   }
	    		   if(mobilenoText.isEnabled() && !mobilenoText.getText().toString().equals("")  || !textnoTxt.getText().toString().equals(""))
	    		   {
	    			   final String mobileno=mobilenoText.getText().toString();
	    			   String[] numbers = mobileno.split(",");
		    		   final int textno=Integer.parseInt(textnoTxt.getText().toString());
		    		   final int interval=getInterval();
		    		   if(msgTxt.isEnabled())
		    		   { 
	    				   msg=msgTxt.getText().toString();
		    		   }else{ 
		    			   msg="Message Error";
		    		   }
		    		   if(!msg.equals(""))
		    		   {
		    			   for(int i=0;i<numbers.length;i++) {
		    				   smsThread=sendSMS(numbers[i], interval, textno, msg);
						   	   smsThread.start();
						   	   
		    			   }	    			   
		    		   }else{
		    			   Toast msgError=Toast.makeText(MainActivity.this, "Please input a Message", Toast.LENGTH_LONG);
		    			   msgError.show();
		    		   }
	    		   }else{
	    			   Toast error=Toast.makeText(MainActivity.this, "Please input a number and amount!", Toast.LENGTH_LONG);
	    			   error.show();
	    		   }
    		   }else{
    			   Toast error=Toast.makeText(MainActivity.this, "Messages are already being sent, Please Wait!", Toast.LENGTH_LONG);
    			   error.show();
    		   }
    	   }
       });    
       
       stopBtn.setOnClickListener(new View.OnClickListener()
       {
			@Override
			public void onClick(View v)
			{
				stop=true;
			}
   		});
       
       exitBtn.setOnClickListener(new View.OnClickListener()
       {
			@Override
			public void onClick(View v)
			{
				finish();
			}
   		});
   } 
   
   public Thread sendSMS(final String mobileno, final int interval, final int textno, final String msgTxt)
   {
	   smsThread=new Thread()
	   {
		   public void run()
		   {
			   for(int i=0;i<textno;i++)
			   {
				   if(stop==false)
				   {
					   SmsManager m = SmsManager.getDefault();
					   String destinationNumber=mobileno;  
				   	   String text = msgTxt;  		   	   
					   m.sendTextMessage(destinationNumber, null, text, null, null);
					   showToast(i, textno, destinationNumber);
						   try 
						   {
							Thread.sleep(interval);
						   } catch (InterruptedException e) {
							e.printStackTrace();
						   }
				   }
			   }
			   
		   }
	   };
	   return smsThread;
   }
   
   public void showToast(final int i, final int textno, final String destinationNumber)
   {
	   handler.post(new Runnable() {
		   public void run() {
			   Toast.makeText(getApplicationContext(), "Sending Message " + (i + 1) + " of " + textno + " to: " + destinationNumber, Toast.LENGTH_SHORT).show();			   
		   }
		   
	   });
	  
   }
 //  Toast.makeText(MainActivity.this, "Sending Message " + (i + 1) + " of " + textno, Toast.LENGTH_SHORT).show();
   
   public int getInterval()
   {
	   try 
	   {
		   intervalSel=((Number)NumberFormat.getInstance().parse(intervalSpin.getSelectedItem().toString())).intValue();
	   }catch (ParseException e){
		   e.printStackTrace();
	   }
	   
	   if(intervalSpin.getSelectedItem().toString().contains("Seconds"))
	   {
		   intervalSel=intervalSel*1000;
	   }else if(intervalSpin.getSelectedItem().toString().contains("Minute")){
		   intervalSel=intervalSel*60000;
	   }else if(intervalSpin.getSelectedItem().toString().contains("Hour")){
		   intervalSel=intervalSel*3600000;
	   }
	   return intervalSel;
   }
   
   @Override
   public void onActivityResult(int reqCode, int resultCode, Intent data){
       super.onActivityResult(reqCode, resultCode, data);
       Boolean hasPhone=false;
       switch(reqCode){        
		case (PICK_CONTACT):
	            if (resultCode == Activity.RESULT_OK){
	                Uri contactData = data.getData();
	                Cursor c = managedQuery(contactData, null, null, null, null);
		                if (c.moveToFirst()){
		                    // other data is available for the Contact.  I have decided
		                    //    to only get the name of the Contact.
		                    String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
		                    if(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)>0) {
		                    	hasPhone=true;
		                    }
		                    getNumber(hasPhone, id);             
	                }
	            }
       }
   }
   
   public void getNumber(Boolean hasPhone, String name) {
	   if(hasPhone) {
		   ContentResolver cr = getContentResolver();
		Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
    			   							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] {name}, null);
    	   
    	   if(phoneCursor.moveToFirst()) {
    		   String number = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
    		   if(contactClickCount==0) {
    			   mobilenoText.setText(number);
    			   contactClickCount++;
    		   }else{
    			   String mnTxt = mobilenoText.getText().toString();
    			   mobilenoText.setText(mnTxt + "," + number);
    			   
    		   }
    		   	
    	   }
       }
   }

}