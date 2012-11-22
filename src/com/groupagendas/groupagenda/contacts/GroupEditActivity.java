package com.groupagendas.groupagenda.contacts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.londatiga.android.CropOption;
import net.londatiga.android.CropOptionAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.utils.MapUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class GroupEditActivity extends Activity implements OnClickListener {
	private String ERROR_STRING = "";
	private final int ERROR_DIALOG = 0;
	private final int CROP_IMAGE_DIALOG = 1;
	private final int CHOOSE_CONTACTS_DIALOG = 2;

	private Uri mImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;

	private DataManagement dm;
	
	private ProgressBar pb;

	private String group_name;
	public Group editedGroup;

	private Button sendButton;
	private Button contactsButton;
	private EditText groupNameView;
	private ImageView imageView;
	private CheckBox removeImage;

	private CharSequence[] titles;
	private int[] ids;
	private boolean[] selections;
	
	private boolean ACTION_EDIT = true;
	
	Map<String, String> groupContactsMapTemp = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.group_edit);
		
		pb = (ProgressBar) findViewById(R.id.progress);
		
		dm = DataManagement.getInstance(this);

		Intent intent = getIntent();
		
		
		// Views
		TextView titleView = (TextView) findViewById(R.id.title);
		
		sendButton = (Button) findViewById(R.id.sendbutton);
		sendButton.setOnClickListener(this);

		contactsButton = (Button) findViewById(R.id.contactsButton);
		contactsButton.setOnClickListener(this);
		
		groupNameView = (EditText) findViewById(R.id.group_name);
		
		imageView = (ImageView) findViewById(R.id.group_image);
		imageView.setOnClickListener(this);
		
		removeImage = (CheckBox) findViewById(R.id.remove_image);
		
		// GET ACTION
		ACTION_EDIT = intent.getBooleanExtra("action", true);
		
		if(ACTION_EDIT){
			group_name = intent.getStringExtra("group_name");
			new GetGroupTask().execute(intent.getIntExtra("group_id", 0));
			
			titleView.setText(group_name);
			groupNameView.setText(group_name);
			
		}else{
			TableRow RIRow = (TableRow) findViewById(R.id.remove_image_row);
			RIRow.setVisibility(View.GONE);
			View RIView = findViewById(R.id.remove_image_line);
			RIView.setVisibility(View.GONE);
			
			new GetContactsTask().execute();
			
			editedGroup = new Group();
			
			titleView.setText(getString(R.string.add_group));
			imageView.setImageResource(R.drawable.group_icon);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sendbutton:
			if(ACTION_EDIT){
				new EditGroupTask().execute();
			}else{
				new CreateGroupTask().execute(editedGroup);
			}
			break;
		case R.id.group_image:
			showDialog(CROP_IMAGE_DIALOG);
			break;
		case R.id.contactsButton:
			showDialog(CHOOSE_CONTACTS_DIALOG);
			break;
		}
	}
	
	class CreateGroupTask extends AsyncTask<Group, Boolean, Boolean> {
		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Group... groups) {
			boolean check = true;
			String temp = "";
			ContentValues cv = new ContentValues();
			
			// title
			temp = groupNameView.getText().toString();
			if(temp.length() <= 0){
				check = false;
				ERROR_STRING = getString(R.string.title_is_required);
			}
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.TITLE, temp);
			editedGroup.title = temp;
			
			// contacts
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS, MapUtils.mapToString(getApplicationContext(), editedGroup.contacts));
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT, editedGroup.contacts.size());
			
			if(editedGroup.image_bytes != null){
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, editedGroup.image_bytes);
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE , true);
			}
			
			if(check){
				check = ContactManagement.insertGroup(GroupEditActivity.this, editedGroup);
			}
			
			return check;
			
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				onBackPressed();
			} else {
				ERROR_STRING = DataManagement.getError();
				showDialog(ERROR_DIALOG);
			}
			pb.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
	}
	
	class EditGroupTask extends AsyncTask<Void, Boolean, Boolean> {
		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... groups) {
			boolean check = true;
			String temp = "";
			ContentValues cv = new ContentValues();
			
			// title
			temp = groupNameView.getText().toString();
			if(temp.length() <= 0){
				check = false;
				ERROR_STRING = getString(R.string.title_is_required);
			}
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.TITLE, temp);
			editedGroup.title = temp;
			
			// contacts
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS, MapUtils.mapToString(getApplicationContext(), editedGroup.contacts));
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT, editedGroup.contacts.size());
			
			// image
			editedGroup.remove_image = removeImage.isChecked();
			if(removeImage.isChecked()){
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE, false);
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL, "");
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL, "");
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, "");
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE, removeImage.isChecked());
			}else{
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, editedGroup.image_bytes);
			}
			
			if(check){
				Uri uri = Uri.parse(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI+"/"+editedGroup.group_id);
				getContentResolver().update(uri, cv, null, null);
				//check = dm.editGroup(getApplicationContext(), editedGroup);
				check = ContactManagement.editGroupOnRemoteDb(getApplicationContext(), editedGroup, 0, true);
				
				for(String s : groupContactsMapTemp.keySet()){
					Contact c = ContactManagement.getContactFromLocalDb(getApplicationContext(), Integer.parseInt(groupContactsMapTemp.get(s)), 0);
					if(c.groups != null){
						Set<String> keySet = c.groups.keySet();
						for(String g : keySet){
							if(c.groups.get(g).equalsIgnoreCase(""+editedGroup.group_id)){
								c.groups.remove(g);
								ContactManagement.updateContactOnLocalDb(getApplicationContext(), c);
								break;
							}
						}
					}
				}
				
				int max_key = 0;
				if(editedGroup.contacts != null){
					
					for(String s : editedGroup.contacts.keySet()){
						Contact c = ContactManagement.getContactFromLocalDb(getApplicationContext(), Integer.parseInt(editedGroup.contacts.get(s)), 0);
						
						if(c.groups != null){
							for(String key : c.groups.keySet()){
								int temp2 = Integer.parseInt(key);
								if(temp2 > max_key){
									max_key = temp2;
								}
							}
							c.groups.put(""+(max_key+1), ""+editedGroup.group_id);
						} else {
							c.groups = new HashMap<String, String>();
							c.groups.put(""+max_key, ""+editedGroup.group_id);
						}
						
						ContactManagement.updateContactOnLocalDb(getApplicationContext(), c);
					}
				}

//				if(!success){
//					cv = new ContentValues();
//					cv.put(ContactsProvider.CMetaData.GroupsMetaData.NEED_UPDATE, 1);
//					getContentResolver().update(uri, cv, null, null);
//				}
			}
			
			return check;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Intent intent = new Intent();
				intent.putExtra("group_name", groupNameView.getText().toString());
				setResult(1, intent);
				finish();
			} else {
				ERROR_STRING = DataManagement.getError();
				showDialog(ERROR_DIALOG);
			}
			pb.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
	}

	class GetGroupTask extends AsyncTask<Integer, Group, Group> {
		@Override
		protected void onPreExecute() {
			pb.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected Group doInBackground(Integer... id) {
			editedGroup = ContactManagement.getGroupFromLocalDb(GroupEditActivity.this, id[0], 0);

			ArrayList<Contact> contacts = ContactManagement.getContactsFromLocalDb(GroupEditActivity.this, null);
			getContactsList(contacts, false);

			return editedGroup;
		}

		@Override
		protected void onPostExecute(Group result) {
			if (result.image) {
				Bitmap bitmap = Utils.getResizedBitmap(BitmapFactory.decodeByteArray(result.image_bytes, 0, result.image_bytes.length), 120, 120);
				imageView.setImageBitmap(bitmap);
			} else {
				imageView.setImageResource(R.drawable.group_icon);
			}

			pb.setVisibility(View.GONE);
			super.onPostExecute(result);
		}

	}
	
	class GetContactsTask extends AsyncTask<Void, Void, Void>{
		@Override
		protected void onPreExecute() {
			contactsButton.setEnabled(false);
		}
		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<Contact> contacts = ContactManagement.getContactsFromLocalDb(GroupEditActivity.this, null);
			getContactsList(contacts, true);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			contactsButton.setEnabled(true);
		}
	}
	
	private void getContactsList(ArrayList<Contact> contacts, boolean isFalse){
		
		int l = contacts.size();
		titles = new CharSequence[l];
		ids = new int[l];
		selections = new boolean[l];

		for (int i = 0; i < l; i++) {
			titles[i] = new StringBuilder(contacts.get(i).name).append(" ").append(contacts.get(i).lastname).toString();
			ids[i] = contacts.get(i).contact_id;
			if(isFalse || editedGroup.contacts == null){
				selections[i] = false;
			}else{
				selections[i] = editedGroup.contacts.containsValue(String.valueOf(contacts.get(i).contact_id));
				groupContactsMapTemp = editedGroup.contacts;
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case ERROR_DIALOG:
			builder.setMessage(ERROR_STRING).setCancelable(false).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			break;
		case CHOOSE_CONTACTS_DIALOG:
			builder.setTitle(getString(R.string.choose_contacts)).setMultiChoiceItems(titles, selections, new DialogSelectionClickHandler())
					.setPositiveButton(getString(R.string.ok), new DialogButtonClickHandler());
			break;
		case CROP_IMAGE_DIALOG:
			final String[] items = new String[] { getString(R.string.take_from_camera), getString(R.string.select_from_gallery) };
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
			builder.setTitle(getString(R.string.select_image));
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					if (item == 0) {
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

						mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_avatar_"
								+ String.valueOf(System.currentTimeMillis()) + ".jpg"));

						intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

						try {
							intent.putExtra("return-data", true);

							startActivityForResult(intent, PICK_FROM_CAMERA);
						} catch (ActivityNotFoundException e) {
							Reporter.reportError(getApplicationContext(), this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
						}
					} else { // pick from file
						Intent intent = new Intent();

						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);

						startActivityForResult(Intent.createChooser(intent, getString(R.string.complete_action_using)), PICK_FROM_FILE);
					}
				}
			});
			break;
		}
		return builder.create();
	}

	public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
		@Override
		public void onClick(DialogInterface dialog, int clicked, boolean selected) {
			selections[clicked] = selected;
		}
	}

	private class DialogButtonClickHandler implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int clicked) {
			switch (clicked) {
			case DialogInterface.BUTTON_POSITIVE:
				editedGroup.contacts = new HashMap<String, String>();

				for (int i = 0, l = ids.length; i < l; i++) {
					if (selections[i]) {
						editedGroup.contacts.put(""+i, ""+ids[i]);
					}
				}
				editedGroup.contact_count = editedGroup.contacts.size();
				break;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case PICK_FROM_CAMERA:
			doCrop();

			break;

		case PICK_FROM_FILE:
			mImageCaptureUri = data.getData();

			doCrop();

			break;

		case CROP_FROM_CAMERA:
			Bundle extras = data.getExtras();

			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				imageView.setImageBitmap(photo);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				photo.compress(CompressFormat.PNG, 100, bos);
				editedGroup.image_bytes = bos.toByteArray();
			}

			File f = new File(mImageCaptureUri.getPath());
			if (f.exists())
				f.delete();

			break;

		}
	}

	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

		int size = list.size();

		if (size == 0) {
			Toast.makeText(this, getString(R.string.can_not_find_image_crop_app), Toast.LENGTH_SHORT).show();

			return;
		} else {
			intent.setData(mImageCaptureUri);

			intent.putExtra("outputX", 200);
			intent.putExtra("outputY", 200);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.choose_crop_app));
				builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
					}
				});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

						if (mImageCaptureUri != null) {
							getContentResolver().delete(mImageCaptureUri, null, null);
							mImageCaptureUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}
}
