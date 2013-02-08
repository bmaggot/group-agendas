package com.groupagendas.groupagenda.contacts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.ContactManagement;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.utils.MapUtils;
import com.groupagendas.groupagenda.utils.StringValueUtils;

public class GroupEditActivity extends Activity implements OnClickListener {
	public static ArrayList<Contact> selectedContacts;

	private String ERROR_STRING = "";
	private final int ERROR_DIALOG = 0;
	private final int CROP_IMAGE_DIALOG = 1;
	private final int CHOOSE_CONTACTS_DIALOG = 2;

	private Uri mImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;

	private ProgressBar pb;

	private String group_name;
	public Group editedGroup;

	private Button sendButton;
	private Button contactsButton;
	private EditText groupNameView;
	// private ImageView imageView;
	// private CheckBox removeImage;

	private CharSequence[] titles;
	private int[] ids;
	private boolean[] selections;

	private boolean ACTION_EDIT = true;
	public static boolean changesMade = false;

	Map<String, String> groupContactsMapTemp = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.group_edit);

		pb = (ProgressBar) findViewById(R.id.progress);

		Intent intent = getIntent();

		// Views
		TextView titleView = (TextView) findViewById(R.id.title);

		sendButton = (Button) findViewById(R.id.sendbutton);
		sendButton.setOnClickListener(this);

		contactsButton = (Button) findViewById(R.id.contactsButton);
		contactsButton.setOnClickListener(this);

		groupNameView = (EditText) findViewById(R.id.group_name);

		// imageView = (ImageView) findViewById(R.id.group_image);
		// imageView.setOnClickListener(this);
		//
		// removeImage = (CheckBox) findViewById(R.id.remove_image);

		// GET ACTION
		ACTION_EDIT = intent.getBooleanExtra("action", true);

		if (ACTION_EDIT) {
			group_name = intent.getStringExtra("group_name");
			try {
				new GetGroupTask().execute(intent.getIntExtra("group_id", 0)).get();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			titleView.setText(group_name);
			groupNameView.setText(group_name);
			groupNameView.addTextChangedListener(new TextWatcher() {

				private String oldText = null;

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					oldText = s.toString();
				}

				@Override
				public void afterTextChanged(Editable s) {
					if (!s.toString().equalsIgnoreCase(oldText)) {
						changesMade = true;
						// saveButton.setEnabled(changesMade);
					}
				}
			});
			try {
				selectedContacts = new GetContactsTask().execute().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		} else {
			// TableRow RIRow = (TableRow) findViewById(R.id.remove_image_row);
			// RIRow.setVisibility(View.GONE);
			// View RIView = findViewById(R.id.remove_image_line);
			// RIView.setVisibility(View.GONE);

			editedGroup = new Group();

			titleView.setText(getString(R.string.add_group));
			// imageView.setImageResource(R.drawable.group_icon);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		displaySelectedContacts();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sendbutton:
			if (ACTION_EDIT) {
				new EditGroupTask().execute();
			} else {
				new CreateGroupTask().execute(editedGroup);
			}
			break;
		// case R.id.group_image:
		// showDialog(CROP_IMAGE_DIALOG);
		// break;
		case R.id.contactsButton:
			Intent i = new Intent(GroupEditActivity.this, ContactsActivity.class);
			i.putExtra(ContactsActivity.TASK_MODE_KEY, ContactsActivity.TASK_MODE_SELECTION);
			i.putExtra(ContactsActivity.LIST_MODE_KEY, ContactsActivity.LIST_MODE_CONTACTS);
			i.putExtra(ContactsActivity.DESTINATION_KEY, ContactsActivity.DEST_GROUP_EDIT);
			Data.showSaveButtonInContactsForm = true;
			// TODO Data.eventForSavingNewInvitedPersons = event;
			startActivity(i);
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
			if (temp.length() <= 0) {
				check = false;
				ERROR_STRING = getString(R.string.title_is_required);
			}
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.TITLE, temp);
			editedGroup.title = temp;

			// contacts
			editedGroup.contacts = new HashMap<String, String>();

			for (int i = 0, l = selectedContacts.size(); i < l; i++) {
				editedGroup.contacts.put(StringValueUtils.valueOf(i), StringValueUtils.valueOf(selectedContacts.get(i).contact_id));
			}
			editedGroup.contact_count = editedGroup.contacts.size();

			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS, MapUtils.mapToString(getApplicationContext(), editedGroup.contacts));
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT, editedGroup.contacts.size());

			editedGroup.group_id = (int) Calendar.getInstance().getTimeInMillis();
			editedGroup.created = Calendar.getInstance().getTimeInMillis();
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CREATED, editedGroup.created);
			editedGroup.modified = editedGroup.created;
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED, editedGroup.modified);

			if (editedGroup.image_bytes != null) {
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, editedGroup.image_bytes);
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE, true);
			}

			if (check) {
				check = ContactManagement.insertGroup(GroupEditActivity.this, editedGroup);
			}

			return check;

		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				changesMade = false;
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
			changesMade = false;
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
			if (temp.length() <= 0) {
				check = false;
				ERROR_STRING = getString(R.string.title_is_required);
			}
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.TITLE, temp);
			editedGroup.title = temp;

			editedGroup.modified = Calendar.getInstance().getTimeInMillis();
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.MODIFIED, editedGroup.modified);

			// contacts
			editedGroup.contacts = new HashMap<String, String>();

			for (int i = 0, l = selectedContacts.size(); i < l; i++) {
				editedGroup.contacts.put(StringValueUtils.valueOf(i), StringValueUtils.valueOf(selectedContacts.get(i).contact_id));
			}
			editedGroup.contact_count = editedGroup.contacts.size();

			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACTS, MapUtils.mapToString(getApplicationContext(), editedGroup.contacts));
			cv.put(ContactsProvider.CMetaData.GroupsMetaData.CONTACT_COUNT, editedGroup.contacts.size());

			// image
			// editedGroup.remove_image = removeImage.isChecked();
			editedGroup.remove_image = false;

			if (editedGroup.remove_image) {
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE, false);
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_URL, "");
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_THUMB_URL, "");
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, "");
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.REMOVE_IMAGE, editedGroup.remove_image);
			} else {
				cv.put(ContactsProvider.CMetaData.GroupsMetaData.IMAGE_BYTES, editedGroup.image_bytes);
			}

			if (check) {
				Uri uri = Uri.parse(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI + "/" + editedGroup.group_id);
				getContentResolver().update(uri, cv, null, null);
				// check = dm.editGroup(getApplicationContext(), editedGroup);
				if (DataManagement.networkAvailable) {
					check = ContactManagement.editGroupOnRemoteDb(getApplicationContext(), editedGroup, 0, true);
				}

				for (Entry<String, String> e : groupContactsMapTemp.entrySet()) {
					Contact c = ContactManagement.getContactFromLocalDb(getApplicationContext(),
							Integer.parseInt(e.getValue()), 0);
					if (c.groups != null) {
						for (Iterator<Entry<String, String>> it = c.groups.entrySet().iterator(); it.hasNext();) {
							if (it.next().getValue().equalsIgnoreCase(StringValueUtils.valueOf(editedGroup.group_id))) {
								it.remove();
								// very arguable
								// if (c.groups.size() == 0) {
								//	c.groups = new HashMap<String, String>();
								// }
								ContactManagement.updateContactOnLocalDb(getApplicationContext(), c);
								break; // prevents CME when not using an iterator
							}
						}
					}
				}

				int max_key = 0;
				if (editedGroup.contacts != null) {

					for (Entry<String, String> e : editedGroup.contacts.entrySet()) {
						Contact c = ContactManagement.getContactFromLocalDb(getApplicationContext(),
								Integer.parseInt(e.getValue()), 0);

						if (c.groups == null)
							c.groups = new HashMap<String, String>();

						if (!c.groups.isEmpty()) {
							for (String key : c.groups.keySet()) {
								if (!key.contentEquals("")) {
									int temp2 = Integer.parseInt(key);
									if (temp2 > max_key) {
										max_key = temp2;
									}
								} else {
									// TODO: What is this and why is there no break?
									// c.groups = new HashMap<String, String>();

									// Suggested fix below
									c.groups.clear();
									break;
								}
							}
							max_key++;
						}
						c.groups.put(StringValueUtils.valueOf(max_key), StringValueUtils.valueOf(editedGroup.group_id));

						ContactManagement.updateContactOnLocalDb(getApplicationContext(), c);
					}
				}

				// if(!success){
				// cv = new ContentValues();
				// cv.put(ContactsProvider.CMetaData.GroupsMetaData.NEED_UPDATE,
				// 1);
				// getContentResolver().update(uri, cv, null, null);
				// }
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
			// if (result.image) {
				// Bitmap bitmap =
				// Utils.getResizedBitmap(BitmapFactory.decodeByteArray(result.image_bytes,
				// 0, result.image_bytes.length), 120, 120);
				// imageView.setImageBitmap(bitmap);
			// } else {
				// imageView.setImageResource(R.drawable.group_icon);
			// }

			pb.setVisibility(View.GONE);
			super.onPostExecute(result);
		}

	}

	class GetContactsTask extends AsyncTask<Void, Void, ArrayList<Contact>> {
		@Override
		protected void onPreExecute() {
			pb = (ProgressBar) findViewById(R.id.progress);
			pb.setVisibility(View.VISIBLE);
			contactsButton.setEnabled(false);
		}

		@Override
		protected ArrayList<Contact> doInBackground(Void... params) {
			String where = ContactsProvider.CMetaData.ContactsMetaData.GROUPS + " LIKE '%=" + editedGroup.group_id + "&%' OR "
					+ ContactsProvider.CMetaData.ContactsMetaData.GROUPS + " LIKE '%=" + editedGroup.group_id + "'";
			return ContactManagement.getContactsFromLocalDb(GroupEditActivity.this, where);
		}

		@Override
		protected void onPostExecute(ArrayList<Contact> result) {
			pb.setVisibility(View.GONE);
			contactsButton.setEnabled(true);
			selectedContacts = result;
		}
	}

	private void getContactsList(ArrayList<Contact> contacts, boolean isFalse) {

		int l = contacts.size();
		titles = new CharSequence[l];
		ids = new int[l];
		selections = new boolean[l];

		for (int i = 0; i < l; i++) {
			Contact c = contacts.get(i);
			titles[i] = new StringBuilder(c.name).append(" ").append(c.lastname).toString();
			ids[i] = c.contact_id;
			if (isFalse || editedGroup.contacts == null) {
				selections[i] = false;
			} else {
				selections[i] = editedGroup.contacts.containsValue(StringValueUtils.valueOf(c.contact_id));
				groupContactsMapTemp = editedGroup.contacts;
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case ERROR_DIALOG:
			builder.setMessage(ERROR_STRING).setCancelable(false)
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			break;
		case CHOOSE_CONTACTS_DIALOG:
			// i.putExtra(ContactsActivity.DESTINATION_KEY,
			// ContactsActivity.DEST_EVENT_ACTIVITY);
			builder.setTitle(getString(R.string.choose_contacts))
					.setMultiChoiceItems(titles, selections, new DialogSelectionClickHandler())
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
								+ StringValueUtils.valueOf(System.currentTimeMillis()) + ".jpg"));

						intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

						try {
							intent.putExtra("return-data", true);

							startActivityForResult(intent, PICK_FROM_CAMERA);
						} catch (ActivityNotFoundException e) {
							Reporter.reportError(getApplicationContext(), this.getClass().toString(), Thread.currentThread()
									.getStackTrace()[2].getMethodName().toString(), e.getMessage());
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
						editedGroup.contacts.put(StringValueUtils.valueOf(i), StringValueUtils.valueOf(ids[i]));
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
				// imageView.setImageBitmap(photo);
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
				final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>(list.size());
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

	private void displaySelectedContacts() {
		LayoutInflater mInflater = LayoutInflater.from(GroupEditActivity.this);
		LinearLayout groupsList = (LinearLayout) findViewById(R.id.contactsList);

		contactsButton = (Button) findViewById(R.id.contactsButton);

		if (groupsList.getChildCount() > 0) {
			groupsList.removeAllViews();
		}

		if (selectedContacts != null) {
			int groupAmount = selectedContacts.size();

			if (groupAmount > 0) {
				contactsButton.setBackgroundResource(R.drawable.contact_edit_invitegroup_button_notalone);

				for (int iterator = 0; iterator < groupAmount; iterator++) {
					Contact c = selectedContacts.get(iterator);
					String fullname = c.name + c.lastname;
					TextView entry = (TextView) mInflater.inflate(R.layout.contact_edit_invited_entry, null);
					entry.setText(fullname);

					if (iterator == (groupAmount - 1)) {
						entry.setBackgroundResource(R.drawable.contact_edit_invitegroup_entry_last_background);
					}

					groupsList.addView(entry);
				}
			}
		} else {
			contactsButton.setBackgroundResource(R.drawable.event_icon_placeholder);
		}
	}

	@Override
	public void onBackPressed() {
		if (changesMade) {
			changesMade = false;
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(this.getResources().getString(R.string.save_your_changes))
					.setMessage(this.getResources().getString(R.string.do_you_want_to_save_your_changes))
					.setPositiveButton(this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (ACTION_EDIT) {
								new EditGroupTask().execute();
							} else {
								new CreateGroupTask().execute(editedGroup);
							}
						}

					}).setNegativeButton(this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}

					}).setCancelable(false).show();
		} else {
			super.onBackPressed();
		}
	}
}
