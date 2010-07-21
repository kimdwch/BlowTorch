package com.happygoatstudios.bt.responder.toast;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.responder.TriggerResponderEditorDoneListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class ToastResponderEditor extends Dialog {

	private ToastResponder the_responder;
	private ToastResponder original;
	
	boolean isEditor = false;;
	
	TriggerResponderEditorDoneListener finish_with;
	
	public ToastResponderEditor(Context context,ToastResponder input,TriggerResponderEditorDoneListener doneListener) {
		super(context);
		// TODO Auto-generated constructor stub
		finish_with = doneListener;
		if(input != null) {
			original = input.copy();
			the_responder = input.copy();
			isEditor = true;
		} else {
			the_responder = new ToastResponder();
		}
	}
	
	public void onCreate(Bundle b) {
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		setContentView(R.layout.responder_toast_dialog);
		
		Button done = (Button)findViewById(R.id.responder_toast_done_button);
		done.setOnClickListener(new DoneListener());
		
		EditText message = (EditText)findViewById(R.id.responder_toast_message);
		EditText delay = (EditText)findViewById(R.id.responder_toast_delay);
		
		delay.setText(Integer.toString(the_responder.getDelay()));
		message.setText(the_responder.getMessage());
		
		Button cancel = (Button)findViewById(R.id.responder_toast_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ToastResponderEditor.this.dismiss();
			}
		});
	}
	
	private class DoneListener implements View.OnClickListener {

		
		
		public void onClick(View arg0) {
			
			EditText message = (EditText)ToastResponderEditor.this.findViewById(R.id.responder_toast_message);
			EditText delay = (EditText)ToastResponderEditor.this.findViewById(R.id.responder_toast_delay);
			
			the_responder.setDelay(Integer.parseInt(delay.getText().toString()));
			the_responder.setMessage(message.getText().toString());
			
			if(isEditor) {
				finish_with.editTriggerResponder(the_responder, original);
			} else {
				finish_with.newTriggerResponder(the_responder);
			}
			
			ToastResponderEditor.this.dismiss();
		}
		
	};

	
	
}